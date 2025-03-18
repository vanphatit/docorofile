package com.group.docorofile.services.impl;

import com.group.docorofile.entities.NotificationEntity;
import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.enums.ENotificationType;
import com.group.docorofile.exceptions.UserNotFoundException;
import com.group.docorofile.models.dto.NotificationDTO;
import com.group.docorofile.models.dto.ResultPaginationDTO;
import com.group.docorofile.repositories.NotificationRepository;
import com.group.docorofile.repositories.UserRepository;
import com.group.docorofile.services.iNotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements iNotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public NotificationDTO createNotification(UUID receiverId, ENotificationType type, String message) {
        UserEntity receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + receiverId));

        NotificationEntity notification = NotificationEntity.builder()
                .receiver(receiver)
                .type(type.name())
                .message(message)
                .isSeen(false)
                .createdOn(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);

        return convertToDTO(notification);
    }

    @Override
    public ResultPaginationDTO fetchNotifications(String email, Pageable pageable) {

        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }
        UUID receiverId = userOpt.get().getUserId();


        Page<NotificationEntity> pageNotification = notificationRepository.findAllByReceiverId(receiverId, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageNotification.getTotalPages());
        meta.setTotal(pageNotification.getTotalElements());

        rs.setMeta(meta);
        rs.setResult(pageNotification.getContent());

        return rs;
    }

    @Override
    public void markAsRead(UUID id) {
        notificationRepository.findById(id).ifPresent(notification -> {
            notification.setSeen(true);
            notificationRepository.save(notification);
        });
    }

    private NotificationDTO convertToDTO(NotificationEntity notification) {
        LocalDateTime seenOnTime = null;
        if (notification.isSeen()) {
            seenOnTime = LocalDateTime.now();
        }

        return NotificationDTO.builder()
                .notificationId(notification.getNotificationId())
                .receiverId(notification.getReceiver().getUserId())
                .type(ENotificationType.valueOf(notification.getType()))
                .message(notification.getMessage())
                .isSeen(notification.isSeen())
                .createdOn(notification.getCreatedOn())
                .seenOn(seenOnTime)
                .build();
    }

}
