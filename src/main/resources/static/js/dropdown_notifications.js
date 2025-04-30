function updateNotificationBadge(unreadCount) {
    const $badge = $('#notification-badge');
    const $count = $('#notification-count');

    if (unreadCount > 0) {
        $badge.show();
        $count.text(`${unreadCount} New`);
    } else {
        $badge.hide();
        $count.text(`0 New`);
    }
}

function loadDropdownNotifications() {
    $.ajax({
        url: `/v1/api/notifications?size=5&page=0`,
        method: 'GET',
        success: function (data) {
            const items = data.result || [];
            const $list = $('#notification-dropdown-list');
            let unreadCount = 0;

            $list.empty();

            if (items.length === 0) {
                $list.append('<li class="list-group-item text-muted">Không có thông báo nào.</li>');
                updateNotificationBadge(0);
                return;
            }

            items.forEach((item) => {
                const isUnread = !item.seen;
                if (isUnread) unreadCount++;

                const time = new Date(item.createdOn).toLocaleString();
                const dotClass = item.seen ? 'bg-secondary' : 'bg-primary';

                const avatar = item.avatarUrl
                    ? `<img src="${item.avatarUrl}" class="rounded-circle" alt width="40" height="40"/>`
                    : `
            <div class="position-relative">
              <span class="avatar-initial rounded-circle bg-label-${item.color || 'primary'} d-inline-flex justify-content-center align-items-center"
                    style="width: 40px; height: 40px; font-weight: bold; color: #fff;">
                ${item.initials || 'S'}
              </span>
              <span class="position-absolute top-0 end-0 translate-middle badge-dot ${dotClass}"
                    style="width: 10px; height: 10px; border-radius: 50%; display: inline-block;"></span>
            </div>
          `;

                const $item = $(`
          <li class="list-group-item list-group-item-action dropdown-notifications-item ${item.seen ? 'marked-as-read' : ''}">
            <div class="d-flex">
              <div class="flex-shrink-0 me-3">
                <div class="avatar">${avatar}</div>
              </div>
              <div class="flex-grow-1">
                <h6 class="mb-1 small fw-semibold">${item.title}</h6>
                <small class="mb-1 d-block text-body">${item.content || ''}</small>
                <small class="text-muted">${time}</small>
              </div>
            </div>
          </li>
        `);

                $item.click(function () {
                    if (!item.seen) {
                        $.ajax({
                            url: `/v1/api/notifications/${item.notificationId}/mark-as-read`,
                            type: 'PATCH',
                            success: function () {
                                item.seen = true;
                                $item.addClass('marked-as-read');
                                $item.find('.badge-dot')
                                    .removeClass('bg-primary')
                                    .addClass('bg-secondary');

                                unreadCount--;
                                updateNotificationBadge(unreadCount);
                            }
                        });
                    }
                });

                $list.append($item);
            });

            updateNotificationBadge(unreadCount);
        },
        error: function () {
            $('#notification-dropdown-list').html('<li class="list-group-item text-danger">Lỗi khi tải thông báo.</li>');
        }
    });
}

$(document).ready(function () {
    // Load ban đầu
    loadDropdownNotifications();

    // Khi mở dropdown thì reload
    $('.dropdown-notifications').on('show.bs.dropdown', function () {
        loadDropdownNotifications();
    });

    // Đánh dấu tất cả là đã đọc
    $('#mark-all-read').click(function () {
        $.ajax({
            url: `/v1/api/notifications/mark-as-read`,
            type: 'PATCH',
            success: function () {
                loadDropdownNotifications();
            }
        });
    });
});
