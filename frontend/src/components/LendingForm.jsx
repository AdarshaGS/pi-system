import React, { useState, useEffect } from 'react';
import { X, DollarSign } from 'lucide-react';
import lendingApi from '../services/lendingApi';
import './LendingForm.css';

const LendingForm = ({ loan, userId, onClose }) => {
  const [formData, setFormData] = useState({
    userId: userId,
    loanType: 'PERSONAL',
    lenderName: '',
    borrowerName: '',
    principalAmount: '',
    interestRate: '',
    startDate: '',
    endDate: '',
    repaymentFrequency: 'MONTHLY',
    notes: '',
    status: 'ACTIVE'
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (loan) {
      setFormData({
        userId: userId,
        loanType: loan.loanType || 'PERSONAL',
        lenderName: loan.lenderName || '',
        borrowerName: loan.borrowerName || '',
        principalAmount: loan.principalAmount || '',
        interestRate: loan.interestRate || '',
        startDate: loan.startDate ? loan.startDate.split('T')[0] : '',
        endDate: loan.endDate ? loan.endDate.split('T')[0] : '',
        repaymentFrequency: loan.repaymentFrequency || 'MONTHLY',
        notes: loan.notes || '',
        status: loan.status || 'ACTIVE'
      });
    }
  }, [loan, userId]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const validateForm = () => {
    if (!formData.lenderName && !formData.borrowerName) {
      setError('Please provide either lender name or borrower name');
      return false;
    }
    if (!formData.principalAmount || formData.principalAmount <= 0) {
      setError('Please enter a valid principal amount');
      return false;
    }
    if (!formData.interestRate || formData.interestRate < 0) {
      setError('Please enter a valid interest rate');
      return false;
    }
    if (!formData.startDate) {
      setError('Please select a start date');
      return false;
    }
    if (!formData.endDate) {
      setError('Please select an end date');
      return false;
    }
    if (new Date(formData.endDate) <= new Date(formData.startDate)) {
      setError('End date must be after start date');
      return false;
    }
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!validateForm()) {
      return;
    }

    setLoading(true);
    try {
      const payload = {
        ...formData,
        principalAmount: parseFloat(formData.principalAmount),
        interestRate: parseFloat(formData.interestRate)
      };

      if (loan) {
        await lendingApi.updateLoan(loan.id, payload);
      } else {
        await lendingApi.createLoan(payload);
      }
      
      onClose();
    } catch (error) {
      console.error('Error saving loan:', error);
      setError(error.response?.data?.message || 'Failed to save loan. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content lending-form-modal">
        <div className="modal-header">
          <h2>
            <DollarSign size={24} />
            {loan ? 'Edit Loan' : 'Add New Loan'}
          </h2>
          <button className="close-btn" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="lending-form">
          {error && (
            <div className="error-message">
              {error}
            </div>
          )}

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="loanType">Loan Type <span className="required">*</span></label>
              <select
                id="loanType"
                name="loanType"
                value={formData.loanType}
                onChange={handleChange}
                required
              >
                <option value="PERSONAL">Personal</option>
                <option value="BUSINESS">Business</option>
                <option value="HOME">Home</option>
                <option value="AUTO">Auto</option>
                <option value="EDUCATION">Education</option>
                <option value="OTHER">Other</option>
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="status">Status <span className="required">*</span></label>
              <select
                id="status"
                name="status"
                value={formData.status}
                onChange={handleChange}
                required
              >
                <option value="ACTIVE">Active</option>
                <option value="CLOSED">Closed</option>
                <option value="DEFAULTED">Defaulted</option>
                <option value="PENDING">Pending</option>
              </select>
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="lenderName">Lender Name</label>
              <input
                type="text"
                id="lenderName"
                name="lenderName"
                value={formData.lenderName}
                onChange={handleChange}
                placeholder="Name of the person/institution lending"
              />
            </div>

            <div className="form-group">
              <label htmlFor="borrowerName">Borrower Name</label>
              <input
                type="text"
                id="borrowerName"
                name="borrowerName"
                value={formData.borrowerName}
                onChange={handleChange}
                placeholder="Name of the person borrowing"
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="principalAmount">Principal Amount (₹) <span className="required">*</span></label>
              <input
                type="number"
                id="principalAmount"
                name="principalAmount"
                value={formData.principalAmount}
                onChange={handleChange}
                placeholder="Enter loan amount"
                step="0.01"
                min="0"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="interestRate">Interest Rate (%) <span className="required">*</span></label>
              <input
                type="number"
                id="interestRate"
                name="interestRate"
                value={formData.interestRate}
                onChange={handleChange}
                placeholder="Enter interest rate"
                step="0.01"
                min="0"
                required
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="startDate">Start Date <span className="required">*</span></label>
              <input
                type="date"
                id="startDate"
                name="startDate"
                value={formData.startDate}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="endDate">End Date <span className="required">*</span></label>
              <input
                type="date"
                id="endDate"
                name="endDate"
                value={formData.endDate}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="repaymentFrequency">Repayment Frequency <span className="required">*</span></label>
            <select
              id="repaymentFrequency"
              name="repaymentFrequency"
              value={formData.repaymentFrequency}
              onChange={handleChange}
              required
            >
              <option value="DAILY">Daily</option>
              <option value="WEEKLY">Weekly</option>
              <option value="MONTHLY">Monthly</option>
              <option value="QUARTERLY">Quarterly</option>
              <option value="YEARLY">Yearly</option>
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="notes">Notes</label>
            <textarea
              id="notes"
              name="notes"
              value={formData.notes}
              onChange={handleChange}
              placeholder="Add any additional notes or comments..."
              rows="3"
            />
          </div>

          <div className="form-actions">
            <button 
              type="button" 
              className="btn-secondary" 
              onClick={onClose}
              disabled={loading}
            >
              Cancel
            </button>
            <button 
              type="submit" 
              className="btn-primary" 
              disabled={loading}
            >
              {loading ? 'Saving...' : loan ? 'Update Loan' : 'Create Loan'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default LendingForm;
