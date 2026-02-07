import React, { useState, useEffect, useMemo } from 'react';
import taxApi from '../services/taxApi';
import './TaxDashboard.css';
import {
  Calculator,
  TrendingUp,
  FileText,
  AlertCircle,
  Calendar,
  DollarSign,
  PieChart,
  ArrowRight,
  RefreshCw,
  Download
} from 'lucide-react';

const TaxDashboard = () => {
  const user = JSON.parse(localStorage.getItem('user'));
  const userId = useMemo(() => user?.userId, []);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('overview');
  
  // Financial Year State
  const currentYear = new Date().getFullYear();
  const currentMonth = new Date().getMonth();
  const defaultFY = currentMonth >= 3 ? `${currentYear}-${(currentYear + 1) % 100}` : `${currentYear - 1}-${currentYear % 100}`;
  const [selectedFY, setSelectedFY] = useState(defaultFY);
  
  // Data State
  const [taxDetails, setTaxDetails] = useState(null);
  const [regimeComparison, setRegimeComparison] = useState(null);
  const [capitalGainsSummary, setCapitalGainsSummary] = useState(null);
  const [tdsReconciliation, setTdsReconciliation] = useState(null);
  const [taxProjection, setTaxProjection] = useState(null);
  const [outstandingLiability, setOutstandingLiability] = useState(0);
useEffect(() => {
    if (!userId) return;
    
    const loadDashboardData = async () => {
      try {
        setLoading(true);
        
        // Load tax details
        const taxRes = await taxApi.getTaxDetails(userId, selectedFY);
        setTaxDetails(taxRes);
        
        // Load regime comparison if we have gross income
        if (taxRes?.grossSalary) {
          const regimeRes = await taxApi.compareTaxRegimes(
            userId,
            selectedFY,
            taxRes.grossSalary
          );
          setRegimeComparison(regimeRes);
        }
        
        // Load capital gains summary
        const cgRes = await taxApi.getCapitalGainsSummary(userId, selectedFY);
        setCapitalGainsSummary(cgRes);
        
        // Load TDS reconciliation
        const tdsRes = await taxApi.getTDSReconciliation(userId, selectedFY);
        setTdsReconciliation(tdsRes);
        
        // Load tax projection
        const projRes = await taxApi.getTaxProjection(userId, selectedFY);
        setTaxProjection(projRes);
        
        // Load outstanding liability
        const liabilityRes = await taxApi.getOutstandingLiability(userId);
        setOutstandingLiability(liabilityRes);
        
      } catch (error) {
        console.error('Error loading tax dashboard:', error);
      } finally {
        setLoading(false);
      }
    };
    
    loadDashboardData();
  }, [userId, selectedFY]);

  const handleAutoPopulate = async () => {
    if (!userId) return;
    
    try {
      setLoading(true);
      await taxApi.autoPopulateAll(userId, selectedFY);
      // Reload data after auto-populate
      window.location.reload();
    } catch (error) {
      console.error('Error auto-populating:', error);
      alert('Failed to auto-populate tax data');
    } finally {
      setLoading(false);
    }
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

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0
    }).format(amount || 0);
  };

  if (loading && !taxDetails) {
    return (
      <div className="tax-dashboard">
        <div className="loading-spinner">
          <RefreshCw className="spin" size={48} />
          <p>Loading tax dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="tax-dashboard">
      {/* Header */}
      <div className="dashboard-header">
        <div className="header-left">
          <h1>
            <Calculator size={32} />
            Tax Management
          </h1>
          <p className="subtitle">Comprehensive tax planning and ITR filing assistant</p>
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
          <button className="btn-secondary" onClick={handleAutoPopulate}>
            <RefreshCw size={18} />
            Auto-Populate
          </button>
        </div>
      </div>

      {/* Quick Stats */}
      <div className="quick-stats">
        <div className="stat-card highlight">
          <div className="stat-icon" style={{background: '#dc2626'}}>
            <AlertCircle size={24} />
          </div>
          <div className="stat-content">
            <span className="stat-label">Outstanding Tax</span>
            <span className="stat-value">{formatCurrency(outstandingLiability)}</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon" style={{background: '#2563eb'}}>
            <DollarSign size={24} />
          </div>
          <div className="stat-content">
            <span className="stat-label">Total Tax Payable</span>
            <span className="stat-value">{formatCurrency(taxDetails?.taxPayable)}</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon" style={{background: '#059669'}}>
            <TrendingUp size={24} />
          </div>
          <div className="stat-content">
            <span className="stat-label">Tax Already Paid</span>
            <span className="stat-value">{formatCurrency(taxDetails?.taxPaid)}</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon" style={{background: '#7c3aed'}}>
            <PieChart size={24} />
          </div>
          <div className="stat-content">
            <span className="stat-label">Capital Gains</span>
            <span className="stat-value">
              {formatCurrency((capitalGainsSummary?.totalSTCG || 0) + (capitalGainsSummary?.totalLTCG || 0))}
            </span>
          </div>
        </div>
      </div>

      {/* Tab Navigation */}
      <div className="tab-navigation">
        <button 
          className={activeTab === 'overview' ? 'active' : ''}
          onClick={() => setActiveTab('overview')}
        >
          Overview
        </button>
        <button 
          className={activeTab === 'regime' ? 'active' : ''}
          onClick={() => setActiveTab('regime')}
        >
          Regime Comparison
        </button>
        <button 
          className={activeTab === 'breakdown' ? 'active' : ''}
          onClick={() => setActiveTab('breakdown')}
        >
          Income Breakdown
        </button>
        <button 
          className={activeTab === 'projections' ? 'active' : ''}
          onClick={() => setActiveTab('projections')}
        >
          Projections
        </button>
      </div>

      {/* Tab Content */}
      <div className="tab-content">
        {activeTab === 'overview' && (
          <div className="overview-grid">
            {/* Income Summary */}
            <div className="info-card">
              <h3>Income Summary</h3>
              <div className="income-breakdown">
                <div className="income-item">
                  <span>Gross Salary</span>
                  <span className="amount">{formatCurrency(taxDetails?.grossSalary)}</span>
                </div>
                <div className="income-item">
                  <span>Business Income</span>
                  <span className="amount">{formatCurrency(taxDetails?.businessIncome)}</span>
                </div>
                <div className="income-item">
                  <span>Capital Gains (STCG)</span>
                  <span className="amount">{formatCurrency(taxDetails?.capitalGainsShortTerm)}</span>
                </div>
                <div className="income-item">
                  <span>Capital Gains (LTCG)</span>
                  <span className="amount">{formatCurrency(taxDetails?.capitalGainsLongTerm)}</span>
                </div>
                <div className="income-item">
                  <span>Other Income</span>
                  <span className="amount">{formatCurrency(taxDetails?.otherIncome)}</span>
                </div>
                <div className="income-item total">
                  <span><strong>Total Income</strong></span>
                  <span className="amount">
                    <strong>
                      {formatCurrency(
                        (taxDetails?.grossSalary || 0) +
                        (taxDetails?.businessIncome || 0) +
                        (taxDetails?.capitalGainsShortTerm || 0) +
                        (taxDetails?.capitalGainsLongTerm || 0) +
                        (taxDetails?.otherIncome || 0)
                      )}
                    </strong>
                  </span>
                </div>
              </div>
            </div>

            {/* Deductions Summary */}
            <div className="info-card">
              <h3>Deductions Summary</h3>
              <div className="deductions-breakdown">
                <div className="deduction-item">
                  <span>Section 80C</span>
                  <span className="amount">{formatCurrency(taxDetails?.section80CDeductions)}</span>
                </div>
                <div className="deduction-item">
                  <span>Section 80D</span>
                  <span className="amount">{formatCurrency(taxDetails?.section80DDeductions)}</span>
                </div>
                <div className="deduction-item">
                  <span>Other Deductions</span>
                  <span className="amount">{formatCurrency(taxDetails?.otherDeductions)}</span>
                </div>
                <div className="deduction-item total">
                  <span><strong>Total Deductions</strong></span>
                  <span className="amount">
                    <strong>
                      {formatCurrency(
                        (taxDetails?.section80CDeductions || 0) +
                        (taxDetails?.section80DDeductions || 0) +
                        (taxDetails?.otherDeductions || 0)
                      )}
                    </strong>
                  </span>
                </div>
              </div>
            </div>

            {/* Capital Gains Summary */}
            {capitalGainsSummary && (
              <div className="info-card">
                <h3>Capital Gains Summary</h3>
                <div className="cg-summary">
                  <div className="cg-section">
                    <h4>Short Term (STCG)</h4>
                    <div className="cg-details">
                      <span>Total Gains</span>
                      <span className="amount">{formatCurrency(capitalGainsSummary.totalSTCG)}</span>
                    </div>
                    <div className="cg-details">
                      <span>Tax @ 15%</span>
                      <span className="amount">{formatCurrency(capitalGainsSummary.totalSTCGTax)}</span>
                    </div>
                  </div>
                  <div className="cg-section">
                    <h4>Long Term (LTCG)</h4>
                    <div className="cg-details">
                      <span>Total Gains</span>
                      <span className="amount">{formatCurrency(capitalGainsSummary.totalLTCG)}</span>
                    </div>
                    <div className="cg-details">
                      <span>Exemption (₹1L)</span>
                      <span className="amount success">{formatCurrency(capitalGainsSummary.ltcgExemption)}</span>
                    </div>
                    <div className="cg-details">
                      <span>Tax @ 10%</span>
                      <span className="amount">{formatCurrency(capitalGainsSummary.totalLTCGTax)}</span>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* TDS Summary */}
            {tdsReconciliation && (
              <div className="info-card">
                <h3>TDS Reconciliation</h3>
                <div className="tds-summary">
                  <div className="tds-stat">
                    <span>Total TDS Claimed</span>
                    <span className="amount">{formatCurrency(tdsReconciliation.totalTDSClaimed)}</span>
                  </div>
                  <div className="tds-stat">
                    <span>Matched with 26AS</span>
                    <span className="amount success">{formatCurrency(tdsReconciliation.totalTDSMatched)}</span>
                  </div>
                  <div className="tds-stat">
                    <span>Mismatch</span>
                    <span className="amount danger">{formatCurrency(tdsReconciliation.totalMismatch)}</span>
                  </div>
                  <div className="tds-stat">
                    <span>Status</span>
                    <span className={`badge ${tdsReconciliation.reconciliationStatus?.toLowerCase()}`}>
                      {tdsReconciliation.reconciliationStatus}
                    </span>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {activeTab === 'regime' && regimeComparison && (
          <div className="regime-comparison">
            <div className="comparison-header">
              <h3>Tax Regime Comparison for FY {selectedFY}</h3>
              <p>Compare Old vs New Tax Regime to optimize your tax liability</p>
            </div>

            <div className="comparison-grid">
              <div className="regime-card old-regime">
                <h4>Old Regime</h4>
                <div className="regime-details">
                  <div className="detail-row">
                    <span>Taxable Income</span>
                    <span>{formatCurrency(regimeComparison.oldRegimeTaxableIncome)}</span>
                  </div>
                  <div className="detail-row">
                    <span>Tax Calculated</span>
                    <span>{formatCurrency(regimeComparison.oldRegimeTax)}</span>
                  </div>
                  <div className="detail-row">
                    <span>Health & Education Cess (4%)</span>
                    <span>{formatCurrency(regimeComparison.oldRegimeTax * 0.04)}</span>
                  </div>
                  <div className="detail-row total">
                    <span><strong>Total Tax</strong></span>
                    <span><strong>{formatCurrency(regimeComparison.oldRegimeTotalTax)}</strong></span>
                  </div>
                  <div className="detail-row">
                    <span>Effective Rate</span>
                    <span>{regimeComparison.oldRegimeEffectiveRate?.toFixed(2)}%</span>
                  </div>
                </div>
              </div>

              <div className="regime-card new-regime">
                <h4>New Regime</h4>
                <div className="regime-details">
                  <div className="detail-row">
                    <span>Taxable Income</span>
                    <span>{formatCurrency(regimeComparison.newRegimeTaxableIncome)}</span>
                  </div>
                  <div className="detail-row">
                    <span>Tax Calculated</span>
                    <span>{formatCurrency(regimeComparison.newRegimeTax)}</span>
                  </div>
                  <div className="detail-row">
                    <span>Health & Education Cess (4%)</span>
                    <span>{formatCurrency(regimeComparison.newRegimeTax * 0.04)}</span>
                  </div>
                  <div className="detail-row total">
                    <span><strong>Total Tax</strong></span>
                    <span><strong>{formatCurrency(regimeComparison.newRegimeTotalTax)}</strong></span>
                  </div>
                  <div className="detail-row">
                    <span>Effective Rate</span>
                    <span>{regimeComparison.newRegimeEffectiveRate?.toFixed(2)}%</span>
                  </div>
                </div>
              </div>
            </div>

            <div className="recommendation-box">
              <div className="recommendation-header">
                <div className={`recommendation-badge ${regimeComparison.recommendedRegime === 'OLD' ? 'old' : 'new'}`}>
                  Recommended: {regimeComparison.recommendedRegime} Regime
                </div>
                <div className="savings">
                  {regimeComparison.taxSavings > 0 ? (
                    <>
                      <TrendingUp className="success" size={24} />
                      <span className="success">Save {formatCurrency(Math.abs(regimeComparison.taxSavings))}</span>
                    </>
                  ) : (
                    <>
                      <AlertCircle className="danger" size={24} />
                      <span className="danger">Extra {formatCurrency(Math.abs(regimeComparison.taxSavings))}</span>
                    </>
                  )}
                </div>
              </div>
              <p className="recommendation-text">{regimeComparison.recommendation}</p>
            </div>
          </div>
        )}

        {activeTab === 'breakdown' && (
          <div className="breakdown-view">
            <p className="placeholder-text">
              Detailed income breakdown will be displayed here. 
              Navigate to Income Entry forms to add more details.
            </p>
          </div>
        )}

        {activeTab === 'projections' && taxProjection && (
          <div className="projections-view">
            <div className="projection-summary">
              <h3>Tax Projection for FY {selectedFY}</h3>
              <div className="projection-stats">
                <div className="proj-stat">
                  <span>Projected Tax Liability</span>
                  <span className="amount">{formatCurrency(taxProjection.projectedTaxLiability)}</span>
                </div>
                <div className="proj-stat">
                  <span>Advance Tax Paid</span>
                  <span className="amount success">{formatCurrency(taxProjection.advanceTaxPaid)}</span>
                </div>
                <div className="proj-stat">
                  <span>Remaining Liability</span>
                  <span className="amount danger">{formatCurrency(taxProjection.remainingTaxLiability)}</span>
                </div>
                {taxProjection.nextAdvanceTaxDue && (
                  <div className="proj-stat">
                    <span>Next Due Date</span>
                    <span className="date">
                      <Calendar size={16} />
                      {new Date(taxProjection.nextAdvanceTaxDue).toLocaleDateString()}
                    </span>
                  </div>
                )}
              </div>
              {taxProjection.recommendedMonthlyInvestment > 0 && (
                <div className="recommendation-banner">
                  <AlertCircle size={20} />
                  <span>
                    Recommended monthly investment: <strong>{formatCurrency(taxProjection.recommendedMonthlyInvestment)}</strong> 
                    {' '}to optimize your tax savings
                  </span>
                </div>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Quick Actions */}
      <div className="quick-actions">
        <h3>Quick Actions</h3>
        <div className="actions-grid">
          <button className="action-card" onClick={() => window.location.href = '/tax/income'}>
            <FileText size={32} />
            <span>Add Income</span>
            <ArrowRight size={16} />
          </button>
          <button className="action-card" onClick={() => window.location.href = '/tax/deductions'}>
            <Calculator size={32} />
            <span>Add Deductions</span>
            <ArrowRight size={16} />
          </button>
          <button className="action-card" onClick={() => window.location.href = '/tax/capital-gains'}>
            <TrendingUp size={32} />
            <span>Capital Gains</span>
            <ArrowRight size={16} />
          </button>
          <button className="action-card" onClick={() => window.location.href = '/tax/tds'}>
            <DollarSign size={32} />
            <span>Manage TDS</span>
            <ArrowRight size={16} />
          </button>
          <button className="action-card" onClick={() => window.location.href = '/tax/planning'}>
            <PieChart size={32} />
            <span>Tax Planning</span>
            <ArrowRight size={16} />
          </button>
          <button className="action-card" onClick={() => window.location.href = '/tax/itr'}>
            <Download size={32} />
            <span>Export ITR</span>
            <ArrowRight size={16} />
          </button>
        </div>
      </div>
    </div>
  );
};

export default TaxDashboard;
