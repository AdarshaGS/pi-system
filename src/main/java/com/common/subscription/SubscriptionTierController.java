package com.common.subscription;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.common.security.AuthenticationHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for subscription tier management
 */
@RestController
@RequestMapping("/api/v1/subscription")
@Tag(name = "Subscription Tier", description = "APIs for subscription tier information and limits")
public class SubscriptionTierController {
    
    @Autowired
    private SubscriptionTierService subscriptionTierService;
    
    @Autowired
    private AuthenticationHelper authenticationHelper;
    
    /**
     * Get current user's tier and limits
     */
    @GetMapping("/my-tier")
    @Operation(summary = "Get my tier", description = "Get current user's subscription tier and limits")
    public ResponseEntity<TierLimitsDTO> getMyTier() {
        Long userId = authenticationHelper.getCurrentUserId();
        TierLimitsDTO limits = subscriptionTierService.getUserTierLimits(userId);
        return ResponseEntity.ok(limits);
    }
    
    /**
     * Get tier information by user ID
     */
    @GetMapping("/tier/{userId}")
    @Operation(summary = "Get user tier", description = "Get subscription tier and limits for a specific user")
    public ResponseEntity<TierLimitsDTO> getUserTier(@PathVariable Long userId) {
        authenticationHelper.validateUserAccess(userId);
        TierLimitsDTO limits = subscriptionTierService.getUserTierLimits(userId);
        return ResponseEntity.ok(limits);
    }
    
    /**
     * Get user's current usage counts
     */
    @GetMapping("/usage/{userId}")
    @Operation(summary = "Get usage counts", description = "Get current usage counts for tier-limited features")
    public ResponseEntity<Map<String, Object>> getUserUsage(@PathVariable Long userId) {
        authenticationHelper.validateUserAccess(userId);
        
        // This would typically be populated from actual counts
        Map<String, Object> usage = new HashMap<>();
        usage.put("userId", userId);
        usage.put("tier", subscriptionTierService.getUserTier(userId));
        
        // Note: Actual counts would be fetched from respective repositories
        // For now, returning structure for frontend
        usage.put("stocks", Map.of("current", 0, "limit", TierLimits.getMaxStocks(subscriptionTierService.getUserTier(userId))));
        usage.put("budgetCategories", Map.of("current", 0, "limit", TierLimits.getMaxBudgetCategories(subscriptionTierService.getUserTier(userId))));
        usage.put("policies", Map.of("current", 0, "limit", TierLimits.getMaxPolicies(subscriptionTierService.getUserTier(userId))));
        
        return ResponseEntity.ok(usage);
    }
    
    /**
     * Get all tier information (comparison)
     */
    @GetMapping("/tiers")
    @Operation(summary = "Get all tiers", description = "Get information about all available subscription tiers")
    public ResponseEntity<Map<String, Object>> getAllTiers() {
        Map<String, Object> tiers = new HashMap<>();
        
        for (SubscriptionTier tier : SubscriptionTier.values()) {
            Map<String, Object> tierInfo = new HashMap<>();
            tierInfo.put("name", tier.name());
            tierInfo.put("displayName", tier.getDisplayName());
            tierInfo.put("description", tier.getDescription());
            tierInfo.put("maxStocks", TierLimits.getMaxStocks(tier));
            tierInfo.put("maxBudgetCategories", TierLimits.getMaxBudgetCategories(tier));
            tierInfo.put("maxPolicies", TierLimits.getMaxPolicies(tier));
            tierInfo.put("upiAlwaysFree", TierLimits.UPI_ALWAYS_FREE);
            tierInfo.put("loanCalculatorFree", TierLimits.LOAN_CALCULATOR_FREE);
            
            tiers.put(tier.name(), tierInfo);
        }
        
        return ResponseEntity.ok(tiers);
    }
    
    /**
     * Get free tier features
     */
    @GetMapping("/free-features")
    @Operation(summary = "Get free features", description = "Get list of features available in free tier")
    public ResponseEntity<Map<String, Object>> getFreeFeatures() {
        Map<String, Object> features = new HashMap<>();
        
        // Portfolio
        Map<String, Object> portfolio = new HashMap<>();
        portfolio.put("enabled", true);
        portfolio.put("maxStocks", TierLimits.FREE_MAX_STOCKS);
        portfolio.put("features", new String[]{"Manual entry", "Basic P&L calculation", "Daily price updates"});
        features.put("portfolio", portfolio);
        
        // Budget
        Map<String, Object> budget = new HashMap<>();
        budget.put("enabled", true);
        budget.put("maxCategories", TierLimits.FREE_MAX_CATEGORIES);
        budget.put("features", new String[]{"Manual expense entry", "Monthly reports"});
        features.put("budget", budget);
        
        // UPI (Always Free)
        Map<String, Object> upi = new HashMap<>();
        upi.put("enabled", true);
        upi.put("alwaysFree", true);
        upi.put("features", new String[]{"Send/receive money", "Transaction history", "QR payments"});
        features.put("upi", upi);
        
        // Loan Calculator (Always Free)
        Map<String, Object> loan = new HashMap<>();
        loan.put("enabled", true);
        loan.put("alwaysFree", true);
        loan.put("features", new String[]{"EMI calculation", "Amortization schedule"});
        features.put("loanCalculator", loan);
        
        // Insurance
        Map<String, Object> insurance = new HashMap<>();
        insurance.put("enabled", true);
        insurance.put("maxPolicies", TierLimits.FREE_MAX_POLICIES);
        insurance.put("features", new String[]{"Store policies", "Premium reminders"});
        features.put("insurance", insurance);
        
        return ResponseEntity.ok(features);
    }
}
