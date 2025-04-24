let currentRoomId = null;
let currentPage = 0;
let hasMore = true;
let isLoading = false;
let stompClient = null;
let currentSubscription = null;

function connectWebSocket() {
  const socket = new SockJS("http://localhost:9091/ws");
  stompClient = Stomp.over(socket);
  stompClient.connect({}, () => {
    console.log("WebSocket connected");
  });
}

function subscribeToRoom(roomId) {
  if (currentSubscription) currentSubscription.unsubscribe();

  currentSubscription = stompClient.subscribe(`/topic/room.${roomId}`, function (message) {
    const msg = JSON.parse(message.body);
    const html = buildMessageHTML({
      content: msg.content,
      createdAt: msg.timestamp,
      sender: { fullName: msg.sender },
      mine: msg.senderId === getCurrentUserId()
    });
    $('.chat-history-body ul').append(html);
    $('.chat-history-body').scrollTop($('.chat-history-body')[0].scrollHeight);
  });
}

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
              <div class="flex-shrink-0 avatar avatar-online me-4" data-bs-toggle="sidebar" data-overlay="app-overlay-ex" data-target="#app-chat-sidebar-left">
                <img class="user-avatar rounded-circle cursor-pointer" src="/assets/img/avatars/1.png" alt="Avatar" />
              </div>
              <div class="flex-grow-1 input-group input-group-sm input-group-merge rounded-pill">
                <span class="input-group-text"><i class="ri-search-line lh-1 ri-20px"></i></span>
                <input type="text" class="form-control chat-search-input" placeholder="Search..." />
              </div>
            </div>
            <i class="ri-close-line ri-24px cursor-pointer position-absolute top-50 end-0 translate-middle fs-4 d-lg-none d-block"
              data-overlay data-bs-toggle="sidebar" data-target="#app-chat-contacts"></i>
          </div>
          <div class="sidebar-body">
            <ul class="list-unstyled chat-contact-list py-2 mb-0" id="chat-list">
              <li class="chat-contact-list-item chat-contact-list-item-title mt-0 not-clickable">
                <h5 class="text-primary mb-0">Chats</h5>
              </li>`;

        if (!rooms.length) {
          html += `<li class="chat-contact-list-item chat-list-item-0"><h6 class="text-muted mb-0">No Chats Found</h6></li>`;
        } else {
          rooms.forEach(room => {
            html += `
              <li class="chat-contact-list-item mb-1" data-room-id="${room.id}">
                <a href="#" class="d-flex align-items-center">
                  <div class="flex-shrink-0 avatar ${room.status === 'offline' ? 'avatar-offline' : 'avatar-online'}">
                    ${room.avatarUrl ? `<img src="${room.avatarUrl}" alt="Avatar" class="rounded-circle" />`
                : `<span class="avatar-initial rounded-circle bg-label-success">${room.title?.charAt(0) || '?'}</span>`}
                  </div>
                  <div class="chat-contact-info flex-grow-1 ms-4">
                    <div class="d-flex justify-content-between align-items-center">
                      <h6 class="chat-contact-name text-truncate m-0 fw-normal">${room.title}</h6>
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
        $('#chat-contacts-wrapper').html('<div class="text-danger text-center p-4">Failed to load chat contacts</div>');
        console.error('Chat room loading failed:', err);
      }
    });
  }

function getCurrentUserId() {
  return window.currentUserId;
}

function buildMessageHTML(msg) {
  console.log(msg)
  const isMine = msg.mine;
  return `
    <li class="chat-message ${isMine ? 'chat-message-right' : ''}">
      <div class="d-flex overflow-hidden">
        ${!isMine ? `
        <div class="user-avatar flex-shrink-0 me-4">
          <div class="avatar avatar-sm">
            <span class="avatar-initial rounded-circle bg-label-secondary">${msg.sender.fullName[0]}</span>
          </div>
        </div>` : ''}
        <div class="chat-message-wrapper flex-grow-1 ${isMine ? 'text-end' : ''}">
          <div class="chat-message-text">
            <p class="mb-0 text-break">${msg.content}</p>
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
      if (!append) {
        $('.chat-history-body').html('<div class="text-danger text-center p-3">Không thể tải tin nhắn</div>');
      }
      isLoading = false;
    }
  });
}

$(document).ready(function () {
  connectWebSocket();

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
      $('#room-avatar').text(selectedRoom.name?.charAt(0) || '#');
    }

    loadMessagesForRoom(currentRoomId);
    subscribeToRoom(currentRoomId);
  });

  $('.chat-history-body').on('scroll', function () {
    if ($(this).scrollTop() <= 50 && hasMore && !isLoading && currentRoomId) {
      loadMessagesForRoom(currentRoomId, currentPage + 1, 6, true);
    }
  });

  loadChatSidebar();
});

$(document).on('submit', '.form-send-message', function (e) {
  e.preventDefault();

  const messageInput = $('.message-input');
  const content = messageInput.val().trim();
  if (!content) return;

  const messageData = {
    roomId: currentRoomId,
    content: content
  };

  $.ajax({
    url: `v1/api/messages/send`,
    method: 'POST',
    contentType: 'application/json',
    data: JSON.stringify(messageData),
    success: function () {
      messageInput.val('');
    },
    error: function () {
      alert('Không gửi được tin nhắn!');
    }
  });
});
