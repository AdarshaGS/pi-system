package com.alerts.dto;

import com.alerts.entity.AlertChannel;
import com.alerts.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for notification responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {

    private Long id;
    private Long userId;
    private String title;
    private String message;
    private NotificationType type;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private Map<String, String> metadata;
    private Long alertRuleId;
    private AlertChannel channel;
}
