package com.budget.dto;

import com.budget.data.Alert;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Alert response DTO")
public class AlertResponse {

    @Schema(description = "Alert ID", example = "1")
    private Long id;

    @Schema(description = "Alert type", example = "OVERSPENDING")
    private String alertType;

    @Schema(description = "Alert severity", example = "WARNING")
    private String severity;

    @Schema(description = "Category name", example = "FOOD")
    private String category;

    @Schema(description = "Alert message", example = "You've spent 75% of your FOOD budget")
    private String message;

    @Schema(description = "Budget limit", example = "15000.00")
    private BigDecimal budgetLimit;

    @Schema(description = "Amount spent", example = "11250.00")
    private BigDecimal amountSpent;

    @Schema(description = "Percentage used", example = "75.00")
    private BigDecimal percentageUsed;

    @Schema(description = "Month and year", example = "2026-02")
    private String monthYear;

    @Schema(description = "Whether alert is read", example = "false")
    private Boolean isRead;

    @Schema(description = "Whether notification was sent", example = "true")
    private Boolean notificationSent;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Read timestamp")
    private LocalDateTime readAt;

    // Static factory method
    public static AlertResponse fromEntity(Alert alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .alertType(alert.getAlertType().name())
                .severity(alert.getSeverity().name())
                .category(alert.getCategory())
                .message(alert.getMessage())
                .budgetLimit(alert.getBudgetLimit())
                .amountSpent(alert.getAmountSpent())
                .percentageUsed(alert.getPercentageUsed())
                .monthYear(alert.getMonthYear())
                .isRead(alert.getIsRead())
                .notificationSent(alert.getNotificationSent())
                .createdAt(alert.getCreatedAt())
                .readAt(alert.getReadAt())
                .build();
    }
}
