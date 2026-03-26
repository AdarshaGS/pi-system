package com.budget.service;

import com.budget.data.Alert;
import com.budget.data.Alert.AlertSeverity;
import com.budget.data.Alert.AlertType;
import com.budget.data.Budget;
import com.budget.data.BudgetVarianceAnalysis;
import com.budget.data.Expense;
import com.budget.dto.AlertResponse;
import com.budget.dto.AlertSummary;
import com.budget.exception.ResourceNotFoundException;
import com.budget.repo.AlertRepository;
import com.budget.repo.BudgetRepository;
import com.budget.repo.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    // Alert thresholds
    private static final BigDecimal WARNING_THRESHOLD = BigDecimal.valueOf(75); // 75%
    private static final BigDecimal CRITICAL_THRESHOLD = BigDecimal.valueOf(90); // 90%
    private static final BigDecimal DANGER_THRESHOLD = BigDecimal.valueOf(100); // 100%

    @Override
    @Transactional
    public List<Alert> checkBudgetsAndGenerateAlerts(Long userId, String monthYear) {
        log.info("Checking budgets for user {} in month {}", userId, monthYear);
        
        List<Alert> generatedAlerts = new ArrayList<>();
        
        // Get all budgets for the user and month
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthYear(userId, monthYear);
        
        if (budgets.isEmpty()) {
            log.debug("No budgets found for user {} in month {}", userId, monthYear);
            return generatedAlerts;
        }
        
        // Parse month year for date filtering
        YearMonth yearMonth = YearMonth.parse(monthYear);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        for (Budget budget : budgets) {
            try {
                Alert alert = checkBudgetAndCreateAlert(budget, startDate, endDate);
                if (alert != null) {
                    generatedAlerts.add(alert);
                }
            } catch (Exception e) {
                log.error("Error checking budget {} for user {}: {}", 
                         budget.getId(), userId, e.getMessage(), e);
            }
        }
        
        log.info("Generated {} alerts for user {} in month {}", 
                generatedAlerts.size(), userId, monthYear);
        
        return generatedAlerts;
    }

    private Alert checkBudgetAndCreateAlert(Budget budget, LocalDate startDate, LocalDate endDate) {
        String categoryName = budget.getEffectiveCategoryName();
        
        // Calculate total spent for this category
        BigDecimal totalSpent = calculateCategorySpending(
            budget.getUserId(), 
            categoryName, 
            startDate, 
            endDate
        );
        
        // Calculate percentage used
        BigDecimal percentageUsed = BigDecimal.ZERO;
        if (budget.getMonthlyLimit().compareTo(BigDecimal.ZERO) > 0) {
            percentageUsed = totalSpent
                .multiply(BigDecimal.valueOf(100))
                .divide(budget.getMonthlyLimit(), 2, RoundingMode.HALF_UP);
        }
        
        log.debug("Budget check - Category: {}, Limit: {}, Spent: {}, Percentage: {}%",
                 categoryName, budget.getMonthlyLimit(), totalSpent, percentageUsed);
        
        // Determine if we need to create an alert
        AlertSeverity severity = determineSeverity(percentageUsed);
        
        // Only create alerts for WARNING and above
        if (severity == AlertSeverity.INFO) {
            return null;
        }
        
        // Check if alert already exists for this category and threshold
        boolean alertExists = alertRepository.existsByUserIdAndCategoryAndMonthYearAndAlertType(
            budget.getUserId(),
            categoryName,
            budget.getMonthYear(),
            determineAlertType(percentageUsed)
        );
        
        if (alertExists) {
            log.debug("Alert already exists for category {} in month {}", 
                     categoryName, budget.getMonthYear());
            return null;
        }
        
        // Create and save new alert
        Alert alert = buildAlert(
            budget.getUserId(),
            categoryName,
            budget.getMonthlyLimit(),
            totalSpent,
            percentageUsed,
            budget.getMonthYear(),
            severity
        );
        
        Alert savedAlert = alertRepository.save(alert);
        log.info("Created {} alert for user {} - Category: {}, {}% used",
                severity, budget.getUserId(), categoryName, percentageUsed);
        
        return savedAlert;
    }

    private BigDecimal calculateCategorySpending(Long userId, String categoryName, 
                                                  LocalDate startDate, LocalDate endDate) {
        List<Expense> expenses = expenseRepository
            .findByUserIdAndExpenseDateBetween(userId, startDate, endDate);
        
        return expenses.stream()
            .filter(expense -> {
                String expenseCategory = expense.isCustomCategory() 
                    ? expense.getCustomCategoryName() 
                    : (expense.getCategory() != null ? expense.getCategory().name() : null);
                return categoryName.equals(expenseCategory);
            })
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private AlertSeverity determineSeverity(BigDecimal percentageUsed) {
        if (percentageUsed.compareTo(DANGER_THRESHOLD) >= 0) {
            return AlertSeverity.DANGER;
        } else if (percentageUsed.compareTo(CRITICAL_THRESHOLD) >= 0) {
            return AlertSeverity.CRITICAL;
        } else if (percentageUsed.compareTo(WARNING_THRESHOLD) >= 0) {
            return AlertSeverity.WARNING;
        } else {
            return AlertSeverity.INFO;
        }
    }

    private AlertType determineAlertType(BigDecimal percentageUsed) {
        if (percentageUsed.compareTo(DANGER_THRESHOLD) >= 0) {
            return AlertType.BUDGET_EXCEEDED;
        } else if (percentageUsed.compareTo(CRITICAL_THRESHOLD) >= 0) {
            return AlertType.APPROACHING_LIMIT;
        } else {
            return AlertType.OVERSPENDING;
        }
    }

    private Alert buildAlert(Long userId, String category, BigDecimal budgetLimit,
                            BigDecimal amountSpent, BigDecimal percentageUsed,
                            String monthYear, AlertSeverity severity) {
        String message = generateAlertMessage(category, percentageUsed, severity, monthYear);
        AlertType alertType = determineAlertType(percentageUsed);
        
        return Alert.builder()
            .userId(userId)
            .alertType(alertType)
            .severity(severity)
            .category(category)
            .message(message)
            .budgetLimit(budgetLimit)
            .amountSpent(amountSpent)
            .percentageUsed(percentageUsed)
            .monthYear(monthYear)
            .isRead(false)
            .notificationSent(false)
            .build();
    }

    private String generateAlertMessage(String category, BigDecimal percentageUsed, 
                                       AlertSeverity severity, String monthYear) {
        String monthName = formatMonthYear(monthYear);
        
        return switch (severity) {
            case DANGER -> String.format(
                "⚠️ BUDGET EXCEEDED: You've spent %.0f%% of your %s budget for %s. Consider reducing expenses!",
                percentageUsed, category, monthName
            );
            case CRITICAL -> String.format(
                "🔴 CRITICAL: You've used %.0f%% of your %s budget for %s. Only %.0f%% remaining!",
                percentageUsed, category, monthName, BigDecimal.valueOf(100).subtract(percentageUsed)
            );
            case WARNING -> String.format(
                "⚡ WARNING: You've spent %.0f%% of your %s budget for %s. Monitor your spending!",
                percentageUsed, category, monthName
            );
            default -> String.format(
                "ℹ️ You've spent %.0f%% of your %s budget for %s.",
                percentageUsed, category, monthName
            );
        };
    }

    private String formatMonthYear(String monthYear) {
        try {
            YearMonth ym = YearMonth.parse(monthYear);
            return ym.getMonth().toString() + " " + ym.getYear();
        } catch (Exception e) {
            return monthYear;
        }
    }

    @Override
    public List<AlertResponse> getUserAlerts(Long userId) {
        List<Alert> alerts = alertRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return alerts.stream()
            .map(AlertResponse::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    public List<AlertResponse> getUnreadAlerts(Long userId) {
        List<Alert> alerts = alertRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        return alerts.stream()
            .map(AlertResponse::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    public List<AlertResponse> getAlertsByMonth(Long userId, String monthYear) {
        List<Alert> alerts = alertRepository.findByUserIdAndMonthYearOrderByCreatedAtDesc(
            userId, monthYear
        );
        return alerts.stream()
            .map(AlertResponse::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AlertResponse markAlertAsRead(Long alertId, Long userId) {
        Alert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new ResourceNotFoundException("Alert", "id", alertId));
        
        // Security check
        if (!alert.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Alert does not belong to user");
        }
        
        alert.markAsRead();
        Alert savedAlert = alertRepository.save(alert);
        
        log.info("Marked alert {} as read for user {}", alertId, userId);
        
        return AlertResponse.fromEntity(savedAlert);
    }

    @Override
    @Transactional
    public Integer markAllAlertsAsRead(Long userId) {
        List<Alert> unreadAlerts = alertRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        
        unreadAlerts.forEach(Alert::markAsRead);
        alertRepository.saveAll(unreadAlerts);
        
        log.info("Marked {} alerts as read for user {}", unreadAlerts.size(), userId);
        
        return unreadAlerts.size();
    }

    @Override
    public AlertSummary getAlertSummary(Long userId) {
        List<Alert> allAlerts = alertRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        long unreadCount = allAlerts.stream().filter(a -> !a.getIsRead()).count();
        long criticalCount = allAlerts.stream()
            .filter(a -> !a.getIsRead() && a.isCritical())
            .count();
        long warningCount = allAlerts.stream()
            .filter(a -> !a.getIsRead() && a.getSeverity() == AlertSeverity.WARNING)
            .count();
        long infoCount = allAlerts.stream()
            .filter(a -> !a.getIsRead() && a.getSeverity() == AlertSeverity.INFO)
            .count();
        long overBudgetCount = allAlerts.stream()
            .filter(Alert::isOverBudget)
            .count();
        
        return AlertSummary.builder()
            .totalAlerts(allAlerts.size())
            .unreadAlerts((int) unreadCount)
            .criticalAlerts((int) criticalCount)
            .warningAlerts((int) warningCount)
            .infoAlerts((int) infoCount)
            .categoriesOverBudget((int) overBudgetCount)
            .build();
    }

    @Override
    @Transactional
    public void deleteAlert(Long alertId, Long userId) {
        Alert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new ResourceNotFoundException("Alert", "id", alertId));
        
        // Security check
        if (!alert.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Alert does not belong to user");
        }
        
        alertRepository.delete(alert);
        log.info("Deleted alert {} for user {}", alertId, userId);
    }

    @Override
    @Transactional
    public void deleteAllAlerts(Long userId) {
        List<Alert> alerts = alertRepository.findByUserIdOrderByCreatedAtDesc(userId);
        alertRepository.deleteAll(alerts);
        log.info("Deleted {} alerts for user {}", alerts.size(), userId);
    }
}
