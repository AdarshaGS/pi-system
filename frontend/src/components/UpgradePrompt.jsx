import React from 'react';
import { useTier } from '../contexts/TierContext';
import { FaCrown, FaTimes, FaCheck } from 'react-icons/fa';
import './UpgradePrompt.css';

const UpgradePrompt = ({ show, onClose, feature, limit }) => {
  const { tier } = useTier();

  if (!show) return null;

  const freeFeatures = [
    'Track up to 20 stocks',
    'Manual entry',
    'Basic P&L calculation',
    'Daily price updates',
    '5 budget categories',
    'Monthly reports',
    'UPI payments (Always Free!)',
    'Basic loan calculator',
    'Store 2 insurance policies'
  ];

  const premiumFeatures = [
    'Unlimited stocks tracking',
    'Real-time price updates',
    'Advanced analytics',
    'Unlimited budget categories',
    'AI-powered insights',
    'Advanced tax planning',
    'Unlimited insurance policies',
    'Priority support',
    'Export to Excel/PDF',
    'Custom reports'
  ];

  return (
    <div className="upgrade-overlay">
      <div className="upgrade-modal">
        <button className="upgrade-close" onClick={onClose}>
          <FaTimes />
        </button>
        
        <div className="upgrade-header">
          <FaCrown className="upgrade-crown" />
          <h2>Upgrade to Premium</h2>
          <p className="upgrade-message">
            You've reached the limit of {limit} {feature} on the {tier} plan.
          </p>
        </div>

        <div className="upgrade-comparison">
          <div className="upgrade-plan current-plan">
            <h3>FREE Plan</h3>
            <p className="plan-subtitle">Your Current Plan</p>
            <ul className="feature-list">
              {freeFeatures.map((feature, index) => (
                <li key={index}>
                  <FaCheck className="check-icon free" />
                  <span>{feature}</span>
                </li>
              ))}
            </ul>
          </div>

          <div className="upgrade-plan premium-plan">
            <h3>PREMIUM Plan</h3>
            <p className="plan-subtitle">Unlock Everything</p>
            <div className="plan-price">
              <span className="price">₹499</span>
              <span className="period">/month</span>
            </div>
            <ul className="feature-list">
              {premiumFeatures.map((feature, index) => (
                <li key={index}>
                  <FaCheck className="check-icon premium" />
                  <span>{feature}</span>
                </li>
              ))}
            </ul>
            <button className="upgrade-button">
              Upgrade Now
            </button>
          </div>
        </div>

        <div className="upgrade-footer">
          <p>💳 Cancel anytime • 📞 24/7 support • 🔒 Secure payments</p>
        </div>
      </div>
    </div>
  );
};

export default UpgradePrompt;
