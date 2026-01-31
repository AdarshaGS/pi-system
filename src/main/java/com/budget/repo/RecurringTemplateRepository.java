package com.budget.repo;

import com.budget.data.RecurringTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecurringTemplateRepository extends JpaRepository<RecurringTemplate, Long> {
    
    List<RecurringTemplate> findByUserId(Long userId);
    
    List<RecurringTemplate> findByUserIdAndIsActive(Long userId, Boolean isActive);
    
    List<RecurringTemplate> findByIsActiveTrue();
}
