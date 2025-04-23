let currentPage = 0;
const pageSize = 7;
let totalPages = 1;

function loadNotifications() {
    $.ajax({
        url: `/v1/api/notifications?page=${currentPage}&size=${pageSize}`,
        method: 'GET',
        success: function (data) {
            const items = data.result || [];
            totalPages = data.meta.pages;

            const $list = $('#notification-list');

            if (items.length === 0 && currentPage === 0) {
                $list.append(`<div class="list-group-item">Không có thông báo nào.</div>`);
                return;
            }

            items.forEach((item, index) => {
                const $item = $(`
                        <div class="list-group-item notification-item d-flex justify-content-between align-items-center" data-id="${item.notificationId}">
                            <div>
                                <strong>${item.title}</strong><br>
                                <small class="text-muted">${new Date(item.createdOn).toLocaleString()}</small>
                            </div>
                            <span class="status-dot ${item.seen? 'read' : 'unread'}"></span>
                        </div>
                    `);

                $item.click(function () {
                    $('.notification-item').removeClass('active');
                    $item.addClass('active');

                    $('#notification-title').text(item.title);
                    $('#notification-author').text(item.author || 'Không rõ');
                    $('#notification-time').text(new Date(item.createdOn).toLocaleString());
                    $('#notification-content').text(item.content || '(Không có nội dung)');

                    if (!item.seen) {
                        $.ajax({
                            url: `/v1/api/notifications/${item.notificationId}/mark-as-read`,
                            type: 'PATCH',
                            success: function () {
                                item.seen = true;
                                $item.find('.status-dot').removeClass('unread').addClass('read');
                            }
                        });
                    } else {
                        $item.find('.status-dot').removeClass('unread').addClass('read');
                    }
                });

                $list.append($item);

                // Tự động chọn item đầu tiên của trang đầu
                if (currentPage === 0 && index === 0) {
                    $item.click();
                }
            });

            // Ẩn nút nếu hết trang
            if (currentPage + 1 >= totalPages) {
                $('#load-more-btn')
                    .prop('disabled', true)
                    .text('Đã tải hết');
            } else {
                $('#load-more-btn')
                    .prop('disabled', false)
                    .text('Xem thêm');
            }
        },
        error: function () {
            $('#notification-list').append('<div class="list-group-item text-danger">Lỗi khi tải thông báo.</div>');
        }
    });
}

$(document).ready(function () {
    loadNotifications();

    $('#load-more-btn').click(function () {
        currentPage++;
        loadNotifications();
    });
});