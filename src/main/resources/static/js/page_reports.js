var token = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsInJvbGUiOlt7ImF1dGhvcml0eSI6IlJPTEVfQURNSU4ifV0sImlhdCI6MTc0NDkzOTIzNywiZXhwIjoxNzQ0OTQyODM3fQ.fD6KzzzBgSQsrJ2AsAPTaRHkDMZlEyvepm7TqA6kf4E'
function loadReports(page = 0, size = 1) {
    $.ajax({
        url: `/v1/api/reports?page=${page}&size=${size}`,
        type: 'GET',
        dataType: 'json',
        success: function (data) {
            const reports = data.result || [];
            const meta = data.meta || {};
            const totalPages = meta.pages || 0;
            const totalItems = meta.total || 0;
            const tbody = $("#reportTableBody");
            const pagination = $("#paginationContainer");
            const info = $("#reportInfo");
            tbody.empty();
            pagination.empty();
            info.empty();

            // Render rows
            if (reports.length > 0) {
                $.each(reports, function (index, report) {
                    const statusColor = colorMap[report.status] || "info";
                    const row = `
                            <tr>
                                <td>${index + 1 + page * size}</td>
                                <td>${report.title}</td>
                                <td><span class="btn btn-sm btn-${statusColor}">${report.status}</span></td>
                                <td>${report.reportCount}</td>
                                <td>
                                     <button class="btn btn-sm btn-outline-dark"
                                        onclick=showReportModal('${report.documentId}')>
                                        Chi tiết
                                    </button>
                                </td>
                            </tr>
                        `;
                    tbody.append(row);
                });
            } else {
                tbody.html(`<tr><td colspan="5" class="text-center text-muted">Không có báo cáo nào</td></tr>`);
            }

            // Entry info
            const start = reports.length > 0 ? page * size + 1 : 0;
            const end = reports.length > 0 ? start + reports.length - 1 : 0;
            info.text(`Showing ${start} to ${end} of ${totalItems} entries`);

            // Prev button
            const prevBtn = $(`
                    <li class="page-item ${page === 0 ? 'disabled' : ''}">
                        <a class="page-link" href="#">❮</a>
                    </li>
                `);
            prevBtn.on("click", function (e) {
                e.preventDefault();
                if (page > 0) loadReports(page - 1);
            });
            pagination.append(prevBtn);

            // Next button
            const nextBtn = $(`
                    <li class="page-item ${page >= totalPages - 1 ? 'disabled' : ''}">
                        <a class="page-link" href="#">❯</a>
                    </li>
                `);
            nextBtn.on("click", function (e) {
                e.preventDefault();
                if (page < totalPages - 1) loadReports(page + 1);
            });
            pagination.append(nextBtn);
        },
        error: function (err) {
            $("#reportTableBody").html(`
                    <tr><td colspan="5" class="text-center text-danger">Không thể tải dữ liệu</td></tr>
                `);
            $("#reportInfo").text("Không thể tải dữ liệu");
            $("#paginationContainer").empty();
        }
    });
}

