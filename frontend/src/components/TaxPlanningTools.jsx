import React, { useState, useEffect, useMemo } from 'react';
import taxApi from '../services/taxApi';
import './TaxPlanningTools.css';
import {
  PieChart,
  TrendingUp,
  Calendar,
  Lightbulb,
  Target,
  DollarSign,
  AlertCircle,
  CheckCircle
} from 'lucide-react';

const TaxPlanningTools = () => {
  const user = JSON.parse(localStorage.getItem('user'));
  const userId = useMemo(() => user?.userId, []);
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('projection');
  
  // Financial Year
  const currentYear = new Date().getFullYear();
  const currentMonth = new Date().getMonth();
  const defaultFY = currentMonth >= 3 ? `${currentYear}-${(currentYear + 1) % 100}` : `${currentYear - 1}-${currentYear % 100}`;
  const [selectedFY, setSelectedFY] = useState(defaultFY);
  
  // Data State
  const [projection, setProjection] = useState(null);
  const [recommendations, setRecommendations] = useState([]);
  const [regimeComparison, setRegimeComparison] = useState(null);
  
  // Scenario Calculator State
  const [scenarioIncome, setScenarioIncome] = useState('');
  const [scenario80C, setScenario80C] = useState('');
  const [scenario80D, setScenario80D] = useState('');
  const [scenarioResult, setScenarioResult] = useState(null);

  useEffect(() => {
    if (user) {
      loadPlanningData();
    }
  }, [selectedFY]);

  const loadPlanningData = async () => {
    try {
      setLoading(true);
      
      const [projRes, recoRes] = await Promise.all([
        taxApi.getTaxProjection(userId, selectedFY),
        taxApi.getTaxSavingRecommendations(userId, selectedFY)
      ]);
      
      setProjection(projRes);
      setRecommendations(Array.isArray(recoRes) ? recoRes : []);
      
      // Load regime comparison if we have projection data
      if (projRes?.estimatedIncome) {
        const regimeRes = await taxApi.compareTaxRegimes(
          userId,
          selectedFY,
          projRes.estimatedIncome
        );
        setRegimeComparison(regimeRes);
      }
      
    } catch (error) {
      console.error('Error loading planning data:', error);
    } finally {
      setLoading(false);
    }
  };

  const calculateScenario = async () => {
    if (!scenarioIncome) {
      alert('Please enter income amount');
      return;
    }
    
    try {
      setLoading(true);
      
      const response = await taxApi.compareTaxRegimes(
        userId,
        selectedFY,
        parseFloat(scenarioIncome),
        parseFloat(scenario80C) || 0,
        parseFloat(scenario80D) || 0
      );
      
      setScenarioResult(response);
      
    } catch (error) {
      console.error('Error calculating scenario:', error);
      alert('Failed to calculate scenario');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0
    }).format(amount || 0);
  };

  const generateFinancialYears = () => {
    const years = [];
    for (let i = 0; i < 5; i++) {
      const year = currentYear - i;
      const fy = currentMonth >= 3 ? `${year}-${(year + 1) % 100}` : `${year - 1}-${year % 100}`;
      years.push(fy);
    }
    return years;
  };

  const advanceTaxDates = [
    { quarter: 'Q1', date: '15-Jun', percentage: 15, label: 'First Installment' },
    { quarter: 'Q2', date: '15-Sep', percentage: 45, label: 'Second Installment (cumulative 45%)' },
    { quarter: 'Q3', date: '15-Dec', percentage: 75, label: 'Third Installment (cumulative 75%)' },
    { quarter: 'Q4', date: '15-Mar', percentage: 100, label: 'Fourth Installment (full)' }
  ];

  return (
    <div className="tax-planning-tools">
      {/* Header */}
      <div className="planning-header">
        <div className="header-left">
          <h1>
            <PieChart size={32} />
            Tax Planning Tools
          </h1>
          <p className="subtitle">Optimize your tax liability with smart planning</p>
        </div>
        <div className="header-actions">
          <select 
            value={selectedFY} 
            onChange={(e) => setSelectedFY(e.target.value)}
            className="fy-selector"
          >
            {generateFinancialYears().map(fy => (
              <option key={fy} value={fy}>FY {fy}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Tabs */}
      <div className="planning-tabs">
        <button 
          className={activeTab === 'projection' ? 'active' : ''}
          onClick={() => setActiveTab('projection')}
        >
          <TrendingUp size={18} />
          Tax Projection
        </button>
        <button 
          className={activeTab === 'recommendations' ? 'active' : ''}
          onClick={() => setActiveTab('recommendations')}
        >
          <Lightbulb size={18} />
          Recommendations
        </button>
        <button 
          className={activeTab === 'advance-tax' ? 'active' : ''}
          onClick={() => setActiveTab('advance-tax')}
        >
          <Calendar size={18} />
          Advance Tax Calendar
        </button>
        <button 
          className={activeTab === 'scenario' ? 'active' : ''}
          onClick={() => setActiveTab('scenario')}
        >
          <Target size={18} />
          What-If Scenarios
        </button>
      </div>

      {/* Tax Projection Tab */}
      {activeTab === 'projection' && projection && (
        <div className="planning-content">
          <div className="projection-grid">
            <div className="projection-card">
              <h3>Tax Liability Projection</h3>
              <div className="projection-stats">
                <div className="stat-item">
                  <span className="stat-label">Estimated Annual Income</span>
                  <span className="stat-value">{formatCurrency(projection.estimatedIncome)}</span>
                </div>
                <div className="stat-item">
                  <span className="stat-label">Expected Deductions</span>
                  <span className="stat-value success">{formatCurrency(projection.expectedDeductions)}</span>
                </div>
                <div className="stat-item">
                  <span className="stat-label">Projected Tax Liability</span>
                  <span className="stat-value danger">{formatCurrency(projection.projectedTaxLiability)}</span>
                </div>
                <div className="stat-item">
                  <span className="stat-label">Advance Tax Paid</span>
                  <span className="stat-value">{formatCurrency(projection.advanceTaxPaid)}</span>
                </div>
                <div className="stat-item highlight">
                  <span className="stat-label">Remaining Liability</span>
                  <span className="stat-value">{formatCurrency(projection.remainingTaxLiability)}</span>
                </div>
              </div>
            </div>

            <div className="projection-card">
              <h3>Investment Planning</h3>
              <div className="investment-planning">
                {projection.recommendedMonthlyInvestment > 0 && (
                  <div className="planning-alert info">
                    <Lightbulb size={20} />
                    <div>
                      <strong>Recommended Monthly Investment</strong>
                      <p>Invest {formatCurrency(projection.recommendedMonthlyInvestment)} per month to maximize tax savings</p>
                    </div>
                  </div>
                )}
                
                <div className="gap-analysis">
                  <h4>Gap Analysis</h4>
                  <div className="gap-items">
                    <div className="gap-item">
                      <span>80C Utilization</span>
                      <div className="progress-bar">
                        <div 
                          className="progress-fill" 
                          style={{width: `${Math.min(((projection.section80CUsed || 0) / 150000) * 100, 100)}%`}}
                        ></div>
                      </div>
                      <span className="gap-value">
                        {formatCurrency(projection.section80CUsed)} / ₹1.5L
                      </span>
                    </div>
                    <div className="gap-item">
                      <span>80D Utilization</span>
                      <div className="progress-bar">
                        <div 
                          className="progress-fill" 
                          style={{width: `${Math.min(((projection.section80DUsed || 0) / 75000) * 100, 100)}%`}}
                        ></div>
                      </div>
                      <span className="gap-value">
                        {formatCurrency(projection.section80DUsed)} / ₹75K
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {regimeComparison && (
            <div className="regime-comparison-card">
              <h3>Tax Regime Recommendation</h3>
              <div className="comparison-boxes">
                <div className="regime-box old">
                  <h4>Old Regime</h4>
                  <div className="regime-value">{formatCurrency(regimeComparison.oldRegimeTotalTax)}</div>
                  <div className="regime-rate">Effective: {regimeComparison.oldRegimeEffectiveRate?.toFixed(2)}%</div>
                </div>
                <div className="comparison-arrow">
                  <TrendingUp size={32} />
                </div>
                <div className="regime-box new">
                  <h4>New Regime</h4>
                  <div className="regime-value">{formatCurrency(regimeComparison.newRegimeTotalTax)}</div>
                  <div className="regime-rate">Effective: {regimeComparison.newRegimeEffectiveRate?.toFixed(2)}%</div>
                </div>
              </div>
              <div className="recommendation-banner">
                <CheckCircle size={20} />
                <span><strong>Recommendation:</strong> {regimeComparison.recommendation}</span>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Recommendations Tab */}
      {activeTab === 'recommendations' && (
        <div className="planning-content">
          <div className="recommendations-list">
            {recommendations.length === 0 ? (
              <div className="empty-state">
                <Lightbulb size={48} />
                <p>No recommendations available yet</p>
                <p className="small">Complete your income and deduction details to get personalized recommendations</p>
              </div>
            ) : (
              recommendations.map((reco, index) => (
                <div key={index} className={`recommendation-card ${reco.priority?.toLowerCase()}`}>
                  <div className="reco-header">
                    <div className="reco-icon">
                      <Lightbulb size={24} />
                    </div>
                    <div className="reco-title">
                      <h4>{reco.title}</h4>
                      <span className={`priority-badge ${reco.priority?.toLowerCase()}`}>
                        {reco.priority}
                      </span>
                    </div>
                  </div>
                  <p className="reco-description">{reco.description}</p>
                  <div className="reco-savings">
                    <DollarSign size={18} />
                    <span>Potential Savings: {formatCurrency(reco.potentialSavings)}</span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      )}

      {/* Advance Tax Calendar Tab */}
      {activeTab === 'advance-tax' && projection && (
        <div className="planning-content">
          <div className="advance-tax-calendar">
            <h3>Advance Tax Payment Schedule for FY {selectedFY}</h3>
            <p className="calendar-desc">
              If your tax liability exceeds ₹10,000, you must pay advance tax in four installments
            </p>
            
            <div className="tax-installments">
              {advanceTaxDates.map((installment, index) => {
                const dueAmount = (projection.projectedTaxLiability || 0) * (installment.percentage / 100);
                const isPast = new Date() > new Date(`${installment.date}-${currentYear}`);
                
                return (
                  <div key={index} className={`installment-card ${isPast ? 'past' : 'upcoming'}`}>
                    <div className="installment-header">
                      <Calendar size={24} />
                      <div className="installment-date">
                        <strong>{installment.date}</strong>
                        <span>{installment.quarter}</span>
                      </div>
                    </div>
                    <div className="installment-details">
                      <span className="installment-label">{installment.label}</span>
                      <span className="installment-percentage">{installment.percentage}%</span>
                      <span className="installment-amount">{formatCurrency(dueAmount)}</span>
                    </div>
                    {isPast && (
                      <div className="installment-status past-status">
                        <AlertCircle size={16} />
                        <span>Past Due</span>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>

            <div className="calendar-notes">
              <h4>Important Notes:</h4>
              <ul>
                <li>Interest is charged @ 1% per month for late or short payment</li>
                <li>No advance tax required if total tax liability is less than ₹10,000</li>
                <li>Senior citizens (60+ years) with no business income are exempt from advance tax</li>
                <li>Payment can be made online through income tax e-filing portal</li>
              </ul>
            </div>
          </div>
        </div>
      )}

      {/* What-If Scenarios Tab */}
      {activeTab === 'scenario' && (
        <div className="planning-content">
          <div className="scenario-calculator">
            <h3>What-If Scenario Calculator</h3>
            <p className="scenario-desc">
              Test different income and investment scenarios to find the optimal tax strategy
            </p>
            
            <div className="scenario-form">
              <div className="form-row">
                <div className="form-field">
                  <label>Annual Income</label>
                  <input
                    type="number"
                    value={scenarioIncome}
                    onChange={(e) => setScenarioIncome(e.target.value)}
                    placeholder="Enter annual income"
                  />
                </div>
                <div className="form-field">
                  <label>80C Investments</label>
                  <input
                    type="number"
                    value={scenario80C}
                    onChange={(e) => setScenario80C(e.target.value)}
                    placeholder="Max ₹1.5L"
                  />
                </div>
                <div className="form-field">
                  <label>80D Investments</label>
                  <input
                    type="number"
                    value={scenario80D}
                    onChange={(e) => setScenario80D(e.target.value)}
                    placeholder="Max ₹75K"
                  />
                </div>
              </div>
              
              <button className="btn-primary" onClick={calculateScenario} disabled={loading}>
                <Target size={18} />
                {loading ? 'Calculating...' : 'Calculate Tax'}
              </button>
            </div>

            {scenarioResult && (
              <div className="scenario-result">
                <h4>Scenario Results</h4>
                <div className="result-boxes">
                  <div className="result-box">
                    <h5>Old Regime</h5>
                    <div className="result-value">{formatCurrency(scenarioResult.oldRegimeTotalTax)}</div>
                    <div className="result-detail">
                      Effective Rate: {scenarioResult.oldRegimeEffectiveRate?.toFixed(2)}%
                    </div>
                    <div className="result-detail">
                      After Deductions: {formatCurrency(scenarioResult.oldRegimeTaxableIncome)}
                    </div>
                  </div>
                  
                  <div className="result-box">
                    <h5>New Regime</h5>
                    <div className="result-value">{formatCurrency(scenarioResult.newRegimeTotalTax)}</div>
                    <div className="result-detail">
                      Effective Rate: {scenarioResult.newRegimeEffectiveRate?.toFixed(2)}%
                    </div>
                    <div className="result-detail">
                      Taxable Income: {formatCurrency(scenarioResult.newRegimeTaxableIncome)}
                    </div>
                  </div>
                  
                  <div className="result-box savings">
                    <h5>Tax Savings</h5>
                    <div className="result-value">
                      {formatCurrency(Math.abs(scenarioResult.taxSavings))}
                    </div>
                    <div className="result-detail">
                      Choose: <strong>{scenarioResult.recommendedRegime}</strong>
                    </div>
                  </div>
                </div>
                <div className="scenario-recommendation">
                  <CheckCircle size={20} />
                  <span>{scenarioResult.recommendation}</span>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default TaxPlanningTools;
