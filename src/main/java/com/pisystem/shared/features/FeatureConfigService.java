package com.pisystem.shared.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing feature flags and configuration
 * Configuration stored in database only
 */
@Service
public class FeatureConfigService {
    
    private static final Logger logger = LoggerFactory.getLogger(FeatureConfigService.class);
    
    @Autowired
    private FeatureConfigRepository featureConfigRepository;

    @Autowired
    private CacheManager cacheManager;
    
    /**
     * Initialize feature configs on startup
     */
    @PostConstruct
    public void initializeFeatures() {
        logger.info("Initializing feature configurations...");
        
        for (FeatureFlag flag : FeatureFlag.values()) {
            if (!featureConfigRepository.existsByFeatureFlag(flag)) {
                FeatureConfig config = new FeatureConfig();
                config.setFeatureFlag(flag);
                config.setParentFeatureFlag(flag.getParentFlag());
                config.setDescription(flag.getDescription());
                config.setCategory(flag.getCategory());
                config.setEnabled(true);

                featureConfigRepository.save(config);
                logger.info("Initialized feature: {} (parent: {})", flag.name(),
                        flag.getParentFlag() != null ? flag.getParentFlag().name() : "none");
            } else {
                // Backfill parent link for rows that existed before the hierarchy was introduced
                featureConfigRepository.findByFeatureFlag(flag).ifPresent(existing -> {
                    if (existing.getParentFeatureFlag() == null && flag.getParentFlag() != null) {
                        existing.setParentFeatureFlag(flag.getParentFlag());
                        featureConfigRepository.save(existing);
                    }
                });
            }
        }
        
        logger.info("Feature configuration initialization complete");
    }
    
    /**
     * Check if a feature is enabled.
     *
     * <p>For sub-features, the parent module must also be enabled.
     * The result is cached per flag name.
     */
    @Cacheable(value = "featureFlags", key = "#flag != null ? #flag.name() : 'null'", unless = "#flag == null")
    public boolean isFeatureEnabled(FeatureFlag flag) {
        if (flag == null) {
            return false;
        }

        // Parent module must be enabled for any sub-feature to be active
        FeatureFlag parent = flag.getParentFlag();
        if (parent != null) {
            Optional<FeatureConfig> parentConfig = featureConfigRepository.findByFeatureFlag(parent);
            if (parentConfig.isPresent() && Boolean.FALSE.equals(parentConfig.get().getEnabled())) {
                return false;
            }
        }

        Optional<FeatureConfig> config = featureConfigRepository.findByFeatureFlag(flag);
        return config.map(FeatureConfig::getEnabled).orElse(true);
    }
    
    /**
     * Check if feature is enabled for a specific user
     */
    public boolean isFeatureEnabledForUser(FeatureFlag flag, Long userId) {
        if (!isFeatureEnabled(flag)) {
            return false;
        }
        
        Optional<FeatureConfig> config = featureConfigRepository.findByFeatureFlag(flag);
        if (config.isEmpty()) {
            return true;
        }
        
        FeatureConfig featureConfig = config.get();
        
        // If enabled for all users
        if (featureConfig.getEnabledForAll()) {
            return true;
        }
        
        // TODO: Implement user-specific feature flags
        // Check if user has access (based on subscription tier, beta access, etc.)
        
        return false;
    }
    
    /**
     * Enable a feature
     */
    @Transactional
    public void enableFeature(FeatureFlag flag) {
        logger.info("Enabling feature: {}", flag.name());
        
        FeatureConfig config = featureConfigRepository.findByFeatureFlag(flag)
                .orElseGet(() -> {
                    FeatureConfig newConfig = new FeatureConfig();
                    newConfig.setFeatureFlag(flag);
                    newConfig.setDescription(flag.getDescription());
                    newConfig.setCategory(flag.getCategory());
                    return newConfig;
                });
        
        config.setEnabled(true);
        config.setEnabledSince(LocalDateTime.now());
        featureConfigRepository.save(config);
        
        // Clear cache
        clearFeatureCache(flag);
    }
    
    /**
     * Disable a feature
     */
    @Transactional
    public void disableFeature(FeatureFlag flag) {
        logger.info("Disabling feature: {}", flag.name());
        
        Optional<FeatureConfig> configOpt = featureConfigRepository.findByFeatureFlag(flag);
        if (configOpt.isPresent()) {
            FeatureConfig config = configOpt.get();
            config.setEnabled(false);
            config.setDisabledSince(LocalDateTime.now());
            featureConfigRepository.save(config);
            
            // Clear cache
            clearFeatureCache(flag);
        }
    }
    
