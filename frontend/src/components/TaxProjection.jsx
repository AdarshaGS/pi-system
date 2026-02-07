import React from 'react';
import './TaxProjection.css';

function TaxProjection({ userId, financialYear, projection, onRefresh }) {
  if (!projection) {
    return (
      <div className="tax-projection">
        <div className="empty-state">
          <p>Complete your tax details to see projection</p>
          <button onClick={onRefresh} className="btn-primary">
            Calculate Projection
          </button>
        </div>
      </div>
    );
  }

  const {
    grossTotalIncome,
    totalDeductions,
    taxableIncome,
    totalTaxLiability,
    tdsPaid,
    balanceTax,
    effectiveTaxRate,
    taxBreakdown
  } = projection;

  return (
    <div className="tax-projection">
      <div className="projection-header">
        <h2>Tax Projection - FY {financialYear}</h2>
        <button onClick={onRefresh} className="btn-secondary">
          Refresh Projection
        </button>
      </div>

      {/* Main Tax Calculation */}
      <div className="tax-calculation-flow">
        <div className="flow-step">
          <div className="step-label">Gross Total Income</div>
          <div className="step-value">₹{grossTotalIncome?.toLocaleString() || '0'}</div>
        </div>

        <div className="flow-arrow">−</div>

        <div className="flow-step">
          <div className="step-label">Total Deductions</div>
          <div className="step-value deduction">₹{totalDeductions?.toLocaleString() || '0'}</div>
        </div>

        <div className="flow-arrow">=</div>

        <div className="flow-step highlight">
          <div className="step-label">Taxable Income</div>
          <div className="step-value">₹{taxableIncome?.toLocaleString() || '0'}</div>
        </div>

        <div className="flow-arrow">→</div>

        <div className="flow-step highlight">
          <div className="step-label">Tax Liability</div>
          <div className="step-value tax">₹{totalTaxLiability?.toLocaleString() || '0'}</div>
        </div>
      </div>

      {/* Tax Breakdown */}
      {taxBreakdown && (
        <div className="tax-breakdown-section">
          <h3>Tax Slab-wise Breakdown</h3>
          <div className="breakdown-table">
            <table>
              <thead>
                <tr>
                  <th>Income Slab</th>
                  <th>Rate</th>
                  <th>Taxable Amount</th>
                  <th>Tax</th>
                </tr>
              </thead>
              <tbody>
                {taxBreakdown.map((slab, index) => (
                  <tr key={index}>
                    <td>{slab.slabRange}</td>
                    <td>{slab.rate}%</td>
                    <td>₹{slab.taxableAmount?.toLocaleString()}</td>
                    <td>₹{slab.tax?.toLocaleString()}</td>
                  </tr>
                ))}
                <tr className="total-row">
                  <td colSpan="3"><strong>Total Tax (before cess)</strong></td>
                  <td><strong>₹{taxBreakdown.reduce((sum, s) => sum + (s.tax || 0), 0).toLocaleString()}</strong></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Final Calculation */}
      <div className="final-calculation">
        <h3>Final Tax Calculation</h3>
        <div className="calc-steps">
          <div className="calc-row">
            <span>Tax as per slabs</span>
            <span>₹{(totalTaxLiability / 1.04)?.toFixed(0).toLocaleString()}</span>
          </div>
          <div className="calc-row">
            <span>Add: Health & Education Cess @ 4%</span>
            <span>+ ₹{((totalTaxLiability / 1.04) * 0.04)?.toFixed(0).toLocaleString()}</span>
          </div>
          <div className="calc-row total">
            <span><strong>Total Tax Liability</strong></span>
            <span><strong>₹{totalTaxLiability?.toLocaleString()}</strong></span>
          </div>
          <div className="calc-row">
            <span>Less: TDS Already Paid</span>
            <span className="deduction">- ₹{tdsPaid?.toLocaleString() || '0'}</span>
          </div>
          <div className={`calc-row balance ${balanceTax >= 0 ? 'payment-due' : 'refund'}`}>
            <span><strong>{balanceTax >= 0 ? 'Balance Tax to Pay' : 'Refund Due'}</strong></span>
            <span><strong>₹{Math.abs(balanceTax || 0).toLocaleString()}</strong></span>
          </div>
        </div>
      </div>

      {/* Effective Tax Rate */}
      <div className="effective-rate">
        <div className="rate-card">
          <h4>Effective Tax Rate</h4>
          <div className="rate-value">{effectiveTaxRate?.toFixed(2) || '0'}%</div>
          <p>of your gross total income</p>
        </div>
        <div className="rate-info">
          <p>
            Your effective tax rate is {effectiveTaxRate?.toFixed(2)}%, meaning you pay 
            ₹{((grossTotalIncome * effectiveTaxRate) / 100)?.toFixed(0).toLocaleString()} 
            in taxes for every ₹{grossTotalIncome?.toLocaleString()} earned.
          </p>
        </div>
      </div>

      {/* Tax Optimization Tips */}
      <div className="optimization-tips">
        <h3>💡 Tax Optimization Opportunities</h3>
        <div className="tips-grid">
          {balanceTax > 10000 && (
            <div className="tip-card">
              <div className="tip-icon">💰</div>
              <h4>Advance Tax Payment</h4>
              <p>
                You may need to pay advance tax as your balance is over ₹10,000. 
                Calculate quarterly installments to avoid interest under Section 234C.
              </p>
            </div>
          )}

          {effectiveTaxRate > 15 && (
            <div className="tip-card">
              <div className="tip-icon">📉</div>
              <h4>Maximize Deductions</h4>
              <p>
                Your effective rate is {effectiveTaxRate?.toFixed(2)}%. Consider maximizing 
                Section 80C (₹1.5L), 80D (₹25-50K), and home loan interest (₹2L) deductions.
              </p>
            </div>
          )}

          <div className="tip-card">
            <div className="tip-icon">📊</div>
            <h4>Track Capital Gains</h4>
            <p>
              Keep track of all your stock/mutual fund transactions for accurate capital gains 
              calculation. LTCG on equity above ₹1L is taxed at 10%.
            </p>
          </div>

          <div className="tip-card">
            <div className="tip-icon">🗂️</div>
            <h4>Maintain Records</h4>
            <p>
              Keep all investment proofs, TDS certificates, and bank statements ready for 
              ITR filing. File before July 31 to avoid penalties.
            </p>
          </div>
        </div>
      </div>

      {/* Payment Schedule */}
      {balanceTax > 10000 && (
        <div className="advance-tax-schedule">
          <h3>Advance Tax Payment Schedule</h3>
          <p className="schedule-note">
            Since your tax liability exceeds ₹10,000, you need to pay advance tax in installments:
          </p>
          <table>
            <thead>
              <tr>
                <th>Due Date</th>
                <th>Cumulative %</th>
                <th>Amount to Pay</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>June 15, {new Date().getFullYear()}</td>
                <td>15%</td>
                <td>₹{((balanceTax * 0.15)?.toFixed(0)).toLocaleString()}</td>
              </tr>
              <tr>
                <td>September 15, {new Date().getFullYear()}</td>
                <td>45%</td>
                <td>₹{((balanceTax * 0.30)?.toFixed(0)).toLocaleString()}</td>
              </tr>
              <tr>
                <td>December 15, {new Date().getFullYear()}</td>
                <td>75%</td>
                <td>₹{((balanceTax * 0.30)?.toFixed(0)).toLocaleString()}</td>
              </tr>
              <tr>
                <td>March 15, {new Date().getFullYear() + 1}</td>
                <td>100%</td>
                <td>₹{((balanceTax * 0.25)?.toFixed(0)).toLocaleString()}</td>
              </tr>
            </tbody>
          </table>
        </div>
      )}

      {/* Action Buttons */}
      <div className="projection-actions">
        <button className="btn-primary">
          Export Tax Projection
        </button>
        <button className="btn-secondary">
          Download ITR Prefill Data
        </button>
      </div>

      {/* Disclaimer */}
      <div className="disclaimer">
        <strong>⚠️ Disclaimer:</strong> This is an estimated tax projection based on the information 
        you provided. Actual tax liability may vary. Please consult a certified tax professional for 
        accurate tax planning and filing.
      </div>
    </div>
  );
}

export default TaxProjection;
