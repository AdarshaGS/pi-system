package com.budget.repo;

import com.budget.data.Expense;
import com.budget.data.ExpenseCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository tests for ExpenseRepository - Sprint 3
 * Tests JPA Specifications and custom queries
 */
@DataJpaTest
@ContextConfiguration(classes = RepositoryTestConfig.class)
class ExpenseRepositoryTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Long userId;
    private Expense expense1;
    private Expense expense2;
    private Expense expense3;

    @BeforeEach
    void setUp() {
        userId = 100L;

        expense1 = new Expense();
        expense1.setUserId(userId);
        expense1.setAmount(BigDecimal.valueOf(5000));
        expense1.setCategory(ExpenseCategory.FOOD);
        expense1.setExpenseDate(LocalDate.now());
        expense1.setDescription("Grocery shopping");
        entityManager.persist(expense1);

        expense2 = new Expense();
        expense2.setUserId(userId);
        expense2.setAmount(BigDecimal.valueOf(2000));
        expense2.setCategory(ExpenseCategory.TRANSPORT);
        expense2.setExpenseDate(LocalDate.now().minusDays(5));
        expense2.setDescription("Taxi ride");
        entityManager.persist(expense2);

        expense3 = new Expense();
        expense3.setUserId(userId);
        expense3.setAmount(BigDecimal.valueOf(15000));
        expense3.setCategory(ExpenseCategory.FOOD);
        expense3.setExpenseDate(LocalDate.now().minusDays(10));
        expense3.setDescription("Restaurant dinner");
        entityManager.persist(expense3);

        entityManager.flush();
    }

    @Test
    @DisplayName("Should find expenses by userId")
    void testFindByUserId() {
        List<Expense> expenses = expenseRepository.findByUserId(userId);

        assertNotNull(expenses);
        assertEquals(3, expenses.size());
    }

    @Test
    @DisplayName("Should find expenses by userId and date range")
    void testFindByUserIdAndExpenseDateBetween() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        List<Expense> expenses = expenseRepository.findByUserIdAndExpenseDateBetween(
                userId, startDate, endDate);

        assertNotNull(expenses);
        assertEquals(2, expenses.size()); // expense1 and expense2
    }

    @Test
    @DisplayName("Should filter by category using Specification")
    void testFilterByCategory() {
        Specification<Expense> spec = (root, query, cb) -> 
            cb.and(
                cb.equal(root.get("userId"), userId),
                cb.equal(root.get("category"), ExpenseCategory.FOOD)
            );

        Pageable pageable = PageRequest.of(0, 20);
        Page<Expense> result = expenseRepository.findAll(spec, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().stream()
                .allMatch(e -> e.getCategory() == ExpenseCategory.FOOD));
    }

    @Test
    @DisplayName("Should filter by date range using Specification")
    void testFilterByDateRange() {
        LocalDate startDate = LocalDate.now().minusDays(6);
        LocalDate endDate = LocalDate.now();

        Specification<Expense> spec = (root, query, cb) -> 
            cb.and(
                cb.equal(root.get("userId"), userId),
                cb.between(root.get("expenseDate"), startDate, endDate)
            );

        Pageable pageable = PageRequest.of(0, 20);
        Page<Expense> result = expenseRepository.findAll(spec, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
    }

    @Test
    @DisplayName("Should search by description using Specification")
    void testSearchByDescription() {
        Specification<Expense> spec = (root, query, cb) -> 
            cb.and(
                cb.equal(root.get("userId"), userId),
                cb.like(cb.lower(root.get("description")), "%shopping%")
            );

        Pageable pageable = PageRequest.of(0, 20);
        Page<Expense> result = expenseRepository.findAll(spec, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).getDescription().toLowerCase().contains("shopping"));
    }

    @Test
    @DisplayName("Should combine multiple filters using Specification")
    void testCombinedFilters() {
        LocalDate startDate = LocalDate.now().minusDays(15);
        LocalDate endDate = LocalDate.now();

        Specification<Expense> spec = (root, query, cb) -> 
            cb.and(
                cb.equal(root.get("userId"), userId),
                cb.equal(root.get("category"), ExpenseCategory.FOOD),
                cb.between(root.get("expenseDate"), startDate, endDate)
            );

        Pageable pageable = PageRequest.of(0, 20);
        Page<Expense> result = expenseRepository.findAll(spec, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().stream()
                .allMatch(e -> e.getCategory() == ExpenseCategory.FOOD));
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void testPagination() {
        Specification<Expense> spec = (root, query, cb) -> 
            cb.equal(root.get("userId"), userId);

        Pageable pageable = PageRequest.of(0, 2); // Page 0, size 2
        Page<Expense> result = expenseRepository.findAll(spec, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertTrue(result.hasContent());
    }

    @Test
    @DisplayName("Should return empty result for non-existent user")
    void testFindByNonExistentUser() {
        List<Expense> expenses = expenseRepository.findByUserId(999L);

        assertNotNull(expenses);
        assertTrue(expenses.isEmpty());
    }

    @Test
    @DisplayName("Should save and retrieve expense")
    void testSaveAndRetrieve() {
        Expense newExpense = new Expense();
        newExpense.setUserId(userId);
        newExpense.setAmount(BigDecimal.valueOf(3000));
        newExpense.setCategory(ExpenseCategory.ENTERTAINMENT);
        newExpense.setExpenseDate(LocalDate.now());
        newExpense.setDescription("Movie tickets");

        Expense saved = expenseRepository.save(newExpense);

        assertNotNull(saved.getId());
        
        Expense retrieved = expenseRepository.findById(saved.getId()).orElse(null);
        assertNotNull(retrieved);
        assertEquals(saved.getAmount(), retrieved.getAmount());
        assertEquals(saved.getCategory(), retrieved.getCategory());
    }

    @Test
    @DisplayName("Should delete expense")
    void testDeleteExpense() {
        Long expenseId = expense1.getId();
        
        expenseRepository.deleteById(expenseId);
        entityManager.flush();

        assertFalse(expenseRepository.findById(expenseId).isPresent());
    }
}
