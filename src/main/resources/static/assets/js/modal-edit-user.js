/**
 * Edit User
 */

'use strict';

document.addEventListener('DOMContentLoaded', async function () {
  const editUserModal = document.getElementById('editUser');
  const formEditUser = document.getElementById('editUserForm');
  const submitBtn = document.getElementById('btn-modelSubmit');

  if (!editUserModal || !formEditUser) return;

  let currentUserId = null;
  let currentUserRole = null;
  let isProfile = false;

  editUserModal.addEventListener('show.bs.modal', async () => {
    const parts = window.location.pathname.split('/').filter(p => p);
    currentUserId = parts[parts.length - 1];
    const regex = /^[0-9a-f]{8}-[0-9a-f]{4}-7[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
    if(!regex.test(currentUserId) && parts[0] !== "admin") {

      isProfile = true;
      const response = await fetch('/v1/api/auth/current-user', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        credentials: 'include' // QUAN TRỌNG: gửi cookie (JWT) kèm request
      });

      if (!response.ok) {
        throw new Error('Không thể lấy thông tin người dùng');
      }

      const result = await response.json();
      const data = result.data;
      if (!data) return;
      currentUserId = data.id;
    }

    try {
      const response = await fetch(`/v1/api/users/user-detail/${currentUserId}`, {
        method: 'GET',
        credentials: 'include'
      });

      const result = await response.json();
      const data = result.data;

      if (!data) return;

      // Gán giá trị vào form
      document.getElementById('modalEditUserFullName').value = data.name || '';

      // Reset all optional fields hidden
      [
        'li-modalEditUniversity',
        'li-modalEditIsChat',
        'li-modalEditIsComment',
        'li-modalEditIsReportManager',
        'modalEditUserDownloadLimit'
      ].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.closest('.col-md-6').classList.add('d-none');
      });

      currentUserRole = data.role;

      if (currentUserRole === "Member") {
        if(!isProfile) {
          document.getElementById('li-modalEditIsChat').classList.remove('d-none');
          document.getElementById('li-modalEditIsComment').classList.remove('d-none');
          document.getElementById('modalEditUserDownloadLimit').closest('.col-md-6').classList.remove('d-none');

          document.getElementById('modalEditUserDownloadLimit').value = data.downloadLimit || 0;
          document.getElementById('modalEditIsChat').value = data.chat ? "True" : "False";
          document.getElementById('modalEditIsComment').value = data.comment ? "True" : "False";
        }
        document.getElementById('li-modalEditUniversity').classList.remove('d-none');
        const select = document.getElementById('modalEditUniversity');
        try {
          const uniRes = await fetch('/v1/api/universities/names', {
            method: 'GET',
            credentials: 'include'
          });
          const uniResult = await uniRes.json();
          const universities = uniResult.data || [];

          // Xóa toàn bộ option cũ
          select.innerHTML = '';
          universities.forEach(u => {
            const option = document.createElement('option');
            option.value = u.univName;
            option.textContent = u.univName;
            if (u.univName === data.university) {
              option.selected = true;
            }
            select.appendChild(option);
          });
        } catch (err) {
          console.error('❌ Failed to load universities:', err);
        }
      } else if (currentUserRole === "Moderator") {
        document.getElementById('li-modalEditIsReportManager').classList.remove('d-none');
        document.getElementById('modalEditIsReportManager').value = data.isReportManager ? "True" : "False";
      }
      // Admin: không có gì thêm ngoài FullName

      if (submitBtn) {
        submitBtn.addEventListener('click', async (e) => {
          e.preventDefault();
          try {
            const formData = new URLSearchParams();
            formData.append('fullName', document.getElementById('modalEditUserFullName').value);

            if (currentUserRole === "Member") {
              if(isProfile) {
                console.log("isProfile");
                console.log("chat", data.chat);
                console.log("comment", data.comment);
                console.log("downloadLimit", data.downloadLimit);
                formData.append('downloadLimit', data.downloadLimit);
                formData.append('isChat', data.chat);
                formData.append('isComment', data.comment);
              } else {
                formData.append('downloadLimit', document.getElementById('modalEditUserDownloadLimit').value);
                formData.append('isChat', document.getElementById('modalEditIsChat').value);
                formData.append('isComment', document.getElementById('modalEditIsComment').value);
              }

              formData.append('universityName', document.getElementById('modalEditUniversity').value);
            } else if (currentUserRole === "Moderator") {
              formData.append('isReportManage', document.getElementById('modalEditIsReportManager').value);
            }

            // Gọi API Update
            const res = await fetch(`/v1/api/users/${currentUserId}`, {
              method: 'PUT',
              headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
              },
              credentials: 'include',
              body: formData.toString()
            });

            if (!res.ok) throw new Error('Failed to update user');

            Swal.fire({
              icon: 'success',
              title: 'Success',
              text: 'User updated successfully!',
              customClass: {
                confirmButton: 'btn btn-success waves-effect'
              }
            }).then(() => {
              window.location.reload();
            });

          } catch (error) {
            console.error('❌ Failed to submit update user:', error);
            Swal.fire({
              icon: 'error',
              title: 'Error',
              text: 'Failed to update user. Please try again.' + error,
              customClass: {
                confirmButton: 'btn btn-danger waves-effect'
              }
            });
          }
        });
      }
    } catch (error) {
      console.error('❌ Failed to fetch user detail for edit:', error);
    }
  });


});
