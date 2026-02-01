package com.common.features;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for FeatureConfig entity
 */
@Repository
public interface FeatureConfigRepository extends JpaRepository<FeatureConfig, Long> {
    
    /**
     * Find feature config by feature flag
     */
    Optional<FeatureConfig> findByFeatureFlag(FeatureFlag featureFlag);
    
    /**
     * Find all enabled features
     */
    List<FeatureConfig> findByEnabledTrue();
    
    /**
     * Find all features by category
     */
    List<FeatureConfig> findByCategory(String category);
    
    /**
     * Find all enabled features by category
     */
    List<FeatureConfig> findByCategoryAndEnabledTrue(String category);
    
    /**
     * Find all beta features
     */
    List<FeatureConfig> findByBetaFeatureTrue();
    
    /**
     * Check if feature exists
     */
    boolean existsByFeatureFlag(FeatureFlag featureFlag);
}
