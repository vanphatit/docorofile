package com.group.docorofile.services.impl;

import com.group.docorofile.entities.NotificationEntity;
import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.enums.ENotificationType;
import com.group.docorofile.exceptions.UserNotFoundException;
import com.group.docorofile.models.dto.NotificationDTO;
import com.group.docorofile.models.dto.ResultPaginationDTO;
import com.group.docorofile.repositories.NotificationRepository;
import com.group.docorofile.repositories.UserRepository;
import com.group.docorofile.request.CreateNotificationMultiRequest;
import com.group.docorofile.request.CreateNotificationRequest;
import com.group.docorofile.services.iNotificationService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public void createNotification(CreateNotificationRequest request, String email) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("User not found with email: " + email);
        }

        UserEntity receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + request.getReceiverId()));

        NotificationEntity notification = NotificationEntity.builder()
                .receiver(receiver)
                .author(userOpt.get().getFullName())
                .title(request.getTitle())
                .content(request.getContent())
                .type(request.getType())
                .isSeen(false)
                .createdOn(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    public void notifyFromSystem(CreateNotificationRequest request) {
        UserEntity receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + request.getReceiverId()));

        NotificationEntity notification = NotificationEntity.builder()
                .receiver(receiver)
                .author("SYSTEM")
                .title(request.getTitle())
                .content(request.getContent())
                .type(ENotificationType.SYSTEM)
                .isSeen(false)
                .createdOn(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    @Transactional
    public void sendSystemNotificationToUsers(CreateNotificationMultiRequest request, String email) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("User not found with email: " + email);
        }

        List<UserEntity> receivers = userRepository.findAllById(request.getReceiverIds());

        if (receivers.size() != request.getReceiverIds().size()) {
            throw new UserNotFoundException("Một hoặc nhiều người dùng không tồn tại.");
        }

        List<NotificationEntity> notifications = receivers.stream()
                .map(user -> NotificationEntity.builder()
                        .receiver(user)
                        .title(request.getTitle())
                        .content(request.getContent())
                        .type(request.getType())
                        .author(userOpt.get().getFullName())
                        .isSeen(false)
                        .build())
                .collect(Collectors.toList());

        notificationRepository.saveAll(notifications);
    }

    @Override
    public ResultPaginationDTO fetchNotifications(String email, Pageable pageable) {

        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("User not found with email: " + email);
        }
        UUID receiverId = userOpt.get().getUserId();

        Page<NotificationEntity> pageNotification = notificationRepository.findAllByReceiverId(receiverId, pageable);

        Page<NotificationDTO> dtoPage = pageNotification.map(this::convertToDTO);

        ResultPaginationDTO rs = new ResultPaginationDTO();

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(dtoPage.getTotalPages());
        meta.setTotal(dtoPage.getTotalElements());

        rs.setMeta(meta);
        rs.setResult(dtoPage.getContent());
        return rs;
    }

    @Override
    public void markAsRead(UUID id) {
        notificationRepository.findById(id).ifPresent(notification -> {
            notification.setSeen(true);
            notification.setSeenOn(LocalDateTime.now());
            notificationRepository.save(notification);
        });
    }

    private NotificationDTO convertToDTO(NotificationEntity notification) {
        return NotificationDTO.builder()
                .notificationId(notification.getNotificationId())
                .receiverId(notification.getReceiver().getUserId())
                .author(notification.getAuthor())
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType())
                .isSeen(notification.isSeen())
                .createdOn(notification.getCreatedOn())
                .seenOn(notification.getSeenOn())
                .build();
    }
}
