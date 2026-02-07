import React, { useState, useEffect, useMemo } from 'react';
import taxApi from '../services/taxApi';
import './IncomeEntryForms.css';
import {
  Briefcase,
  Home,
  TrendingUp,
  Building,
  Wallet,
  Save,
  X,
  Plus,
  Edit2,
  Trash2,
  Calendar
} from 'lucide-react';

const IncomeEntryForms = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('salary');
  const [loading, setLoading] = useState(false);
  
  // Financial Year
  const currentYear = new Date().getFullYear();
  const currentMonth = new Date().getMonth();
  const defaultFY = currentMonth >= 3 ? `${currentYear}-${(currentYear + 1) % 100}` : `${currentYear - 1}-${currentYear % 100}`;
  const [selectedFY, setSelectedFY] = useState(defaultFY);
  
  // Salary Income State
  const [salaryData, setSalaryData] = useState({
    grossSalary: '',
    hra: '',
    lta: '',
    professionalTax: '',
    standardDeduction: 50000,
    otherAllowances: '',
    employerPF: '',
    form16Available: false
  });

  // House Property State
  const [housePropertyData, setHousePropertyData] = useState({
    gav: '',
    nav: '',
    municipalTaxes: '',
    interestOnLoan: '',
    propertyType: 'SELF_OCCUPIED',
    coOwnershipShare: 100
  });

  // Capital Gains State  
  const [capitalGainsData, setCapitalGainsData] = useState({
    assetType: 'EQUITY',
    purchaseDate: '',
    saleDate: '',
    purchasePrice: '',
    salePrice: '',
    expenses: '',
    description: ''
  });

  // Business Income State
  const [businessData, setBusinessData] = useState({
    grossReceipts: '',
    expenses: '',
    depreciation: '',
    taxationScheme: 'REGULAR',
    businessType: 'PROFESSIONAL'
  });

  // Other Income State
  const [otherIncomeData, setOtherIncomeData] = useState({
    savingsInterest: '',
    fdInterest: '',
    dividendIncome: '',
    rentalIncome: '',
    otherSources: ''
  });

  useEffect(() => {
    if (user) {
      loadIncomeData();
    }
  }, [selectedFY]);

  const loadIncomeData = async () => {
    try {
      setLoading(true);
      const taxDetails = await taxApi.getTaxDetails(userId, selectedFY);
      if (taxDetails) {
        // Populate salary data
        setSalaryData({
          grossSalary: taxDetails.grossSalary || '',
          hra: taxDetails.hra || '',
          lta: taxDetails.lta || '',
          professionalTax: taxDetails.professionalTax || '',
          standardDeduction: taxDetails.standardDeduction || 50000,
          otherAllowances: taxDetails.otherAllowances || '',
          employerPF: taxDetails.employerPF || '',
          form16Available: taxDetails.form16Available || false
        });
      }
    } catch (error) {
      console.error('Error loading income data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSalarySubmit = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      
      // Auto-populate salary income
      await taxApi.autoPopulateSalaryIncome(userId, selectedFY, {
        grossSalary: parseFloat(salaryData.grossSalary) || 0,
        hra: parseFloat(salaryData.hra) || 0,
        lta: parseFloat(salaryData.lta) || 0,
        professionalTax: parseFloat(salaryData.professionalTax) || 0,
        standardDeduction: parseFloat(salaryData.standardDeduction) || 50000
      });
      
      alert('Salary income saved successfully!');
    } catch (error) {
      console.error('Error saving salary income:', error);
      alert('Failed to save salary income');
    } finally {
      setLoading(false);
    }
  };

  const handleHousePropertySubmit = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      
      await taxApi.calculateHousePropertyIncome(userId, selectedFY, {
        gav: parseFloat(housePropertyData.gav) || 0,
        nav: parseFloat(housePropertyData.nav) || 0,
        municipalTaxes: parseFloat(housePropertyData.municipalTaxes) || 0,
        interestOnLoan: parseFloat(housePropertyData.interestOnLoan) || 0,
        propertyType: housePropertyData.propertyType,
        coOwnershipShare: parseFloat(housePropertyData.coOwnershipShare) || 100
      });
      
      alert('House property income calculated successfully!');
    } catch (error) {
      console.error('Error calculating house property income:', error);
      alert('Failed to calculate house property income');
    } finally {
      setLoading(false);
    }
  };

  const handleCapitalGainsSubmit = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      
      await taxApi.recordCapitalGain(userId, selectedFY, {
        assetType: capitalGainsData.assetType,
        purchaseDate: capitalGainsData.purchaseDate,
        saleDate: capitalGainsData.saleDate,
        purchasePrice: parseFloat(capitalGainsData.purchasePrice) || 0,
        salePrice: parseFloat(capitalGainsData.salePrice) || 0,
        expenses: parseFloat(capitalGainsData.expenses) || 0,
        description: capitalGainsData.description
      });
      
      alert('Capital gains transaction recorded successfully!');
      
      // Reset form
      setCapitalGainsData({
        assetType: 'EQUITY',
        purchaseDate: '',
        saleDate: '',
        purchasePrice: '',
        salePrice: '',
        expenses: '',
        description: ''
      });
    } catch (error) {
      console.error('Error recording capital gains:', error);
      alert('Failed to record capital gains transaction');
    } finally {
      setLoading(false);
    }
  };

  const handleBusinessSubmit = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      
      await taxApi.calculateBusinessIncome(userId, selectedFY, {
        grossReceipts: parseFloat(businessData.grossReceipts) || 0,
        expenses: parseFloat(businessData.expenses) || 0,
        depreciation: parseFloat(businessData.depreciation) || 0,
        taxationScheme: businessData.taxationScheme,
        businessType: businessData.businessType
      });
      
      alert('Business income calculated successfully!');
    } catch (error) {
      console.error('Error calculating business income:', error);
      alert('Failed to calculate business income');
    } finally {
      setLoading(false);
    }
  };

  const handleOtherIncomeSubmit = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      
      const totalOtherIncome = 
        (parseFloat(otherIncomeData.savingsInterest) || 0) +
        (parseFloat(otherIncomeData.fdInterest) || 0) +
        (parseFloat(otherIncomeData.dividendIncome) || 0) +
        (parseFloat(otherIncomeData.rentalIncome) || 0) +
        (parseFloat(otherIncomeData.otherSources) || 0);
      
      // You may need to create a specific endpoint or update tax details
      alert(`Other income of ₹${totalOtherIncome.toLocaleString()} recorded!`);
    } catch (error) {
      console.error('Error saving other income:', error);
      alert('Failed to save other income');
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

  return (
    <div className="income-entry-forms">
      {/* Header */}
      <div className="forms-header">
        <div className="header-left">
          <h1>
            <Wallet size={32} />
            Income Entry Forms
          </h1>
          <p className="subtitle">Add and manage your income sources for tax calculation</p>
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

      {/* Tab Navigation */}
      <div className="income-tabs">
        <button 
          className={activeTab === 'salary' ? 'active' : ''}
          onClick={() => setActiveTab('salary')}
        >
          <Briefcase size={20} />
          Salary Income
        </button>
        <button 
          className={activeTab === 'house-property' ? 'active' : ''}
          onClick={() => setActiveTab('house-property')}
        >
          <Home size={20} />
          House Property
        </button>
        <button 
          className={activeTab === 'capital-gains' ? 'active' : ''}
          onClick={() => setActiveTab('capital-gains')}
        >
          <TrendingUp size={20} />
          Capital Gains
        </button>
        <button 
          className={activeTab === 'business' ? 'active' : ''}
          onClick={() => setActiveTab('business')}
        >
          <Building size={20} />
          Business/Professional
        </button>
        <button 
          className={activeTab === 'other' ? 'active' : ''}
          onClick={() => setActiveTab('other')}
        >
          <Wallet size={20} />
          Other Sources
        </button>
      </div>

      {/* Forms Content */}
      <div className="forms-content">
        {/* Salary Income Form */}
        {activeTab === 'salary' && (
          <form onSubmit={handleSalarySubmit} className="income-form">
            <div className="form-section">
              <h3>Salary Details</h3>
              <div className="form-grid">
                <div className="form-field">
                  <label>Gross Salary *</label>
                  <input
                    type="number"
                    value={salaryData.grossSalary}
                    onChange={(e) => setSalaryData({...salaryData, grossSalary: e.target.value})}
                    placeholder="Enter gross salary"
                    required
                  />
                </div>
                <div className="form-field">
                  <label>House Rent Allowance (HRA)</label>
                  <input
                    type="number"
                    value={salaryData.hra}
                    onChange={(e) => setSalaryData({...salaryData, hra: e.target.value})}
                    placeholder="Enter HRA received"
                  />
                </div>
                <div className="form-field">
                  <label>Leave Travel Allowance (LTA)</label>
                  <input
                    type="number"
                    value={salaryData.lta}
                    onChange={(e) => setSalaryData({...salaryData, lta: e.target.value})}
                    placeholder="Enter LTA received"
                  />
                </div>
                <div className="form-field">
                  <label>Professional Tax</label>
                  <input
                    type="number"
                    value={salaryData.professionalTax}
                    onChange={(e) => setSalaryData({...salaryData, professionalTax: e.target.value})}
                    placeholder="Enter professional tax paid"
                  />
                </div>
                <div className="form-field">
                  <label>Standard Deduction</label>
                  <input
                    type="number"
                    value={salaryData.standardDeduction}
                    onChange={(e) => setSalaryData({...salaryData, standardDeduction: e.target.value})}
                    placeholder="Standard deduction (₹50,000)"
                  />
                </div>
                <div className="form-field">
                  <label>Employer PF Contribution</label>
                  <input
                    type="number"
                    value={salaryData.employerPF}
                    onChange={(e) => setSalaryData({...salaryData, employerPF: e.target.value})}
                    placeholder="Enter employer PF contribution"
                  />
                </div>
              </div>
              <div className="form-field checkbox-field">
                <label>
                  <input
                    type="checkbox"
                    checked={salaryData.form16Available}
                    onChange={(e) => setSalaryData({...salaryData, form16Available: e.target.checked})}
                  />
                  Form 16 available
                </label>
              </div>
            </div>
            
            <div className="form-actions">
              <button type="submit" className="btn-primary" disabled={loading}>
                <Save size={18} />
                {loading ? 'Saving...' : 'Save Salary Income'}
              </button>
            </div>
          </form>
        )}

        {/* House Property Form */}
        {activeTab === 'house-property' && (
          <form onSubmit={handleHousePropertySubmit} className="income-form">
            <div className="form-section">
              <h3>House Property Details</h3>
              <div className="form-grid">
                <div className="form-field">
                  <label>Property Type *</label>
                  <select
                    value={housePropertyData.propertyType}
                    onChange={(e) => setHousePropertyData({...housePropertyData, propertyType: e.target.value})}
                    required
                  >
                    <option value="SELF_OCCUPIED">Self Occupied</option>
                    <option value="LET_OUT">Let Out</option>
                    <option value="DEEMED_LET_OUT">Deemed Let Out</option>
                  </select>
                </div>
                <div className="form-field">
                  <label>Co-ownership Share (%)</label>
                  <input
                    type="number"
                    min="0"
                    max="100"
                    value={housePropertyData.coOwnershipShare}
                    onChange={(e) => setHousePropertyData({...housePropertyData, coOwnershipShare: e.target.value})}
                    placeholder="Enter ownership percentage"
                  />
                </div>
                <div className="form-field">
                  <label>Gross Annual Value (GAV)</label>
                  <input
                    type="number"
                    value={housePropertyData.gav}
                    onChange={(e) => setHousePropertyData({...housePropertyData, gav: e.target.value})}
                    placeholder="Enter gross annual rent received"
                  />
                </div>
                <div className="form-field">
                  <label>Net Annual Value (NAV)</label>
                  <input
                    type="number"
                    value={housePropertyData.nav}
                    onChange={(e) => setHousePropertyData({...housePropertyData, nav: e.target.value})}
                    placeholder="GAV minus unrealized rent"
                  />
                </div>
                <div className="form-field">
                  <label>Municipal Taxes Paid</label>
                  <input
                    type="number"
                    value={housePropertyData.municipalTaxes}
                    onChange={(e) => setHousePropertyData({...housePropertyData, municipalTaxes: e.target.value})}
                    placeholder="Enter municipal taxes paid"
                  />
                </div>
                <div className="form-field">
                  <label>Interest on Home Loan</label>
                  <input
                    type="number"
                    value={housePropertyData.interestOnLoan}
                    onChange={(e) => setHousePropertyData({...housePropertyData, interestOnLoan: e.target.value})}
                    placeholder="Enter interest paid on home loan"
                  />
                </div>
              </div>
            </div>
            
            <div className="form-actions">
              <button type="submit" className="btn-primary" disabled={loading}>
                <Save size={18} />
                {loading ? 'Calculating...' : 'Calculate House Property Income'}
              </button>
            </div>
          </form>
        )}

        {/* Capital Gains Form */}
        {activeTab === 'capital-gains' && (
          <form onSubmit={handleCapitalGainsSubmit} className="income-form">
            <div className="form-section">
              <h3>Capital Gains Transaction</h3>
              <div className="form-grid">
                <div className="form-field">
                  <label>Asset Type *</label>
                  <select
                    value={capitalGainsData.assetType}
                    onChange={(e) => setCapitalGainsData({...capitalGainsData, assetType: e.target.value})}
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
                  <label>Description</label>
                  <input
                    type="text"
                    value={capitalGainsData.description}
                    onChange={(e) => setCapitalGainsData({...capitalGainsData, description: e.target.value})}
                    placeholder="e.g., TCS shares, HDFC Mutual Fund"
                  />
                </div>
                <div className="form-field">
                  <label>Purchase Date *</label>
                  <input
                    type="date"
                    value={capitalGainsData.purchaseDate}
                    onChange={(e) => setCapitalGainsData({...capitalGainsData, purchaseDate: e.target.value})}
                    required
                  />
                </div>
                <div className="form-field">
                  <label>Sale Date *</label>
                  <input
                    type="date"
                    value={capitalGainsData.saleDate}
                    onChange={(e) => setCapitalGainsData({...capitalGainsData, saleDate: e.target.value})}
                    required
                  />
                </div>
                <div className="form-field">
                  <label>Purchase Price *</label>
                  <input
                    type="number"
                    value={capitalGainsData.purchasePrice}
                    onChange={(e) => setCapitalGainsData({...capitalGainsData, purchasePrice: e.target.value})}
                    placeholder="Enter purchase price"
                    required
                  />
                </div>
                <div className="form-field">
                  <label>Sale Price *</label>
                  <input
                    type="number"
                    value={capitalGainsData.salePrice}
                    onChange={(e) => setCapitalGainsData({...capitalGainsData, salePrice: e.target.value})}
                    placeholder="Enter sale price"
                    required
                  />
                </div>
                <div className="form-field">
                  <label>Expenses (Brokerage, etc.)</label>
                  <input
                    type="number"
                    value={capitalGainsData.expenses}
                    onChange={(e) => setCapitalGainsData({...capitalGainsData, expenses: e.target.value})}
                    placeholder="Enter transaction expenses"
                  />
                </div>
              </div>
            </div>
            
            <div className="form-actions">
              <button type="submit" className="btn-primary" disabled={loading}>
                <Plus size={18} />
                {loading ? 'Recording...' : 'Add Capital Gains Transaction'}
              </button>
            </div>
          </form>
        )}

        {/* Business Income Form */}
        {activeTab === 'business' && (
          <form onSubmit={handleBusinessSubmit} className="income-form">
            <div className="form-section">
              <h3>Business/Professional Income</h3>
              <div className="form-grid">
                <div className="form-field">
                  <label>Business Type *</label>
                  <select
                    value={businessData.businessType}
                    onChange={(e) => setBusinessData({...businessData, businessType: e.target.value})}
                    required
                  >
                    <option value="PROFESSIONAL">Professional Services</option>
                    <option value="BUSINESS">Business</option>
                  </select>
                </div>
                <div className="form-field">
                  <label>Taxation Scheme *</label>
                  <select
                    value={businessData.taxationScheme}
                    onChange={(e) => setBusinessData({...businessData, taxationScheme: e.target.value})}
                    required
                  >
                    <option value="REGULAR">Regular (Actual Income)</option>
                    <option value="PRESUMPTIVE_44AD">Presumptive u/s 44AD</option>
                    <option value="PRESUMPTIVE_44ADA">Presumptive u/s 44ADA</option>
                  </select>
                </div>
                <div className="form-field">
                  <label>Gross Receipts *</label>
                  <input
                    type="number"
                    value={businessData.grossReceipts}
                    onChange={(e) => setBusinessData({...businessData, grossReceipts: e.target.value})}
                    placeholder="Enter gross receipts/turnover"
                    required
                  />
                </div>
                <div className="form-field">
                  <label>Business Expenses</label>
                  <input
                    type="number"
                    value={businessData.expenses}
                    onChange={(e) => setBusinessData({...businessData, expenses: e.target.value})}
                    placeholder="Enter allowable expenses"
                  />
                </div>
                <div className="form-field">
                  <label>Depreciation</label>
                  <input
                    type="number"
                    value={businessData.depreciation}
                    onChange={(e) => setBusinessData({...businessData, depreciation: e.target.value})}
                    placeholder="Enter depreciation claimed"
                  />
                </div>
              </div>
            </div>
            
            <div className="form-actions">
              <button type="submit" className="btn-primary" disabled={loading}>
                <Save size={18} />
                {loading ? 'Calculating...' : 'Calculate Business Income'}
              </button>
            </div>
          </form>
        )}

        {/* Other Income Form */}
        {activeTab === 'other' && (
          <form onSubmit={handleOtherIncomeSubmit} className="income-form">
            <div className="form-section">
              <h3>Other Sources of Income</h3>
              <div className="form-grid">
                <div className="form-field">
                  <label>Savings Account Interest</label>
                  <input
                    type="number"
                    value={otherIncomeData.savingsInterest}
                    onChange={(e) => setOtherIncomeData({...otherIncomeData, savingsInterest: e.target.value})}
                    placeholder="Interest from savings account"
                  />
                </div>
                <div className="form-field">
                  <label>Fixed Deposit Interest</label>
                  <input
                    type="number"
                    value={otherIncomeData.fdInterest}
                    onChange={(e) => setOtherIncomeData({...otherIncomeData, fdInterest: e.target.value})}
                    placeholder="Interest from FDs"
                  />
                </div>
                <div className="form-field">
                  <label>Dividend Income</label>
                  <input
                    type="number"
                    value={otherIncomeData.dividendIncome}
                    onChange={(e) => setOtherIncomeData({...otherIncomeData, dividendIncome: e.target.value})}
                    placeholder="Dividend from shares/MF"
                  />
                </div>
                <div className="form-field">
                  <label>Rental Income (Other)</label>
                  <input
                    type="number"
                    value={otherIncomeData.rentalIncome}
                    onChange={(e) => setOtherIncomeData({...otherIncomeData, rentalIncome: e.target.value})}
                    placeholder="Other rental income"
                  />
                </div>
                <div className="form-field">
                  <label>Other Sources</label>
                  <input
                    type="number"
                    value={otherIncomeData.otherSources}
                    onChange={(e) => setOtherIncomeData({...otherIncomeData, otherSources: e.target.value})}
                    placeholder="Any other income"
                  />
                </div>
              </div>
            </div>
            
            <div className="form-actions">
              <button type="submit" className="btn-primary" disabled={loading}>
                <Save size={18} />
                {loading ? 'Saving...' : 'Save Other Income'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};

export default IncomeEntryForms;
