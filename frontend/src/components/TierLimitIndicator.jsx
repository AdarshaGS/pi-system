import React from 'react';
import { useTier } from '../contexts/TierContext';
import { FaInfoCircle, FaLock } from 'react-icons/fa';
import './TierLimitIndicator.css';

const TierLimitIndicator = ({ feature, currentCount }) => {
  const { checkLimit, limits } = useTier();

  if (!limits) return null;

  const { allowed, limit, current } = checkLimit(feature, currentCount);
  const percentage = (current / limit) * 100;
  const isNearLimit = percentage > 80;
  const isAtLimit = !allowed;

  if (limit === Infinity || limit === 2147483647) {
    return null; // Don't show for unlimited
  }

  return (
    <div className={`tier-limit-indicator ${isAtLimit ? 'at-limit' : isNearLimit ? 'near-limit' : ''}`}>
      <div className="limit-header">
        <span className="limit-icon">
          {isAtLimit ? <FaLock /> : <FaInfoCircle />}
        </span>
        <span className="limit-text">
          {current} / {limit} {feature}
        </span>
      </div>
      
      <div className="limit-progress-bar">
        <div 
          className="limit-progress-fill" 
          style={{ width: `${Math.min(percentage, 100)}%` }}
        />
      </div>
      
      {isAtLimit && (
        <div className="limit-message">
          You've reached your limit. Upgrade to add more!
        </div>
      )}
      
      {isNearLimit && !isAtLimit && (
        <div className="limit-warning">
          You're close to your limit. Consider upgrading.
        </div>
      )}
    </div>
  );
};

export default TierLimitIndicator;
