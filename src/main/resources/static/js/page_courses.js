const apiBase = "/v1/api/courses";
let currentPage = 0;
const pageSize = 5;


$(document).ready(function () {
    loadCourses();
    loadUniversityOptions();

    $('#createCourseForm').submit(function (e) {
        e.preventDefault();

        const courseName = $('#createName').val().trim();
        const universityName = $('#createUniv option:selected').text();
        const description = $('#createDesc').val().trim();


        const btn = $('#createSubmitBtn');

        btn.prop('disabled', true).text('Đang tạo...');

        $.ajax({
            method: 'POST',
            url: apiBase + '/create',
            contentType: 'application/json',
            data: JSON.stringify({ courseName, universityName, description }),
            success: () => {
                const modal = bootstrap.Modal.getOrCreateInstance('#createModal');
                modal.hide();

                $('.modal-backdrop').remove();
                $('body').removeClass('modal-open').css('padding-right', '');

                $('#createName').val('');
                $('#createUniv').val('');
                $('#createDesc').val('');

                showToast('Thêm khóa học thành công!', 'success');
                currentPage = 0;
                loadCourses();
            },
            error: () => {
                showToast('Thêm khóa học thất bại!', 'error');
            },
            complete: () => {
                btn.prop('disabled', false).text('Tạo');
            }
        });
    });

    $('#editCourseForm').submit(function (e) {
        e.preventDefault();

        const courseId = $('#editId').val();
        const courseName = $('#editName').val().trim();
        const universityName = $('#editUniv option:selected').text();
        const description = $('#editDesc').val().trim();
        const btn = $('#editSubmitBtn');



        btn.prop('disabled', true).text('Đang cập nhật...');

        $.ajax({
            method: 'PUT',
            url: apiBase + '/update',
            contentType: 'application/json',
            data: JSON.stringify({ courseId, courseName, universityName, description }),
            success: () => {
                bootstrap.Modal.getOrCreateInstance('#editModal').hide();
                showToast("Cập nhật khóa học thành công!", 'success');
                currentPage = 0;
                loadCourses();
            },
            error: () => {
                showToast("Cập nhật khóa học thất bại!", 'error');
            },
            complete: () => {
                btn.prop('disabled', false).text('Cập nhật');
            }
        });
    });
});

function loadUniversityOptions() {
    $.ajax({
        method: 'GET',
        url: '/v1/api/universities/names',
        success: function (res) {
            const universities = res.data || [];
            const selects = $('#createUniv, #editUniv');
            selects.empty().append(`<option value="">-- Chọn trường đại học --</option>`);
            universities.forEach(u => {
                selects.append(`<option>${u.univName}</option>`);
            });
        },
        error: function () {
            showToast('Không thể tải danh sách trường đại học', 'error');
        }
    });
}


function loadCourses() {
    $.ajax({
        method: 'GET',
        url: `${apiBase}/all`,
        success: function (res) {
            const courses = res.data || [];
            const pageCourses = courses.slice(currentPage * pageSize, (currentPage + 1) * pageSize);
            renderTable(pageCourses);

            const start = pageCourses.length > 0 ? currentPage * pageSize + 1 : 0;
            const end = pageCourses.length > 0 ? start + pageCourses.length - 1 : 0;
            $('#courseInfo').text(`Hiển thị ${start} đến ${end} trong tổng ${courses.length} khóa học`);

            renderPagination(courses.length);
        },
        error: function () {
            $('#courseTableBody').html(`<tr><td colspan="4" class="text-center text-danger">Không thể tải dữ liệu</td></tr>`);
            $('#courseInfo').text("Không thể tải dữ liệu");
            $('#pagination').empty();
        }
    });
}

function renderTable(courses) {
    const tbody = $('#courseTableBody');
    tbody.empty();
    courses.forEach((item, idx) => {
        const row = `
            <tr>
                <td>${idx + 1 + currentPage * pageSize}</td>
                <td>${item.courseName}</td>
                <td>${item.universityName}</td>
                <td>
                    <button class="btn btn-info btn-sm"
                        onclick="viewCourseDetail('${item.courseName}', '${item.universityName}')">Xem chi tiết</button>
                    <button class="btn btn-warning btn-sm"
                        onclick="openEditModal('${item.courseId}', \`${item.courseName}\`, \`${item.universityName}\`, \`${item.description || ''}\`)">Sửa</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteCourse('${item.courseId}')">Xoá</button>
                </td>
            </tr>`;
        tbody.append(row);
    });
}

function openEditModal(courseId, courseName, universityName, description) {
    $('#editId').val(courseId);
    $('#editName').val(courseName);
    $('#editDesc').val(description);
    $('#editUniv option').filter(function () {
        return $(this).text().trim() === universityName.trim();
    }).prop('selected', true);
    $('#editModal').modal('show');
}

function deleteCourse(courseId) {
    if (confirm("Bạn có chắc chắn muốn xoá khóa học này?")) {
        $.ajax({
            method: 'DELETE',
            url: `${apiBase}/delete/${courseId}`,
            success: () => {
                showToast("Xoá khóa học thành công!", 'success');
                currentPage = 0;
                loadCourses();
            },
            error: () => {
                showToast("Xoá khóa học thất bại!", 'error');
            }
        });
    }
}

function viewCourseDetail(courseName, universityName) {
    const url = `/admin/course/detail?courseName=${encodeURIComponent(courseName)}&universityName=${encodeURIComponent(universityName)}`;
    window.open(url, '_blank');
}

function renderPagination(totalItems) {
    const totalPages = Math.ceil(totalItems / pageSize);
    const pagination = $('#pagination');
    pagination.empty();

    const prevBtn = $(`<li class="page-item ${currentPage === 0 ? 'disabled' : ''}">
        <a class="page-link" href="#">❮</a></li>`);
    prevBtn.on("click", function (e) {
        e.preventDefault();
        if (currentPage > 0) {
            currentPage--;
            loadCourses();
        }
    });
    pagination.append(prevBtn);

    for (let i = 0; i < totalPages; i++) {
        const li = $(`<li class="page-item ${i === currentPage ? 'active' : ''}">
            <a class="page-link" href="#">${i + 1}</a></li>`);
        li.on("click", function (e) {
            e.preventDefault();
            currentPage = i;
            loadCourses();
        });
        pagination.append(li);
    }

    const nextBtn = $(`<li class="page-item ${currentPage >= totalPages - 1 ? 'disabled' : ''}">
        <a class="page-link" href="#">❯</a></li>`);
    nextBtn.on("click", function (e) {
        e.preventDefault();
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadCourses();
        }
    });
    pagination.append(nextBtn);
}

function showToast(message, type = "success") {
    const toastEl = document.getElementById("courseToast");
    const toastBody = document.getElementById("toastBody");
    if (!toastEl || !toastBody) return;

    const toastHeader = toastEl.querySelector(".toast-header");
    toastEl.classList.remove("bg-success", "bg-danger", "bg-warning");
    toastHeader.classList.remove("bg-success", "bg-danger", "bg-warning");

    const colorClass = {
        success: "bg-success",
        error: "bg-danger",
        warning: "bg-warning"
    }[type] || "bg-success";

    toastEl.classList.add(colorClass, "text-white");
    toastHeader.classList.add(colorClass, "text-white");

    toastBody.textContent = message;
    bootstrap.Toast.getOrCreateInstance(toastEl).show();
}
