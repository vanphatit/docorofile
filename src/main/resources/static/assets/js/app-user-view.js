/**
 * App User View - Suspend User Script
 */
'use strict';

document.addEventListener('DOMContentLoaded', async () => {
  const parts = window.location.pathname.split('/').filter(p => p);
  const userId = parts[parts.length - 1];

  try {

    await fetch(`/v1/api/users/checkMembership/${userId}`, {
      method: 'POST',
      credentials: 'include'
    });

    const response = await fetch(`/v1/api/users/user-detail/${userId}`, {
      method: 'GET',
      credentials: 'include'
    });
    const result = await response.json();
    const data = result.data;

    if (!data) return;

    const setText = (id, value) => {
      const el = document.getElementById(id);
      if (el) el.innerText = value ?? '-';
    };

    const toggleVisible = (id, show) => {
      const el = document.getElementById(id);
      if (el) el.classList.toggle('d-none', !show);
    };

    // Gán thông tin chung
    setText('user-info-name', data.name);
    setText('user-info-role', data.role?.replace('ROLE_', ''));
    setText('user-info-email', data.email);
    setText('user-info-active', data.status);

    // Set class cho role và active badge
    const userInfoRole = document.getElementById('user-info-role');
    const userInfoActive = document.getElementById('user-info-active');

    if (userInfoRole) {
      userInfoRole.className = data.role === "Admin"
          ? "badge bg-label-primary rounded-pill"
          : data.role === "Member"
              ? "badge bg-label-success rounded-pill"
              : "badge bg-label-warning rounded-pill";
    }

    if (userInfoActive) {
      userInfoActive.className = data.status === "Active"
          ? "badge bg-label-success rounded-pill"
          : "badge bg-label- rounded-pill";
    }

    // Ẩn/hiện thông tin riêng cho từng role
    const isMember = data.role === "Member";
    const isModerator = data.role === "Moderator";

    toggleVisible('li-user-info-university', isMember);
    toggleVisible('li-user-info-downloadLimit', isMember);
    toggleVisible('li-user-info-isChat', isMember);
    toggleVisible('li-user-info-isComment', isMember);
    toggleVisible('li-user-info-isReportManager', isModerator);

    if (isMember) {
      setText('user-info-university', data.university);
      setText('user-info-downloadLimit', data.downloadLimit);
      setText('user-info-isChat', data.chat ? "Yes" : "No");
      setText('user-info-isComment', data.comment ? "Yes" : "No");
    }

    if (isModerator) {
      setText('user-info-isReportManager', data.isReportManager ? "Yes" : "No");
    }

    // Created, Updated date
    setText('user-info-createdOn', data.createdOn ? new Date(data.createdOn).toLocaleString() : '-');
    setText('user-info-updatedOn', data.modifiedOn ? new Date(data.modifiedOn).toLocaleString() : '-');

    // Plan
    const planBadge = document.getElementById('plan-badge');
    const planPrice = document.getElementById('plan-price');
    const planBenefits = document.getElementById('plan-benefits');

    const membershipUsedEl = document.getElementById('userInfo-membershipUsed');
    const membershipContainsEl = document.getElementById('userInfo-membershipContains');
    const membershipProgressEl = document.getElementById('userInfo-membershipProgress');

    if (planBadge) planBadge.innerText = data.current_plan || "Free";
    if (planPrice) planPrice.innerText = data.current_plan === "Premium" ? "80.000" : "0";

    // Ẩn danh sách quyền lợi nếu là Free
    if (planBenefits && data.current_plan !== "PREMIUM" ) {
      planBenefits.classList.add("d-none");
      membershipUsedEl.classList.add("d-none");
      membershipContainsEl.classList.add("d-none");
      membershipProgressEl.classList.add("d-none");
    } else if (planBenefits) {
      planBenefits.classList.remove("d-none");
    }
    // Membership Info

    if (data.membershipStartDate && data.membershipEndDate) {
      const start = new Date(data.membershipStartDate);
      const end = new Date(data.membershipEndDate);
      const today = new Date();

      const totalDays = Math.max(1, Math.floor((end - start) / (1000 * 60 * 60 * 24)));
      const usedDays = Math.floor((today - start) / (1000 * 60 * 60 * 24));
      const remainingDays = data.membershipContain
      const percent = Math.min(100, Math.round((usedDays / totalDays) * 100));

      if (membershipUsedEl) {
        membershipUsedEl.innerText = `${usedDays} of ${totalDays} Days`;
      }

      if (membershipProgressEl) {
        membershipProgressEl.style.width = `${percent}%`;
        membershipProgressEl.setAttribute('aria-valuenow', percent.toString());
      }

      if (membershipContainsEl) {
        membershipContainsEl.innerText = `${remainingDays} Days Remaining`;
      }
    }

    // Avatar (optional)
    // const avatarImg = document.querySelector('.user-avatar-section img');
    // if (avatarImg) avatarImg.src = data.avatar || '/images/default-avatar.png';

  } catch (err) {
    console.error('❌ Failed to fetch user detail:', err);
  }
});

(function () {
  const suspendUser = document.querySelector('.suspend-user');

  // Suspend User javascript
  if (suspendUser) {
    suspendUser.onclick = function () {
      Swal.fire({
        title: 'Are you sure?',
        text: "You won't be able to revert user!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Yes, Suspend user!',
        customClass: {
          confirmButton: 'btn btn-primary me-2 waves-effect waves-light',
          cancelButton: 'btn btn-outline-secondary waves-effect'
        },
        buttonsStyling: false
      }).then(function (result) {
        if (result.value) {
          Swal.fire({
            icon: 'success',
            title: 'Suspended!',
            text: 'User has been suspended.',
            customClass: {
              confirmButton: 'btn btn-success waves-effect'
            }
          });
        } else if (result.dismiss === Swal.DismissReason.cancel) {
          Swal.fire({
            title: 'Cancelled',
            text: 'Cancelled Suspension :)',
            icon: 'error',
            customClass: {
              confirmButton: 'btn btn-success waves-effect'
            }
          });
        }
      });
    };
  }

  //? Billing page have multiple buttons
  // Cancel Subscription alert
  const cancelSubscription = document.querySelectorAll('.cancel-subscription');

  // Alert With Functional Confirm Button
  if (cancelSubscription) {
    cancelSubscription.forEach(btnCancle => {
      btnCancle.onclick = function () {
        Swal.fire({
          text: 'Are you sure you would like to cancel your subscription?',
          icon: 'warning',
          showCancelButton: true,
          confirmButtonText: 'Yes',
          customClass: {
            confirmButton: 'btn btn-primary me-2 waves-effect waves-light',
            cancelButton: 'btn btn-outline-secondary waves-effect'
          },
          buttonsStyling: false
        }).then(function (result) {
          if (result.value) {
            Swal.fire({
              icon: 'success',
              title: 'Unsubscribed!',
              text: 'Your subscription cancelled successfully.',
              customClass: {
                confirmButton: 'btn btn-success waves-effect'
              }
            });
          } else if (result.dismiss === Swal.DismissReason.cancel) {
            Swal.fire({
              title: 'Cancelled',
              text: 'Unsubscription Cancelled!!',
              icon: 'error',
              customClass: {
                confirmButton: 'btn btn-success waves-effect'
              }
            });
          }
        });
      };
    });
  }
})();
