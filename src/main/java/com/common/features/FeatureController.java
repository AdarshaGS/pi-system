package com.common.features;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST Controller for feature toggle management
 * All endpoints require admin role
 * UI integration should use public proxy/query service
 */
@RestController
@RequestMapping("/api/v1/admin/features")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Feature Management (Admin)", description = "Admin APIs for feature toggles and configuration")
public class FeatureController {
    
    private static final Logger logger = LoggerFactory.getLogger(FeatureController.class);
    
    @Autowired
    private FeatureConfigService featureConfigService;
    
    /**
     * Get all enabled features
     */
    @GetMapping("/enabled")
    @Operation(summary = "Get enabled features", description = "Get list of all enabled features")
    public ResponseEntity<List<String>> getEnabledFeatures() {
        logger.debug("REST request to get enabled features");
        
        List<String> enabledFeatures = featureConfigService.getEnabledFeatures().stream()
                .map(Enum::name)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(enabledFeatures);
    }
    
    /**
     * Get all features with details
     */
    @GetMapping
    @Operation(summary = "Get all features", description = "Get all features with their enabled status and details")
    public ResponseEntity<List<FeatureDTO>> getAllFeatures() {
        logger.debug("REST request to get all features");
        
        List<FeatureDTO> features = new ArrayList<>();
        
        for (FeatureFlag flag : FeatureFlag.values()) {
            boolean enabled = featureConfigService.isFeatureEnabled(flag);
            Optional<FeatureConfig> config = featureConfigService.getFeatureConfig(flag);
            
            if (config.isPresent()) {
                features.add(new FeatureDTO(flag, config.get()));
            } else {
                features.add(new FeatureDTO(flag, enabled));
            }
        }
        
        return ResponseEntity.ok(features);
    }
    
    /**
     * Get features by category
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Get features by category", description = "Get all features in a specific category")
    public ResponseEntity<List<FeatureDTO>> getFeaturesByCategory(
            @Parameter(description = "Feature category") @PathVariable String category) {
        logger.debug("REST request to get features by category: {}", category);
        
        List<FeatureDTO> features = featureConfigService.getFeaturesByCategory(category).stream()
                .map(flag -> {
                    boolean enabled = featureConfigService.isFeatureEnabled(flag);
                    Optional<FeatureConfig> config = featureConfigService.getFeatureConfig(flag);
                    return config.map(featureConfig -> new FeatureDTO(flag, featureConfig))
                            .orElseGet(() -> new FeatureDTO(flag, enabled));
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(features);
    }
    
    /**
     * Check if specific feature is enabled
     */
    @GetMapping("/{featureName}/enabled")
    @Operation(summary = "Check if feature is enabled", description = "Check if a specific feature is enabled")
    public ResponseEntity<Map<String, Boolean>> isFeatureEnabled(
            @Parameter(description = "Feature name") @PathVariable String featureName) {
        logger.debug("REST request to check if feature is enabled: {}", featureName);
        
        try {
            FeatureFlag flag = FeatureFlag.valueOf(featureName.toUpperCase());
            boolean enabled = featureConfigService.isFeatureEnabled(flag);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("enabled", enabled);
            response.put("feature", true); // Feature exists
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Boolean> response = new HashMap<>();
            response.put("enabled", false);
            response.put("feature", false); // Feature doesn't exist
            
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * Get all categories
     */
    @GetMapping("/categories")
    @Operation(summary = "Get all categories", description = "Get list of all feature categories")
    public ResponseEntity<Set<String>> getAllCategories() {
        logger.debug("REST request to get all feature categories");
        
        Set<String> categories = featureConfigService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    /**
     * Get feature configuration map (for UI initialization)
     */
    @GetMapping("/config")
    @Operation(summary = "Get feature configuration", description = "Get feature configuration map for UI initialization")
    public ResponseEntity<Map<String, Object>> getFeatureConfig() {
        logger.debug("REST request to get feature configuration");
        
        Map<String, Object> config = new HashMap<>();
        
        // Enabled features
        List<String> enabledFeatures = featureConfigService.getEnabledFeatures().stream()
                .map(Enum::name)
                .collect(Collectors.toList());
        config.put("enabledFeatures", enabledFeatures);
        
        // Features by category
        Map<String, List<String>> byCategory = new HashMap<>();
        for (String category : featureConfigService.getAllCategories()) {
            List<String> categoryFeatures = featureConfigService.getEnabledFeaturesByCategory(category).stream()
                    .map(Enum::name)
                    .collect(Collectors.toList());
            byCategory.put(category, categoryFeatures);
        }
        config.put("featuresByCategory", byCategory);
        
        return ResponseEntity.ok(config);
    }
    
    // ============ ADMIN ENDPOINTS ============
    
    /**
     * Enable a feature
     */
    @PostMapping("/{featureName}/enable")
    @Operation(summary = "Enable feature", description = "Enable a specific feature")
    public ResponseEntity<Map<String, String>> enableFeature(
            @Parameter(description = "Feature name") @PathVariable("featureName") String featureName) {
        logger.info("REST request to enable feature: {}", featureName);
        
        try {
            FeatureFlag flag = FeatureFlag.valueOf(featureName.toUpperCase());
            featureConfigService.enableFeature(flag);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Feature enabled successfully");
            response.put("feature", flag.name());
            response.put("displayName", flag.getDisplayName());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Invalid feature name: " + featureName);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Disable a feature
     */
    @PostMapping("/{featureName}/disable")
    @Operation(summary = "Disable feature", description = "Disable a specific feature")
    public ResponseEntity<Map<String, String>> disableFeature(
            @Parameter(description = "Feature name") @PathVariable("featureName") String featureName) {
        logger.info("REST request to disable feature: {}", featureName);
        
        try {
            FeatureFlag flag = FeatureFlag.valueOf(featureName.toUpperCase());
            featureConfigService.disableFeature(flag);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Feature disabled successfully");
            response.put("feature", flag.name());
            response.put("displayName", flag.getDisplayName());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Invalid feature name: " + featureName);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Update feature configuration
     */
    @PutMapping("/{featureName}")
    @Operation(summary = "Update feature config", description = "Update feature configuration")
    public ResponseEntity<FeatureConfig> updateFeatureConfig(
            @Parameter(description = "Feature name") @PathVariable String featureName,
            @RequestBody FeatureConfig updates) {
        logger.info("REST request to update feature config: {}", featureName);
        
        try {
            FeatureFlag flag = FeatureFlag.valueOf(featureName.toUpperCase());
            FeatureConfig updated = featureConfigService.updateFeatureConfig(flag, updates);
            
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
