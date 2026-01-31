package com.budget.repo;

import com.budget.data.CustomCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomCategoryRepository extends JpaRepository<CustomCategory, Long> {
    
    List<CustomCategory> findByUserIdAndIsActive(Long userId, Boolean isActive);
    
    List<CustomCategory> findByUserId(Long userId);
    
    Optional<CustomCategory> findByUserIdAndCategoryName(Long userId, String categoryName);
    
    boolean existsByUserIdAndCategoryName(Long userId, String categoryName);
}
