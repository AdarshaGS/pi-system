package com.investments.stocks.repo;

import com.investments.stocks.data.FinancialGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, Long> {
    
    List<FinancialGoal> findByUserId(Long userId);
    
    List<FinancialGoal> findByUserIdAndStatus(Long userId, FinancialGoal.GoalStatus status);
    
    List<FinancialGoal> findByUserIdAndGoalType(Long userId, FinancialGoal.GoalType goalType);
    
    @Query("SELECT g FROM FinancialGoal g WHERE g.userId = :userId AND g.targetDate BETWEEN :startDate AND :endDate")
    List<FinancialGoal> findGoalsByTargetDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT g FROM FinancialGoal g WHERE g.userId = :userId AND g.status = 'ACTIVE' AND g.targetDate < :currentDate")
    List<FinancialGoal> findOverdueGoals(@Param("userId") Long userId, @Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT g FROM FinancialGoal g WHERE g.autoContribute = true AND g.status = 'ACTIVE' AND g.reminderDayOfMonth = :dayOfMonth")
    List<FinancialGoal> findGoalsForAutoContribution(@Param("dayOfMonth") Integer dayOfMonth);
    
    @Query("SELECT g FROM FinancialGoal g WHERE g.userId = :userId ORDER BY g.priority DESC, g.targetDate ASC")
    List<FinancialGoal> findByUserIdOrderedByPriority(@Param("userId") Long userId);
}
