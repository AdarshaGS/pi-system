package com.alerts.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alerts.dto.NotificationDTO;
import com.alerts.entity.AlertChannel;
import com.alerts.entity.NotificationType;
import com.alerts.entity.UserNotification;
import com.alerts.repository.UserNotificationRepository;
import com.users.data.Users;
import com.users.repo.UsersRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing user notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final UserNotificationRepository notificationRepository;
    private final UsersRepository userRepository;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send a notification to a user
     */
    @Transactional
    public NotificationDTO sendNotification(Long userId, String title, String message, 
                                           NotificationType type, AlertChannel channel) {
        return sendNotification(userId, title, message, type, channel, null, null);
    }

    /**
     * Send a notification with metadata and alert rule ID
     */
    @Transactional
    public NotificationDTO sendNotification(Long userId, String title, String message, 
                                           NotificationType type, AlertChannel channel,
                                           Map<String, String> metadata, Long alertRuleId) {
        // Create and save notification
        UserNotification notification = UserNotification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .channel(channel)
                .isRead(false)
                .metadata(metadata != null ? metadata : Map.of())
                .alertRuleId(alertRuleId)
                .build();

        notification = notificationRepository.save(notification);
        log.info("Created notification ID: {} for user: {}", notification.getId(), userId);

        // Send via appropriate channel
        if (channel == AlertChannel.EMAIL || channel == AlertChannel.IN_APP) {
            // Always send in-app notification
            sendInAppNotification(userId, notification);
        }

        if (channel == AlertChannel.EMAIL) {
            sendEmailNotification(userId, title, message);
        }

        return mapToDTO(notification);
    }

    /**
     * Send in-app notification via WebSocket
     */
    private void sendInAppNotification(Long userId, UserNotification notification) {
        try {
            messagingTemplate.convertAndSend(
                "/topic/notifications/" + userId, 
                mapToDTO(notification)
            );
            log.info("Sent in-app notification to user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to send in-app notification to user: {}. Error: {}", 
                     userId, e.getMessage());
        }
    }

    /**
     * Send email notification
     */
    private void sendEmailNotification(Long userId, String title, String message) {
        try {
            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            
            emailService.sendEmail(user.getEmail(), title, message);
        } catch (Exception e) {
            log.error("Failed to send email notification to user: {}. Error: {}", 
                     userId, e.getMessage());
        }
    }

    /**
     * Get all notifications for a user
     */
    public List<NotificationDTO> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get unread notifications for a user
     */
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get unread notification count
     */
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public NotificationDTO markAsRead(Long notificationId) {
        UserNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        
        notification.markAsRead();
        notification = notificationRepository.save(notification);
        
        return mapToDTO(notification);
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        List<UserNotification> notifications = notificationRepository
                .findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
        
        notifications.forEach(n -> {
            n.markAsRead();
            notificationRepository.save(n);
        });
    }

    /**
     * Delete a notification
     */
    @Transactional
    public void deleteNotification(Long userId, Long notificationId) {
        notificationRepository.deleteByUserIdAndId(userId, notificationId);
    }

    /**
     * Get recent notifications (last 7 days)
     */
    public List<NotificationDTO> getRecentNotifications(Long userId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return notificationRepository
                .findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, sevenDaysAgo)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Map entity to DTO
     */
    private NotificationDTO mapToDTO(UserNotification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .metadata(notification.getMetadata())
                .alertRuleId(notification.getAlertRuleId())
                .channel(notification.getChannel())
                .build();
    }
}
