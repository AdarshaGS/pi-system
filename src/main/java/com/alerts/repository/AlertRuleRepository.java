package com.alerts.repository;

import com.alerts.entity.AlertRule;
import com.alerts.entity.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for AlertRule entity
 */
@Repository
public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {

    List<AlertRule> findByUserId(Long userId);

    List<AlertRule> findByUserIdAndEnabled(Long userId, Boolean enabled);

    List<AlertRule> findByTypeAndEnabled(AlertType type, Boolean enabled);

    List<AlertRule> findBySymbolAndEnabled(String symbol, Boolean enabled);

    void deleteByUserIdAndId(Long userId, Long id);
}
