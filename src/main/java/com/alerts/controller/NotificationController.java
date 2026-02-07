package com.alerts.controller;

import com.alerts.dto.NotificationDTO;
import com.alerts.entity.UserNotification;
import com.alerts.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for managing user notifications
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Get all notifications for a user
     * GET /api/v1/notifications/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(@PathVariable Long userId) {
        List<NotificationDTO> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread notifications for a user
     * GET /api/v1/notifications/user/{userId}/unread
     */
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(@PathVariable Long userId) {
        List<NotificationDTO> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread notification count
     * GET /api/v1/notifications/user/{userId}/unread-count
     */
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable Long userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * Get recent notifications (last 7 days)
     * GET /api/v1/notifications/user/{userId}/recent
     */
    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<List<NotificationDTO>> getRecentNotifications(@PathVariable Long userId) {
        List<NotificationDTO> notifications = notificationService.getRecentNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Mark notification as read
     * PATCH /api/v1/notifications/{id}/read
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationDTO> markAsRead(@PathVariable Long id) {
        NotificationDTO notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(notification);
    }

    /**
     * Mark all notifications as read for a user
     * PATCH /api/v1/notifications/user/{userId}/read-all
     */
    @PatchMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete a notification
     * DELETE /api/v1/notifications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id,
            @RequestParam Long userId) {
        notificationService.deleteNotification(userId, id);
        return ResponseEntity.noContent().build();
    }

}

