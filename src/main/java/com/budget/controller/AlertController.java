package com.budget.controller;

import com.budget.dto.AlertResponse;
import com.budget.dto.AlertSummary;
import com.budget.service.AlertService;
import com.common.security.AuthenticationHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Alert Management", description = "APIs for budget alerts and notifications")
public class AlertController {

    private final AlertService alertService;
    private final AuthenticationHelper authenticationHelper;

    @GetMapping
    @Operation(
        summary = "Get all alerts",
        description = "Retrieve all alerts for the authenticated user, ordered by creation date (newest first)"
    )
    public ResponseEntity<List<AlertResponse>> getAllAlerts(
            @Parameter(description = "User ID", required = true)
            @RequestParam Long userId
    ) {
        authenticationHelper.validateUserAccess(userId);
        log.info("Fetching all alerts for user {}", userId);
        
        List<AlertResponse> alerts = alertService.getUserAlerts(userId);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/unread")
    @Operation(
        summary = "Get unread alerts",
        description = "Retrieve all unread alerts for the authenticated user"
    )
    public ResponseEntity<List<AlertResponse>> getUnreadAlerts(
            @Parameter(description = "User ID", required = true)
            @RequestParam Long userId
    ) {
        authenticationHelper.validateUserAccess(userId);
        log.info("Fetching unread alerts for user {}", userId);
        
        List<AlertResponse> alerts = alertService.getUnreadAlerts(userId);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/month/{monthYear}")
    @Operation(
        summary = "Get alerts by month",
        description = "Retrieve all alerts for a specific month (format: YYYY-MM)"
    )
    public ResponseEntity<List<AlertResponse>> getAlertsByMonth(
            @Parameter(description = "User ID", required = true)
            @RequestParam Long userId,
            @Parameter(description = "Month and year (format: YYYY-MM)", example = "2026-02")
            @PathVariable("monthYear") String monthYear
    ) {
        authenticationHelper.validateUserAccess(userId);
        log.info("Fetching alerts for user {} in month {}", userId, monthYear);
        
        List<AlertResponse> alerts = alertService.getAlertsByMonth(userId, monthYear);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/summary")
    @Operation(
        summary = "Get alert summary",
        description = "Get summary statistics of all alerts for the authenticated user"
    )
    public ResponseEntity<AlertSummary> getAlertSummary(
            @Parameter(description = "User ID", required = true)
            @RequestParam Long userId
    ) {
        authenticationHelper.validateUserAccess(userId);
        log.info("Fetching alert summary for user {}", userId);
        
        AlertSummary summary = alertService.getAlertSummary(userId);
        return ResponseEntity.ok(summary);
    }

    @PutMapping("/{alertId}/read")
    @Operation(
        summary = "Mark alert as read",
        description = "Mark a specific alert as read"
    )
    public ResponseEntity<AlertResponse> markAlertAsRead(
            @Parameter(description = "Alert ID", required = true)
            @PathVariable("alertId") Long alertId,
            @Parameter(description = "User ID", required = true)
            @RequestParam Long userId
    ) {
        authenticationHelper.validateUserAccess(userId);
        log.info("Marking alert {} as read for user {}", alertId, userId);
        
        AlertResponse alert = alertService.markAlertAsRead(alertId, userId);
        return ResponseEntity.ok(alert);
    }

    @PutMapping("/read-all")
    @Operation(
        summary = "Mark all alerts as read",
        description = "Mark all unread alerts as read for the authenticated user"
    )
    public ResponseEntity<Map<String, Object>> markAllAlertsAsRead(
            @Parameter(description = "User ID", required = true)
            @RequestParam Long userId
    ) {
        authenticationHelper.validateUserAccess(userId);
        log.info("Marking all alerts as read for user {}", userId);
        
        Integer count = alertService.markAllAlertsAsRead(userId);
        return ResponseEntity.ok(Map.of(
            "message", "All alerts marked as read",
            "count", count
        ));
    }

    @DeleteMapping("/{alertId}")
    @Operation(
        summary = "Delete alert",
        description = "Delete a specific alert"
    )
    public ResponseEntity<Map<String, String>> deleteAlert(
            @Parameter(description = "Alert ID", required = true)
            @PathVariable("alertId") Long alertId,
            @Parameter(description = "User ID", required = true)
            @RequestParam Long userId
    ) {
        authenticationHelper.validateUserAccess(userId);
        log.info("Deleting alert {} for user {}", alertId, userId);
        
        alertService.deleteAlert(alertId, userId);
        return ResponseEntity.ok(Map.of("message", "Alert deleted successfully"));
    }

    @DeleteMapping
    @Operation(
        summary = "Delete all alerts",
        description = "Delete all alerts for the authenticated user"
    )
    public ResponseEntity<Map<String, String>> deleteAllAlerts(
            @Parameter(description = "User ID", required = true)
            @RequestParam Long userId
    ) {
        authenticationHelper.validateUserAccess(userId);
        log.info("Deleting all alerts for user {}", userId);
        
        alertService.deleteAllAlerts(userId);
        return ResponseEntity.ok(Map.of("message", "All alerts deleted successfully"));
    }

    @PostMapping("/check")
    @Operation(
        summary = "Manual alert check",
        description = "Manually trigger budget check and alert generation for current month"
    )
    public ResponseEntity<Map<String, Object>> checkBudgetsManually(
            @Parameter(description = "User ID", required = true)
            @RequestParam Long userId,
            @Parameter(description = "Month year (format: YYYY-MM). Defaults to current month")
            @RequestParam(required = false) String monthYear
    ) {
        authenticationHelper.validateUserAccess(userId);
        
        String targetMonth = monthYear != null ? monthYear : 
            java.time.YearMonth.now().toString();
        
        log.info("Manual alert check triggered for user {} in month {}", userId, targetMonth);
        
        var alerts = alertService.checkBudgetsAndGenerateAlerts(userId, targetMonth);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "message", "Budget check completed",
            "alertsGenerated", alerts.size(),
            "monthYear", targetMonth
        ));
    }
}
