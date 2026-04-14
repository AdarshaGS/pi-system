package com.pisystem.modules.budget.repo;

import com.pisystem.modules.budget.data.Expense;
import com.pisystem.modules.budget.data.ExpenseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    // Eagerly load tags in every paginated query — prevents LazyInitializationException
    // when Jackson serializes Expense.tags after the transaction closes
    @EntityGraph(attributePaths = {"tags"})
    @NonNull
    @Override
    Page<Expense> findAll(@NonNull Specification<Expense> spec, @NonNull Pageable pageable);

    @EntityGraph(attributePaths = {"tags"})
    List<Expense> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"tags"})
    List<Expense> findByUserIdAndExpenseDateBetween(Long userId, LocalDate start, LocalDate end);

    @EntityGraph(attributePaths = {"tags"})
    List<Expense> findByUserIdAndCategoryAndExpenseDateBetween(Long userId, ExpenseCategory category, LocalDate start,
            LocalDate end);

    @EntityGraph(attributePaths = {"tags"})
    List<Expense> findByUserIdAndCategory(Long userId, ExpenseCategory category);
}
