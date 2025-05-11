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

//
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
                    <td>
                        <a href="/member/course/detail?courseId=${item.courseId}" class="fw-semibold text-decoration-none">
                            ${item.courseName}
                        </a>
                    </td>
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

function viewCourseDetail(courseId) {
    window.location.href = `/member/course/detail?courseId=${courseId}`;
}

function checkFollowedCourses() {
    fetch('/v1/api/followed')
        .then(res => res.json())
        .then(res => {
            const followed = res.data || [];
            if (followed.length === 0) {
                $('#chooseCourseModal').modal('show');
                loadAllCoursesToChoose();
            }
        });
}

let modalCourses = [];
let modalCurrentPage = 0;
const modalPageSize = 5;

function loadAllCoursesToChoose() {
    fetch('/v1/api/courses/all')
        .then(res => res.json())
        .then(res => {
            modalCourses = res.data || [];
            renderModalCoursePage();
        });
}

// function renderModalCoursePage() {
//     const tbody = document.getElementById("chooseCourseBody");
//     tbody.innerHTML = "";
//
//     if (modalCourses.length === 0) {
//         tbody.innerHTML = `<tr><td class="text-muted text-center" colspan="2">Không có khóa học nào</td></tr>`;
//         return;
//     }
//
//     const start = modalCurrentPage * modalPageSize;
//     const end = Math.min(start + modalPageSize, modalCourses.length);
//     const currentCourses = modalCourses.slice(start, end);
//
//     currentCourses.forEach(course => {
//         const row = document.createElement("tr");
//         row.innerHTML = `
//             <td>
//                 <a href="/member/course/detail?courseId=${course.courseId}" class="fw-semibold text-decoration-none">${course.courseName}</a>
//                 <div class="text-muted small">
//                     ${course.universityName}<br>
//                     📄 ${course.totalDocuments || 0} tài liệu • 👤 ${course.totalFollowers || 0} người theo dõi
//                 </div>
//             </td>
//             <td class="text-end align-middle">
//                 <button class="btn btn-sm btn-primary" onclick="followAndGo('${course.courseId}', '${course.courseName}')">Follow</button>
//             </td>`;
//         tbody.appendChild(row);
//     });
//
//     renderModalPagination();
// }

function renderModalCoursePage() {
    const tbody = document.getElementById("chooseCourseBody");
    tbody.innerHTML = "";

    if (modalCourses.length === 0) {
        tbody.innerHTML = `<tr><td class="text-muted text-center" colspan="2">Không có khóa học nào</td></tr>`;
        return;
    }

    const start = modalCurrentPage * modalPageSize;
    const end = Math.min(start + modalPageSize, modalCourses.length);
    const currentCourses = modalCourses.slice(start, end);

    currentCourses.forEach(course => {
        const row = document.createElement("tr");

        const statsId = `courseStats-${course.courseId}`;

        row.innerHTML = `
            <td>
                <a href="/member/course/detail?courseId=${course.courseId}" class="fw-semibold text-decoration-none">
                    ${course.courseName}
                </a>
                <div class="text-muted small" id="${statsId}">
                    Đang tải...
                </div>
            </td>
            <td class="text-end align-middle">
                <button class="btn btn-sm btn-primary" onclick="followAndGo('${course.courseId}', '${course.courseName}')">Follow</button>
            </td>`;

        tbody.appendChild(row);

        // 👇 Gọi API để cập nhật số liệu
        fetch(`/v1/api/course/detail?courseId=${course.courseId}`)
            .then(res => res.json())
            .then(res => {
                const data = res.data;
                const statEl = document.getElementById(statsId);
                if (data && statEl) {
                    statEl.innerHTML = `📄 ${data.totalDocuments || 0} tài liệu • 👤 ${data.totalFollowers || 0} người theo dõi`;
                }
            })
            .catch(() => {
                const statEl = document.getElementById(statsId);
                if (statEl) statEl.innerHTML = `<span class="text-danger">Lỗi tải số liệu</span>`;
            });
    });

    renderModalPagination();
}


function renderModalPagination() {
    const pagination = document.getElementById("modalPagination");
    pagination.innerHTML = "";

    const totalPages = Math.ceil(modalCourses.length / modalPageSize);

    for (let i = 0; i < totalPages; i++) {
        const li = document.createElement("li");
        li.className = "page-item" + (i === modalCurrentPage ? " active" : "");
        li.innerHTML = `<a class="page-link" href="#">${i + 1}</a>`;
        li.addEventListener("click", (e) => {
            e.preventDefault();
            modalCurrentPage = i;
            renderModalCoursePage();
        });
        pagination.appendChild(li);
    }
}


function filterCourseList() {
    const input = document.getElementById("courseSearchInput").value.toLowerCase();
    const rows = document.querySelectorAll("#chooseCourseBody .course-row");
    rows.forEach(row => {
        const courseName = row.children[0].innerText.toLowerCase();
        const university = row.children[1].innerText.toLowerCase();
        if (courseName.includes(input) || university.includes(input)) {
            row.style.display = "";
        } else {
            row.style.display = "none";
        }
    });
}


// function followAndGo(courseId, courseName) {
//     $.post(`/v1/api/follow?courseId=${courseId}`)
//         .done(() => {
//             $('#chooseCourseModal').modal('hide');
//             window.location.href = `/member/course/detail?courseId=${courseId}`;
//         })
//         .fail(() => showToast("Lỗi theo dõi khóa học", "error"));
// }

function followAndGo(courseId, courseName) {
    $.post(`/v1/api/follow?courseId=${courseId}`)
        .done(() => {
            showToast(`Đã theo dõi khóa học <b>${courseName}</b>`, "success");

            // Reload full course list before going
            fetch('/v1/api/courses/all')
                .then(res => res.json())
                .then(res => {
                    modalCourses = res.data || [];
                    renderModalCoursePage();

                    setTimeout(() => {
                        $('#chooseCourseModal').modal('hide');
                        window.location.href = `/member/course/detail?courseId=${courseId}`;
                    }, 1000);
                });
        })
        .fail(() => showToast("Lỗi theo dõi khóa học", "error"));
}

// Gọi khi load trang
$(document).ready(() => {
    checkFollowedCourses();
});



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