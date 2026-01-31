package com.budget.service;

import com.budget.data.*;
import com.budget.repo.ExpenseRepository;
import com.budget.repo.IncomeRepository;
import com.budget.repo.RecurringTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing recurring transactions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringTransactionService {

    private final RecurringTemplateRepository recurringTemplateRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;

    /**
     * Get all recurring templates for a user
     */
    public List<RecurringTemplate> getUserTemplates(Long userId) {
        return recurringTemplateRepository.findByUserId(userId);
    }

    /**
     * Get active recurring templates for a user
     */
    public List<RecurringTemplate> getActiveTemplates(Long userId) {
        return recurringTemplateRepository.findByUserIdAndIsActive(userId, true);
    }

    /**
     * Create a new recurring template
     */
    @Transactional
    public RecurringTemplate createTemplate(RecurringTemplate template) {
        log.info("Creating recurring template - Type: {}, Name: {}, UserId: {}, Category: {}, Source: {}", 
            template.getType(), template.getName(), template.getUserId(), 
            template.getCategory(), template.getSource());
        
        // Validate template
        if (template.getStartDate() == null) {
            template.setStartDate(LocalDate.now());
        }
        if (template.getIsActive() == null) {
            template.setIsActive(true);
        }
        
        log.info("Saving template to database...");
        try {
            RecurringTemplate saved = recurringTemplateRepository.save(template);
            log.info("Template saved successfully with ID: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            log.error("Error saving recurring template to database", e);
            throw e;
        }
    }

    /**
     * Update an existing recurring template
     */
    @Transactional
    public RecurringTemplate updateTemplate(Long templateId, RecurringTemplate updatedTemplate) {
        RecurringTemplate existing = recurringTemplateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Recurring template not found: " + templateId));
        
        // Update fields
        existing.setName(updatedTemplate.getName());
        existing.setAmount(updatedTemplate.getAmount());
        existing.setPattern(updatedTemplate.getPattern());
        existing.setStartDate(updatedTemplate.getStartDate());
        existing.setEndDate(updatedTemplate.getEndDate());
        existing.setDescription(updatedTemplate.getDescription());
        existing.setIsActive(updatedTemplate.getIsActive());
        
        if (updatedTemplate.getType() == TransactionType.EXPENSE) {
            existing.setCategory(updatedTemplate.getCategory());
            existing.setCustomCategoryName(updatedTemplate.getCustomCategoryName());
        } else {
            existing.setSource(updatedTemplate.getSource());
        }
        
        return recurringTemplateRepository.save(existing);
    }

    /**
     * Delete a recurring template
     */
    @Transactional
    public void deleteTemplate(Long templateId) {
        recurringTemplateRepository.deleteById(templateId);
    }

    /**
     * Toggle active status of a template
     */
    @Transactional
    public RecurringTemplate toggleActive(Long templateId) {
        RecurringTemplate template = recurringTemplateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Recurring template not found: " + templateId));
        
        template.setIsActive(!template.getIsActive());
        return recurringTemplateRepository.save(template);
    }

    /**
     * Get upcoming dates for a template
     */
    public List<LocalDate> getUpcomingDates(Long templateId, int months) {
        RecurringTemplate template = recurringTemplateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Recurring template not found: " + templateId));
        
        List<LocalDate> dates = new ArrayList<>();
        LocalDate currentDate = template.getLastGenerated() != null 
            ? template.getLastGenerated().plusDays(1) 
            : template.getStartDate();
        LocalDate endDate = LocalDate.now().plusMonths(months);
        
        while (currentDate.isBefore(endDate) && dates.size() < 50) {
            LocalDate nextDate = calculateNextDate(currentDate, template.getPattern());
            if (nextDate.isAfter(endDate)) break;
            if (template.getEndDate() != null && nextDate.isAfter(template.getEndDate())) break;
            
            dates.add(nextDate);
            currentDate = nextDate.plusDays(1);
        }
        
        return dates;
    }

    /**
     * Scheduled job to generate recurring transactions
     * Runs every day at 1:00 AM
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void generateRecurringTransactions() {
        log.info("Starting recurring transactions generation job");
        LocalDate today = LocalDate.now();
        
        List<RecurringTemplate> activeTemplates = recurringTemplateRepository.findByIsActiveTrue();
        int generatedCount = 0;
        
        for (RecurringTemplate template : activeTemplates) {
            try {
                if (shouldGenerateToday(template, today)) {
                    generateTransaction(template, today);
                    template.setLastGenerated(today);
                    recurringTemplateRepository.save(template);
                    generatedCount++;
                }
            } catch (Exception e) {
                log.error("Failed to generate transaction for template {}: {}", template.getId(), e.getMessage());
            }
        }
        
        log.info("Recurring transactions generation job completed. Generated {} transactions", generatedCount);
    }

    /**
     * Check if a transaction should be generated today
     */
    private boolean shouldGenerateToday(RecurringTemplate template, LocalDate today) {
        // Check if today is before start date
        if (today.isBefore(template.getStartDate())) {
            return false;
        }
        
        // Check if today is after end date
        if (template.getEndDate() != null && today.isAfter(template.getEndDate())) {
            return false;
        }
        
        // If never generated before, generate if today is start date or after
        if (template.getLastGenerated() == null) {
            return !today.isBefore(template.getStartDate());
        }
        
        // Check if enough time has passed based on pattern
        LocalDate nextDueDate = calculateNextDate(template.getLastGenerated(), template.getPattern());
        return !today.isBefore(nextDueDate);
    }

    /**
     * Calculate the next date based on pattern
     */
    private LocalDate calculateNextDate(LocalDate currentDate, RecurrencePattern pattern) {
        return switch (pattern) {
            case DAILY -> currentDate.plusDays(1);
            case WEEKLY -> currentDate.plusWeeks(1);
            case MONTHLY -> currentDate.plusMonths(1);
            case QUARTERLY -> currentDate.plusMonths(3);
            case YEARLY -> currentDate.plusYears(1);
        };
    }

    /**
     * Generate a transaction from a template
     */
    private void generateTransaction(RecurringTemplate template, LocalDate date) {
        if (template.getType() == TransactionType.EXPENSE) {
            Expense expense = Expense.builder()
                .userId(template.getUserId())
                .amount(template.getAmount())
                .category(template.getCategory())
                .customCategoryName(template.getCustomCategoryName())
                .expenseDate(date)
                .description(template.getDescription() != null 
                    ? "Auto-generated: " + template.getDescription() 
                    : "Auto-generated: " + template.getName())
                .notes("Generated from recurring template: " + template.getName())
                .build();
            expenseRepository.save(expense);
            log.info("Generated recurring expense: {} for user {}", template.getName(), template.getUserId());
        } else {
            Income income = Income.builder()
                .userId(template.getUserId())
                .source(template.getSource() != null ? template.getSource() : template.getName())
                .amount(template.getAmount())
                .date(date)
                .isRecurring(true)
                .isStable(true)
                .notes("Generated from recurring template: " + template.getName())
                .build();
            incomeRepository.save(income);
            log.info("Generated recurring income: {} for user {}", template.getName(), template.getUserId());
        }
    }
}
