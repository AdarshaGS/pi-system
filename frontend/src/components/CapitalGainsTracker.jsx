import React, { useState, useEffect } from 'react';
import {
  recordCapitalGains,
  getCapitalGainsByType,
  updateCapitalGains,
  deleteCapitalGains
} from '../api/taxApi';
import './CapitalGainsTracker.css';

function CapitalGainsTracker({ userId, financialYear, summary, onUpdate }) {
  const [transactions, setTransactions] = useState([]);
  const [showAddForm, setShowAddForm] = useState(false);
  const [editingTransaction, setEditingTransaction] = useState(null);
  const [filterType, setFilterType] = useState('ALL');
  const [formData, setFormData] = useState({
    assetType: 'EQUITY',
    assetName: '',
    purchaseDate: '',
    saleDate: '',
    purchasePrice: 0,
    salePrice: 0,
    quantity: 1,
    indexationBenefit: 0,
    expenses: 0
  });

  useEffect(() => {
    loadTransactions();
  }, [userId, filterType]);

  const loadTransactions = async () => {
    if (filterType === 'ALL') {
      // Load all types
      try {
        const [equity, debt, property, other] = await Promise.all([
          getCapitalGainsByType(userId, 'EQUITY').catch(() => []),
          getCapitalGainsByType(userId, 'DEBT').catch(() => []),
          getCapitalGainsByType(userId, 'PROPERTY').catch(() => []),
          getCapitalGainsByType(userId, 'OTHER').catch(() => [])
        ]);
        setTransactions([...equity, ...debt, ...property, ...other]);
      } catch (error) {
        console.error('Error loading transactions:', error);
      }
    } else {
      try {
        const data = await getCapitalGainsByType(userId, filterType);
        setTransactions(data);
      } catch (error) {
        console.error('Error loading transactions:', error);
      }
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: ['purchasePrice', 'salePrice', 'quantity', 'indexationBenefit', 'expenses'].includes(name)
        ? parseFloat(value) || 0
        : value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingTransaction) {
        await updateCapitalGains(editingTransaction.id, formData);
      } else {
        await recordCapitalGains(userId, formData);
      }
      resetForm();
      loadTransactions();
      if (onUpdate) onUpdate();
    } catch (error) {
      console.error('Error saving capital gains:', error);
      alert('Failed to save capital gains transaction');
    }
  };

  const handleEdit = (transaction) => {
    setEditingTransaction(transaction);
    setFormData({
      assetType: transaction.assetType,
      assetName: transaction.assetName,
      purchaseDate: transaction.purchaseDate,
      saleDate: transaction.saleDate,
      purchasePrice: transaction.purchasePrice,
      salePrice: transaction.salePrice,
      quantity: transaction.quantity,
      indexationBenefit: transaction.indexationBenefit || 0,
      expenses: transaction.expenses || 0
    });
    setShowAddForm(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this transaction?')) {
      try {
        await deleteCapitalGains(id);
        loadTransactions();
        if (onUpdate) onUpdate();
      } catch (error) {
        console.error('Error deleting capital gains:', error);
        alert('Failed to delete transaction');
      }
    }
  };

  const resetForm = () => {
    setFormData({
      assetType: 'EQUITY',
      assetName: '',
      purchaseDate: '',
      saleDate: '',
      purchasePrice: 0,
      salePrice: 0,
      quantity: 1,
      indexationBenefit: 0,
      expenses: 0
    });
    setEditingTransaction(null);
    setShowAddForm(false);
  };

  const calculateGain = (transaction) => {
    const totalPurchase = transaction.purchasePrice * transaction.quantity;
    const totalSale = transaction.salePrice * transaction.quantity;
    return totalSale - totalPurchase - (transaction.expenses || 0);
  };

  const determineGainType = (purchaseDate, saleDate, assetType) => {
    const purchase = new Date(purchaseDate);
    const sale = new Date(saleDate);
    const months = (sale - purchase) / (1000 * 60 * 60 * 24 * 30);
    
    if (assetType === 'EQUITY') {
      return months > 12 ? 'LTCG' : 'STCG';
    } else if (assetType === 'DEBT' || assetType === 'PROPERTY') {
      return months > 24 ? 'LTCG' : 'STCG';
    }
    return 'STCG';
  };

  return (
    <div className="capital-gains-tracker">
      <div className="tracker-header">
        <h2>Capital Gains Tracker - FY {financialYear}</h2>
        <button 
          className="btn-primary"
          onClick={() => setShowAddForm(!showAddForm)}
        >
          {showAddForm ? 'Cancel' : '+ Add Transaction'}
        </button>
      </div>

      {/* Summary Cards */}
      <div className="gains-summary">
        <div className="summary-card">
          <h4>Total Gains</h4>
          <p className="amount">₹{summary?.totalGains?.toLocaleString() || '0'}</p>
        </div>
        <div className="summary-card">
          <h4>Short Term (STCG)</h4>
          <p className="amount">₹{summary?.shortTermGains?.toLocaleString() || '0'}</p>
          <small>Tax Rate: 15% (Equity) / Slab (Others)</small>
        </div>
        <div className="summary-card">
          <h4>Long Term (LTCG)</h4>
          <p className="amount">₹{summary?.longTermGains?.toLocaleString() || '0'}</p>
          <small>Tax Rate: 10% above ₹1L (Equity) / 20% with indexation</small>
        </div>
        <div className="summary-card">
          <h4>Estimated Tax</h4>
          <p className="amount">₹{summary?.estimatedTax?.toLocaleString() || '0'}</p>
        </div>
      </div>

      {/* Add/Edit Form */}
      {showAddForm && (
        <div className="transaction-form">
          <h3>{editingTransaction ? 'Edit Transaction' : 'Add Capital Gains Transaction'}</h3>
          <form onSubmit={handleSubmit}>
            <div className="form-row">
              <div className="form-group">
                <label>Asset Type</label>
                <select
                  name="assetType"
                  value={formData.assetType}
                  onChange={handleInputChange}
                  required
                >
                  <option value="EQUITY">Equity/Stocks</option>
                  <option value="DEBT">Debt/Bonds/MF</option>
                  <option value="PROPERTY">Real Estate</option>
                  <option value="OTHER">Other Assets</option>
                </select>
              </div>

              <div className="form-group">
                <label>Asset Name</label>
                <input
                  type="text"
                  name="assetName"
                  value={formData.assetName}
                  onChange={handleInputChange}
                  placeholder="e.g., RELIANCE, HDFC MF"
                  required
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Purchase Date</label>
                <input
                  type="date"
                  name="purchaseDate"
                  value={formData.purchaseDate}
                  onChange={handleInputChange}
                  required
                />
              </div>

              <div className="form-group">
                <label>Sale Date</label>
                <input
                  type="date"
                  name="saleDate"
                  value={formData.saleDate}
                  onChange={handleInputChange}
                  required
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Purchase Price (per unit)</label>
                <input
                  type="number"
                  name="purchasePrice"
                  value={formData.purchasePrice}
                  onChange={handleInputChange}
                  placeholder="0"
                  min="0"
                  step="0.01"
                  required
                />
              </div>

              <div className="form-group">
                <label>Sale Price (per unit)</label>
                <input
                  type="number"
                  name="salePrice"
                  value={formData.salePrice}
                  onChange={handleInputChange}
                  placeholder="0"
                  min="0"
                  step="0.01"
                  required
                />
              </div>

              <div className="form-group">
                <label>Quantity</label>
                <input
                  type="number"
                  name="quantity"
                  value={formData.quantity}
                  onChange={handleInputChange}
                  placeholder="1"
                  min="1"
                  required
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Expenses (brokerage, etc.)</label>
                <input
                  type="number"
                  name="expenses"
                  value={formData.expenses}
                  onChange={handleInputChange}
                  placeholder="0"
                  min="0"
                  step="0.01"
                />
              </div>

              {formData.assetType !== 'EQUITY' && (
                <div className="form-group">
                  <label>Indexation Benefit (LTCG only)</label>
                  <input
                    type="number"
                    name="indexationBenefit"
                    value={formData.indexationBenefit}
                    onChange={handleInputChange}
                    placeholder="0"
                    min="0"
                    step="0.01"
                  />
                </div>
              )}
            </div>

            <div className="form-actions">
              <button type="submit" className="btn-primary">
                {editingTransaction ? 'Update' : 'Add'} Transaction
              </button>
              <button type="button" className="btn-secondary" onClick={resetForm}>
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Filter */}
      <div className="filter-bar">
        <label>Filter by type:</label>
        <select value={filterType} onChange={(e) => setFilterType(e.target.value)}>
          <option value="ALL">All Assets</option>
          <option value="EQUITY">Equity</option>
          <option value="DEBT">Debt</option>
          <option value="PROPERTY">Property</option>
          <option value="OTHER">Other</option>
        </select>
      </div>

      {/* Transactions Table */}
      <div className="transactions-table">
        {transactions.length === 0 ? (
          <div className="empty-state">
            <p>No capital gains transactions recorded</p>
            <button className="btn-primary" onClick={() => setShowAddForm(true)}>
              Add First Transaction
            </button>
          </div>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Asset</th>
                <th>Type</th>
                <th>Purchase Date</th>
                <th>Sale Date</th>
                <th>Purchase Price</th>
                <th>Sale Price</th>
                <th>Qty</th>
                <th>Gain/Loss</th>
                <th>Term</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map((transaction) => {
                const gain = calculateGain(transaction);
                const gainType = determineGainType(
                  transaction.purchaseDate,
                  transaction.saleDate,
                  transaction.assetType
                );
                return (
                  <tr key={transaction.id}>
                    <td>{transaction.assetName}</td>
                    <td>
                      <span className={`asset-badge ${transaction.assetType.toLowerCase()}`}>
                        {transaction.assetType}
                      </span>
                    </td>
                    <td>{new Date(transaction.purchaseDate).toLocaleDateString()}</td>
                    <td>{new Date(transaction.saleDate).toLocaleDateString()}</td>
                    <td>₹{transaction.purchasePrice.toLocaleString()}</td>
                    <td>₹{transaction.salePrice.toLocaleString()}</td>
                    <td>{transaction.quantity}</td>
                    <td className={gain >= 0 ? 'positive' : 'negative'}>
                      ₹{gain.toLocaleString()}
                    </td>
                    <td>
                      <span className={`term-badge ${gainType.toLowerCase()}`}>
                        {gainType}
                      </span>
                    </td>
                    <td>
                      <button
                        className="btn-icon"
                        onClick={() => handleEdit(transaction)}
                        title="Edit"
                      >
                        ✏️
                      </button>
                      <button
                        className="btn-icon"
                        onClick={() => handleDelete(transaction.id)}
                        title="Delete"
                      >
                        🗑️
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>

      {/* Info Box */}
      <div className="info-box">
        <strong>ℹ️ Capital Gains Holding Periods:</strong>
        <ul>
          <li><strong>Equity:</strong> STCG if held ≤12 months, LTCG if &gt;12 months</li>
          <li><strong>Debt/Property:</strong> STCG if held ≤24 months (36 for property bought before 2017), LTCG if longer</li>
          <li><strong>Tax Rates:</strong> STCG Equity @15%, LTCG Equity @10% (above ₹1L), LTCG Others @20% with indexation</li>
        </ul>
      </div>
    </div>
  );
}

export default CapitalGainsTracker;
