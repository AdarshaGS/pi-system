import React, { useState, useEffect, useMemo } from 'react';
import { PieChart, Pie, Cell, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { DollarSign, TrendingUp, Calendar, AlertCircle, Plus, Edit2, Trash2, Eye } from 'lucide-react';
import lendingApi from '../services/lendingApi';
import LendingForm from './LendingForm';
import RepaymentTracker from './RepaymentTracker';
import './Lending.css';

const Lending = () => {
  const user = JSON.parse(localStorage.getItem('user'));
  const userId = useMemo(() => user?.userId, []);

  const [summary, setSummary] = useState(null);
  const [loans, setLoans] = useState([]);
  const [upcomingPayments, setUpcomingPayments] = useState([]);
  const [overduePayments, setOverduePayments] = useState([]);
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showLoanForm, setShowLoanForm] = useState(false);
  const [showRepaymentTracker, setShowRepaymentTracker] = useState(false);
  const [selectedLoan, setSelectedLoan] = useState(null);
  const [activeTab, setActiveTab] = useState('overview');

  useEffect(() => {
    if (userId) {
      loadLendingData();
    }
  }, [userId]);

  const loadLendingData = async () => {
    setLoading(true);
    try {
      const [summaryRes, loansRes, upcomingRes, overdueRes, analyticsRes] = await Promise.all([
        lendingApi.getLoanSummary(userId),
        lendingApi.getAllLoans(userId),
        lendingApi.getUpcomingPayments(userId),
        lendingApi.getOverduePayments(userId),
        lendingApi.getLendingAnalytics(userId)
      ]);

      setSummary(summaryRes || {});
      setLoans(Array.isArray(loansRes) ? loansRes : []);
      setUpcomingPayments(Array.isArray(upcomingRes) ? upcomingRes : []);
      setOverduePayments(Array.isArray(overdueRes) ? overdueRes : []);
      setAnalytics(analyticsRes || {});
    } catch (error) {
      console.error('Error loading lending data:', error);
      // Set empty data on error
      setSummary({});
      setLoans([]);
      setUpcomingPayments([]);
      setOverduePayments([]);
      setAnalytics({});
    } finally {
      setLoading(false);
    }
  };

  const handleAddLoan = () => {
    setSelectedLoan(null);
    setShowLoanForm(true);
  };

  const handleEditLoan = (loan) => {
    setSelectedLoan(loan);
    setShowLoanForm(true);
  };

  const handleDeleteLoan = async (loanId) => {
    if (window.confirm('Are you sure you want to delete this loan?')) {
      try {
        await lendingApi.deleteLoan(loanId);
        loadLendingData();
      } catch (error) {
        console.error('Error deleting loan:', error);
        alert('Failed to delete loan');
      }
    }
  };

  const handleViewRepayments = (loan) => {
    setSelectedLoan(loan);
    setShowRepaymentTracker(true);
  };

  const handleLoanFormClose = () => {
    setShowLoanForm(false);
    setSelectedLoan(null);
    loadLendingData();
  };

  const handleRepaymentTrackerClose = () => {
    setShowRepaymentTracker(false);
    setSelectedLoan(null);
    loadLendingData();
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0
    }).format(amount);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-IN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const getLoanStatusClass = (status) => {
    switch (status?.toLowerCase()) {
      case 'active': return 'status-active';
      case 'closed': return 'status-closed';
      case 'defaulted': return 'status-defaulted';
      default: return 'status-pending';
    }
  };

  const COLORS = ['#4CAF50', '#2196F3', '#FF9800', '#F44336', '#9C27B0'];

  if (loading) {
    return <div className="lending-container"><div className="loading">Loading lending data...</div></div>;
  }

  return (
    <div className="lending-container">
      <div className="lending-header">
        <h1>Lending & Borrowing</h1>
        <button className="btn-primary" onClick={handleAddLoan}>
          <Plus size={20} /> Add Loan
        </button>
      </div>

      {/* Summary Cards */}
      <div className="lending-summary">
        <div className="summary-card">
          <div className="summary-icon" style={{ backgroundColor: '#e3f2fd' }}>
            <DollarSign size={24} color="#2196F3" />
          </div>
          <div className="summary-content">
            <h3>Total Borrowed</h3>
            <p className="summary-value">{formatCurrency(summary?.totalBorrowed || 0)}</p>
            <span className="summary-label">{summary?.totalLoans || 0} active loans</span>
          </div>
        </div>

        <div className="summary-card">
          <div className="summary-icon" style={{ backgroundColor: '#e8f5e9' }}>
            <TrendingUp size={24} color="#4CAF50" />
          </div>
          <div className="summary-content">
            <h3>Total Repaid</h3>
            <p className="summary-value">{formatCurrency(summary?.totalRepaid || 0)}</p>
            <span className="summary-label">{((summary?.totalRepaid / summary?.totalBorrowed) * 100 || 0).toFixed(1)}% paid</span>
          </div>
        </div>

        <div className="summary-card">
          <div className="summary-icon" style={{ backgroundColor: '#fff3e0' }}>
            <Calendar size={24} color="#FF9800" />
          </div>
          <div className="summary-content">
            <h3>Outstanding</h3>
            <p className="summary-value">{formatCurrency(summary?.outstandingAmount || 0)}</p>
            <span className="summary-label">{upcomingPayments.length} upcoming payments</span>
          </div>
        </div>

        <div className="summary-card">
          <div className="summary-icon" style={{ backgroundColor: '#ffebee' }}>
            <AlertCircle size={24} color="#F44336" />
          </div>
          <div className="summary-content">
            <h3>Overdue</h3>
            <p className="summary-value">{formatCurrency(summary?.overdueAmount || 0)}</p>
            <span className="summary-label">{overduePayments.length} overdue payments</span>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="lending-tabs">
        <button 
          className={`tab-btn ${activeTab === 'overview' ? 'active' : ''}`}
          onClick={() => setActiveTab('overview')}
        >
          Overview
        </button>
        <button 
          className={`tab-btn ${activeTab === 'loans' ? 'active' : ''}`}
          onClick={() => setActiveTab('loans')}
        >
          All Loans
        </button>
        <button 
          className={`tab-btn ${activeTab === 'upcoming' ? 'active' : ''}`}
          onClick={() => setActiveTab('upcoming')}
        >
          Upcoming Payments ({upcomingPayments.length})
        </button>
        <button 
          className={`tab-btn ${activeTab === 'overdue' ? 'active' : ''}`}
          onClick={() => setActiveTab('overdue')}
        >
          Overdue ({overduePayments.length})
        </button>
      </div>

      {/* Tab Content */}
      {activeTab === 'overview' && (
        <div className="overview-content">
          <div className="charts-row">
            <div className="chart-card">
              <h3>Loan Distribution</h3>
              {analytics?.loansByType && analytics.loansByType.length > 0 ? (
                <ResponsiveContainer width="100%" height={300}>
                  <PieChart>
                    <Pie
                      data={analytics.loansByType}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      label={(entry) => `${entry.name}: ${entry.value}`}
                      outerRadius={100}
                      fill="#8884d8"
                      dataKey="value"
                    >
                      {analytics.loansByType.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <div className="no-data">No loan data available</div>
              )}
            </div>

            <div className="chart-card">
              <h3>Repayment Trend</h3>
              {analytics?.repaymentTrend && analytics.repaymentTrend.length > 0 ? (
                <ResponsiveContainer width="100%" height={300}>
                  <LineChart data={analytics.repaymentTrend}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="month" />
                    <YAxis />
                    <Tooltip formatter={(value) => formatCurrency(value)} />
                    <Legend />
                    <Line type="monotone" dataKey="principal" stroke="#2196F3" name="Principal" />
                    <Line type="monotone" dataKey="interest" stroke="#FF9800" name="Interest" />
                  </LineChart>
                </ResponsiveContainer>
              ) : (
                <div className="no-data">No repayment data available</div>
              )}
            </div>
          </div>

          {/* Quick Stats */}
          <div className="quick-stats">
            <div className="stat-item">
              <span className="stat-label">Average Interest Rate</span>
              <span className="stat-value">{analytics?.avgInterestRate?.toFixed(2) || '0.00'}%</span>
            </div>
            <div className="stat-item">
              <span className="stat-label">Total Interest Paid</span>
              <span className="stat-value">{formatCurrency(analytics?.totalInterestPaid || 0)}</span>
            </div>
            <div className="stat-item">
              <span className="stat-label">Next Payment Due</span>
              <span className="stat-value">
                {upcomingPayments.length > 0 
                  ? formatDate(upcomingPayments[0].dueDate)
                  : 'No upcoming payments'}
              </span>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'loans' && (
        <div className="loans-table-container">
          <table className="loans-table">
            <thead>
              <tr>
                <th>Lender/Borrower</th>
                <th>Type</th>
                <th>Amount</th>
                <th>Interest Rate</th>
                <th>Start Date</th>
                <th>End Date</th>
                <th>Outstanding</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loans.length > 0 ? (
                loans.map((loan) => (
                  <tr key={loan.id}>
                    <td>{loan.lenderName || loan.borrowerName}</td>
                    <td>
                      <span className={`loan-type ${loan.loanType?.toLowerCase()}`}>
                        {loan.loanType}
                      </span>
                    </td>
                    <td>{formatCurrency(loan.principalAmount)}</td>
                    <td>{loan.interestRate}%</td>
                    <td>{formatDate(loan.startDate)}</td>
                    <td>{formatDate(loan.endDate)}</td>
                    <td>{formatCurrency(loan.outstandingAmount)}</td>
                    <td>
                      <span className={`loan-status ${getLoanStatusClass(loan.status)}`}>
                        {loan.status}
                      </span>
                    </td>
                    <td>
                      <div className="action-buttons">
                        <button 
                          className="btn-icon" 
                          onClick={() => handleViewRepayments(loan)}
                          title="View Repayments"
                        >
                          <Eye size={16} />
                        </button>
                        <button 
                          className="btn-icon" 
                          onClick={() => handleEditLoan(loan)}
                          title="Edit Loan"
                        >
                          <Edit2 size={16} />
                        </button>
                        <button 
                          className="btn-icon btn-delete" 
                          onClick={() => handleDeleteLoan(loan.id)}
                          title="Delete Loan"
                        >
                          <Trash2 size={16} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="9" className="no-data">No loans found. Click "Add Loan" to create one.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {activeTab === 'upcoming' && (
        <div className="payments-table-container">
          <table className="payments-table">
            <thead>
              <tr>
                <th>Loan</th>
                <th>Due Date</th>
                <th>Principal</th>
                <th>Interest</th>
                <th>Total Amount</th>
                <th>Days Until Due</th>
              </tr>
            </thead>
            <tbody>
              {upcomingPayments.length > 0 ? (
                upcomingPayments.map((payment) => (
                  <tr key={payment.id}>
                    <td>{payment.loanName || `Loan #${payment.loanId}`}</td>
                    <td>{formatDate(payment.dueDate)}</td>
                    <td>{formatCurrency(payment.principalAmount)}</td>
                    <td>{formatCurrency(payment.interestAmount)}</td>
                    <td><strong>{formatCurrency(payment.totalAmount)}</strong></td>
                    <td>
                      <span className="days-badge">
                        {Math.ceil((new Date(payment.dueDate) - new Date()) / (1000 * 60 * 60 * 24))} days
                      </span>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="6" className="no-data">No upcoming payments</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {activeTab === 'overdue' && (
        <div className="payments-table-container">
          <table className="payments-table overdue-table">
            <thead>
              <tr>
                <th>Loan</th>
                <th>Due Date</th>
                <th>Principal</th>
                <th>Interest</th>
                <th>Total Amount</th>
                <th>Days Overdue</th>
              </tr>
            </thead>
            <tbody>
              {overduePayments.length > 0 ? (
                overduePayments.map((payment) => (
                  <tr key={payment.id} className="overdue-row">
                    <td>{payment.loanName || `Loan #${payment.loanId}`}</td>
                    <td>{formatDate(payment.dueDate)}</td>
                    <td>{formatCurrency(payment.principalAmount)}</td>
                    <td>{formatCurrency(payment.interestAmount)}</td>
                    <td><strong>{formatCurrency(payment.totalAmount)}</strong></td>
                    <td>
                      <span className="overdue-badge">
                        {Math.ceil((new Date() - new Date(payment.dueDate)) / (1000 * 60 * 60 * 24))} days overdue
                      </span>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="6" className="no-data">No overdue payments</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* Modals */}
      {showLoanForm && (
        <LendingForm
          loan={selectedLoan}
          userId={userId}
          onClose={handleLoanFormClose}
        />
      )}

      {showRepaymentTracker && selectedLoan && (
        <RepaymentTracker
          loan={selectedLoan}
          onClose={handleRepaymentTrackerClose}
        />
      )}
    </div>
  );
};

export default Lending;
