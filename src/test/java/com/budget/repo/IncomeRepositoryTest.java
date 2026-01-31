package com.budget.repo;

import com.budget.data.Income;
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
 * Repository tests for IncomeRepository - Sprint 3
 * Tests JPA Specifications and custom queries
 */
@DataJpaTest
@ContextConfiguration(classes = RepositoryTestConfig.class)
class IncomeRepositoryTest {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Long userId;
    private Income income1;
    private Income income2;
    private Income income3;

    @BeforeEach
    void setUp() {
        userId = 100L;

        income1 = new Income();
        income1.setUserId(userId);
        income1.setAmount(BigDecimal.valueOf(50000));
        income1.setSource("SALARY");
        income1.setDate(LocalDate.now());
        income1.setIsRecurring(true);
        income1.setIsStable(true);
        entityManager.persist(income1);

        income2 = new Income();
        income2.setUserId(userId);
        income2.setAmount(BigDecimal.valueOf(5000));
        income2.setSource("DIVIDEND");
        income2.setDate(LocalDate.now().minusDays(5));
        income2.setIsRecurring(false);
        income2.setIsStable(false);
        entityManager.persist(income2);

        income3 = new Income();
        income3.setUserId(userId);
        income3.setAmount(BigDecimal.valueOf(10000));
        income3.setSource("FREELANCE");
        income3.setDate(LocalDate.now().minusDays(10));
        income3.setIsRecurring(false);
        income3.setIsStable(false);
        entityManager.persist(income3);

        entityManager.flush();
    }

    @Test
    @DisplayName("Should find incomes by userId")
    void testFindByUserId() {
        List<Income> incomes = incomeRepository.findByUserId(userId);

        assertNotNull(incomes);
        assertEquals(3, incomes.size());
    }

    @Test
    @DisplayName("Should find incomes by userId and date range")
    void testFindByUserIdAndDateBetween() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        List<Income> incomes = incomeRepository.findByUserIdAndDateBetween(
                userId, startDate, endDate);

        assertNotNull(incomes);
        assertEquals(2, incomes.size()); // income1 and income2
    }

    @Test
    @DisplayName("Should filter by source using Specification")
    void testFilterBySource() {
        Specification<Income> spec = (root, query, cb) -> 
            cb.and(
                cb.equal(root.get("userId"), userId),
                cb.like(cb.lower(root.get("source")), "%salary%")
            );

        Pageable pageable = PageRequest.of(0, 20);
        Page<Income> result = incomeRepository.findAll(spec, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("SALARY", result.getContent().get(0).getSource());
    }

    @Test
    @DisplayName("Should filter by date range using Specification")
    void testFilterByDateRange() {
        LocalDate startDate = LocalDate.now().minusDays(6);
        LocalDate endDate = LocalDate.now();

        Specification<Income> spec = (root, query, cb) -> 
            cb.and(
                cb.equal(root.get("userId"), userId),
                cb.between(root.get("date"), startDate, endDate)
            );

        Pageable pageable = PageRequest.of(0, 20);
        Page<Income> result = incomeRepository.findAll(spec, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
    }

    @Test
    @DisplayName("Should filter by recurring status using Specification")
    void testFilterByRecurring() {
        Specification<Income> spec = (root, query, cb) -> 
            cb.and(
                cb.equal(root.get("userId"), userId),
                cb.equal(root.get("isRecurring"), true)
            );

        Pageable pageable = PageRequest.of(0, 20);
        Page<Income> result = incomeRepository.findAll(spec, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).getIsRecurring());
    }

    @Test
    @DisplayName("Should combine multiple filters using Specification")
    void testCombinedFilters() {
        LocalDate startDate = LocalDate.now().minusDays(15);
        LocalDate endDate = LocalDate.now();

        Specification<Income> spec = (root, query, cb) -> 
            cb.and(
                cb.equal(root.get("userId"), userId),
                cb.equal(root.get("isRecurring"), false),
                cb.between(root.get("date"), startDate, endDate)
            );

        Pageable pageable = PageRequest.of(0, 20);
        Page<Income> result = incomeRepository.findAll(spec, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().stream()
                .allMatch(i -> !i.getIsRecurring()));
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void testPagination() {
        Specification<Income> spec = (root, query, cb) -> 
            cb.equal(root.get("userId"), userId);

        Pageable pageable = PageRequest.of(0, 2); // Page 0, size 2
        Page<Income> result = incomeRepository.findAll(spec, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertTrue(result.hasContent());
    }

    @Test
    @DisplayName("Should return empty result for non-existent user")
    void testFindByNonExistentUser() {
        List<Income> incomes = incomeRepository.findByUserId(999L);

        assertNotNull(incomes);
        assertTrue(incomes.isEmpty());
    }

    @Test
    @DisplayName("Should save and retrieve income")
    void testSaveAndRetrieve() {
        Income newIncome = new Income();
        newIncome.setUserId(userId);
        newIncome.setAmount(BigDecimal.valueOf(8000));
        newIncome.setSource("BONUS");
        newIncome.setDate(LocalDate.now());
        newIncome.setIsRecurring(false);
        newIncome.setIsStable(false);

        Income saved = incomeRepository.save(newIncome);

        assertNotNull(saved.getId());
        
        Income retrieved = incomeRepository.findById(saved.getId()).orElse(null);
        assertNotNull(retrieved);
        assertEquals(saved.getAmount(), retrieved.getAmount());
        assertEquals(saved.getSource(), retrieved.getSource());
    }

    @Test
    @DisplayName("Should delete income")
    void testDeleteIncome() {
        Long incomeId = income1.getId();
        
        incomeRepository.deleteById(incomeId);
        entityManager.flush();

        assertFalse(incomeRepository.findById(incomeId).isPresent());
    }
}
