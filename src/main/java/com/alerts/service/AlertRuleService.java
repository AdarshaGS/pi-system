package com.alerts.service;

import com.alerts.dto.AlertRuleRequest;
import com.alerts.entity.AlertRule;
import com.alerts.repository.AlertRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing alert rules
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertRuleService {

    private final AlertRuleRepository alertRuleRepository;

    /**
     * Create a new alert rule
     */
    @Transactional
    public AlertRule createAlertRule(AlertRuleRequest request) {
        AlertRule alertRule = AlertRule.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .symbol(request.getSymbol())
                .targetPrice(request.getTargetPrice())
                .priceCondition(request.getPriceCondition())
                .daysBeforeDue(request.getDaysBeforeDue())
                .percentageChange(request.getPercentageChange())
                .channel(request.getChannel())
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .description(request.getDescription())
                .build();

        alertRule = alertRuleRepository.save(alertRule);
        log.info("Created alert rule ID: {} for user: {}", alertRule.getId(), alertRule.getUserId());
        
        return alertRule;
    }

    /**
     * Get all alert rules for a user
     */
    public List<AlertRule> getUserAlertRules(Long userId) {
        return alertRuleRepository.findByUserId(userId);
    }

    /**
     * Get active alert rules for a user
     */
    public List<AlertRule> getActiveUserAlertRules(Long userId) {
        return alertRuleRepository.findByUserIdAndEnabled(userId, true);
    }

    /**
     * Get alert rule by ID
     */
    public AlertRule getAlertRuleById(Long id) {
        return alertRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert rule not found: " + id));
    }

    /**
     * Update an alert rule
     */
    @Transactional
    public AlertRule updateAlertRule(Long id, AlertRuleRequest request) {
        AlertRule alertRule = getAlertRuleById(id);
        
        alertRule.setType(request.getType());
        alertRule.setSymbol(request.getSymbol());
        alertRule.setTargetPrice(request.getTargetPrice());
        alertRule.setPriceCondition(request.getPriceCondition());
        alertRule.setDaysBeforeDue(request.getDaysBeforeDue());
        alertRule.setPercentageChange(request.getPercentageChange());
        alertRule.setChannel(request.getChannel());
        alertRule.setEnabled(request.getEnabled());
        alertRule.setDescription(request.getDescription());
        
        alertRule = alertRuleRepository.save(alertRule);
        log.info("Updated alert rule ID: {}", id);
        
        return alertRule;
    }

    /**
     * Toggle alert rule enabled status
     */
    @Transactional
    public AlertRule toggleAlertRule(Long id) {
        AlertRule alertRule = getAlertRuleById(id);
        alertRule.setEnabled(!alertRule.getEnabled());
        
        alertRule = alertRuleRepository.save(alertRule);
        log.info("Toggled alert rule ID: {} to enabled: {}", id, alertRule.getEnabled());
        
        return alertRule;
    }

    /**
     * Delete an alert rule
     */
    @Transactional
    public void deleteAlertRule(Long userId, Long id) {
        alertRuleRepository.deleteByUserIdAndId(userId, id);
        log.info("Deleted alert rule ID: {} for user: {}", id, userId);
    }

    /**
     * Update last triggered timestamp
     */
    @Transactional
    public void updateLastTriggered(Long id) {
        AlertRule alertRule = getAlertRuleById(id);
        alertRule.setLastTriggeredAt(java.time.LocalDateTime.now());
        alertRuleRepository.save(alertRule);
    }
}
