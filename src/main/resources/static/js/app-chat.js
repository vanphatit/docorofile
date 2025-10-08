let currentRoomId = null;
let currentPage = 0;
let hasMore = true;
let isLoading = false;
let stompClient = null;
let currentSubscription = null;

// Escape HTML để ngăn XSS
function escapeHTML(str) {
  if (!str) return '';
  return str
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
}

// Kết nối WebSocket
function connectWebSocket() {
  const socket = new SockJS("http://localhost:9091/ws");
  stompClient = Stomp.over(socket);
  stompClient.connect({}, () => {
    console.log("WebSocket connected");
    Swal.fire({
      toast: true,
      position: 'top-end',
      icon: 'success',
      title: 'Kết nối máy chủ thành công',
      showConfirmButton: false,
      timer: 1200,
      timerProgressBar: true
    });
  }, () => {
    Swal.fire({
      icon: 'error',
      title: 'Không thể kết nối WebSocket!',
      text: 'Vui lòng kiểm tra lại máy chủ hoặc kết nối mạng.',
      confirmButtonColor: '#d33'
    });
  });
}

// Đăng ký nhận tin nhắn
function subscribeToRoom(roomId) {
  if (currentSubscription) currentSubscription.unsubscribe();

  currentSubscription = stompClient.subscribe(`/topic/room.${roomId}`, function (message) {
    const msg = JSON.parse(message.body);
    const html = buildMessageHTML({
      content: msg.content,
      createdAt: msg.timestamp,
      sender: { fullName: msg.sender },
      mine: msg.mine
    });
    $('.chat-history-body ul').append(html);
    $('.chat-history-body').scrollTop($('.chat-history-body')[0].scrollHeight);

    // Thông báo toast khi có tin nhắn mới từ người khác
    if (!msg.mine) {
      Swal.fire({
        toast: true,
        position: 'top-end',
        icon: 'info',
        title: `${msg.sender} vừa nhắn tin`,
        showConfirmButton: false,
        timer: 2000,
        timerProgressBar: true
      });
    }
  });
}

// Load danh sách phòng chat
function loadChatSidebar(page = 0) {
  $.ajax({
    url: `v1/api/chats/rooms?page=${page}`,
    method: 'GET',
    success: function (response) {
      const rooms = response.result;
      window.rooms = rooms;
      let html = `
        <div class="sidebar-header h-px-75 px-5 border-bottom d-flex align-items-center">
          <div class="d-flex align-items-center me-6 me-lg-0">
            <div class="flex-shrink-0 avatar avatar-online me-4">
              <img class="user-avatar rounded-circle cursor-pointer" src="/assets/img/avatars/1.png" alt="Avatar" />
            </div>
            <div class="flex-grow-1 input-group input-group-sm input-group-merge rounded-pill">
              <span class="input-group-text"><i class="ri-search-line lh-1 ri-20px"></i></span>
              <input type="text" class="form-control chat-search-input" placeholder="Search..." />
            </div>
          </div>
        </div>
        <div class="sidebar-body">
          <ul class="list-unstyled chat-contact-list py-2 mb-0" id="chat-list">
            <li class="chat-contact-list-item chat-contact-list-item-title mt-0 not-clickable">
              <h5 class="text-primary mb-0">Chats</h5>
            </li>`;

      if (!rooms.length) {
        $('.chat-history-body').html(`
          <div class="d-flex justify-content-center align-items-center h-100">
            <h6 class="text-muted mb-0">Không tìm thấy group chat</h6>
          </div>`);
      } else {
        rooms.forEach(room => {
          html += `
            <li class="chat-contact-list-item mb-1" data-room-id="${room.id}">
              <a href="#" class="d-flex align-items-center">
                <div class="flex-shrink-0 avatar ${room.status === 'offline' ? 'avatar-offline' : 'avatar-online'}">
                  ${room.avatarUrl
              ? `<img src="${room.avatarUrl}" alt="Avatar" class="rounded-circle" />`
              : `<span class="avatar-initial rounded-circle bg-label-success">${escapeHTML(room.title?.charAt(0) || '?')}</span>`}
                </div>
                <div class="chat-contact-info flex-grow-1 ms-4">
                  <div class="d-flex justify-content-between align-items-center">
                    <h6 class="chat-contact-name text-truncate m-0 fw-normal">${escapeHTML(room.title || 'Tên phòng')}</h6>
                    <small class="text-muted">${room.lastMessageTime || ''}</small>
                  </div>
                  <small class="chat-contact-status text-truncate">${room.totalMembers + ' thành viên'}</small>
                </div>
              </a>
            </li>`;
        });
      }
      html += `</ul></div>`;
      $('#chat-contacts-wrapper').html(html);

      if (rooms.length > 0) {
        setTimeout(() => {
          $('.chat-contact-info').first().trigger('click');
        }, 100);
      }
    },
    error: function (err) {
      Swal.fire({
        icon: 'error',
        title: 'Không tải được danh sách phòng!',
        text: 'Kiểm tra lại kết nối.',
        confirmButtonColor: '#d33'
      });
      console.error('❌ Chat room loading failed:', err);
    }
  });
}

