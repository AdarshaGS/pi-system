package com.budget.repo;

import com.budget.data.RecurringTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecurringTemplateRepository extends JpaRepository<RecurringTemplate, Long> {
    
    List<RecurringTemplate> findByUserId(Long userId);
    
    List<RecurringTemplate> findByUserIdAndIsActive(Long userId, Boolean isActive);
    
    List<RecurringTemplate> findByIsActiveTrue();
    
    // New queries for next_run_date based automation
    List<RecurringTemplate> findByIsActiveTrueAndNextRunDateLessThanEqual(LocalDate date);
    
    List<RecurringTemplate> findByUserIdAndIsActiveTrueAndNextRunDateLessThanEqual(Long userId, LocalDate date);
    
    List<RecurringTemplate> findByUserIdAndNextRunDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
