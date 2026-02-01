package com.common.features;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DTO for feature information exposed to UI
 */
public class FeatureDTO {
    
    private String name;
    private String displayName;
    private String description;
    private String category;
    private Boolean enabled;
    private Boolean betaFeature;
    private Boolean requiresSubscription;
    private String minSubscriptionTier;
    
    public FeatureDTO() {
    }
    
    public FeatureDTO(FeatureFlag flag, boolean enabled) {
        this.name = flag.name();
        this.displayName = flag.getDisplayName();
        this.description = flag.getDescription();
        this.category = flag.getCategory();
        this.enabled = enabled;
        this.betaFeature = false;
        this.requiresSubscription = false;
    }
    
    public FeatureDTO(FeatureFlag flag, FeatureConfig config) {
        this.name = flag.name();
        this.displayName = flag.getDisplayName();
        this.description = config.getDescription() != null ? config.getDescription() : flag.getDescription();
        this.category = config.getCategory() != null ? config.getCategory() : flag.getCategory();
        this.enabled = config.getEnabled();
        this.betaFeature = config.getBetaFeature();
        this.requiresSubscription = config.getRequiresSubscription();
        this.minSubscriptionTier = config.getMinSubscriptionTier();
    }
    
    // Getters and Setters
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public Boolean getBetaFeature() {
        return betaFeature;
    }
    
    public void setBetaFeature(Boolean betaFeature) {
        this.betaFeature = betaFeature;
    }
    
    public Boolean getRequiresSubscription() {
        return requiresSubscription;
    }
    
    public void setRequiresSubscription(Boolean requiresSubscription) {
        this.requiresSubscription = requiresSubscription;
    }
    
    public String getMinSubscriptionTier() {
        return minSubscriptionTier;
    }
    
    public void setMinSubscriptionTier(String minSubscriptionTier) {
        this.minSubscriptionTier = minSubscriptionTier;
    }
}
