const apiBase = "/v1/api/universities";
let currentPage = 0;
const pageSize = 5;

$(document).ready(function () {


    loadUniversities();

    $('#createUniversityForm').submit(function (e) {
        e.preventDefault();

        const univName = $('#createName').val().trim();
        const address = $('#createAddress').val().trim();
        const btn = $('#createSubmitBtn');

        if (!univName) {
            showToast('Tên trường không được để trống!', false);
            return;
        }

        btn.prop('disabled', true).text('Đang tạo...');

        $.ajax({
            method: 'POST',
            url: apiBase + '/create',
            contentType: 'application/json',
            data: JSON.stringify({ univName, address }),
            success: () => {
                const modalEl = document.getElementById('createModal');
                const modal = bootstrap.Modal.getInstance(modalEl);
                modal.hide();

                // Chờ modal ẩn hoàn toàn rồi mới cleanup
                $(modalEl).one('hidden.bs.modal', function () {
                    $('.modal-backdrop').remove();
                    $('body').removeClass('modal-open').css('padding-right', '');

                    // Reset form
                    $('#createName').val('');
                    $('#createAddress').val('');

                    showToast('Thêm trường thành công!', 'success');
                    currentPage = 0;
                    loadUniversities();
                });
            },
            error: () => {
                showToast('Thêm trường thất bại!', 'error');
            },
            complete: () => {
                btn.prop('disabled', false).text('Tạo');
            }
        });
    });




    $('#editUniversityForm').submit(function (e) {
        e.preventDefault();
        const univId = $('#editId').val();
        const univName = $('#editName').val();
        const address = $('#editAddress').val().trim();
        const btn = $('#editSubmitBtn');
        btn.prop('disabled', true).text('Đang cập nhật...');

        $.ajax({
            method: 'PUT',
            url: apiBase + '/update',
            contentType: 'application/json',
            data: JSON.stringify({univId, univName, address}),
            success: () => {
                $('#editModal').modal('hide');
                showToast("Cập nhật trường thành công!", 'success');
                currentPage = 0;
                loadUniversities();
            },
            error: () => {
                $('#editModal').modal('hide');
                showToast("Cập nhật trường thất bại!", 'error');
                currentPage = 0;
                loadUniversities();
            },

            complete: () => {
                btn.prop('disabled', false).text('Cập nhật');
            }
        });
    });

});

function loadUniversities() {
    $.ajax({
        method: "GET",
        url: `${apiBase}/list?page=${currentPage}&size=${pageSize}`,
        success: function (data) {
            const universities = data.content || [];
            const totalPages = data.totalPages;
            const totalItems = data.totalElements;
            const page = data.number;
            const size = data.size;

            if (currentPage >= totalPages && totalPages > 0) {
                currentPage = totalPages - 1;
                loadUniversities();
                return;
            }

            renderTable(universities);

            // Info
            const start = universities.length > 0 ? page * size + 1 : 0;
            const end = universities.length > 0 ? start + universities.length - 1 : 0;
            $('#univInfo').text(`Hiển thị ${start} đến ${end} trong tổng ${totalItems} trường`);

            const pagination = $('#pagination');
            pagination.empty();

            // Prev
            const prevBtn = $(`<li class="page-item ${currentPage === 0 ? 'disabled' : ''}">
        <a class="page-link" href="#">❮</a></li>`);
            prevBtn.on("click", function (e) {
                e.preventDefault();
                if (currentPage > 0) {
                    currentPage--;
                    loadUniversities();
                }
            });
            pagination.append(prevBtn);

            // Số trang
            for (let i = 0; i < totalPages; i++) {
                const active = i === currentPage ? 'active' : '';
                const li = $(`<li class="page-item ${active}">
            <a class="page-link" href="#">${i + 1}</a></li>`);
                li.on("click", function (e) {
                    e.preventDefault();
                    currentPage = i;
                    loadUniversities();
                });
                pagination.append(li);
            }

            // Next
            const nextBtn = $(`<li class="page-item ${currentPage >= totalPages - 1 ? 'disabled' : ''}">
        <a class="page-link" href="#">❯</a></li>`);
            nextBtn.on("click", function (e) {
                e.preventDefault();
                if (currentPage < totalPages - 1) {
                    currentPage++;
                    loadUniversities();
                }
            });
            pagination.append(nextBtn);

        },
        error: function () {
            $('#universityTableBody').html(`
                    <tr><td colspan="4" class="text-center text-danger">Không thể tải dữ liệu</td></tr>
                `);
            $('#univInfo').text("Không thể tải dữ liệu");
            $('#pagination').empty();
        }
    });
}


function renderTable(universities) {
    const tbody = $('#universityTableBody');
    tbody.empty();
    universities.forEach((item, idx) => {
        const row = `
            <tr>
                <td>${idx + 1 + currentPage * pageSize}</td>
                <td>${item.univName}</td>
                <td>${item.address ?? ''}</td>
                <td>
                    <button class="btn btn-info btn-sm" onclick="viewUniversity('${item.univName}')">Xem</button>
                    <button class="btn btn-warning btn-sm" onclick="openEditModal('${item.univId}', '${item.univName}', '${item.address}')">Sửa</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteUniversity('${item.univId}')">Xoá</button>
                </td>
            </tr>
        `;
        tbody.append(row);
    });
}

function openEditModal(univId, univName, address) {
    console.log('Edit modal data:', univId, univName, address);
    $('#editId').val(univId);
    $('#editName').val(univName);
    $('#editAddress').val(address);
    $('#editModal').modal('show');
}

function deleteUniversity(univId) {
    if (confirm("Bạn có chắc chắn muốn xoá?")) {
        $.ajax({
            method: 'DELETE',
            url: `${apiBase}/delete/${univId}`,
            success: () => {
                showToast("Xoá trường thành công!", 'success');
                currentPage = 0;
                loadUniversities();
            },
            error: () => {
                showToast("Xoá trường thất bại!", 'error');
                currentPage = 0;
                loadUniversities();
            },
        });
    }
}

function viewUniversity(univName) {
    window.open(`university/detail?name=${encodeURIComponent(univName)}`, '_blank');
}
function showToast(message, type = "success") {
    const toastEl = document.getElementById("univToast");
    const toastBody = document.getElementById("toastBody");
    if (!toastEl || !toastBody) return;

    const toastHeader = toastEl.querySelector(".toast-header");
    if (!toastHeader) return;

    // Reset class
    toastEl.classList.remove("bg-success", "bg-danger", "bg-warning");
    toastHeader.classList.remove("bg-success", "bg-danger", "bg-warning");

    // Gán class mới
    const colorClass = {
        success: "bg-success",
        error: "bg-danger",
        warning: "bg-warning"
    }[type] || "bg-success";

    toastEl.classList.add(colorClass, "text-white");
    toastHeader.classList.add(colorClass, "text-white");

    toastBody.textContent = message;

    const toast = bootstrap.Toast.getOrCreateInstance(toastEl);
    toast.show();
}

