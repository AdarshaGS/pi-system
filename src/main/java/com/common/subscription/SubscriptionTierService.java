package com.common.subscription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.users.data.Users;
import com.users.repo.UsersRepository;

/**
 * Service for managing subscription tier checks and validations
 */
@Service
public class SubscriptionTierService {
    
    @Autowired
    private UsersRepository usersRepository;
    
    /**
     * Get user's current subscription tier
     */
    public SubscriptionTier getUserTier(Long userId) {
        Users user = usersRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        SubscriptionTier tier = user.getSubscriptionTier();
        return tier != null ? tier : SubscriptionTier.FREE; // Default to FREE
    }
    
    /**
     * Check if user can add more stocks
     */
    public void checkStockLimit(Long userId, int currentCount) {
        SubscriptionTier tier = getUserTier(userId);
        int maxAllowed = TierLimits.getMaxStocks(tier);
        
        if (currentCount >= maxAllowed) {
            throw new TierLimitExceededException(tier, "stocks", maxAllowed);
        }
    }
    
    /**
     * Check if user can add more budget categories
     */
    public void checkBudgetCategoryLimit(Long userId, int currentCount) {
        SubscriptionTier tier = getUserTier(userId);
        int maxAllowed = TierLimits.getMaxBudgetCategories(tier);
        
        if (currentCount >= maxAllowed) {
            throw new TierLimitExceededException(tier, "budget categories", maxAllowed);
        }
    }
    
    /**
     * Check if user can add more insurance policies
     */
    public void checkInsurancePolicyLimit(Long userId, int currentCount) {
        SubscriptionTier tier = getUserTier(userId);
        int maxAllowed = TierLimits.getMaxPolicies(tier);
        
        if (currentCount >= maxAllowed) {
            throw new TierLimitExceededException(tier, "insurance policies", maxAllowed);
        }
    }
    
    /**
     * Get tier limits for a user
     */
    public TierLimitsDTO getUserTierLimits(Long userId) {
        SubscriptionTier tier = getUserTier(userId);
        
        TierLimitsDTO limits = new TierLimitsDTO();
        limits.setTier(tier);
        limits.setMaxStocks(TierLimits.getMaxStocks(tier));
        limits.setMaxBudgetCategories(TierLimits.getMaxBudgetCategories(tier));
        limits.setMaxPolicies(TierLimits.getMaxPolicies(tier));
        limits.setUpiAlwaysFree(TierLimits.UPI_ALWAYS_FREE);
        limits.setLoanCalculatorFree(TierLimits.LOAN_CALCULATOR_FREE);
        
        return limits;
    }
}