    /**
     * Get all enabled features
     */
    public List<FeatureFlag> getEnabledFeatures() {
        return Arrays.stream(FeatureFlag.values())
                .filter(this::isFeatureEnabled)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all features with their status
     */
    public Map<FeatureFlag, Boolean> getAllFeaturesStatus() {
        Map<FeatureFlag, Boolean> statusMap = new HashMap<>();
        for (FeatureFlag flag : FeatureFlag.values()) {
            statusMap.put(flag, isFeatureEnabled(flag));
        }
        return statusMap;
    }
    
    /**
     * Get features by category
     */
    public List<FeatureFlag> getFeaturesByCategory(String category) {
        return Arrays.stream(FeatureFlag.values())
                .filter(flag -> flag.getCategory().equals(category))
                .collect(Collectors.toList());
    }
    
    /**
     * Get enabled features by category
     */
    public List<FeatureFlag> getEnabledFeaturesByCategory(String category) {
        return getFeaturesByCategory(category).stream()
                .filter(this::isFeatureEnabled)
                .collect(Collectors.toList());
    }
    
    /**
     * Get feature config details
     */
    public Optional<FeatureConfig> getFeatureConfig(FeatureFlag flag) {
        return featureConfigRepository.findByFeatureFlag(flag);
    }
    
    /**
     * Update feature config
     */
    @Transactional
    public FeatureConfig updateFeatureConfig(FeatureFlag flag, FeatureConfig updates) {
        FeatureConfig config = featureConfigRepository.findByFeatureFlag(flag)
                .orElseThrow(() -> new RuntimeException("Feature config not found: " + flag.name()));
        
        if (updates.getEnabled() != null) {
            config.setEnabled(updates.getEnabled());
        }
        if (updates.getEnabledForAll() != null) {
            config.setEnabledForAll(updates.getEnabledForAll());
        }
        if (updates.getDescription() != null) {
            config.setDescription(updates.getDescription());
        }
        if (updates.getRequiresSubscription() != null) {
            config.setRequiresSubscription(updates.getRequiresSubscription());
        }
        if (updates.getMinSubscriptionTier() != null) {
            config.setMinSubscriptionTier(updates.getMinSubscriptionTier());
        }
        if (updates.getBetaFeature() != null) {
            config.setBetaFeature(updates.getBetaFeature());
        }
        
        FeatureConfig saved = featureConfigRepository.save(config);
        clearFeatureCache(flag);
        
        return saved;
    }
    
    /**
     * Check if feature requires specific exception to be thrown
     */
    public void requireFeature(FeatureFlag flag) {
        if (!isFeatureEnabled(flag)) {
            throw new FeatureNotEnabledException(
                    "Feature '" + flag.getDisplayName() + "' is not enabled");
        }
    }
    
    /**
     * Returns a map of each module flag to its list of sub-features.
     * Only module-level flags are keys.
     */
    public Map<FeatureFlag, List<FeatureFlag>> getModuleFeatures() {
        Map<FeatureFlag, List<FeatureFlag>> result = new LinkedHashMap<>();
        Arrays.stream(FeatureFlag.values())
                .filter(FeatureFlag::isModuleFlag)
                .forEach(module -> result.put(module, module.getSubFeatures()));
        return result;
    }

    /**
     * Evicts the flag from cache.
     * For module-level flags, also evicts all child sub-feature entries so
     * they re-evaluate against the updated parent state on next access.
     */
    private void clearFeatureCache(FeatureFlag flag) {
        Cache cache = cacheManager.getCache("featureFlags");
        if (cache != null) {
            cache.evict(flag.name());
            flag.getSubFeatures().forEach(sub -> cache.evict(sub.name()));
        }
        logger.debug("Evicted cache for: {} (+{} sub-features)", flag.name(), flag.getSubFeatures().size());
    }
    
    /**
     * Get all categories
     */
    public Set<String> getAllCategories() {
        return Arrays.stream(FeatureFlag.values())
                .map(FeatureFlag::getCategory)
                .collect(Collectors.toSet());
    }
}
