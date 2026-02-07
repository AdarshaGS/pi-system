package com.alerts.controller;

import com.alerts.dto.AlertRuleRequest;
import com.alerts.entity.AlertRule;
import com.alerts.service.AlertRuleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing alert rules
 */
@RestController
@RequestMapping("/api/v1/alerts/rules")
@RequiredArgsConstructor
public class AlertController {

    private final AlertRuleService alertRuleService;

    /**
     * Create a new alert rule
     * POST /api/v1/alerts/rules
     */
    @PostMapping
    public ResponseEntity<AlertRule> createAlertRule(@Valid @RequestBody AlertRuleRequest request) {
        AlertRule alertRule = alertRuleService.createAlertRule(request);
        return new ResponseEntity<>(alertRule, HttpStatus.CREATED);
    }

    /**
     * Get all alert rules for a user
     * GET /api/v1/alerts/rules/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AlertRule>> getUserAlertRules(@PathVariable Long userId) {
        List<AlertRule> rules = alertRuleService.getUserAlertRules(userId);
        return ResponseEntity.ok(rules);
    }

    /**
     * Get active alert rules for a user
     * GET /api/v1/alerts/rules/user/{userId}/active
     */
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<AlertRule>> getActiveUserAlertRules(@PathVariable Long userId) {
        List<AlertRule> rules = alertRuleService.getActiveUserAlertRules(userId);
        return ResponseEntity.ok(rules);
    }

    /**
     * Get alert rule by ID
     * GET /api/v1/alerts/rules/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AlertRule> getAlertRule(@PathVariable Long id) {
        AlertRule rule = alertRuleService.getAlertRuleById(id);
        return ResponseEntity.ok(rule);
    }

    /**
     * Update an alert rule
     * PUT /api/v1/alerts/rules/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<AlertRule> updateAlertRule(
            @PathVariable Long id,
            @Valid @RequestBody AlertRuleRequest request) {
        AlertRule updatedRule = alertRuleService.updateAlertRule(id, request);
        return ResponseEntity.ok(updatedRule);
    }

    /**
     * Toggle alert rule enabled status
     * PATCH /api/v1/alerts/rules/{id}/toggle
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<AlertRule> toggleAlertRule(@PathVariable Long id) {
        AlertRule rule = alertRuleService.toggleAlertRule(id);
        return ResponseEntity.ok(rule);
    }

    /**
     * Delete an alert rule
     * DELETE /api/v1/alerts/rules/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlertRule(
            @PathVariable Long id,
            @RequestParam Long userId) {
        alertRuleService.deleteAlertRule(userId, id);
        return ResponseEntity.noContent().build();
    }
}
