import React from 'react';
import { useTier } from '../contexts/TierContext';
import { FaCrown, FaLock } from 'react-icons/fa';
import './TierBadge.css';

const TierBadge = ({ showDetails = false }) => {
  const { tier, limits, loading } = useTier();

  if (loading) {
    return <div className="tier-badge loading">Loading...</div>;
  }

  const isFree = tier === 'FREE';

  return (
    <div className={`tier-badge ${isFree ? 'free' : 'premium'}`}>
      <div className="tier-badge-icon">
        {isFree ? <FaLock /> : <FaCrown />}
      </div>
      <div className="tier-badge-text">
        <span className="tier-name">{tier}</span>
        {showDetails && limits && (
          <div className="tier-limits">
            <small>
              Stocks: {limits.maxStocks === 2147483647 ? '∞' : limits.maxStocks} | 
              Categories: {limits.maxBudgetCategories === 2147483647 ? '∞' : limits.maxBudgetCategories} | 
              Policies: {limits.maxPolicies === 2147483647 ? '∞' : limits.maxPolicies}
            </small>
          </div>
        )}
      </div>
    </div>
  );
};

export default TierBadge;
