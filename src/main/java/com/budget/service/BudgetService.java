package com.budget.service;

import com.budget.data.*;
import com.budget.exception.*;
import com.budget.repo.BudgetRepository;
import com.budget.repo.CustomCategoryRepository;
import com.budget.repo.ExpenseRepository;
import com.budget.repo.IncomeRepository;
import com.common.security.AuthenticationHelper;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final IncomeRepository incomeRepository;
    private final CustomCategoryRepository customCategoryRepository;
    private final AuthenticationHelper authenticationHelper;

    @Transactional
    public Expense addExpense(Expense expense) {
        authenticationHelper.validateUserAccess(expense.getUserId());
        if (expense.getExpenseDate() == null) {
            expense.setExpenseDate(LocalDate.now());
        }
        return expenseRepository.save(expense);
    }

    @Transactional
    public Budget setBudget(Budget budget) {
        authenticationHelper.validateUserAccess(budget.getUserId());
        if (budget.getMonthYear() == null) {
            budget.setMonthYear(YearMonth.now().toString());
        }
        
        // Validate that either category or customCategoryName is set (but not both)
        if (budget.getCategory() == null && (budget.getCustomCategoryName() == null || budget.getCustomCategoryName().trim().isEmpty())) {
            throw new IllegalArgumentException("Either category or customCategoryName must be specified");
        }
        
        if (budget.getCategory() != null && budget.getCustomCategoryName() != null && !budget.getCustomCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Cannot specify both category and customCategoryName");
        }
        
        // If custom category name is provided, validate it exists
        if (budget.getCustomCategoryName() != null && !budget.getCustomCategoryName().trim().isEmpty()) {
            if (!customCategoryRepository.existsByUserIdAndCategoryName(budget.getUserId(), budget.getCustomCategoryName())) {
                throw new IllegalArgumentException("Custom category '" + budget.getCustomCategoryName() + "' does not exist. Please create it first.");
            }
        }
        
        Budget savedBudget;
        
        // Check if custom category budget
        if (budget.isCustomCategory()) {
            // Find existing custom category budget
            Optional<Budget> existingOpt = budgetRepository.findByUserIdAndMonthYear(budget.getUserId(), budget.getMonthYear())
                    .stream()
                    .filter(b -> budget.getCustomCategoryName().equals(b.getCustomCategoryName()))
                    .findFirst();
            
            savedBudget = existingOpt.map(existing -> {
                existing.setMonthlyLimit(budget.getMonthlyLimit());
                return budgetRepository.save(existing);
            }).orElseGet(() -> budgetRepository.save(budget));
        } else {
            // System category budget
            savedBudget = budgetRepository.findByUserIdAndCategoryAndMonthYear(
                    budget.getUserId(), budget.getCategory(), budget.getMonthYear())
                    .map(existing -> {
                        existing.setMonthlyLimit(budget.getMonthlyLimit());
                        return budgetRepository.save(existing);
                    })
                    .orElseGet(() -> budgetRepository.save(budget));
        }
        
        // Auto-update TOTAL budget if this is not TOTAL
        if (budget.getCategory() != ExpenseCategory.TOTAL) {
            updateTotalBudget(budget.getUserId(), budget.getMonthYear());
        }
        
        return savedBudget;
    }
    
    /**
     * Batch update multiple budgets at once - OPTIMIZED for bulk operations
     * This prevents multiple API calls when setting budgets for multiple categories
     */
    @Transactional
    public List<Budget> setBudgetsBatch(List<Budget> budgets) {
        if (budgets == null || budgets.isEmpty()) {
            throw new IllegalArgumentException("Budget list cannot be empty");
        }
        
        // Validate all budgets belong to the same user and month
        Long userId = budgets.get(0).getUserId();
        String monthYear = budgets.get(0).getMonthYear() != null ? 
                budgets.get(0).getMonthYear() : YearMonth.now().toString();
        
        authenticationHelper.validateUserAccess(userId);
        
        // Validate consistency
        for (Budget budget : budgets) {
            if (!userId.equals(budget.getUserId())) {
                throw new IllegalArgumentException("All budgets must belong to the same user");
            }
            if (budget.getMonthYear() == null) {
                budget.setMonthYear(monthYear);
            }
            if (!monthYear.equals(budget.getMonthYear())) {
                throw new IllegalArgumentException("All budgets must be for the same month");
            }
        }
        
        List<Budget> savedBudgets = new ArrayList<>();
        
        // Fetch existing budgets for this user and month
        List<Budget> existingBudgets = budgetRepository.findByUserIdAndMonthYear(userId, monthYear);
        Map<String, Budget> existingBudgetMap = new HashMap<>();
        
        for (Budget existing : existingBudgets) {
            String key = existing.isCustomCategory() ? 
                    "CUSTOM:" + existing.getCustomCategoryName() : 
                    "SYSTEM:" + existing.getCategory().name();
            existingBudgetMap.put(key, existing);
        }
        
        // Process each budget in the batch
        for (Budget budget : budgets) {
            // Skip TOTAL category - it will be auto-calculated
            if (budget.getCategory() == ExpenseCategory.TOTAL) {
                continue;
            }
            
            // Validate category specification
            if (budget.getCategory() == null && 
                (budget.getCustomCategoryName() == null || budget.getCustomCategoryName().trim().isEmpty())) {
                throw new IllegalArgumentException("Either category or customCategoryName must be specified");
            }
            
            if (budget.getCategory() != null && budget.getCustomCategoryName() != null && 
                !budget.getCustomCategoryName().trim().isEmpty()) {
                throw new IllegalArgumentException("Cannot specify both category and customCategoryName");
            }
            
            // Validate custom category exists
            if (budget.isCustomCategory()) {
                if (!customCategoryRepository.existsByUserIdAndCategoryName(
                        budget.getUserId(), budget.getCustomCategoryName())) {
                    throw new IllegalArgumentException("Custom category '" + 
                            budget.getCustomCategoryName() + "' does not exist");
                }
            }
            
            // Check if budget exists and update or create
            String key = budget.isCustomCategory() ? 
                    "CUSTOM:" + budget.getCustomCategoryName() : 
                    "SYSTEM:" + budget.getCategory().name();
            
            Budget savedBudget;
            if (existingBudgetMap.containsKey(key)) {
                Budget existing = existingBudgetMap.get(key);
                existing.setMonthlyLimit(budget.getMonthlyLimit());
                savedBudget = budgetRepository.save(existing);
            } else {
                savedBudget = budgetRepository.save(budget);
            }
            
            savedBudgets.add(savedBudget);
        }
        
        // Auto-update TOTAL budget once after all categories are processed
        updateTotalBudget(userId, monthYear);
        
        // Add TOTAL budget to the response
        budgetRepository.findByUserIdAndCategoryAndMonthYear(userId, ExpenseCategory.TOTAL, monthYear)
                .ifPresent(savedBudgets::add);
        
        return savedBudgets;
    }
    
    /**
     * Automatically update the TOTAL budget to sum of all category budgets
     */
    @Transactional
    public void updateTotalBudget(Long userId, String monthYear) {
        BigDecimal totalBudget = calculateTotalMonthlyBudget(userId, monthYear);
        
        // Find or create TOTAL budget
        Optional<Budget> totalBudgetOpt = budgetRepository.findByUserIdAndCategoryAndMonthYear(
                userId, ExpenseCategory.TOTAL, monthYear);
        
        if (totalBudgetOpt.isPresent()) {
            Budget total = totalBudgetOpt.get();
            total.setMonthlyLimit(totalBudget);
            budgetRepository.save(total);
        } else {
            Budget total = Budget.builder()
                    .userId(userId)
                    .category(ExpenseCategory.TOTAL)
                    .monthYear(monthYear)
                    .monthlyLimit(totalBudget)
                    .build();
            budgetRepository.save(total);
        }
    }

    @Transactional(readOnly = true)
    public BudgetReportDTO getMonthlyReport(Long userId, String monthYear) {
        authenticationHelper.validateUserAccess(userId);
        if (monthYear == null) {
            monthYear = YearMonth.now().toString();
        }
        YearMonth ym = YearMonth.parse(monthYear);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Expense> expenses = expenseRepository.findByUserIdAndExpenseDateBetween(userId, start, end);
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthYear(userId, monthYear);
        List<Income> incomes = incomeRepository.findByUserIdAndDateBetween(userId, start, end);

        Map<ExpenseCategory, BigDecimal> spentPerCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)));

        Map<ExpenseCategory, BudgetReportDTO.CategorySummary> breakdown = new HashMap<>();

        for (Budget b : budgets) {
            if (b.getCategory() == ExpenseCategory.TOTAL) {
                continue;
            }
            BigDecimal spent = spentPerCategory.getOrDefault(b.getCategory(), BigDecimal.ZERO);
            BigDecimal limit = b.getMonthlyLimit();
            breakdown.put(b.getCategory(), BudgetReportDTO.CategorySummary.builder()
                    .limit(limit)
                    .spent(spent)
                    .remaining(limit.subtract(spent))
                    .percentageUsed(calculatePercentage(spent, limit))
                    .build());
        }

        // Add categories that have spending but no budget
        spentPerCategory.forEach((cat, spent) -> {
            if (!breakdown.containsKey(cat) && cat != ExpenseCategory.TOTAL) {
                breakdown.put(cat, BudgetReportDTO.CategorySummary.builder()
                        .limit(BigDecimal.ZERO)
                        .spent(spent)
                        .remaining(spent.negate())
                        .percentageUsed(100.0)
                        .build());
            }
        });

        BigDecimal totalSpent = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total budget as sum of all category budgets (excluding TOTAL)
        BigDecimal totalBudget = budgets.stream()
                .filter(b -> b.getCategory() != ExpenseCategory.TOTAL)
                .map(Budget::getMonthlyLimit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalIncome = incomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = totalIncome.subtract(totalSpent);
        BigDecimal remainingBudget = totalBudget.subtract(totalSpent);
        BigDecimal savings = totalIncome.subtract(totalSpent);
        
        // Calculate budget usage percentage
        double budgetUsagePercentage = calculatePercentage(totalSpent, totalBudget);

        return BudgetReportDTO.builder()
                .monthYear(monthYear)
                .totalBudget(totalBudget)
                .totalSpent(totalSpent)
                .totalExpenses(totalSpent)
                .totalIncome(totalIncome)
                .balance(balance)
                .remainingBudget(remainingBudget)
                .savings(savings)
                .budgetUsagePercentage(budgetUsagePercentage)
                .categoryBreakdown(breakdown)
                .recentExpenses(expenses)
                .recentIncomes(incomes)
                .build();
    }

    private double calculatePercentage(BigDecimal spent, BigDecimal limit) {
        if (limit.compareTo(BigDecimal.ZERO) == 0)
            return 0.0;
        return spent.divide(limit, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .doubleValue();
    }

    public List<Expense> getRecentExpenses(Long userId) {
        authenticationHelper.validateUserAccess(userId);
        return expenseRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Expense getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
        authenticationHelper.validateUserAccess(expense.getUserId());
        return expense;
    }

    @Transactional
    public Expense updateExpense(Long id, Expense expenseDetails) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
        authenticationHelper.validateUserAccess(expense.getUserId());
        
        expense.setAmount(expenseDetails.getAmount());
        expense.setCategory(expenseDetails.getCategory());
        expense.setExpenseDate(expenseDetails.getExpenseDate());
        expense.setDescription(expenseDetails.getDescription());
        
        return expenseRepository.save(expense);
    }

    @Transactional
    public void deleteExpense(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
        authenticationHelper.validateUserAccess(expense.getUserId());
        expenseRepository.delete(expense);
    }

    public Income addIncome(Income income) {
        authenticationHelper.validateUserAccess(income.getUserId());
        if (income.getDate() == null) {
            income.setDate(LocalDate.now());
        }
        if (income.getIsRecurring() == null) {
            income.setIsRecurring(false);
        }
        if (income.getIsStable() == null) {
            income.setIsStable(false);
        }
        return incomeRepository.save(income);
    }

    public List<Income> getIncomes(Long userId) {
        authenticationHelper.validateUserAccess(userId);
        return incomeRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Income getIncomeById(Long id) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new IncomeNotFoundException(id));
        authenticationHelper.validateUserAccess(income.getUserId());
        return income;
    }

    @Transactional
    public Income updateIncome(Long id, Income incomeDetails) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new IncomeNotFoundException(id));
        authenticationHelper.validateUserAccess(income.getUserId());
        
        income.setSource(incomeDetails.getSource());
        income.setAmount(incomeDetails.getAmount());
        income.setDate(incomeDetails.getDate());
        income.setIsRecurring(incomeDetails.getIsRecurring());
        income.setIsStable(incomeDetails.getIsStable());
        
        return incomeRepository.save(income);
    }

    @Transactional
    public void deleteIncome(Long id) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new IncomeNotFoundException(id));
        authenticationHelper.validateUserAccess(income.getUserId());
        incomeRepository.delete(income);
    }

    public List<Budget> getAllBudgets(Long userId, String monthYear) {
        authenticationHelper.validateUserAccess(userId);
        if (monthYear == null) {
            monthYear = YearMonth.now().toString();
        }
        return budgetRepository.findByUserIdAndMonthYear(userId, monthYear);
    }

    @Transactional
    public void deleteBudget(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new BudgetNotFoundException(id));
        authenticationHelper.validateUserAccess(budget.getUserId());
        budgetRepository.delete(budget);
    }

    @Transactional(readOnly = true)
    public CashFlowDTO getCashFlowAnalysis(Long userId, String monthYear) {
        authenticationHelper.validateUserAccess(userId);
        if (monthYear == null) {
            monthYear = YearMonth.now().toString();
        }
        YearMonth ym = YearMonth.parse(monthYear);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        // Fetch data for current month
        List<Income> incomes = incomeRepository.findByUserIdAndDateBetween(userId, start, end);
        List<Expense> expenses = expenseRepository.findByUserIdAndExpenseDateBetween(userId, start, end);

        // Calculate total income
        BigDecimal totalIncome = incomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate income breakdowns
        BigDecimal stableIncome = incomes.stream()
                .filter(income -> Boolean.TRUE.equals(income.getIsStable()))
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal variableIncome = totalIncome.subtract(stableIncome);

        BigDecimal recurringIncome = incomes.stream()
                .filter(income -> Boolean.TRUE.equals(income.getIsRecurring()))
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Income by source
        Map<String, BigDecimal> incomeBySource = incomes.stream()
                .collect(Collectors.groupingBy(
                        Income::getSource,
                        Collectors.reducing(BigDecimal.ZERO, Income::getAmount, BigDecimal::add)));

        // Calculate total expenses
        BigDecimal totalExpenses = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Expenses by category
        Map<ExpenseCategory, BigDecimal> expenseByCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)));

        // Calculate cash flow metrics
        BigDecimal netCashFlow = totalIncome.subtract(totalExpenses);
        BigDecimal savingsAmount = netCashFlow;

        Double savingsRate = 0.0;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = savingsAmount.divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .doubleValue();
        }

        Double incomeStability = 0.0;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            incomeStability = stableIncome.divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .doubleValue();
        }

        // Count income types
        int recurringCount = (int) incomes.stream()
                .filter(income -> Boolean.TRUE.equals(income.getIsRecurring()))
                .count();
        int oneTimeCount = incomes.size() - recurringCount;

        // Calculate burn rate (monthly expense rate)
        Double burnRate = totalExpenses.doubleValue();

        // Calculate historical trends (last 6 months)
        List<CashFlowDTO.MonthlyTrend> trends = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            YearMonth pastMonth = ym.minusMonths(i);
            LocalDate pastStart = pastMonth.atDay(1);
            LocalDate pastEnd = pastMonth.atEndOfMonth();

            List<Income> pastIncomes = incomeRepository.findByUserIdAndDateBetween(userId, pastStart, pastEnd);
            List<Expense> pastExpenses = expenseRepository.findByUserIdAndExpenseDateBetween(userId, pastStart, pastEnd);

            BigDecimal pastIncome = pastIncomes.stream()
                    .map(Income::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal pastExpense = pastExpenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal pastSavings = pastIncome.subtract(pastExpense);

            Double pastSavingsRate = 0.0;
            if (pastIncome.compareTo(BigDecimal.ZERO) > 0) {
                pastSavingsRate = pastSavings.divide(pastIncome, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .doubleValue();
            }

            trends.add(CashFlowDTO.MonthlyTrend.builder()
                    .monthYear(pastMonth.toString())
                    .income(pastIncome)
                    .expenses(pastExpense)
                    .savings(pastSavings)
                    .savingsRate(pastSavingsRate)
                    .build());
        }
        Collections.reverse(trends); // Most recent last

        // Determine cash flow status
        String cashFlowStatus;
        if (netCashFlow.compareTo(BigDecimal.ZERO) > 0) {
            cashFlowStatus = "POSITIVE";
        } else if (netCashFlow.compareTo(BigDecimal.ZERO) < 0) {
            cashFlowStatus = "NEGATIVE";
        } else {
            cashFlowStatus = "BREAK_EVEN";
        }

        // Generate recommendations
        List<String> recommendations = new ArrayList<>();
        if (savingsRate < 20.0) {
            recommendations.add("Try to increase savings rate to at least 20% of income");
        }
        if (incomeStability < 60.0) {
            recommendations.add("Focus on building stable income sources");
        }
        if (netCashFlow.compareTo(BigDecimal.ZERO) < 0) {
            recommendations.add("Expenses exceed income - review discretionary spending");
        }
        if (recurringCount == 0) {
            recommendations.add("Consider setting up recurring income streams for financial stability");
        }

        return CashFlowDTO.builder()
                .monthYear(monthYear)
                .totalIncome(totalIncome)
                .stableIncome(stableIncome)
                .variableIncome(variableIncome)
                .recurringIncome(recurringIncome)
                .incomeBySource(incomeBySource)
                .totalExpenses(totalExpenses)
                .expenseByCategory(expenseByCategory)
                .netCashFlow(netCashFlow)
                .savingsAmount(savingsAmount)
                .savingsRate(savingsRate)
                .burnRate(burnRate)
                .incomeStability(incomeStability)
                .recurringIncomeCount(recurringCount)
                .oneTimeIncomeCount(oneTimeCount)
                .last6Months(trends)
                .cashFlowStatus(cashFlowStatus)
                .recommendations(recommendations)
                .build();
    }

    // Pagination and filtering methods
    @Transactional(readOnly = true)
    public Page<Expense> getExpensesFiltered(Long userId, String category, LocalDate startDate, LocalDate endDate, 
                                              String search, Pageable pageable) {
        authenticationHelper.validateUserAccess(userId);
        
        // If no date range specified, use current month
        final LocalDate finalStartDate;
        final LocalDate finalEndDate;
        if (startDate == null || endDate == null) {
            YearMonth currentMonth = YearMonth.now();
            finalStartDate = currentMonth.atDay(1);
            finalEndDate = currentMonth.atEndOfMonth();
        } else {
            finalStartDate = startDate;
            finalEndDate = endDate;
        }
        
        // Build dynamic filter
        Specification<Expense> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            
            // User ID filter (always required)
            predicates.add(cb.equal(root.get("userId"), userId));
            
            // Date range filter
            predicates.add(cb.between(root.get("expenseDate"), finalStartDate, finalEndDate));
            
            // Category filter
            if (category != null && !category.isEmpty()) {
                predicates.add(cb.equal(root.get("category"), ExpenseCategory.valueOf(category)));
            }
            
            // Search in description
            if (search != null && !search.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + search.toLowerCase() + "%"));
            }
            
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        
        return expenseRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Income> getIncomesFiltered(Long userId, String source, LocalDate startDate, LocalDate endDate,
                                            Pageable pageable) {
        authenticationHelper.validateUserAccess(userId);
        
        // If no date range specified, use current month
        final LocalDate finalStartDate;
        final LocalDate finalEndDate;
        if (startDate == null || endDate == null) {
            YearMonth currentMonth = YearMonth.now();
            finalStartDate = currentMonth.atDay(1);
            finalEndDate = currentMonth.atEndOfMonth();
        } else {
            finalStartDate = startDate;
            finalEndDate = endDate;
        }
        
        // Build dynamic filter
        Specification<Income> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            
            // User ID filter (always required)
            predicates.add(cb.equal(root.get("userId"), userId));
            
            // Date range filter
            predicates.add(cb.between(root.get("date"), finalStartDate, finalEndDate));
            
            // Source filter
            if (source != null && !source.isEmpty()) {
                predicates.add(cb.equal(root.get("source"), source));
            }
            
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        
        return incomeRepository.findAll(spec, pageable);
    }

    // ==================== Custom Category Management ====================
    
    @Transactional
    public CustomCategory createCustomCategory(CustomCategory customCategory) {
        authenticationHelper.validateUserAccess(customCategory.getUserId());
        
        // Validate category name is not empty
        if (customCategory.getCategoryName() == null || customCategory.getCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        
        // Check if category name conflicts with existing enum categories
        String categoryName = customCategory.getCategoryName().toUpperCase();
        try {
            ExpenseCategory.valueOf(categoryName);
            throw new IllegalArgumentException("Category name conflicts with system category: " + categoryName);
        } catch (IllegalArgumentException e) {
            // This is good - means it doesn't conflict with enum
            if (e.getMessage().startsWith("Category name conflicts")) {
                throw e;
            }
        }
        
        // Check if user already has a category with this name
        if (customCategoryRepository.existsByUserIdAndCategoryName(
                customCategory.getUserId(), customCategory.getCategoryName())) {
            throw new IllegalArgumentException("Category '" + customCategory.getCategoryName() + "' already exists");
        }
        
        return customCategoryRepository.save(customCategory);
    }

    @Transactional(readOnly = true)
    public List<CustomCategory> getUserCustomCategories(Long userId) {
        authenticationHelper.validateUserAccess(userId);
        return customCategoryRepository.findByUserIdAndIsActive(userId, true);
    }

    @Transactional(readOnly = true)
    public List<CustomCategory> getAllUserCategories(Long userId) {
        authenticationHelper.validateUserAccess(userId);
        return customCategoryRepository.findByUserId(userId);
    }

    @Transactional
    public CustomCategory updateCustomCategory(Long id, CustomCategory updatedCategory) {
        CustomCategory existing = customCategoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Custom category not found with id: " + id));
        
        authenticationHelper.validateUserAccess(existing.getUserId());
        
        // Update fields
        if (updatedCategory.getDescription() != null) {
            existing.setDescription(updatedCategory.getDescription());
        }
        if (updatedCategory.getIcon() != null) {
            existing.setIcon(updatedCategory.getIcon());
        }
        if (updatedCategory.getColor() != null) {
            existing.setColor(updatedCategory.getColor());
        }
        if (updatedCategory.getIsActive() != null) {
            existing.setIsActive(updatedCategory.getIsActive());
        }
        
        return customCategoryRepository.save(existing);
    }

    @Transactional
    public void deleteCustomCategory(Long id) {
        CustomCategory category = customCategoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Custom category not found with id: " + id));
        
        authenticationHelper.validateUserAccess(category.getUserId());
        
        // Soft delete - mark as inactive instead of deleting
        category.setIsActive(false);
        customCategoryRepository.save(category);
    }

    @Transactional
    public void hardDeleteCustomCategory(Long id) {
        CustomCategory category = customCategoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Custom category not found with id: " + id));
        
        authenticationHelper.validateUserAccess(category.getUserId());
        
        // Hard delete - permanently remove
        customCategoryRepository.delete(category);
    }

    // ==================== Enhanced Budget Management ====================
    
    /**
     * Calculate total monthly budget from all category budgets (excluding TOTAL category)
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalMonthlyBudget(Long userId, String monthYear) {
        authenticationHelper.validateUserAccess(userId);
        
        if (monthYear == null) {
            monthYear = YearMonth.now().toString();
        }
        
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthYear(userId, monthYear);
        
        return budgets.stream()
                .filter(b -> b.getCategory() != ExpenseCategory.TOTAL) // Exclude TOTAL category
                .map(Budget::getMonthlyLimit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get all categories (system + custom) for a user
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAllCategories(Long userId) {
        authenticationHelper.validateUserAccess(userId);
        
        Map<String, Object> result = new HashMap<>();
        
        // System categories
        List<String> systemCategories = Arrays.stream(ExpenseCategory.values())
                .filter(cat -> cat != ExpenseCategory.TOTAL)
                .map(Enum::name)
                .collect(Collectors.toList());
        
        // Custom categories
        List<CustomCategory> customCategories = customCategoryRepository.findByUserIdAndIsActive(userId, true);
        
        result.put("systemCategories", systemCategories);
        result.put("customCategories", customCategories);
        
        return result;
    }

    /**
     * Bulk delete expenses
     */
    @Transactional
    public Map<String, Object> bulkDeleteExpenses(List<Long> expenseIds, Long userId) {
        int deletedCount = 0;
        List<Long> failedIds = new ArrayList<>();
        
        for (Long expenseId : expenseIds) {
            try {
                Expense expense = expenseRepository.findById(expenseId)
                    .orElseThrow(() -> new RuntimeException("Expense not found: " + expenseId));
                
                // Verify user owns this expense
                if (!expense.getUserId().equals(userId)) {
                    failedIds.add(expenseId);
                    continue;
                }
                
                expenseRepository.delete(expense);
                deletedCount++;
            } catch (Exception e) {
                failedIds.add(expenseId);
            }
        }
        
        return Map.of(
            "deleted", deletedCount,
            "failed", failedIds.size(),
            "failedIds", failedIds
        );
    }

    /**
     * Bulk update category for expenses
     */
    @Transactional
    public Map<String, Object> bulkUpdateCategory(List<Long> expenseIds, ExpenseCategory newCategory, 
                                                   String customCategoryName, Long userId) {
        int updatedCount = 0;
        List<Long> failedIds = new ArrayList<>();
        
        for (Long expenseId : expenseIds) {
            try {
                Expense expense = expenseRepository.findById(expenseId)
                    .orElseThrow(() -> new RuntimeException("Expense not found: " + expenseId));
                
                // Verify user owns this expense
                if (!expense.getUserId().equals(userId)) {
                    failedIds.add(expenseId);
                    continue;
                }
                
                expense.setCategory(newCategory);
                expense.setCustomCategoryName(customCategoryName);
                expenseRepository.save(expense);
                updatedCount++;
            } catch (Exception e) {
                failedIds.add(expenseId);
            }
        }
        
        return Map.of(
            "updated", updatedCount,
            "failed", failedIds.size(),
            "failedIds", failedIds
        );
    }
}

