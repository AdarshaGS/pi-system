import React, { useState, useEffect } from 'react';
import { createOrUpdateTaxDetails } from '../api/taxApi';
import './TaxDetailsForm.css';

function TaxDetailsForm({ userId, taxDetails, onUpdate }) {
  const [formData, setFormData] = useState({
    regime: 'NEW',
    salaryIncome: 0,
    housePropertyIncome: 0,
    businessIncome: 0,
    otherIncome: 0,
    section80C: 0,
    section80D: 0,
    section80G: 0,
    section24B: 0,
    otherDeductions: 0,
    standardDeduction: 50000,
    professionalTax: 0
  });

  const [loading, setLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    if (taxDetails) {
      setFormData({
        regime: taxDetails.regime || 'NEW',
        salaryIncome: taxDetails.salaryIncome || 0,
        housePropertyIncome: taxDetails.housePropertyIncome || 0,
        businessIncome: taxDetails.businessIncome || 0,
        otherIncome: taxDetails.otherIncome || 0,
        section80C: taxDetails.section80C || 0,
        section80D: taxDetails.section80D || 0,
        section80G: taxDetails.section80G || 0,
        section24B: taxDetails.section24B || 0,
        otherDeductions: taxDetails.otherDeductions || 0,
        standardDeduction: taxDetails.standardDeduction || 50000,
        professionalTax: taxDetails.professionalTax || 0
      });
    }
  }, [taxDetails]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'regime' ? value : parseFloat(value) || 0
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setSuccessMessage('');
    setErrorMessage('');

    try {
      await createOrUpdateTaxDetails(userId, formData);
      setSuccessMessage('Tax details saved successfully!');
      setTimeout(() => {
        setSuccessMessage('');
        if (onUpdate) onUpdate();
      }, 2000);
    } catch (error) {
      setErrorMessage('Failed to save tax details. Please try again.');
      console.error('Error saving tax details:', error);
    } finally {
      setLoading(false);
    }
  };

  const calculateGrossTotalIncome = () => {
    return (
      formData.salaryIncome +
      formData.housePropertyIncome +
      formData.businessIncome +
      formData.otherIncome
    );
  };

  const calculateTotalDeductions = () => {
    if (formData.regime === 'NEW') {
      return formData.standardDeduction;
    }
    return (
      formData.section80C +
      formData.section80D +
      formData.section80G +
      formData.section24B +
      formData.standardDeduction +
      formData.otherDeductions
    );
  };

  const calculateTaxableIncome = () => {
    return Math.max(0, calculateGrossTotalIncome() - calculateTotalDeductions());
  };

  return (
    <div className="tax-details-form">
      <h2>Tax Details - FY 2025-26</h2>
      
      {successMessage && (
        <div className="alert alert-success">{successMessage}</div>
      )}
      
      {errorMessage && (
        <div className="alert alert-error">{errorMessage}</div>
      )}

      <form onSubmit={handleSubmit}>
        {/* Regime Selection */}
        <div className="form-section">
          <h3>Tax Regime</h3>
          <div className="form-group">
            <label>Select Tax Regime</label>
            <div className="regime-options">
              <label className="radio-option">
                <input
                  type="radio"
                  name="regime"
                  value="OLD"
                  checked={formData.regime === 'OLD'}
                  onChange={handleInputChange}
                />
                <span>Old Regime (with deductions)</span>
              </label>
              <label className="radio-option">
                <input
                  type="radio"
                  name="regime"
                  value="NEW"
                  checked={formData.regime === 'NEW'}
                  onChange={handleInputChange}
                />
                <span>New Regime (lower rates, limited deductions)</span>
              </label>
            </div>
          </div>
        </div>

        {/* Income Sources */}
        <div className="form-section">
          <h3>Income from Various Sources</h3>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="salaryIncome">
                Salary Income
                <span className="tooltip" title="Gross salary including allowances">ℹ️</span>
              </label>
              <input
                type="number"
                id="salaryIncome"
                name="salaryIncome"
                value={formData.salaryIncome}
                onChange={handleInputChange}
                placeholder="0"
                min="0"
                step="1000"
              />
            </div>

            <div className="form-group">
              <label htmlFor="housePropertyIncome">
                House Property Income
                <span className="tooltip" title="Rental income from properties">ℹ️</span>
              </label>
              <input
                type="number"
                id="housePropertyIncome"
                name="housePropertyIncome"
                value={formData.housePropertyIncome}
                onChange={handleInputChange}
                placeholder="0"
                min="0"
                step="1000"
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="businessIncome">
                Business/Profession Income
                <span className="tooltip" title="Income from business or profession">ℹ️</span>
              </label>
              <input
                type="number"
                id="businessIncome"
                name="businessIncome"
                value={formData.businessIncome}
                onChange={handleInputChange}
                placeholder="0"
                min="0"
                step="1000"
              />
            </div>

            <div className="form-group">
              <label htmlFor="otherIncome">
                Other Sources Income
                <span className="tooltip" title="Interest, dividends, etc.">ℹ️</span>
              </label>
              <input
                type="number"
                id="otherIncome"
                name="otherIncome"
                value={formData.otherIncome}
                onChange={handleInputChange}
                placeholder="0"
                min="0"
                step="1000"
              />
            </div>
          </div>
        </div>

        {/* Deductions (Old Regime) */}
        {formData.regime === 'OLD' && (
          <div className="form-section">
            <h3>Deductions (Old Regime Only)</h3>
            
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="section80C">
                  Section 80C
                  <span className="tooltip" title="PPF, ELSS, Life Insurance (Max: ₹1.5L)">ℹ️</span>
                </label>
                <input
                  type="number"
                  id="section80C"
                  name="section80C"
                  value={formData.section80C}
                  onChange={handleInputChange}
                  placeholder="0"
                  min="0"
                  max="150000"
                  step="1000"
                />
                <small>Max: ₹1,50,000</small>
              </div>

              <div className="form-group">
                <label htmlFor="section80D">
                  Section 80D
                  <span className="tooltip" title="Health insurance premium (Max: ₹25K-₹50K)">ℹ️</span>
                </label>
                <input
                  type="number"
                  id="section80D"
                  name="section80D"
                  value={formData.section80D}
                  onChange={handleInputChange}
                  placeholder="0"
                  min="0"
                  max="100000"
                  step="1000"
                />
                <small>Max: ₹25K self + ₹25K parents (₹50K if senior citizen)</small>
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="section80G">
                  Section 80G
                  <span className="tooltip" title="Donations to charitable institutions">ℹ️</span>
                </label>
                <input
                  type="number"
                  id="section80G"
                  name="section80G"
                  value={formData.section80G}
                  onChange={handleInputChange}
                  placeholder="0"
                  min="0"
                  step="1000"
                />
              </div>

              <div className="form-group">
                <label htmlFor="section24B">
                  Section 24B
                  <span className="tooltip" title="Home loan interest (Max: ₹2L)">ℹ️</span>
                </label>
                <input
                  type="number"
                  id="section24B"
                  name="section24B"
                  value={formData.section24B}
                  onChange={handleInputChange}
                  placeholder="0"
                  min="0"
                  max="200000"
                  step="1000"
                />
                <small>Max: ₹2,00,000</small>
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="otherDeductions">
                  Other Deductions
                  <span className="tooltip" title="80E, 80TTA, etc.">ℹ️</span>
                </label>
                <input
                  type="number"
                  id="otherDeductions"
                  name="otherDeductions"
                  value={formData.otherDeductions}
                  onChange={handleInputChange}
                  placeholder="0"
                  min="0"
                  step="1000"
                />
              </div>

              <div className="form-group">
                <label htmlFor="professionalTax">Professional Tax</label>
                <input
                  type="number"
                  id="professionalTax"
                  name="professionalTax"
                  value={formData.professionalTax}
                  onChange={handleInputChange}
                  placeholder="0"
                  min="0"
                  max="2500"
                  step="100"
                />
                <small>Max: ₹2,500</small>
              </div>
            </div>
          </div>
        )}

        {/* Standard Deduction */}
        <div className="form-section">
          <h3>Standard Deduction</h3>
          <div className="form-group">
            <label htmlFor="standardDeduction">
              Standard Deduction (Both Regimes)
            </label>
            <input
              type="number"
              id="standardDeduction"
              name="standardDeduction"
              value={formData.standardDeduction}
              onChange={handleInputChange}
              placeholder="50000"
              min="0"
              step="1000"
            />
            <small>Standard deduction from salary income: ₹50,000</small>
          </div>
        </div>

        {/* Summary */}
        <div className="form-section calculation-summary">
          <h3>Tax Calculation Summary</h3>
          <div className="summary-row">
            <span>Gross Total Income:</span>
            <strong>₹{calculateGrossTotalIncome().toLocaleString()}</strong>
          </div>
          <div className="summary-row">
            <span>Total Deductions:</span>
            <strong>- ₹{calculateTotalDeductions().toLocaleString()}</strong>
          </div>
          <div className="summary-row total">
            <span>Taxable Income:</span>
            <strong>₹{calculateTaxableIncome().toLocaleString()}</strong>
          </div>
        </div>

        {/* Submit Button */}
        <div className="form-actions">
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Saving...' : 'Save Tax Details'}
          </button>
        </div>
      </form>
    </div>
  );
}

export default TaxDetailsForm;
