import React, { useState, useEffect, useMemo } from 'react';
import taxApi from '../services/taxApi';
import './DeductionsTracker.css';
import {
  Shield,
  Heart,
  GraduationCap,
  Home as HomeIcon,
  Plus,
  Trash2,
  TrendingUp,
  AlertCircle,
  CheckCircle
} from 'lucide-react';

const DeductionsTracker = () => {
  const user = JSON.parse(localStorage.getItem('user'));
  const userId = useMemo(() => user?.userId, []);
  const [loading, setLoading] = useState(false);
  const [showAddModal, setShowAddModal] = useState(false);
  const [activeSection, setActiveSection] = useState('80C');
  
  // Financial Year
  const currentYear = new Date().getFullYear();
  const currentMonth = new Date().getMonth();
  const defaultFY = currentMonth >= 3 ? `${currentYear}-${(currentYear + 1) % 100}` : `${currentYear - 1}-${currentYear % 100}`;
  const [selectedFY, setSelectedFY] = useState(defaultFY);
  
  // Deductions State
  const [section80C, setSection80C] = useState([]);
  const [section80D, setSection80D] = useState([]);
  const [otherDeductions, setOtherDeductions] = useState([]);
  
  // New Investment Form State
  const [newInvestment, setNewInvestment] = useState({
    investmentType: '',
    amount: '',
    description: '',
    documentNumber: '',
    investmentDate: ''
  });

  const section80COptions = [
    { value: 'PPF', label: 'Public Provident Fund (PPF)', limit: 150000 },
    { value: 'EPF', label: 'Employee Provident Fund (EPF)', limit: 150000 },
    { value: 'ELSS', label: 'Equity Linked Savings Scheme (ELSS)', limit: 150000 },
    { value: 'LIC', label: 'Life Insurance Premium', limit: 150000 },
    { value: 'NSC', label: 'National Savings Certificate (NSC)', limit: 150000 },
    { value: 'FD', label: 'Tax Saving Fixed Deposit', limit: 150000 },
    { value: 'HOUSING_LOAN', label: 'Housing Loan Principal', limit: 150000 },
    { value: 'TUITION_FEES', label: 'Tuition Fees (2 children)', limit: 150000 },
    { value: 'SSY', label: 'Sukanya Samriddhi Yojana', limit: 150000 },
    { value: 'NPS', label: 'National Pension Scheme (80CCD)', limit: 150000 }
  ];

  const section80DOptions = [
    { value: 'SELF', label: 'Health Insurance - Self & Family', limit: 25000 },
    { value: 'PARENTS', label: 'Health Insurance - Parents (<60)', limit: 25000 },
    { value: 'PARENTS_SENIOR', label: 'Health Insurance - Parents (>=60)', limit: 50000 },
    { value: 'PREVENTIVE_CHECKUP', label: 'Preventive Health Checkup', limit: 5000 }
  ];

  const otherDeductionsOptions = [
    { value: '80E', label: 'Education Loan Interest', section: '80E', limit: 'No Limit' },
    { value: '80G', label: 'Donations to Charity', section: '80G', limit: 'Varies' },
    { value: '80TTA', label: 'Savings Account Interest', section: '80TTA', limit: 10000 },
    { value: '80TTB', label: 'Interest for Senior Citizens', section: '80TTB', limit: 50000 },
    { value: '80GG', label: 'Rent Paid (No HRA)', section: '80GG', limit: 'Varies' },
    { value: '80CCD1B', label: 'Additional NPS Contribution', section: '80CCD(1B)', limit: 50000 }
  ];

  useEffect(() => {
    if (user) {
      loadDeductions();
    }
  }, [selectedFY]);

  const loadDeductions = async () => {
    try {
      setLoading(true);
      const response = await taxApi.getTaxSavingInvestments(userId, selectedFY);
      
      // Categorize investments
      const data = Array.isArray(response) ? response : [];
      setSection80C(data.filter(inv => inv.section === '80C'));
      setSection80D(data.filter(inv => inv.section === '80D'));
      setOtherDeductions(data.filter(inv => !['80C', '80D'].includes(inv.section)));
      
    } catch (error) {
      console.error('Error loading deductions:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAddInvestment = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      
      await taxApi.recordTaxSavingInvestment(userId, selectedFY, {
        ...newInvestment,
        amount: parseFloat(newInvestment.amount),
        section: activeSection,
        investmentDate: newInvestment.investmentDate || new Date().toISOString().split('T')[0]
      });
      
      // Reset form
      setNewInvestment({
        investmentType: '',
        amount: '',
        description: '',
        documentNumber: '',
        investmentDate: ''
      });
      
      setShowAddModal(false);
      await loadDeductions();
      alert('Investment recorded successfully!');
      
    } catch (error) {
      console.error('Error recording investment:', error);
      alert('Failed to record investment');
    } finally {
      setLoading(false);
    }
  };

  const handleAutoPopulate80C = async () => {
    try {
      setLoading(true);
      await taxApi.autoPopulate80CInvestments(userId, selectedFY);
      await loadDeductions();
      alert('80C investments auto-populated successfully!');
    } catch (error) {
      console.error('Error auto-populating 80C:', error);
      alert('Failed to auto-populate 80C investments');
    } finally {
      setLoading(false);
    }
  };

  const handleAutoPopulate80D = async () => {
    try {
      setLoading(true);
      await taxApi.autoPopulate80DInvestments(userId, selectedFY);
      await loadDeductions();
      alert('80D investments auto-populated successfully!');
    } catch (error) {
      console.error('Error auto-populating 80D:', error);
      alert('Failed to auto-populate 80D investments');
    } finally {
      setLoading(false);
    }
  };

  const calculateTotal = (investments) => {
    return investments.reduce((sum, inv) => sum + (inv.amount || 0), 0);
  };

  const calculateProgress = (total, limit) => {
    return Math.min((total / limit) * 100, 100);
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

  const total80C = calculateTotal(section80C);
  const total80D = calculateTotal(section80D);
  const totalOther = calculateTotal(otherDeductions);
  const totalDeductions = total80C + total80D + totalOther;

  return (
    <div className="deductions-tracker">
      {/* Header */}
      <div className="tracker-header">
        <div className="header-left">
          <h1>
            <Shield size={32} />
            Deductions Tracker
          </h1>
          <p className="subtitle">Track your tax-saving investments and deductions</p>
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

      {/* Summary Cards */}
      <div className="summary-cards">
        <div className="summary-card primary">
          <div className="card-header">
            <h3>Total Deductions</h3>
            <TrendingUp size={24} />
          </div>
          <div className="card-value">{formatCurrency(totalDeductions)}</div>
          <div className="card-detail">Tax Savings: ~{formatCurrency(totalDeductions * 0.3)}</div>
        </div>

        <div className="summary-card section-80c">
          <div className="card-header">
            <h3>Section 80C</h3>
            <Shield size={24} />
          </div>
          <div className="card-value">{formatCurrency(total80C)}</div>
          <div className="progress-bar">
            <div className="progress-fill" style={{width: `${calculateProgress(total80C, 150000)}%`}}></div>
          </div>
          <div className="card-detail">
            {formatCurrency(Math.max(0, 150000 - total80C))} remaining of ₹1.5L limit
          </div>
          <button className="auto-populate-btn" onClick={handleAutoPopulate80C}>
            Auto-populate from Portfolio
          </button>
        </div>

        <div className="summary-card section-80d">
          <div className="card-header">
            <h3>Section 80D</h3>
            <Heart size={24} />
          </div>
          <div className="card-value">{formatCurrency(total80D)}</div>
          <div className="progress-bar">
            <div className="progress-fill" style={{width: `${calculateProgress(total80D, 75000)}%`}}></div>
          </div>
          <div className="card-detail">
            {formatCurrency(Math.max(0, 75000 - total80D))} remaining of ₹75K limit
          </div>
          <button className="auto-populate-btn" onClick={handleAutoPopulate80D}>
            Auto-populate from Insurance
          </button>
        </div>

        <div className="summary-card other">
          <div className="card-header">
            <h3>Other Deductions</h3>
            <GraduationCap size={24} />
          </div>
          <div className="card-value">{formatCurrency(totalOther)}</div>
          <div className="card-detail">80E, 80G, 80TTA, etc.</div>
        </div>
      </div>

      {/* Deductions Lists */}
      <div className="deductions-content">
        <div className="section-tabs">
          <button 
            className={activeSection === '80C' ? 'active' : ''}
            onClick={() => setActiveSection('80C')}
          >
            Section 80C
          </button>
          <button 
            className={activeSection === '80D' ? 'active' : ''}
            onClick={() => setActiveSection('80D')}
          >
            Section 80D
          </button>
          <button 
            className={activeSection === 'OTHER' ? 'active' : ''}
            onClick={() => setActiveSection('OTHER')}
          >
            Other Deductions
          </button>
        </div>

        <div className="section-content">
          <div className="content-header">
            <h3>
              {activeSection === '80C' && 'Section 80C Investments'}
              {activeSection === '80D' && 'Section 80D Investments'}
              {activeSection === 'OTHER' && 'Other Deductions'}
            </h3>
            <button className="add-btn" onClick={() => setShowAddModal(true)}>
              <Plus size={18} />
              Add Investment
            </button>
          </div>

          {/* 80C List */}
          {activeSection === '80C' && (
            <div className="investments-list">
              {section80C.length === 0 ? (
                <div className="empty-state">
                  <Shield size={48} />
                  <p>No Section 80C investments recorded yet</p>
                  <button className="btn-primary" onClick={() => setShowAddModal(true)}>
                    <Plus size={18} />
                    Add First Investment
                  </button>
                </div>
              ) : (
                <div className="investment-cards">
                  {section80C.map((investment, index) => (
                    <div key={index} className="investment-card">
                      <div className="investment-header">
                        <h4>{investment.investmentType}</h4>
                        <span className="investment-amount">{formatCurrency(investment.amount)}</span>
                      </div>
                      {investment.description && (
                        <p className="investment-description">{investment.description}</p>
                      )}
                      <div className="investment-meta">
                        {investment.investmentDate && (
                          <span className="meta-item">
                            Date: {new Date(investment.investmentDate).toLocaleDateString()}
                          </span>
                        )}
                        {investment.documentNumber && (
                          <span className="meta-item">
                            Doc: {investment.documentNumber}
                          </span>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* 80D List */}
          {activeSection === '80D' && (
            <div className="investments-list">
              {section80D.length === 0 ? (
                <div className="empty-state">
                  <Heart size={48} />
                  <p>No Section 80D investments recorded yet</p>
                  <button className="btn-primary" onClick={() => setShowAddModal(true)}>
                    <Plus size={18} />
                    Add First Investment
                  </button>
                </div>
              ) : (
                <div className="investment-cards">
                  {section80D.map((investment, index) => (
                    <div key={index} className="investment-card">
                      <div className="investment-header">
                        <h4>{investment.investmentType}</h4>
                        <span className="investment-amount">{formatCurrency(investment.amount)}</span>
                      </div>
                      {investment.description && (
                        <p className="investment-description">{investment.description}</p>
                      )}
                      <div className="investment-meta">
                        {investment.investmentDate && (
                          <span className="meta-item">
                            Date: {new Date(investment.investmentDate).toLocaleDateString()}
                          </span>
                        )}
                        {investment.documentNumber && (
                          <span className="meta-item">
                            Policy: {investment.documentNumber}
                          </span>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* Other Deductions List */}
          {activeSection === 'OTHER' && (
            <div className="investments-list">
              {otherDeductions.length === 0 ? (
                <div className="empty-state">
                  <GraduationCap size={48} />
                  <p>No other deductions recorded yet</p>
                  <button className="btn-primary" onClick={() => setShowAddModal(true)}>
                    <Plus size={18} />
                    Add First Deduction
                  </button>
                </div>
              ) : (
                <div className="investment-cards">
                  {otherDeductions.map((investment, index) => (
                    <div key={index} className="investment-card">
                      <div className="investment-header">
                        <h4>
                          {investment.investmentType}
                          <span className="section-badge">{investment.section}</span>
                        </h4>
                        <span className="investment-amount">{formatCurrency(investment.amount)}</span>
                      </div>
                      {investment.description && (
                        <p className="investment-description">{investment.description}</p>
                      )}
                      <div className="investment-meta">
                        {investment.investmentDate && (
                          <span className="meta-item">
                            Date: {new Date(investment.investmentDate).toLocaleDateString()}
                          </span>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Add Investment Modal */}
      {showAddModal && (
        <div className="modal-overlay" onClick={() => setShowAddModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Add {activeSection} Investment</h3>
              <button className="close-btn" onClick={() => setShowAddModal(false)}>
                ×
              </button>
            </div>
            <form onSubmit={handleAddInvestment} className="modal-form">
              <div className="form-field">
                <label>Investment Type *</label>
                <select
                  value={newInvestment.investmentType}
                  onChange={(e) => setNewInvestment({...newInvestment, investmentType: e.target.value})}
                  required
                >
                  <option value="">Select investment type</option>
                  {activeSection === '80C' && section80COptions.map(opt => (
                    <option key={opt.value} value={opt.value}>{opt.label}</option>
                  ))}
                  {activeSection === '80D' && section80DOptions.map(opt => (
                    <option key={opt.value} value={opt.value}>{opt.label}</option>
                  ))}
                  {activeSection === 'OTHER' && otherDeductionsOptions.map(opt => (
                    <option key={opt.value} value={opt.value}>{opt.label}</option>
                  ))}
                </select>
              </div>

              <div className="form-field">
                <label>Amount *</label>
                <input
                  type="number"
                  value={newInvestment.amount}
                  onChange={(e) => setNewInvestment({...newInvestment, amount: e.target.value})}
                  placeholder="Enter amount"
                  required
                />
              </div>

              <div className="form-field">
                <label>Investment Date</label>
                <input
                  type="date"
                  value={newInvestment.investmentDate}
                  onChange={(e) => setNewInvestment({...newInvestment, investmentDate: e.target.value})}
                />
              </div>

              <div className="form-field">
                <label>Description</label>
                <input
                  type="text"
                  value={newInvestment.description}
                  onChange={(e) => setNewInvestment({...newInvestment, description: e.target.value})}
                  placeholder="Optional description"
                />
              </div>

              <div className="form-field">
                <label>Document/Policy Number</label>
                <input
                  type="text"
                  value={newInvestment.documentNumber}
                  onChange={(e) => setNewInvestment({...newInvestment, documentNumber: e.target.value})}
                  placeholder="Optional"
                />
              </div>

              <div className="modal-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowAddModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn-primary" disabled={loading}>
                  <Plus size={18} />
                  {loading ? 'Adding...' : 'Add Investment'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default DeductionsTracker;
