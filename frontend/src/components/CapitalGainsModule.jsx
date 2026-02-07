import React, { useState, useEffect, useMemo } from 'react';
import taxApi from '../services/taxApi';
import './CapitalGainsModule.css';
import {
  TrendingUp,
  TrendingDown,
  Plus,
  Filter,
  Download,
  Edit2,
  Trash2,
  Calendar,
  DollarSign,
  Calculator
} from 'lucide-react';

const CapitalGainsModule = () => {
  const user = JSON.parse(localStorage.getItem('user'));
  const userId = useMemo(() => user?.userId, []);
  const [loading, setLoading] = useState(false);
  const [showAddModal, setShowAddModal] = useState(false);
  const [showCalculator, setShowCalculator] = useState(false);
  
  // Financial Year
  const currentYear = new Date().getFullYear();
  const currentMonth = new Date().getMonth();
  const defaultFY = currentMonth >= 3 ? `${currentYear}-${(currentYear + 1) % 100}` : `${currentYear - 1}-${currentYear % 100}`;
  const [selectedFY, setSelectedFY] = useState(defaultFY);
  
  // Data State
  const [transactions, setTransactions] = useState([]);
  const [summary, setSummary] = useState(null);
  const [filterAssetType, setFilterAssetType] = useState('ALL');
  const [filterGainType, setFilterGainType] = useState('ALL');
  
  // New Transaction State
  const [newTransaction, setNewTransaction] = useState({
    assetType: 'EQUITY',
    purchaseDate: '',
    saleDate: '',
    purchasePrice: '',
    salePrice: '',
    quantity: '1',
    expenses: '',
    description: ''
  });

  // Calculator State
  const [calculatorData, setCalculatorData] = useState({
    assetType: 'EQUITY',
    purchaseDate: '',
    saleDate: '',
    purchasePrice: '',
    salePrice: '',
    expenses: ''
  });
  const [calculatorResult, setCalculatorResult] = useState(null);

  useEffect(() => {
    if (user) {
      loadCapitalGains();
    }
  }, [selectedFY]);

  const loadCapitalGains = async () => {
    try {
      setLoading(true);
      
      const [txnRes, summaryRes] = await Promise.all([
        taxApi.getCapitalGainsTransactions(userId, selectedFY),
        taxApi.getCapitalGainsSummary(userId, selectedFY)
      ]);
      
      setTransactions(Array.isArray(txnRes) ? txnRes : []);
      setSummary(summaryRes);
      
    } catch (error) {
      console.error('Error loading capital gains:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAddTransaction = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      
      await taxApi.recordCapitalGain(userId, selectedFY, {
        ...newTransaction,
        purchasePrice: parseFloat(newTransaction.purchasePrice),
        salePrice: parseFloat(newTransaction.salePrice),
        quantity: parseFloat(newTransaction.quantity) || 1,
        expenses: parseFloat(newTransaction.expenses) || 0
      });
      
      setNewTransaction({
        assetType: 'EQUITY',
        purchaseDate: '',
        saleDate: '',
        purchasePrice: '',
        salePrice: '',
        quantity: '1',
        expenses: '',
        description: ''
      });
      
      setShowAddModal(false);
      await loadCapitalGains();
      alert('Transaction recorded successfully!');
      
    } catch (error) {
      console.error('Error recording transaction:', error);
      alert('Failed to record transaction');
    } finally {
      setLoading(false);
    }
  };

  const handleCalculate = async () => {
    try {
      setLoading(true);
      
      const response = await taxApi.calculateCapitalGains(userId, selectedFY, {
        ...calculatorData,
        purchasePrice: parseFloat(calculatorData.purchasePrice),
        salePrice: parseFloat(calculatorData.salePrice),
        expenses: parseFloat(calculatorData.expenses) || 0
      });
      
      setCalculatorResult(response);
      
    } catch (error) {
      console.error('Error calculating capital gains:', error);
      alert('Failed to calculate capital gains');
    } finally {
      setLoading(false);
    }
  };

  const handleAutoPopulate = async () => {
    try {
      setLoading(true);
      await taxApi.autoPopulateCapitalGains(userId, selectedFY);
      await loadCapitalGains();
      alert('Capital gains auto-populated from portfolio!');
    } catch (error) {
      console.error('Error auto-populating:', error);
      alert('Failed to auto-populate capital gains');
    } finally {
      setLoading(false);
    }
  };

  const calculateHoldingPeriod = (purchaseDate, saleDate) => {
    const diff = new Date(saleDate) - new Date(purchaseDate);
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const months = Math.floor(days / 30);
    return { days, months };
  };

  const determineGainType = (assetType, purchaseDate, saleDate) => {
    const { months } = calculateHoldingPeriod(purchaseDate, saleDate);
    
    if (assetType === 'EQUITY' || assetType === 'MUTUAL_FUND') {
      return months >= 12 ? 'LTCG' : 'STCG';
    } else {
      return months >= 36 ? 'LTCG' : 'STCG';
    }
  };

  const filteredTransactions = transactions.filter(txn => {
    if (filterAssetType !== 'ALL' && txn.assetType !== filterAssetType) return false;
    if (filterGainType !== 'ALL') {
      const gainType = determineGainType(txn.assetType, txn.purchaseDate, txn.saleDate);
      if (gainType !== filterGainType) return false;
    }
    return true;
  });

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

  return (
    <div className="capital-gains-module">
      {/* Header */}
      <div className="module-header">
        <div className="header-left">
          <h1>
            <TrendingUp size={32} />
            Capital Gains Management
          </h1>
          <p className="subtitle">Track and optimize your capital gains tax liability</p>
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
            <Download size={18} />
            Auto-Populate
          </button>
          <button className="btn-primary" onClick={() => setShowAddModal(true)}>
            <Plus size={18} />
            Add Transaction
          </button>
        </div>
      </div>

      {/* Summary Cards */}
      <div className="summary-cards">
        <div className="summary-card stcg">
          <div className="card-icon">
            <TrendingDown size={24} />
          </div>
          <div className="card-content">
            <h3>Short Term Capital Gains</h3>
            <div className="card-value">{formatCurrency(summary?.totalSTCG || 0)}</div>
            <div className="card-detail">Tax @ 15%: {formatCurrency((summary?.totalSTCG || 0) * 0.15)}</div>
          </div>
        </div>

        <div className="summary-card ltcg">
          <div className="card-icon">
            <TrendingUp size={24} />
          </div>
          <div className="card-content">
            <h3>Long Term Capital Gains</h3>
            <div className="card-value">{formatCurrency(summary?.totalLTCG || 0)}</div>
            <div className="card-detail">
              After ₹1L exemption: {formatCurrency(Math.max(0, (summary?.totalLTCG || 0) - 100000))}
            </div>
          </div>
        </div>

        <div className="summary-card total">
          <div className="card-icon">
            <DollarSign size={24} />
          </div>
          <div className="card-content">
            <h3>Total Tax Liability</h3>
            <div className="card-value">
              {formatCurrency((summary?.totalSTCGTax || 0) + (summary?.totalLTCGTax || 0))}
            </div>
            <div className="card-detail">
              {transactions.length} transaction{transactions.length !== 1 ? 's' : ''}
            </div>
          </div>
        </div>

        <div className="summary-card calculator-card" onClick={() => setShowCalculator(true)}>
          <div className="card-icon">
            <Calculator size={24} />
          </div>
          <div className="card-content">
            <h3>Capital Gains Calculator</h3>
            <div className="card-detail">Calculate potential tax before selling</div>
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="filters-section">
        <div className="filters">
          <div className="filter-group">
            <label>Asset Type</label>
            <select value={filterAssetType} onChange={(e) => setFilterAssetType(e.target.value)}>
              <option value="ALL">All Assets</option>
              <option value="EQUITY">Equity</option>
              <option value="MUTUAL_FUND">Mutual Fund</option>
              <option value="PROPERTY">Property</option>
              <option value="DEBT">Debt</option>
              <option value="GOLD">Gold</option>
            </select>
          </div>
          <div className="filter-group">
            <label>Gain Type</label>
            <select value={filterGainType} onChange={(e) => setFilterGainType(e.target.value)}>
              <option value="ALL">All Gains</option>
              <option value="STCG">Short Term</option>
              <option value="LTCG">Long Term</option>
            </select>
          </div>
        </div>
        <div className="filter-results">
          Showing {filteredTransactions.length} of {transactions.length} transactions
        </div>
      </div>

      {/* Transactions Table */}
      <div className="transactions-section">
        {filteredTransactions.length === 0 ? (
          <div className="empty-state">
            <TrendingUp size={48} />
            <p>No capital gains transactions recorded yet</p>
            <button className="btn-primary" onClick={() => setShowAddModal(true)}>
              <Plus size={18} />
              Add First Transaction
            </button>
          </div>
        ) : (
          <div className="transactions-table">
            <table>
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Asset Type</th>
                  <th>Description</th>
                  <th>Purchase Price</th>
                  <th>Sale Price</th>
                  <th>Gain/Loss</th>
                  <th>Type</th>
                  <th>Tax</th>
                </tr>
              </thead>
              <tbody>
                {filteredTransactions.map((txn, index) => {
                  const gain = (txn.salePrice || 0) - (txn.purchasePrice || 0) - (txn.expenses || 0);
                  const gainType = determineGainType(txn.assetType, txn.purchaseDate, txn.saleDate);
                  const taxRate = gainType === 'STCG' ? 0.15 : 0.10;
                  const tax = gainType === 'LTCG' ? Math.max(0, gain - 100000) * taxRate : gain * taxRate;
                  const { months } = calculateHoldingPeriod(txn.purchaseDate, txn.saleDate);
                  
                  return (
                    <tr key={index}>
                      <td>
                        <div className="date-cell">
                          <Calendar size={14} />
                          {new Date(txn.saleDate).toLocaleDateString()}
                        </div>
                      </td>
                      <td>
                        <span className="asset-badge">{txn.assetType}</span>
                      </td>
                      <td>{txn.description || '-'}</td>
                      <td>{formatCurrency(txn.purchasePrice)}</td>
                      <td>{formatCurrency(txn.salePrice)}</td>
                      <td className={gain >= 0 ? 'gain' : 'loss'}>
                        {gain >= 0 ? '+' : ''}{formatCurrency(gain)}
                      </td>
                      <td>
                        <span className={`gain-type-badge ${gainType.toLowerCase()}`}>
                          {gainType} ({months}m)
                        </span>
                      </td>
                      <td>{formatCurrency(tax)}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Add Transaction Modal */}
      {showAddModal && (
        <div className="modal-overlay" onClick={() => setShowAddModal(false)}>
          <div className="modal-content large" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Add Capital Gains Transaction</h3>
              <button className="close-btn" onClick={() => setShowAddModal(false)}>×</button>
            </div>
            <form onSubmit={handleAddTransaction} className="modal-form">
              <div className="form-grid">
                <div className="form-field">
                  <label>Asset Type *</label>
                  <select
                    value={newTransaction.assetType}
                    onChange={(e) => setNewTransaction({...newTransaction, assetType: e.target.value})}
                    required
                  >
                    <option value="EQUITY">Equity</option>
                    <option value="MUTUAL_FUND">Mutual Fund</option>
                    <option value="PROPERTY">Property</option>
                    <option value="DEBT">Debt</option>
                    <option value="GOLD">Gold</option>
                  </select>
                </div>

                <div className="form-field">
                  <label>Description *</label>
                  <input
                    type="text"
                    value={newTransaction.description}
                    onChange={(e) => setNewTransaction({...newTransaction, description: e.target.value})}
                    placeholder="e.g., TCS Shares, HDFC MF"
                    required
                  />
                </div>

                <div className="form-field">
                  <label>Purchase Date *</label>
                  <input
                    type="date"
                    value={newTransaction.purchaseDate}
                    onChange={(e) => setNewTransaction({...newTransaction, purchaseDate: e.target.value})}
                    required
                  />
                </div>

                <div className="form-field">
                  <label>Sale Date *</label>
                  <input
                    type="date"
                    value={newTransaction.saleDate}
                    onChange={(e) => setNewTransaction({...newTransaction, saleDate: e.target.value})}
                    required
                  />
                </div>

                <div className="form-field">
                  <label>Quantity</label>
                  <input
                    type="number"
                    value={newTransaction.quantity}
                    onChange={(e) => setNewTransaction({...newTransaction, quantity: e.target.value})}
                    placeholder="Enter quantity"
                  />
                </div>

                <div className="form-field">
                  <label>Purchase Price *</label>
                  <input
                    type="number"
                    value={newTransaction.purchasePrice}
                    onChange={(e) => setNewTransaction({...newTransaction, purchasePrice: e.target.value})}
                    placeholder="Enter purchase price"
                    required
                  />
                </div>

                <div className="form-field">
                  <label>Sale Price *</label>
                  <input
                    type="number"
                    value={newTransaction.salePrice}
                    onChange={(e) => setNewTransaction({...newTransaction, salePrice: e.target.value})}
                    placeholder="Enter sale price"
                    required
                  />
                </div>

                <div className="form-field">
                  <label>Expenses (Brokerage, etc.)</label>
                  <input
                    type="number"
                    value={newTransaction.expenses}
                    onChange={(e) => setNewTransaction({...newTransaction, expenses: e.target.value})}
                    placeholder="Enter expenses"
                  />
                </div>
              </div>

              <div className="modal-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowAddModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn-primary" disabled={loading}>
                  <Plus size={18} />
                  {loading ? 'Adding...' : 'Add Transaction'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Calculator Modal */}
      {showCalculator && (
        <div className="modal-overlay" onClick={() => setShowCalculator(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Capital Gains Calculator</h3>
              <button className="close-btn" onClick={() => setShowCalculator(false)}>×</button>
            </div>
            <div className="calculator-form">
              <div className="form-grid">
                <div className="form-field">
                  <label>Asset Type</label>
                  <select
                    value={calculatorData.assetType}
                    onChange={(e) => setCalculatorData({...calculatorData, assetType: e.target.value})}
                  >
                    <option value="EQUITY">Equity</option>
                    <option value="MUTUAL_FUND">Mutual Fund</option>
                    <option value="PROPERTY">Property</option>
                    <option value="DEBT">Debt</option>
                    <option value="GOLD">Gold</option>
                  </select>
                </div>

                <div className="form-field">
                  <label>Purchase Date</label>
                  <input
                    type="date"
                    value={calculatorData.purchaseDate}
                    onChange={(e) => setCalculatorData({...calculatorData, purchaseDate: e.target.value})}
                  />
                </div>

                <div className="form-field">
                  <label>Sale Date</label>
                  <input
                    type="date"
                    value={calculatorData.saleDate}
                    onChange={(e) => setCalculatorData({...calculatorData, saleDate: e.target.value})}
                  />
                </div>

                <div className="form-field">
                  <label>Purchase Price</label>
                  <input
                    type="number"
                    value={calculatorData.purchasePrice}
                    onChange={(e) => setCalculatorData({...calculatorData, purchasePrice: e.target.value})}
                    placeholder="Enter purchase price"
                  />
                </div>

                <div className="form-field">
                  <label>Sale Price</label>
                  <input
                    type="number"
                    value={calculatorData.salePrice}
                    onChange={(e) => setCalculatorData({...calculatorData, salePrice: e.target.value})}
                    placeholder="Enter sale price"
                  />
                </div>

                <div className="form-field">
                  <label>Expenses</label>
                  <input
                    type="number"
                    value={calculatorData.expenses}
                    onChange={(e) => setCalculatorData({...calculatorData, expenses: e.target.value})}
                    placeholder="Enter expenses"
                  />
                </div>
              </div>

              <button className="btn-primary full-width" onClick={handleCalculate} disabled={loading}>
                <Calculator size={18} />
                {loading ? 'Calculating...' : 'Calculate'}
              </button>

              {calculatorResult && (
                <div className="calculator-result">
                  <h4>Calculation Result</h4>
                  <div className="result-grid">
                    <div className="result-item">
                      <span>Gain Type:</span>
                      <span className="result-value">{calculatorResult.gainType}</span>
                    </div>
                    <div className="result-item">
                      <span>Capital Gain:</span>
                      <span className="result-value">{formatCurrency(calculatorResult.capitalGain)}</span>
                    </div>
                    <div className="result-item">
                      <span>Tax Rate:</span>
                      <span className="result-value">{calculatorResult.taxRate}%</span>
                    </div>
                    <div className="result-item highlight">
                      <span>Tax Payable:</span>
                      <span className="result-value">{formatCurrency(calculatorResult.taxPayable)}</span>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CapitalGainsModule;
