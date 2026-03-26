package com.budget.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Alert summary statistics")
public class AlertSummary {

    @Schema(description = "Total number of active alerts", example = "5")
    private Integer totalAlerts;

    @Schema(description = "Number of unread alerts", example = "3")
    private Integer unreadAlerts;

    @Schema(description = "Number of critical alerts", example = "2")
    private Integer criticalAlerts;

    @Schema(description = "Number of warning alerts", example = "2")
    private Integer warningAlerts;

    @Schema(description = "Number of info alerts", example = "1")
    private Integer infoAlerts;

    @Schema(description = "Number of over-budget categories", example = "1")
    private Integer categoriesOverBudget;
}
