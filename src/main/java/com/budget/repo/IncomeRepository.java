package com.budget.repo;

import com.budget.data.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long>, JpaSpecificationExecutor<Income> {
    List<Income> findByUserId(Long userId);

    List<Income> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
