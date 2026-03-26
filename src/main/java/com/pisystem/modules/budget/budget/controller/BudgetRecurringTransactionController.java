package com.budget.controller;

import com.budget.data.RecurringTemplate;
import com.budget.dto.RecurringTemplateDTO;
import com.budget.service.BudgetRecurringTransactionService;
import com.common.features.FeatureFlag;
import com.common.features.RequiresFeature;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/budget/recurring")
@RequiresFeature(FeatureFlag.BUDGET_MODULE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Budget Recurring Transactions", description = "APIs for managing recurring transaction templates")
public class BudgetRecurringTransactionController {

    private final BudgetRecurringTransactionService recurringTransactionService;

    @GetMapping("/{userId}")
    @Operation(summary = "Get user's recurring templates", description = "Retrieve all recurring templates for a user")
    public List<RecurringTemplateDTO> getUserTemplates(@PathVariable("userId") Long userId) {
        return recurringTransactionService.getUserTemplates(userId).stream()
                .map(RecurringTemplateDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/{userId}/active")
    @Operation(summary = "Get active templates", description = "Retrieve only active recurring templates")
    public List<RecurringTemplateDTO> getActiveTemplates(@PathVariable("userId") Long userId) {
        return recurringTransactionService.getActiveTemplates(userId).stream()
                .map(RecurringTemplateDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @PostMapping
    @Operation(summary = "Create recurring template", description = "Create a new recurring transaction template")
    public RecurringTemplateDTO createTemplate(@RequestBody RecurringTemplateDTO templateDTO) {
        log.info("Creating recurring template: {}", templateDTO);
        try {
            RecurringTemplate template = templateDTO.toEntity();
            RecurringTemplate created = recurringTransactionService.createTemplate(template);
            log.info("Successfully created recurring template with ID: {}", created.getId());
            return RecurringTemplateDTO.fromEntity(created);
        } catch (Exception e) {
            log.error("Error creating recurring template", e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update recurring template", description = "Update an existing recurring template")
    public RecurringTemplateDTO updateTemplate(
            @PathVariable("id") Long id,
            @RequestBody RecurringTemplateDTO templateDTO) {
        RecurringTemplate template = templateDTO.toEntity();
        RecurringTemplate updated = recurringTransactionService.updateTemplate(id, template);
        return RecurringTemplateDTO.fromEntity(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete recurring template", description = "Delete a recurring template")
    public ResponseEntity<Map<String, String>> deleteTemplate(@PathVariable("id") Long id) {
        recurringTransactionService.deleteTemplate(id);
        return ResponseEntity.ok(Map.of("message", "Recurring template deleted successfully"));
    }

    @PostMapping("/{id}/toggle")
    @Operation(summary = "Toggle active status", description = "Toggle the active/inactive status of a template")
    public RecurringTemplateDTO toggleActive(@PathVariable("id") Long id) {
        RecurringTemplate toggled = recurringTransactionService.toggleActive(id);
        return RecurringTemplateDTO.fromEntity(toggled);
    }

    @GetMapping("/{id}/upcoming")
    @Operation(summary = "Get upcoming dates", description = "Get upcoming generation dates for a template")
    public List<LocalDate> getUpcomingDates(
            @PathVariable("id") Long id,
            @RequestParam(defaultValue = "6") int months) {
        return recurringTransactionService.getUpcomingDates(id, months);
    }

    @PostMapping("/generate")
    @Operation(summary = "Manually trigger generation", description = "Manually trigger recurring transactions generation (admin only)")
    public ResponseEntity<Map<String, String>> manuallyGenerate() {
        recurringTransactionService.generateRecurringTransactions();
        return ResponseEntity.ok(Map.of("message", "Recurring transactions generation triggered"));
    }
}
