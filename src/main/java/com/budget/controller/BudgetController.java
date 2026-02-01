package com.budget.controller;

import com.budget.data.Budget;
import com.budget.data.BudgetReportDTO;
import com.budget.data.BudgetVsActualReport;
import com.budget.data.CashFlowDTO;
import com.budget.data.CustomCategory;
import com.budget.data.Expense;
import com.budget.data.ExpenseCategory;
import com.budget.data.Income;
import com.budget.dto.EmailReportRequest;
import com.budget.service.BudgetService;
import com.budget.service.ExportService;
import com.budget.service.ReportGenerationService;
import com.common.features.FeatureFlag;
import com.common.features.RequiresFeature;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/budget")
@RequiredArgsConstructor
@RequiresFeature(FeatureFlag.BUDGET_MODULE)
@Tag(name = "Budget Management", description = "APIs for managing income, expenses, and budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final ExportService exportService;
    private final ReportGenerationService reportGenerationService;

    // Expense endpoints
    @PostMapping("/expense")
    @Operation(summary = "Add expense", description = "Record a new expense")
    public Expense addExpense(@Valid @RequestBody Expense expense) {
        return budgetService.addExpense(expense);
    }

    @GetMapping("/expense/{userId}")
    @Operation(summary = "Get expenses", description = "Retrieve all expenses for a user with optional pagination, filtering, and sorting")
    public Page<Expense> getExpenses(
            @PathVariable("userId") Long userId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "expenseDate") String sortBy,
            @RequestParam(name = "order", defaultValue = "desc") String order,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "search", required = false) String search) {

        Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return budgetService.getExpensesFiltered(userId, category, startDate, endDate, search, pageable);
    }

    @GetMapping("/expense/detail/{id}")
    @Operation(summary = "Get expense by ID", description = "Retrieve a single expense by ID")
    public Expense getExpenseById(@PathVariable("id") Long id) {
        return budgetService.getExpenseById(id);
    }

    @PutMapping("/expense/{id}")
    @Operation(summary = "Update expense", description = "Update an existing expense")
    public Expense updateExpense(@PathVariable("id") Long id, @Valid @RequestBody Expense expense) {
        return budgetService.updateExpense(id, expense);
    }

    @DeleteMapping("/expense/{id}")
    @Operation(summary = "Delete expense", description = "Delete an expense by ID")
    public void deleteExpense(@PathVariable("id") Long id) {
        budgetService.deleteExpense(id);
    }

    // Income endpoints
    @PostMapping("/income")
    @Operation(summary = "Add income", description = "Record a new income entry (salary, dividend, rental, etc.)")
    public Income addIncome(@Valid @RequestBody Income income) {
        return budgetService.addIncome(income);
    }

    @GetMapping("/income/{userId}")
    @Operation(summary = "Get incomes", description = "Retrieve all income entries for a user with pagination and filtering")
    public Page<Income> getIncomes(
            @PathVariable("userId") Long userId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "date") String sortBy,
            @RequestParam(name = "order", defaultValue = "desc") String order,
            @RequestParam(name = "source", required = false) String source,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return budgetService.getIncomesFiltered(userId, source, startDate, endDate, pageable);
    }

    @GetMapping("/income/detail/{id}")
    @Operation(summary = "Get income by ID", description = "Retrieve a single income by ID")
    public Income getIncomeById(@PathVariable("id") Long id) {
        return budgetService.getIncomeById(id);
    }

    @PutMapping("/income/{id}")
    @Operation(summary = "Update income", description = "Update an existing income")
    public Income updateIncome(@PathVariable("id") Long id, @Valid @RequestBody Income income) {
        return budgetService.updateIncome(id, income);
    }

    @DeleteMapping("/income/{id}")
    @Operation(summary = "Delete income", description = "Delete an income by ID")
    public void deleteIncome(@PathVariable("id") Long id) {
        budgetService.deleteIncome(id);
    }

    // Budget limit endpoints
    @PostMapping("/limit")
    @Operation(summary = "Set budget limit", description = "Set or update monthly budget limit for a category")
    public Budget setBudget(@Valid @RequestBody Budget budget) {
        return budgetService.setBudget(budget);
    }

    @PostMapping("/limit/batch")
    @Operation(summary = "Set multiple budget limits", 
               description = "Set or update budget limits for multiple categories at once (optimized for bulk operations)")
    public List<Budget> setBudgetsBatch(@Valid @RequestBody List<Budget> budgets) {
        return budgetService.setBudgetsBatch(budgets);
    }

    @GetMapping("/limit/{userId}")
    @Operation(summary = "Get all budget limits", description = "Get all budget limits for a user")
    public List<Budget> getAllBudgets(@PathVariable("userId") Long userId,
            @RequestParam(name = "monthYear", required = false) String monthYear) {
        return budgetService.getAllBudgets(userId, monthYear);
    }

    @DeleteMapping("/limit/{id}")
    @Operation(summary = "Delete budget limit", description = "Delete a budget limit by ID")
    public void deleteBudget(@PathVariable("id") Long id) {
        budgetService.deleteBudget(id);
    }

    // Reporting endpoints
    @GetMapping("/report/{userId}")
    @Operation(summary = "Get monthly budget report", description = "Get budget vs actual spending breakdown for a month")
    public BudgetReportDTO getReport(@PathVariable("userId") Long userId,
            @RequestParam(name = "monthYear", required = false) String monthYear) {
        return budgetService.getMonthlyReport(userId, monthYear);
    }

    @GetMapping("/cashflow/{userId}")
    @Operation(summary = "Get cash flow analysis", description = "Get comprehensive income vs expense analysis with savings rate and trends")
    public CashFlowDTO getCashFlow(@PathVariable("userId") Long userId,
            @RequestParam(name = "monthYear", required = false) String monthYear) {
        return budgetService.getCashFlowAnalysis(userId, monthYear);
    }

    // Custom Category endpoints
    @PostMapping("/category/custom")
    @Operation(summary = "Create custom category", description = "Create a new custom expense category")
    public CustomCategory createCustomCategory(@Valid @RequestBody CustomCategory customCategory) {
        return budgetService.createCustomCategory(customCategory);
    }

    @GetMapping("/category/custom/{userId}")
    @Operation(summary = "Get custom categories", description = "Get all active custom categories for a user")
    public List<CustomCategory> getUserCustomCategories(@PathVariable("userId") Long userId) {
        return budgetService.getUserCustomCategories(userId);
    }

    @GetMapping("/category/all/{userId}")
    @Operation(summary = "Get all categories", description = "Get both system and custom categories for a user")
    public Map<String, Object> getAllCategories(@PathVariable("userId") Long userId) {
        return budgetService.getAllCategories(userId);
    }

    @PutMapping("/category/custom/{id}")
    @Operation(summary = "Update custom category", description = "Update a custom category (description, icon, color, active status)")
    public CustomCategory updateCustomCategory(@PathVariable("id") Long id, @Valid @RequestBody CustomCategory customCategory) {
        return budgetService.updateCustomCategory(id, customCategory);
    }

    @DeleteMapping("/category/custom/{id}")
    @Operation(summary = "Delete custom category", description = "Soft delete (deactivate) a custom category")
    public void deleteCustomCategory(@PathVariable("id") Long id) {
        budgetService.deleteCustomCategory(id);
    }

    @DeleteMapping("/category/custom/{id}/hard")
    @Operation(summary = "Hard delete custom category", description = "Permanently delete a custom category")
    public void hardDeleteCustomCategory(@PathVariable("id") Long id) {
        budgetService.hardDeleteCustomCategory(id);
    }

    @GetMapping("/total/{userId}")
    @Operation(summary = "Calculate total monthly budget", description = "Get the total of all category budgets for a month")
    public Map<String, Object> getTotalMonthlyBudget(
            @PathVariable("userId") Long userId,
            @RequestParam(name = "monthYear", required = false) String monthYear) {
        BigDecimal total = budgetService.calculateTotalMonthlyBudget(userId, monthYear);
        return Map.of(
            "userId", userId,
            "monthYear", monthYear,
            "totalBudget", total
        );
    }

    // Export endpoints
    @GetMapping("/expense/{userId}/export/csv")
    @Operation(summary = "Export expenses to CSV", description = "Download expenses as CSV file with optional filters")
    public ResponseEntity<byte[]> exportExpensesCSV(
            @PathVariable("userId") Long userId,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "category", required = false) String category) {
        
        byte[] csvData = exportService.exportExpensesToCSV(userId, startDate, endDate, category);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "expenses_" + LocalDate.now() + ".csv");
        
        return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
    }

    @GetMapping("/expense/{userId}/export/excel")
    @Operation(summary = "Export expenses to Excel", description = "Download expenses as Excel file with optional filters")
    public ResponseEntity<byte[]> exportExpensesExcel(
            @PathVariable("userId") Long userId,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "category", required = false) String category) {
        
        byte[] excelData = exportService.exportExpensesToExcel(userId, startDate, endDate, category);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "expenses_" + LocalDate.now() + ".xlsx");
        
        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }

    @GetMapping("/income/{userId}/export/csv")
    @Operation(summary = "Export incomes to CSV", description = "Download incomes as CSV file with optional date filters")
    public ResponseEntity<byte[]> exportIncomesCSV(
            @PathVariable("userId") Long userId,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        byte[] csvData = exportService.exportIncomesToCSV(userId, startDate, endDate);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "incomes_" + LocalDate.now() + ".csv");
        
        return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
    }

    @GetMapping("/income/{userId}/export/excel")
    @Operation(summary = "Export incomes to Excel", description = "Download incomes as Excel file with optional date filters")
    public ResponseEntity<byte[]> exportIncomesExcel(
            @PathVariable("userId") Long userId,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        byte[] excelData = exportService.exportIncomesToExcel(userId, startDate, endDate);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "incomes_" + LocalDate.now() + ".xlsx");
        
        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }

    @GetMapping("/report/{userId}/pdf")
    @Operation(summary = "Generate monthly PDF report", description = "Download monthly budget report as PDF")
    public ResponseEntity<byte[]> generatePDFReport(
            @PathVariable("userId") Long userId,
            @RequestParam(name = "monthYear", required = false) String monthYear) {
        
        byte[] pdfData = reportGenerationService.generateMonthlyReport(userId, monthYear);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "budget_report_" + monthYear + ".pdf");
        
        return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
    }

    @PostMapping("/report/{userId}/email")
    @Operation(summary = "Email report", description = "Send budget report via email")
    public ResponseEntity<Map<String, String>> emailReport(
            @PathVariable("userId") Long userId,
            @RequestBody EmailReportRequest request) {
        // TODO: Implement email service
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Email feature will be implemented in future update"
        ));
    }

    // Bulk operations endpoints
    @PostMapping("/expense/bulk-delete")
    @Operation(summary = "Bulk delete expenses", description = "Delete multiple expenses at once")
    public ResponseEntity<Map<String, Object>> bulkDeleteExpenses(
            @RequestParam("userId") Long userId,
            @RequestBody List<Long> expenseIds) {
        Map<String, Object> result = budgetService.bulkDeleteExpenses(expenseIds, userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/expense/bulk-update-category")
    @Operation(summary = "Bulk update category", description = "Update category for multiple expenses")
    public ResponseEntity<Map<String, Object>> bulkUpdateCategory(
            @RequestParam("userId") Long userId,
            @RequestParam("category") String category,
            @RequestParam(value = "customCategoryName", required = false) String customCategoryName,
            @RequestBody List<Long> expenseIds) {
        ExpenseCategory expenseCategory = category != null && !category.isEmpty() 
            ? ExpenseCategory.valueOf(category) 
            : null;
        Map<String, Object> result = budgetService.bulkUpdateCategory(
            expenseIds, expenseCategory, customCategoryName, userId
        );
        return ResponseEntity.ok(result);
    }
    
    // ========== Budget vs Actual Analysis ==========
    
    @GetMapping("/variance-analysis")
    @Operation(
        summary = "Get budget vs actual analysis",
        description = "Get comprehensive variance analysis comparing budgeted amounts vs actual spending for a month. " +
                      "Includes category-wise breakdown, performance metrics, and overspending alerts."
    )
    public ResponseEntity<BudgetVsActualReport> getBudgetVsActualAnalysis(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "monthYear", required = false) String monthYear) {
        BudgetVsActualReport report = budgetService.getBudgetVsActualReport(userId, monthYear);
        return ResponseEntity.ok(report);
    }
}
