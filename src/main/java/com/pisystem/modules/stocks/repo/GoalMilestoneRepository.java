package com.investments.stocks.repo;

import com.investments.stocks.data.GoalMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalMilestoneRepository extends JpaRepository<GoalMilestone, Long> {
    
    List<GoalMilestone> findByGoalId(Long goalId);
    
    List<GoalMilestone> findByGoalIdAndStatus(Long goalId, GoalMilestone.MilestoneStatus status);
    
    List<GoalMilestone> findByGoalIdOrderByTargetDateAsc(Long goalId);
}