// Render tin nhắn (escape an toàn)
function buildMessageHTML(msg) {
  const isMine = msg.mine;
  const safeContent = escapeHTML(msg.content);
  const safeSender = escapeHTML(msg.sender.fullName);

  return `
    <li class="chat-message ${isMine ? 'chat-message-right' : ''}">
      <div class="d-flex overflow-hidden">
        ${!isMine ? `
        <div class="user-avatar flex-shrink-0 me-4">
          <div class="avatar avatar-sm">
            <span class="avatar-initial rounded-circle bg-label-secondary">${escapeHTML(safeSender[0] || '?')}</span>
          </div>
        </div>` : ''}
        <div class="chat-message-wrapper flex-grow-1 ${isMine ? 'text-end' : ''}">
          <div class="senderName" style="display: block; color: #838383; font-size: 14px; margin-bottom: 4px;">
            <small>${safeSender}</small>
          </div>
          <div class="chat-message-text">
            <p class="mb-0 text-break">${safeContent}</p>
          </div>
          <div class="text-muted mt-1 ${isMine ? 'text-end' : ''}">
            <small>${new Date(msg.createdAt).toLocaleTimeString()}</small>
          </div>
        </div>
        ${isMine ? `
        <div class="user-avatar flex-shrink-0 ms-4">
          <div class="avatar avatar-sm">
            <span class="avatar-initial rounded-circle bg-label-primary">Tôi</span>
          </div>
        </div>` : ''}
      </div>
    </li>`;
}

// Load tin nhắn
function loadMessagesForRoom(roomId, page = 0, size = 6, append = false) {
  isLoading = true;
  $.ajax({
    url: `v1/api/chats/messages`,
    method: 'GET',
    data: { roomId, page, size },
    success: function (response) {
      const messages = response.result;
      const meta = response.meta;
      const chatBody = $('.chat-history-body');

      if (append) {
        const oldScrollHeight = chatBody[0].scrollHeight;
        messages.reverse().forEach(msg => {
          $('.chat-history-body ul').prepend(buildMessageHTML(msg));
        });
        const newScrollHeight = chatBody[0].scrollHeight;
        chatBody.scrollTop(newScrollHeight - oldScrollHeight);
      } else {
        let html = '<ul class="list-unstyled chat-history">';
        messages.reverse().forEach(msg => {
          html += buildMessageHTML(msg);
        });
        html += '</ul>';
        chatBody.html(html);
        chatBody.scrollTop(chatBody[0].scrollHeight);
      }

      currentPage = meta.page;
      hasMore = currentPage < meta.pages - 1;
      isLoading = false;
    },
    error: function () {
      Swal.fire({
        icon: 'error',
        title: 'Không thể tải tin nhắn!',
        text: 'Vui lòng thử lại sau.',
        confirmButtonColor: '#d33'
      });
      isLoading = false;
    }
  });
}

// Khi DOM sẵn sàng
$(document).ready(function () {
  connectWebSocket();
  loadChatSidebar();

  $('#chat-contacts-wrapper').on('click', '.chat-contact-list-item', function (e) {
    e.preventDefault();
    $('.chat-contact-list-item').removeClass('active');
    $(this).addClass('active');

    currentRoomId = $(this).data('room-id');
    currentPage = 0;
    hasMore = true;

    const selectedRoom = rooms.find(r => r.id === currentRoomId);
    if (selectedRoom) {
      $('#room-title').text(selectedRoom.title || 'Tên phòng');
      $('#room-members').text(`${selectedRoom.totalMembers} thành viên`);
      $('#room-avatar').text(selectedRoom.title?.charAt(0) || '#');
    }

    loadMessagesForRoom(currentRoomId);
    subscribeToRoom(currentRoomId);
  });

  $('.chat-history-body').on('scroll', function () {
    if ($(this).scrollTop() <= 50 && hasMore && !isLoading && currentRoomId) {
      loadMessagesForRoom(currentRoomId, currentPage + 1, 6, true);
    }
  });
});

// Gửi tin nhắn
$(document).on('submit', '.form-send-message', function (e) {
  e.preventDefault();

  const messageInput = $('.message-input');
  const content = messageInput.val().trim();

  if (!content) {
    Swal.fire({
      icon: 'warning',
      title: 'Thiếu nội dung',
      text: 'Nội dung tin nhắn không được để trống!',
      confirmButtonColor: '#3085d6',
    });
    return;
  }

  if (content.length > 1000) {
    Swal.fire({
      icon: 'error',
      title: 'Tin nhắn quá dài!',
      text: 'Vui lòng nhập tin nhắn không vượt quá 1000 ký tự.',
      confirmButtonColor: '#d33',
    });
    return;
  }

  const messageData = { roomId: currentRoomId, content };

  $.ajax({
    url: `v1/api/messages/send`,
    method: 'POST',
    contentType: 'application/json',
    data: JSON.stringify(messageData),
    success: function () {
      messageInput.val('');
      Swal.fire({
        toast: true,
        position: 'top-end',
        icon: 'success',
        title: 'Tin nhắn đã gửi!',
        showConfirmButton: false,
        timer: 1200,
        timerProgressBar: true
      });
    },
    error: function () {
      Swal.fire({
        icon: 'error',
        title: 'Không gửi được tin nhắn!',
        text: 'Vui lòng kiểm tra kết nối hoặc thử lại sau.',
        confirmButtonColor: '#d33',
      });
    }
  });
});

// Emoji picker handler
document.addEventListener("DOMContentLoaded", () => {
  const emojiBtn = document.getElementById("emoji-btn");
  const emojiPicker = document.getElementById("emoji-picker");
  const messageInput = document.querySelector(".message-input");

  // Toggle bật/tắt emoji picker
  emojiBtn.addEventListener("click", (e) => {
    e.preventDefault();
    emojiPicker.style.display =
        emojiPicker.style.display === "none" ? "block" : "none";
  });

  // Khi người dùng chọn emoji → thêm vào input
  emojiPicker.addEventListener("emoji-click", (event) => {
    const emoji = event.detail.unicode;
    messageInput.value += emoji;
    emojiPicker.style.display = "none";
    messageInput.focus();
  });

  // Ẩn picker khi click ra ngoài
  document.addEventListener("click", (e) => {
    if (!emojiPicker.contains(e.target) && e.target !== emojiBtn) {
      emojiPicker.style.display = "none";
    }
  });
});