function showReportModal(documentId) {
    $.ajax({
        url: `/v1/api/documents/${documentId}`,
        type: 'GET',
        dataType: 'json',
        success: function (doc) {
            // Gán dữ liệu vào modal
            $.ajax({
                url: `/v1/api/reports/${documentId}`, // Gọi API lấy danh sách chi tiết
                type: 'GET',
                dataType: 'json',
                success: function(response) {
                    if (Array.isArray(response.result)) {
                        const details = response.result; // Dữ liệu chi tiết từ response.result
                        const detailListHtml = details.map(detail => `<li>${detail}</li>`).join('');
                        $("#detailList").html(detailListHtml); // Đưa danh sách chi tiết vào modal
                        renderPaginationControls(documentId, response.meta);
                    } else {
                        showToast("Dữ liệu chi tiết không hợp lệ.", "danger");
                    }
                },
                error: function() {
                    showToast("Không thể tải chi tiết tài liệu", "danger");
                }
            });
            $("#modalImage").attr("src", doc.data.coverImageUrl);

            $("#updateStatusBtn").off('click').on('click', function () {
                const newStatus = $("#statusSelect").val();
                $.ajax({
                    url: `/v1/api/reports/${documentId}`,
                    type: 'PUT',
                    contentType: 'application/json',
                    data: JSON.stringify({ status: newStatus }), // gửi map {status: "..."},
                    success: function () {
                        showToast("Cập nhật trạng thái thành công!")
                        $("#reportModal").modal('hide');
                        loadReports(); // reload lại danh sách
                    },
                    error: function () {
                        showToast("Lỗi khi cập nhật trạng thái.", "danger")
                    }
                });
            });

            $("#deleteDocBtn").off('click').on('click', function () {
                Swal.fire({
                    title: 'Bạn có chắc chắn?',
                    text: "Tài liệu này sẽ bị xóa!",
                    icon: 'warning',
                    showCancelButton: true,
                    confirmButtonText: 'Xoá',
                    cancelButtonText: 'Huỷ',
                    confirmButtonColor: '#d33',
                    cancelButtonColor: '#3085d6'
                }, $("#reportModal").modal('hide')).then((result) => {
                    if (result.isConfirmed) {
                        $.ajax({
                            url: `/v1/api/documents/delete/${documentId}`,
                            type: 'DELETE',
                            success: function () {
                                $.ajax({
                                    url: `/v1/api/reports/${documentId}`,
                                    type: 'PUT',
                                    contentType: 'application/json',
                                    data: JSON.stringify({ status: "RESOLVED" }),
                                    success: function () {
                                        loadReports();
                                        showToast("Xoá tài liệu và cập nhật báo cáo thành công!", "success");
                                    },
                                    error: function () {
                                        showToast("Lỗi khi cập nhật trạng thái sau khi xoá.", "danger");
                                    }
                                });
                            },
                            error: function () {
                                showToast("Lỗi khi xoá tài liệu.", "danger");
                            }
                        });
                    }
                });
            });
            const modal = new bootstrap.Modal(document.getElementById('reportModal'));
            modal.show();
        },
        error: function (err) {
            showToast("Không thể tải chi tiết báo cáo", "danger")
        }
    });
}

function showToast(message, type = "success") {
    const toastEl = document.getElementById("reportToast");
    const toastBody = document.getElementById("toastBody");
    const toastHeader = toastEl.querySelector(".toast-header");

    // Xóa class cũ
    toastEl.classList.remove("bg-success", "bg-danger", "bg-warning");
    toastHeader.classList.remove("bg-success", "bg-danger", "bg-warning", "text-white");

    // Thêm class mới
    toastEl.classList.add(`bg-${type}`);
    toastHeader.classList.add(`bg-${type}`, "text-white");

    // Set nội dung
    toastBody.textContent = message;

    // Show toast
    const bsToast = new bootstrap.Toast(toastEl);
    bsToast.show();
}

function renderPaginationControls(documentId, meta) {
    const paginationContainer = $("#paginationControls");
    paginationContainer.empty();

    for (let i = 0; i < meta.pages; i++) {
        const pageBtn = $(`<button class="btn btn-sm ${i === meta.page ? 'btn-secondary' : 'btn-outline-secondary'} me-2">${i + 1}</button>`);
        pageBtn.click(() => loadReportDetails(documentId, i, meta.pageSize));
        paginationContainer.append(pageBtn);
    }
}

function loadReportDetails(documentId, page = 0, size = 5) {
    $.ajax({
        url: `/v1/api/reports/${documentId}?page=${page}&size=${size}`,
        type: 'GET',
        dataType: 'json',
        success: function(response) {
            if (Array.isArray(response.result)) {
                const details = response.result;
                const detailListHtml = details.map(detail => `<li>${detail}</li>`).join('');
                $("#detailList").html(`<ul>${detailListHtml}</ul>`);
                renderPaginationControls(documentId, response.meta);
            } else {
                $("#detailList").html(`<p>Không có chi tiết nào.</p>`);
            }
        },
        error: function () {
            showToast("Không thể tải chi tiết tài liệu", "danger");
        }
    });
}

const colorMap = {
    "IN_PROGRESS": "info",
    "PENDING": "warning",
    "RESOLVED": "success",
    "DECLINED": "danger"
};

$(document).ready(function () {
    loadReports();
});

