package com.group.docorofile.controllers.v1.api.notification;

import com.group.docorofile.models.dto.ResultPaginationDTO;
import com.group.docorofile.request.CreateNotificationMultiRequest;
import com.group.docorofile.request.CreateNotificationRequest;
import com.group.docorofile.response.SuccessResponse;
import com.group.docorofile.services.iNotificationService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api/notifications")
public class NotificationAPIController {

    private final iNotificationService notificationService;

    public NotificationAPIController(iNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR', 'ROLE_MEMBER')")
    @PostMapping("/create")
    public ResponseEntity<SuccessResponse> createNotification(@RequestBody CreateNotificationRequest request) {
        String email = getEmailFromSecurityContext();
        notificationService.createNotification(request, email);
        SuccessResponse response = new SuccessResponse(
                "Đã gửi thông báo thành công",
                HttpStatus.CREATED.value(), null,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR', 'ROLE_MEMBER')")
    @PostMapping("/notify")
    public ResponseEntity<SuccessResponse> notifyFromSystem(@RequestBody CreateNotificationRequest request) {
        notificationService.notifyFromSystem(request);
        SuccessResponse response = new SuccessResponse(
                "Đã gửi thông báo thành công",
                HttpStatus.CREATED.value(), null,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR', 'ROLE_MEMBER')")
    @PostMapping("/create/multiple")
    public ResponseEntity<SuccessResponse> sendNotificationToMultipleUsers(
            @RequestBody CreateNotificationMultiRequest request) {
        String email = getEmailFromSecurityContext();
        notificationService.sendSystemNotificationToUsers(request,email);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Đã gửi thông báo thành công", 201, null, LocalDateTime.now()));
    }


    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR', 'ROLE_MEMBER')")
    @GetMapping("")
    public ResponseEntity<ResultPaginationDTO> getAllNotifications(Pageable pageable) {

        String email = getEmailFromSecurityContext();

        return ResponseEntity.ok().body(notificationService.fetchNotifications(email, pageable));
    }

    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @PatchMapping("/{id}/mark-as-read")
    public ResponseEntity<SuccessResponse> markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id);
        SuccessResponse response = new SuccessResponse(
                "Notification marked as read",
                HttpStatus.OK.value(),
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }

    private String getEmailFromSecurityContext() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        return null;
    }
}
