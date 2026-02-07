package com.alerts.dto;

import com.alerts.entity.AlertChannel;
import com.alerts.entity.AlertType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO for creating/updating alert rules
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertRuleRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Alert type is required")
    private AlertType type;

    private String symbol;
    private BigDecimal targetPrice;
    private String priceCondition; // ABOVE, BELOW, EQUALS
    private Integer daysBeforeDue;
    private BigDecimal percentageChange;

    @NotNull(message = "Alert channel is required")
    private AlertChannel channel;

    @Builder.Default
    private Boolean enabled = true;
    private String description;
}
