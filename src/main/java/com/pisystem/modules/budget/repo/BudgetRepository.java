package com.pisystem.modules.budget.repo;

import com.pisystem.modules.budget.data.Budget;
import com.pisystem.modules.budget.data.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserIdAndMonthYear(Long userId, String monthYear);

    Optional<Budget> findByUserIdAndCategoryAndMonthYear(Long userId, ExpenseCategory category, String monthYear);
}
