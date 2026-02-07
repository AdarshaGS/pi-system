import React, { useState, useEffect } from 'react';
import { X, Plus, Calendar, DollarSign, TrendingUp, Download } from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import lendingApi from '../services/lendingApi';
import './RepaymentTracker.css';

const RepaymentTracker = ({ loan, onClose }) => {
  const [repayments, setRepayments] = useState([]);
  const [schedule, setSchedule] = useState([]);
  const [amortization, setAmortization] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showRepaymentForm, setShowRepaymentForm] = useState(false);
  const [activeView, setActiveView] = useState('history');

  const [formData, setFormData] = useState({
    loanId: loan.id,
    paymentDate: new Date().toISOString().split('T')[0],
    principalAmount: '',
    interestAmount: '',
    notes: ''
  });

  useEffect(() => {
    loadRepaymentData();
  }, [loan.id]);

  const loadRepaymentData = async () => {
    setLoading(true);
    try {
      const [repaymentsRes, scheduleRes, amortizationRes] = await Promise.all([
        lendingApi.getRepaymentsByLoan(loan.id),
        lendingApi.getRepaymentSchedule(loan.id),
        lendingApi.getAmortizationSchedule(loan.id)
      ]);

      setRepayments(Array.isArray(repaymentsRes) ? repaymentsRes : []);
      setSchedule(Array.isArray(scheduleRes) ? scheduleRes : []);
      setAmortization(Array.isArray(amortizationRes) ? amortizationRes : []);
    } catch (error) {
      console.error('Error loading repayment data:', error);
      setRepayments([]);
      setSchedule([]);
      setAmortization([]);
    } finally {
      setLoading(false);
    }
  };

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleRecordRepayment = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        ...formData,
        principalAmount: parseFloat(formData.principalAmount),
        interestAmount: parseFloat(formData.interestAmount)
      };

      await lendingApi.recordRepayment(payload);
      setShowRepaymentForm(false);
      setFormData({
        loanId: loan.id,
        paymentDate: new Date().toISOString().split('T')[0],
        principalAmount: '',
        interestAmount: '',
        notes: ''
      });
      loadRepaymentData();
    } catch (error) {
      console.error('Error recording repayment:', error);
      alert('Failed to record repayment');
    }
  };

  const handleDeleteRepayment = async (repaymentId) => {
    if (window.confirm('Are you sure you want to delete this repayment?')) {
      try {
        await lendingApi.deleteRepayment(repaymentId);
        loadRepaymentData();
      } catch (error) {
        console.error('Error deleting repayment:', error);
        alert('Failed to delete repayment');
      }
    }
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

  const totalRepaid = repayments.reduce((sum, r) => sum + (r.principalAmount || 0) + (r.interestAmount || 0), 0);
  const totalPrincipal = repayments.reduce((sum, r) => sum + (r.principalAmount || 0), 0);
  const totalInterest = repayments.reduce((sum, r) => sum + (r.interestAmount || 0), 0);

  return (
    <div className="modal-overlay">
      <div className="modal-content repayment-tracker-modal">
        <div className="modal-header">
          <div>
            <h2>Repayment Tracker</h2>
            <p className="loan-info">
              {loan.lenderName || loan.borrowerName} • {formatCurrency(loan.principalAmount)} @ {loan.interestRate}%
            </p>
          </div>
          <button className="close-btn" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        <div className="repayment-summary-cards">
          <div className="summary-card-small">
            <div className="summary-icon-small" style={{ backgroundColor: '#e3f2fd' }}>
              <DollarSign size={20} color="#2196F3" />
            </div>
            <div>
              <div className="summary-label-small">Total Repaid</div>
              <div className="summary-value-small">{formatCurrency(totalRepaid)}</div>
            </div>
          </div>

          <div className="summary-card-small">
            <div className="summary-icon-small" style={{ backgroundColor: '#e8f5e9' }}>
              <TrendingUp size={20} color="#4CAF50" />
            </div>
            <div>
              <div className="summary-label-small">Principal Paid</div>
              <div className="summary-value-small">{formatCurrency(totalPrincipal)}</div>
            </div>
          </div>

          <div className="summary-card-small">
            <div className="summary-icon-small" style={{ backgroundColor: '#fff3e0' }}>
              <Calendar size={20} color="#FF9800" />
            </div>
            <div>
              <div className="summary-label-small">Interest Paid</div>
              <div className="summary-value-small">{formatCurrency(totalInterest)}</div>
            </div>
          </div>

          <div className="summary-card-small">
            <div className="summary-icon-small" style={{ backgroundColor: '#f3e5f5' }}>
              <DollarSign size={20} color="#9C27B0" />
            </div>
            <div>
              <div className="summary-label-small">Outstanding</div>
              <div className="summary-value-small">{formatCurrency(loan.outstandingAmount || 0)}</div>
            </div>
          </div>
        </div>

        <div className="tracker-actions">
          <div className="view-tabs">
            <button 
              className={`view-tab ${activeView === 'history' ? 'active' : ''}`}
              onClick={() => setActiveView('history')}
            >
              Payment History
            </button>
            <button 
              className={`view-tab ${activeView === 'schedule' ? 'active' : ''}`}
              onClick={() => setActiveView('schedule')}
            >
              Schedule
            </button>
            <button 
              className={`view-tab ${activeView === 'amortization' ? 'active' : ''}`}
              onClick={() => setActiveView('amortization')}
            >
              Amortization
            </button>
          </div>

          <button 
            className="btn-add-repayment" 
            onClick={() => setShowRepaymentForm(!showRepaymentForm)}
          >
            <Plus size={18} />
            Record Payment
          </button>
        </div>

        {showRepaymentForm && (
          <div className="repayment-form-section">
            <form onSubmit={handleRecordRepayment} className="repayment-form">
              <div className="form-row-repayment">
                <div className="form-group-repayment">
                  <label>Payment Date</label>
                  <input
                    type="date"
                    name="paymentDate"
                    value={formData.paymentDate}
                    onChange={handleFormChange}
                    required
                  />
                </div>

                <div className="form-group-repayment">
                  <label>Principal Amount (₹)</label>
                  <input
                    type="number"
                    name="principalAmount"
                    value={formData.principalAmount}
                    onChange={handleFormChange}
                    placeholder="0.00"
                    step="0.01"
                    min="0"
                    required
                  />
                </div>

                <div className="form-group-repayment">
                  <label>Interest Amount (₹)</label>
                  <input
                    type="number"
                    name="interestAmount"
                    value={formData.interestAmount}
                    onChange={handleFormChange}
                    placeholder="0.00"
                    step="0.01"
                    min="0"
                    required
                  />
                </div>
              </div>

              <div className="form-group-repayment">
                <label>Notes</label>
                <input
                  type="text"
                  name="notes"
                  value={formData.notes}
                  onChange={handleFormChange}
                  placeholder="Add any notes..."
                />
              </div>

              <div className="form-actions-repayment">
                <button 
                  type="button" 
                  className="btn-cancel"
                  onClick={() => setShowRepaymentForm(false)}
                >
                  Cancel
                </button>
                <button type="submit" className="btn-submit">
                  Record Payment
                </button>
              </div>
            </form>
          </div>
        )}

        <div className="tracker-content">
          {loading ? (
            <div className="loading-state">Loading repayment data...</div>
          ) : (
            <>
              {activeView === 'history' && (
                <div className="repayment-history">
                  {repayments.length > 0 ? (
                    <table className="repayment-table">
                      <thead>
                        <tr>
                          <th>Date</th>
                          <th>Principal</th>
                          <th>Interest</th>
                          <th>Total</th>
                          <th>Notes</th>
                          <th>Actions</th>
                        </tr>
                      </thead>
                      <tbody>
                        {repayments.map((repayment) => (
                          <tr key={repayment.id}>
                            <td>{formatDate(repayment.paymentDate)}</td>
                            <td>{formatCurrency(repayment.principalAmount)}</td>
                            <td>{formatCurrency(repayment.interestAmount)}</td>
                            <td><strong>{formatCurrency((repayment.principalAmount || 0) + (repayment.interestAmount || 0))}</strong></td>
                            <td>{repayment.notes || '-'}</td>
                            <td>
                              <button 
                                className="btn-delete-small"
                                onClick={() => handleDeleteRepayment(repayment.id)}
                              >
                                Delete
                              </button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                      <tfoot>
                        <tr className="totals-row">
                          <td><strong>Totals:</strong></td>
                          <td><strong>{formatCurrency(totalPrincipal)}</strong></td>
                          <td><strong>{formatCurrency(totalInterest)}</strong></td>
                          <td><strong>{formatCurrency(totalRepaid)}</strong></td>
                          <td colSpan="2"></td>
                        </tr>
                      </tfoot>
                    </table>
                  ) : (
                    <div className="empty-state">
                      <Calendar size={48} color="#ccc" />
                      <p>No repayments recorded yet</p>
                      <button className="btn-primary" onClick={() => setShowRepaymentForm(true)}>
                        <Plus size={18} />
                        Record First Payment
                      </button>
                    </div>
                  )}
                </div>
              )}

              {activeView === 'schedule' && (
                <div className="repayment-schedule">
                  {schedule.length > 0 ? (
                    <table className="schedule-table">
                      <thead>
                        <tr>
                          <th>Payment #</th>
                          <th>Due Date</th>
                          <th>Principal</th>
                          <th>Interest</th>
                          <th>Total Payment</th>
                          <th>Balance</th>
                          <th>Status</th>
                        </tr>
                      </thead>
                      <tbody>
                        {schedule.map((payment, index) => (
                          <tr key={index}>
                            <td>{payment.paymentNumber}</td>
                            <td>{formatDate(payment.dueDate)}</td>
                            <td>{formatCurrency(payment.principalAmount)}</td>
                            <td>{formatCurrency(payment.interestAmount)}</td>
                            <td><strong>{formatCurrency(payment.totalAmount)}</strong></td>
                            <td>{formatCurrency(payment.remainingBalance)}</td>
                            <td>
                              <span className={`status-badge ${payment.status?.toLowerCase()}`}>
                                {payment.status}
                              </span>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  ) : (
                    <div className="empty-state">
                      <Calendar size={48} color="#ccc" />
                      <p>No repayment schedule available</p>
                    </div>
                  )}
                </div>
              )}

              {activeView === 'amortization' && (
                <div className="amortization-view">
                  {amortization.length > 0 ? (
                    <>
                      <div className="amortization-chart">
                        <ResponsiveContainer width="100%" height={300}>
                          <LineChart data={amortization}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="period" />
                            <YAxis />
                            <Tooltip formatter={(value) => formatCurrency(value)} />
                            <Legend />
                            <Line type="monotone" dataKey="principal" stroke="#2196F3" name="Principal" strokeWidth={2} />
                            <Line type="monotone" dataKey="interest" stroke="#FF9800" name="Interest" strokeWidth={2} />
                            <Line type="monotone" dataKey="balance" stroke="#4CAF50" name="Balance" strokeWidth={2} />
                          </LineChart>
                        </ResponsiveContainer>
                      </div>

                      <table className="amortization-table">
                        <thead>
                          <tr>
                            <th>Period</th>
                            <th>Payment</th>
                            <th>Principal</th>
                            <th>Interest</th>
                            <th>Balance</th>
                          </tr>
                        </thead>
                        <tbody>
                          {amortization.map((row, index) => (
                            <tr key={index}>
                              <td>{row.period}</td>
                              <td>{formatCurrency(row.payment)}</td>
                              <td>{formatCurrency(row.principal)}</td>
                              <td>{formatCurrency(row.interest)}</td>
                              <td>{formatCurrency(row.balance)}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </>
                  ) : (
                    <div className="empty-state">
                      <TrendingUp size={48} color="#ccc" />
                      <p>No amortization schedule available</p>
                    </div>
                  )}
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default RepaymentTracker;
