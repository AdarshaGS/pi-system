package com.budget.service;

import com.budget.data.*;
import com.budget.repo.BudgetRepository;
import com.budget.repo.CustomCategoryRepository;
import com.budget.repo.ExpenseRepository;
import com.budget.repo.IncomeRepository;
import com.common.security.AuthenticationHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BudgetService - Sprint 3
 * Tests business logic with mocked dependencies
 */
@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private CustomCategoryRepository customCategoryRepository;

    @Mock
    private AuthenticationHelper authenticationHelper;

    @InjectMocks
    private BudgetService budgetService;

    private Long userId;
    private Expense testExpense;
    private Income testIncome;
    private Budget testBudget;

    @BeforeEach
    void setUp() {
        userId = 1L;
        
        testExpense = new Expense();
        testExpense.setId(1L);
        testExpense.setUserId(userId);
        testExpense.setAmount(BigDecimal.valueOf(5000));
        testExpense.setCategory(ExpenseCategory.FOOD);
        testExpense.setExpenseDate(LocalDate.now());
        testExpense.setDescription("Test expense");

        testIncome = new Income();
        testIncome.setId(1L);
        testIncome.setUserId(userId);
        testIncome.setAmount(BigDecimal.valueOf(50000));
        testIncome.setSource("SALARY");
        testIncome.setDate(LocalDate.now());
        testIncome.setIsRecurring(true);
        testIncome.setIsStable(true);

        testBudget = new Budget();
        testBudget.setId(1L);
        testBudget.setUserId(userId);
        testBudget.setCategory(ExpenseCategory.FOOD);
        testBudget.setMonthlyLimit(BigDecimal.valueOf(15000));
        testBudget.setMonthYear(YearMonth.now().toString());
    }

    // ===== EXPENSE TESTS =====

    @Test
    @DisplayName("Should add expense successfully")
    void testAddExpense() {
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        doNothing().when(authenticationHelper).validateUserAccess(userId);

        Expense result = budgetService.addExpense(testExpense);

        assertNotNull(result);
        assertEquals(testExpense.getId(), result.getId());
        verify(authenticationHelper).validateUserAccess(userId);
        verify(expenseRepository).save(testExpense);
    }

    @Test
    @DisplayName("Should set expense date to today if null")
    void testAddExpenseWithNullDate() {
        Expense expenseWithoutDate = new Expense();
        expenseWithoutDate.setUserId(userId);
        expenseWithoutDate.setAmount(BigDecimal.valueOf(1000));
        expenseWithoutDate.setCategory(ExpenseCategory.FOOD);
        expenseWithoutDate.setExpenseDate(null);

        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense saved = invocation.getArgument(0);
            assertNotNull(saved.getExpenseDate());
            assertEquals(LocalDate.now(), saved.getExpenseDate());
            return saved;
        });
        doNothing().when(authenticationHelper).validateUserAccess(userId);

        budgetService.addExpense(expenseWithoutDate);

        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    @DisplayName("Should get expenses with filters")
    void testGetExpensesFiltered() {
        List<Expense> expenses = Arrays.asList(testExpense);
        Page<Expense> expensePage = new PageImpl<>(expenses);
        Pageable pageable = PageRequest.of(0, 20);

        when(expenseRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(expensePage);
        doNothing().when(authenticationHelper).validateUserAccess(userId);

        Page<Expense> result = budgetService.getExpensesFiltered(
                userId, "FOOD", null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(expenseRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Should get expense by ID")
    void testGetExpenseById() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        doNothing().when(authenticationHelper).validateUserAccess(userId);

        Expense result = budgetService.getExpenseById(1L);

        assertNotNull(result);
        assertEquals(testExpense.getId(), result.getId());
        verify(expenseRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when expense not found")
    void testGetExpenseByIdNotFound() {
        when(expenseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            budgetService.getExpenseById(999L));
    }

    @Test
    @DisplayName("Should update expense successfully")
    void testUpdateExpense() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        doNothing().when(authenticationHelper).validateUserAccess(userId);

        Expense updatedExpense = new Expense();
        updatedExpense.setUserId(userId);
        updatedExpense.setAmount(BigDecimal.valueOf(6000));
        updatedExpense.setCategory(ExpenseCategory.ENTERTAINMENT);
        updatedExpense.setExpenseDate(LocalDate.now());

        Expense result = budgetService.updateExpense(1L, updatedExpense);

        assertNotNull(result);
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    @DisplayName("Should delete expense successfully")
    void testDeleteExpense() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        doNothing().when(expenseRepository).delete(any(Expense.class));
        doNothing().when(authenticationHelper).validateUserAccess(userId);

        budgetService.deleteExpense(1L);

        verify(expenseRepository).delete(any(Expense.class));
    }

    // ===== INCOME TESTS =====

    @Test
    @DisplayName("Should add income successfully")
    void testAddIncome() {
        when(incomeRepository.save(any(Income.class))).thenReturn(testIncome);
        doNothing().when(authenticationHelper).validateUserAccess(userId);

        Income result = budgetService.addIncome(testIncome);

        assertNotNull(result);
        assertEquals(testIncome.getId(), result.getId());
        verify(incomeRepository).save(testIncome);
    }

    @Test
    @DisplayName("Should get incomes with filters")
    void testGetIncomesFiltered() {
        List<Income> incomes = Arrays.asList(testIncome);
        Page<Income> incomePage = new PageImpl<>(incomes);
        Pageable pageable = PageRequest.of(0, 20);

        when(incomeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(incomePage);
        doNothing().when(authenticationHelper).validateUserAccess(userId);

        Page<Income> result = budgetService.getIncomesFiltered(
                userId, "SALARY", null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(incomeRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Should get income by ID")
    void testGetIncomeById() {
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(testIncome));
        doNothing().when(authenticationHelper).validateUserAccess(userId);

        Income result = budgetService.getIncomeById(1L);

        assertNotNull(result);
        assertEquals(testIncome.getId(), result.getId());
    }

    @Test
    @DisplayName("Should update income successfully")
    void testUpdateIncome() {
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(testIncome));
        when(incomeRepository.save(any(Income.class))).thenReturn(testIncome);
        doNothing().when(authenticationHelper).validateUserAccess(userId);

        Income updatedIncome = new Income();
        updatedIncome.setUserId(userId);
        updatedIncome.setAmount(BigDecimal.valueOf(55000));
        updatedIncome.setSource("SALARY");
        updatedIncome.setDate(LocalDate.now());

        Income result = budgetService.updateIncome(1L, updatedIncome);

        assertNotNull(result);
        verify(incomeRepository).save(any(Income.class));
    }

    @Test
    @DisplayName("Should delete income successfully")
    void testDeleteIncome() {
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(testIncome));
        doNothing().when(incomeRepository).delete(any(Income.class));
        doNothing().when(authenticationHelper).validateUserAccess(userId);

        budgetService.deleteIncome(1L);

        verify(incomeRepository).delete(any(Income.class));
    }

    // ===== BUDGET LIMIT TESTS =====

    @Test
    @DisplayName("Should set budget limit successfully")
    void testSetBudget() {
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);
        doNothing().when(authenticationHelper).validateUserAccess(userId);

        Budget result = budgetService.setBudget(testBudget);

        assertNotNull(result);
        assertEquals(testBudget.getId(), result.getId());
        verify(budgetRepository).save(testBudget);
    }

    @Test
    @DisplayName("Should set budget monthYear to current if null")
    void testSetBudgetWithNullMonthYear() {
        Budget budgetWithoutMonth = new Budget();
        budgetWithoutMonth.setUserId(userId);
        budgetWithoutMonth.setCategory(ExpenseCategory.FOOD);
        budgetWithoutMonth.setMonthlyLimit(BigDecimal.valueOf(10000));
        budgetWithoutMonth.setMonthYear(null);

        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> {
            Budget saved = invocation.getArgument(0);
            assertNotNull(saved.getMonthYear());
            assertEquals(YearMonth.now().toString(), saved.getMonthYear());
            return saved;
        });
        doNothing().when(authenticationHelper).validateUserAccess(userId);

        budgetService.setBudget(budgetWithoutMonth);

        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    @DisplayName("Should get all budgets for user")
    void testGetAllBudgets() {
        List<Budget> budgets = Arrays.asList(testBudget);
        when(budgetRepository.findByUserIdAndMonthYear(userId, YearMonth.now().toString()))
                .thenReturn(budgets);
        doNothing().when(authenticationHelper).validateUserAccess(userId);

        List<Budget> result = budgetService.getAllBudgets(userId, YearMonth.now().toString());

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(budgetRepository).findByUserIdAndMonthYear(userId, YearMonth.now().toString());
    }

    @Test
    @DisplayName("Should delete budget successfully")
    void testDeleteBudget() {
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(testBudget));
        doNothing().when(budgetRepository).delete(any(Budget.class));
        doNothing().when(authenticationHelper).validateUserAccess(userId);

        budgetService.deleteBudget(1L);

        verify(budgetRepository).delete(any(Budget.class));
    }

    // ===== REPORTING TESTS =====

    @Test
    @DisplayName("Should generate monthly report")
    void testGetMonthlyReport() {
        List<Budget> budgets = Arrays.asList(testBudget);
        List<Expense> expenses = Arrays.asList(testExpense);

        when(budgetRepository.findByUserIdAndMonthYear(userId, YearMonth.now().toString()))
                .thenReturn(budgets);
        when(expenseRepository.findByUserIdAndExpenseDateBetween(eq(userId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(expenses);
        doNothing().when(authenticationHelper).validateUserAccess(userId);

        BudgetReportDTO result = budgetService.getMonthlyReport(userId, YearMonth.now().toString());

        assertNotNull(result);
        verify(budgetRepository).findByUserIdAndMonthYear(userId, YearMonth.now().toString());
        verify(expenseRepository).findByUserIdAndExpenseDateBetween(eq(userId), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("Should generate cash flow analysis")
    void testGetCashFlow() {
        List<Income> incomes = Arrays.asList(testIncome);
        List<Expense> expenses = Arrays.asList(testExpense);

        when(incomeRepository.findByUserIdAndDateBetween(eq(userId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(incomes);
        when(expenseRepository.findByUserIdAndExpenseDateBetween(eq(userId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(expenses);
        doNothing().when(authenticationHelper).validateUserAccess(userId);

        CashFlowDTO result = budgetService.getCashFlowAnalysis(userId, YearMonth.now().toString());

        assertNotNull(result);
        verify(incomeRepository).findByUserIdAndDateBetween(eq(userId), any(LocalDate.class), any(LocalDate.class));
        verify(expenseRepository).findByUserIdAndExpenseDateBetween(eq(userId), any(LocalDate.class), any(LocalDate.class));
    }

    // ===== EDGE CASE TESTS =====

    @Test
    @DisplayName("Should handle empty expense list")
    void testGetExpensesFilteredEmpty() {
        Page<Expense> emptyPage = new PageImpl<>(Arrays.asList());
        Pageable pageable = PageRequest.of(0, 20);

        when(expenseRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);
        doNothing().when(authenticationHelper).validateUserAccess(userId);

        Page<Expense> result = budgetService.getExpensesFiltered(
                userId, null, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(0, result.getContent().size());
    }

    @Test
    @DisplayName("Should handle empty income list")
    void testGetIncomesFilteredEmpty() {
        Page<Income> emptyPage = new PageImpl<>(Arrays.asList());
        Pageable pageable = PageRequest.of(0, 20);

        when(incomeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);
        doNothing().when(authenticationHelper).validateUserAccess(userId);

        Page<Income> result = budgetService.getIncomesFiltered(
                userId, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(0, result.getContent().size());
    }

    @Test
    @DisplayName("Should filter expenses by date range")
    void testGetExpensesWithDateRange() {
        List<Expense> expenses = Arrays.asList(testExpense);
        Page<Expense> expensePage = new PageImpl<>(expenses);
        Pageable pageable = PageRequest.of(0, 20);

        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        when(expenseRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(expensePage);
        doNothing().when(authenticationHelper).validateUserAccess(userId);

        Page<Expense> result = budgetService.getExpensesFiltered(
                userId, null, startDate, endDate, null, pageable);

        assertNotNull(result);
        verify(expenseRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Should search expenses by description")
    void testGetExpensesWithSearch() {
        List<Expense> expenses = Arrays.asList(testExpense);
        Page<Expense> expensePage = new PageImpl<>(expenses);
        Pageable pageable = PageRequest.of(0, 20);

        when(expenseRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(expensePage);
        doNothing().when(authenticationHelper).validateUserAccess(userId);

        Page<Expense> result = budgetService.getExpensesFiltered(
                userId, null, null, null, "test", pageable);

        assertNotNull(result);
        verify(expenseRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Should verify authentication on all operations")
    void testAuthenticationValidation() {
        doNothing().when(authenticationHelper).validateUserAccess(userId);

        // Test multiple operations
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        budgetService.addExpense(testExpense);

        when(incomeRepository.save(any(Income.class))).thenReturn(testIncome);
        budgetService.addIncome(testIncome);

        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);
        budgetService.setBudget(testBudget);

        // Verify authentication was called 3 times
        verify(authenticationHelper, times(3)).validateUserAccess(userId);
    }
}
