package com.alerts.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing user-defined alert rules
 * Supports various alert types: stock price, EMI due, policy expiry, tax deadlines
 */
@Entity
@Table(name = "alert_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType type;

    // For stock alerts
    private String symbol;
    private BigDecimal targetPrice;
    private String priceCondition; // ABOVE, BELOW, EQUALS

    // For due date alerts
    private Integer daysBeforeDue;

    // For percentage change alerts
    private BigDecimal percentageChange;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertChannel channel;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    private LocalDateTime createdAt;
    private LocalDateTime lastTriggeredAt;

    private String description;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
