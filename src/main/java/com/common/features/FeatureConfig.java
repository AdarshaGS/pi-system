package com.common.features;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for storing feature configuration in database
 * Allows runtime feature toggle without restart
 */
@Entity
@Table(name = "feature_config")
public class FeatureConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "feature_flag", nullable = false, unique = true, length = 100)
    private FeatureFlag featureFlag;
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    @Column(name = "enabled_for_all", nullable = false)
    private Boolean enabledForAll = true;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "category", length = 50)
    private String category;
    
    @Column(name = "requires_subscription", nullable = false)
    private Boolean requiresSubscription = false;
    
    @Column(name = "min_subscription_tier", length = 50)
    private String minSubscriptionTier; // FREE, BASIC, PREMIUM, ENTERPRISE
    
    @Column(name = "beta_feature", nullable = false)
    private Boolean betaFeature = false;
    
    @Column(name = "enabled_since")
    private LocalDateTime enabledSince;
    
    @Column(name = "disabled_since")
    private LocalDateTime disabledSince;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (enabled && enabledSince == null) {
            enabledSince = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public FeatureFlag getFeatureFlag() {
        return featureFlag;
    }
    
    public void setFeatureFlag(FeatureFlag featureFlag) {
        this.featureFlag = featureFlag;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        // Track when feature was enabled/disabled
        if (this.enabled != null && !this.enabled.equals(enabled)) {
            if (enabled) {
                this.enabledSince = LocalDateTime.now();
                this.disabledSince = null;
            } else {
                this.disabledSince = LocalDateTime.now();
            }
        }
        this.enabled = enabled;
    }
    
    public Boolean getEnabledForAll() {
        return enabledForAll;
    }
    
    public void setEnabledForAll(Boolean enabledForAll) {
        this.enabledForAll = enabledForAll;
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
    
    public Boolean getBetaFeature() {
        return betaFeature;
    }
    
    public void setBetaFeature(Boolean betaFeature) {
        this.betaFeature = betaFeature;
    }
    
    public LocalDateTime getEnabledSince() {
        return enabledSince;
    }
    
    public void setEnabledSince(LocalDateTime enabledSince) {
        this.enabledSince = enabledSince;
    }
    
    public LocalDateTime getDisabledSince() {
        return disabledSince;
    }
    
    public void setDisabledSince(LocalDateTime disabledSince) {
        this.disabledSince = disabledSince;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
