import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../api';

const TierContext = createContext();

export const TierProvider = ({ children }) => {
  const [tier, setTier] = useState(null);
  const [limits, setLimits] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchTierInfo = async () => {
    try {
      setLoading(true);
      const user = JSON.parse(localStorage.getItem('user'));
      
      if (!user || !user.token) {
        setLoading(false);
        return;
      }

      const response = await api.get('/subscription/my-tier', {
        headers: {
          'Authorization': `Bearer ${user.token}`
        }
      });
      
      setTier(response.data.tier);
      setLimits(response.data);
      setError(null);
    } catch (err) {
      console.error('Error fetching tier info:', err);
      setError(err.message);
      // Default to FREE tier on error
      setTier('FREE');
      setLimits({
        tier: 'FREE',
        maxStocks: 20,
        maxBudgetCategories: 5,
        maxPolicies: 2,
        upiAlwaysFree: true,
        loanCalculatorFree: true
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTierInfo();
  }, []);

  const checkLimit = (feature, currentCount) => {
    if (!limits) return { allowed: true, limit: Infinity };
    
    const limitMap = {
      stocks: limits.maxStocks,
      categories: limits.maxBudgetCategories,
      policies: limits.maxPolicies
    };
    
    const limit = limitMap[feature] || Infinity;
    const allowed = currentCount < limit;
    
    return { allowed, limit, current: currentCount };
  };

  const isFree = tier === 'FREE';
  const isPremium = tier === 'PREMIUM' || tier === 'ENTERPRISE';

  return (
    <TierContext.Provider value={{
      tier,
      limits,
      loading,
      error,
      checkLimit,
      isFree,
      isPremium,
      refreshTier: fetchTierInfo
    }}>
      {children}
    </TierContext.Provider>
  );
};

export const useTier = () => {
  const context = useContext(TierContext);
  if (!context) {
    throw new Error('useTier must be used within TierProvider');
  }
  return context;
};

export default TierContext;
