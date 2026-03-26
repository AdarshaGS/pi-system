package com.budget.service;

import com.budget.data.Alert;
import com.budget.dto.AlertResponse;
import com.budget.dto.AlertSummary;

import java.util.List;

public interface AlertService {

    /**
     * Check all budgets for a user and generate alerts if thresholds are crossed
     * @param userId User ID to check
     * @param monthYear Month to check (format: YYYY-MM)
     * @return List of alerts generated
     */
    List<Alert> checkBudgetsAndGenerateAlerts(Long userId, String monthYear);

    /**
     * Get all alerts for a user
     * @param userId User ID
     * @return List of alert responses
     */
    List<AlertResponse> getUserAlerts(Long userId);

    /**
     * Get unread alerts for a user
     * @param userId User ID
     * @return List of unread alert responses
     */
    List<AlertResponse> getUnreadAlerts(Long userId);

    /**
     * Get alerts for a specific month
     * @param userId User ID
     * @param monthYear Month year (format: YYYY-MM)
     * @return List of alert responses
     */
    List<AlertResponse> getAlertsByMonth(Long userId, String monthYear);

    /**
     * Mark an alert as read
     * @param alertId Alert ID
     * @param userId User ID (for security check)
     * @return Updated alert response
     */
    AlertResponse markAlertAsRead(Long alertId, Long userId);

    /**
     * Mark all alerts as read for a user
     * @param userId User ID
     * @return Number of alerts marked as read
     */
    Integer markAllAlertsAsRead(Long userId);

    /**
     * Get alert summary statistics
     * @param userId User ID
     * @return Alert summary
     */
    AlertSummary getAlertSummary(Long userId);

    /**
     * Delete an alert
     * @param alertId Alert ID
     * @param userId User ID (for security check)
     */
    void deleteAlert(Long alertId, Long userId);

    /**
     * Delete all alerts for a user
     * @param userId User ID
     */
    void deleteAllAlerts(Long userId);
}
