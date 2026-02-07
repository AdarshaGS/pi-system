import React, { useState, useEffect } from 'react';
import {
  getTaxDetails,
  getRegimeComparison,
  getCapitalGainsSummary,
  getTDSRecords,
  getTaxProjection
} from '../api/taxApi';
import TaxDetailsForm from '../components/TaxDetailsForm';
import RegimeComparison from '../components/RegimeComparison';
import CapitalGainsTracker from '../components/CapitalGainsTracker';
import TDSManagement from '../components/TDSManagement';
import TaxProjection from '../components/TaxProjection';
import './Tax.css';

function Tax() {
  const [userId] = useState(1); // TODO: Get from auth context
  const [activeTab, setActiveTab] = useState('overview');
  const [taxDetails, setTaxDetails] = useState(null);
  const [regimeComparison, setRegimeComparison] = useState(null);
  const [capitalGainsSummary, setCapitalGainsSummary] = useState(null);
  const [tdsRecords, setTDSRecords] = useState([]);
  const [taxProjection, setTaxProjection] = useState(null);
  const [loading, setLoading] = useState(true);
  const [currentFinancialYear] = useState('2025-26');

  useEffect(() => {
    loadTaxData();
  }, [userId]);

  const loadTaxData = async () => {
    setLoading(true);
    try {
      const [details, comparison, cgSummary, tds, projection] = await Promise.all([
        getTaxDetails(userId).catch(() => null),
        getRegimeComparison(userId).catch(() => null),
        getCapitalGainsSummary(userId, currentFinancialYear).catch(() => null),
        getTDSRecords(userId, currentFinancialYear).catch(() => []),
        getTaxProjection(userId, currentFinancialYear).catch(() => null)
      ]);

      setTaxDetails(details);
      setRegimeComparison(comparison);
      setCapitalGainsSummary(cgSummary);
      setTDSRecords(tds);
      setTaxProjection(projection);
    } catch (error) {
      console.error('Error loading tax data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleTaxDetailsUpdate = () => {
    loadTaxData();
  };

  if (loading) {
    return (
      <div className="tax-container">
        <div className="loading-spinner">Loading tax data...</div>
      </div>
    );
  }

  return (
    <div className="tax-container">
      <div className="tax-header">
        <h1>Tax Management</h1>
        <div className="financial-year-badge">FY {currentFinancialYear}</div>
      </div>

      {/* Summary Cards */}
      <div className="tax-summary-cards">
        <div className="summary-card">
          <div className="card-icon">💰</div>
          <div className="card-content">
            <h3>Total Tax Liability</h3>
            <p className="amount">
              ₹{taxProjection?.totalTaxLiability?.toLocaleString() || '0'}
            </p>
            <span className="subtitle">Projected for FY {currentFinancialYear}</span>
          </div>
        </div>

        <div className="summary-card">
          <div className="card-icon">📊</div>
          <div className="card-content">
            <h3>Capital Gains</h3>
            <p className="amount">
              ₹{capitalGainsSummary?.totalGains?.toLocaleString() || '0'}
            </p>
            <span className="subtitle">
              STCG: ₹{capitalGainsSummary?.shortTermGains?.toLocaleString() || '0'} | 
              LTCG: ₹{capitalGainsSummary?.longTermGains?.toLocaleString() || '0'}
            </span>
          </div>
        </div>

        <div className="summary-card">
          <div className="card-icon">🧾</div>
          <div className="card-content">
            <h3>TDS Deducted</h3>
            <p className="amount">
              ₹{tdsRecords?.reduce((sum, tds) => sum + (tds.amount || 0), 0).toLocaleString() || '0'}
            </p>
            <span className="subtitle">{tdsRecords?.length || 0} TDS entries</span>
          </div>
        </div>

        <div className="summary-card">
          <div className="card-icon">
            {regimeComparison?.recommendedRegime === 'OLD' ? '🔵' : '🟢'}
          </div>
          <div className="card-content">
            <h3>Recommended Regime</h3>
            <p className="regime-name">
              {regimeComparison?.recommendedRegime || 'Not Available'}
            </p>
            <span className="subtitle">
              {regimeComparison?.savings 
                ? `Save ₹${regimeComparison.savings.toLocaleString()}`
                : 'Complete tax details'
              }
            </span>
          </div>
        </div>
      </div>

      {/* Tab Navigation */}
      <div className="tax-tabs">
        <button
          className={`tab-button ${activeTab === 'overview' ? 'active' : ''}`}
          onClick={() => setActiveTab('overview')}
        >
          Overview
        </button>
        <button
          className={`tab-button ${activeTab === 'details' ? 'active' : ''}`}
          onClick={() => setActiveTab('details')}
        >
          Tax Details
        </button>
        <button
          className={`tab-button ${activeTab === 'regime' ? 'active' : ''}`}
          onClick={() => setActiveTab('regime')}
        >
          Regime Comparison
        </button>
        <button
          className={`tab-button ${activeTab === 'capital-gains' ? 'active' : ''}`}
          onClick={() => setActiveTab('capital-gains')}
        >
          Capital Gains
        </button>
        <button
          className={`tab-button ${activeTab === 'tds' ? 'active' : ''}`}
          onClick={() => setActiveTab('tds')}
        >
          TDS
        </button>
        <button
          className={`tab-button ${activeTab === 'projection' ? 'active' : ''}`}
          onClick={() => setActiveTab('projection')}
        >
          Projection
        </button>
      </div>

      {/* Tab Content */}
      <div className="tax-content">
        {activeTab === 'overview' && (
          <div className="overview-tab">
            <h2>Tax Overview - FY {currentFinancialYear}</h2>
            
            <div className="overview-section">
              <h3>Quick Stats</h3>
              <div className="stats-grid">
                <div className="stat-item">
                  <label>Gross Total Income</label>
                  <span>₹{taxDetails?.grossTotalIncome?.toLocaleString() || '0'}</span>
                </div>
                <div className="stat-item">
                  <label>Total Deductions</label>
                  <span>₹{taxDetails?.totalDeductions?.toLocaleString() || '0'}</span>
                </div>
                <div className="stat-item">
                  <label>Taxable Income</label>
                  <span>₹{taxDetails?.taxableIncome?.toLocaleString() || '0'}</span>
                </div>
                <div className="stat-item">
                  <label>Selected Regime</label>
                  <span className="regime-badge">{taxDetails?.regime || 'Not Selected'}</span>
                </div>
              </div>
            </div>

            <div className="overview-section">
              <h3>Income Breakdown</h3>
              <div className="income-breakdown">
                <div className="income-item">
                  <span>Salary Income</span>
                  <span>₹{taxDetails?.salaryIncome?.toLocaleString() || '0'}</span>
                </div>
                <div className="income-item">
                  <span>House Property Income</span>
                  <span>₹{taxDetails?.housePropertyIncome?.toLocaleString() || '0'}</span>
                </div>
                <div className="income-item">
                  <span>Business/Profession Income</span>
                  <span>₹{taxDetails?.businessIncome?.toLocaleString() || '0'}</span>
                </div>
                <div className="income-item">
                  <span>Other Sources Income</span>
                  <span>₹{taxDetails?.otherIncome?.toLocaleString() || '0'}</span>
                </div>
              </div>
            </div>

            <div className="overview-actions">
              <button 
                className="btn-primary"
                onClick={() => setActiveTab('details')}
              >
                Update Tax Details
              </button>
              <button 
                className="btn-secondary"
                onClick={() => setActiveTab('regime')}
              >
                Compare Regimes
              </button>
            </div>
          </div>
        )}

        {activeTab === 'details' && (
          <TaxDetailsForm
            userId={userId}
            taxDetails={taxDetails}
            onUpdate={handleTaxDetailsUpdate}
          />
        )}

        {activeTab === 'regime' && (
          <RegimeComparison
            userId={userId}
            comparison={regimeComparison}
            onRefresh={loadTaxData}
          />
        )}

        {activeTab === 'capital-gains' && (
          <CapitalGainsTracker
            userId={userId}
            financialYear={currentFinancialYear}
            summary={capitalGainsSummary}
            onUpdate={loadTaxData}
          />
        )}

        {activeTab === 'tds' && (
          <TDSManagement
            userId={userId}
            financialYear={currentFinancialYear}
            tdsRecords={tdsRecords}
            onUpdate={loadTaxData}
          />
        )}

        {activeTab === 'projection' && (
          <TaxProjection
            userId={userId}
            financialYear={currentFinancialYear}
            projection={taxProjection}
            onRefresh={loadTaxData}
          />
        )}
      </div>
    </div>
  );
}

export default Tax;
