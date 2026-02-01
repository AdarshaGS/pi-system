package com.budget.data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Budget alert entity for overspending notifications")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Schema(description = "User ID who receives this alert", example = "1")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Schema(description = "Type of alert", example = "OVERSPENDING")
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Schema(description = "Severity level of alert", example = "WARNING")
    private AlertSeverity severity;

    @Column(nullable = true)
    @Schema(description = "Category related to the alert", example = "FOOD")
    private String category; // Can be standard or custom category

    @Column(nullable = false, length = 500)
    @Schema(description = "Alert message content", example = "You've spent 75% of your FOOD budget for January 2026")
    private String message;

    @Column(nullable = false)
    @Schema(description = "Budget limit amount", example = "15000.00")
    private BigDecimal budgetLimit;

    @Column(nullable = false)
    @Schema(description = "Amount spent so far", example = "11250.00")
    private BigDecimal amountSpent;

    @Column(nullable = false)
    @Schema(description = "Percentage of budget used", example = "75.00")
    private BigDecimal percentageUsed;

    @Column(name = "month_year", nullable = false, length = 7)
    @Schema(description = "Month for which alert is generated", example = "2026-02")
    private String monthYear; // Format: YYYY-MM

    @Column(nullable = false)
    @Builder.Default
    @Schema(description = "Whether the alert has been read", example = "false")
    private Boolean isRead = false;

    @Column(nullable = false)
    @Builder.Default
    @Schema(description = "Whether alert notification was sent", example = "true")
    private Boolean notificationSent = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Alert creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    @Schema(description = "Timestamp when alert was marked as read")
    private LocalDateTime readAt;

    // Enums

    public enum AlertType {
        OVERSPENDING,
        BUDGET_EXCEEDED,
        APPROACHING_LIMIT,
        SAVINGS_MILESTONE,
        RECURRING_PAYMENT_DUE
    }

    public enum AlertSeverity {
        INFO,      // 0-74% - Informational
        WARNING,   // 75-89% - Warning
        CRITICAL,  // 90-99% - Critical
        DANGER     // 100%+ - Over budget
    }

    // Helper methods

    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    public boolean isCritical() {
        return severity == AlertSeverity.CRITICAL || severity == AlertSeverity.DANGER;
    }

    public boolean isOverBudget() {
        return percentageUsed.compareTo(BigDecimal.valueOf(100)) >= 0;
    }
}
