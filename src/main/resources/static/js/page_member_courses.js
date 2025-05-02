const courseApi = "/v1/api/courses";
const followApi = "/v1/api/follow";
let currentPage = 0;
const pageSize = 5;

$(document).ready(function () {
    loadCoursesWithPagination();
});

function loadCoursesWithPagination() {
    $.ajax({
        method: "GET",
        url: `${courseApi}/all`,
        success: function (response) {
            const allCourses = response.data || [];
            const totalItems = allCourses.length;
            const totalPages = Math.ceil(totalItems / pageSize);

            if (currentPage >= totalPages && totalPages > 0) {
                currentPage = totalPages - 1;
                loadCoursesWithPagination();
                return;
            }

            const pageCourses = allCourses.slice(currentPage * pageSize, (currentPage + 1) * pageSize);
            renderTable(pageCourses);

            const start = pageCourses.length > 0 ? currentPage * pageSize + 1 : 0;
            const end = pageCourses.length > 0 ? start + pageCourses.length - 1 : 0;
            $('#courseInfo').text(`Hiển thị ${start} đến ${end} trong tổng ${totalItems} khóa học`);

            renderPagination(totalPages);
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

    if (courses.length === 0) {
        tbody.append(`<tr><td colspan="4" class="text-center text-muted">Không có khóa học nào.</td></tr>`);
        return;
    }

    $.get("/v1/api/followed", function (res) {
        const followedCourses = res.data || [];
        const followedIds = new Set(followedCourses.map(c => c.courseId));

        courses.forEach((item, index) => {
            const isFollowed = followedIds.has(item.courseId);
            const btn = isFollowed
                ? `<button class="btn btn-outline-danger btn-sm" onclick="unfollowCourse('${item.courseId}', '${item.courseName}')">Unfollow</button>`
                : `<button class="btn btn-outline-primary btn-sm" onclick="followCourse('${item.courseId}', '${item.courseName}')">Follow</button>`;

            const row = `
                <tr>
                    <td>${index + 1 + currentPage * pageSize}</td>
                    <td>${item.courseName}</td>
                    <td>${item.universityName}</td>
                    <td>${btn}</td>
                </tr>`;
            tbody.append(row);
        });
    }).fail(() => {
        tbody.append(`<tr><td colspan="4" class="text-center text-danger">Không thể tải danh sách khóa học đã theo dõi.</td></tr>`);
    });
}

function renderPagination(totalPages) {
    const pagination = $('#pagination');
    pagination.empty();

    const prevBtn = $(`<li class="page-item ${currentPage === 0 ? 'disabled' : ''}">
        <a class="page-link" href="#">❮</a></li>`);
    prevBtn.on("click", function (e) {
        e.preventDefault();
        if (currentPage > 0) {
            currentPage--;
            loadCoursesWithPagination();
        }
    });
    pagination.append(prevBtn);

    for (let i = 0; i < totalPages; i++) {
        const active = i === currentPage ? 'active' : '';
        const li = $(`<li class="page-item ${active}"><a class="page-link" href="#">${i + 1}</a></li>`);
        li.on("click", function (e) {
            e.preventDefault();
            currentPage = i;
            loadCoursesWithPagination();
        });
        pagination.append(li);
    }

    const nextBtn = $(`<li class="page-item ${currentPage >= totalPages - 1 ? 'disabled' : ''}">
        <a class="page-link" href="#">❯</a></li>`);
    nextBtn.on("click", function (e) {
        e.preventDefault();
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadCoursesWithPagination();
        }
    });
    pagination.append(nextBtn);
}

function followCourse(courseId, courseName) {
    $.post(`${followApi}?courseId=${courseId}`)
        .done(() => {
            showToast(`Đã theo dõi khóa học <b>${courseName}</b>`, "success");
            loadCoursesWithPagination();
        })
        .fail(err => {
            showToast(err.responseJSON?.message || "Lỗi theo dõi khóa học", "error");
        });
}

function unfollowCourse(courseId, courseName) {
    $.ajax({
        url: `${followApi.replace("/follow", "/unfollow")}?courseId=${courseId}`,
        type: 'DELETE'
    })
        .done(() => {
            showToast(`Đã bỏ theo dõi khóa học <b>${courseName}</b>`, "warning");
            loadCoursesWithPagination();
        })
        .fail(err => {
            showToast(err.responseJSON?.message || "Lỗi bỏ theo dõi khóa học", "error");
        });
}

// Custom notification (SweetAlert2)
function showToast(message, type = "info") {
    Swal.fire({
        toast: true,
        position: 'top-end',
        icon: type,
        html: message,
        showConfirmButton: false,
        timer: 2500,
        timerProgressBar: true,
        customClass: {
            popup: 'custom-toast'
        },
        didOpen: (toast) => {
            toast.style.zIndex = 20000; // đảm bảo cao hơn header
            toast.style.marginTop = "45px"; // đẩy xuống dưới header
        }
    });
}