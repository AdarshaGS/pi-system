import React, { useState } from 'react';
import './RegimeComparison.css';

function RegimeComparison({ userId, comparison, onRefresh }) {
  const [uploadedDocuments, setUploadedDocuments] = useState([]);
  const [uploading, setUploading] = useState(false);

  const handleFileUpload = async (event) => {
    const files = Array.from(event.target.files);
    if (files.length === 0) return;

    setUploading(true);
    const formData = new FormData();
    
    files.forEach(file => {
      formData.append('files', file);
    });
    formData.append('category', 'TAX_DOCUMENTS');
    formData.append('userId', userId);

    try {
      const user = JSON.parse(localStorage.getItem('user'));
      const response = await fetch('http://localhost:8082/api/v1/documents/upload', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${user.token}`
        },
        body: formData
      });

      if (response.ok) {
        const result = await response.json();
        setUploadedDocuments(prev => [...prev, ...result]);
        alert('Documents uploaded successfully!');
      } else {
        alert('Failed to upload documents');
      }
    } catch (error) {
      console.error('Upload error:', error);
      alert('Error uploading documents');
    } finally {
      setUploading(false);
      event.target.value = ''; // Reset file input
    }
  };

  if (!comparison) {
    return (
      <div className="regime-comparison">
        <div className="empty-state">
          <p>Complete your tax details to see regime comparison</p>
          <button onClick={onRefresh} className="btn-primary">
            Calculate Comparison
          </button>
        </div>
      </div>
    );
  }

  const {
    oldRegime,
    newRegime,
    recommendedRegime,
    savings,
    savingsPercentage
  } = comparison;

  return (
    <div className="regime-comparison">
      <div className="comparison-header">
        <h2>Tax Regime Comparison - FY 2025-26</h2>
        <button onClick={onRefresh} className="btn-secondary">
          Refresh Comparison
        </button>
      </div>

      <div className="regime-cards">
        {/* Old Regime Card */}
        <div className={`regime-card ${recommendedRegime === 'OLD' ? 'recommended' : ''}`}>
          {recommendedRegime === 'OLD' && (
            <div className="recommended-badge">✓ Recommended</div>
          )}
          
          <h3>Old Regime</h3>
          <p className="regime-description">
            With deductions under Sections 80C, 80D, etc.
          </p>

          <div className="regime-details">
            <div className="detail-row">
              <span>Gross Total Income</span>
              <strong>₹{oldRegime?.grossTotalIncome?.toLocaleString() || '0'}</strong>
            </div>
            <div className="detail-row">
              <span>Total Deductions</span>
              <strong className="deduction">- ₹{oldRegime?.totalDeductions?.toLocaleString() || '0'}</strong>
            </div>
            <div className="detail-row">
              <span>Taxable Income</span>
              <strong>₹{oldRegime?.taxableIncome?.toLocaleString() || '0'}</strong>
            </div>
            <div className="detail-row highlight">
              <span>Tax Liability</span>
              <strong className="tax-amount">₹{oldRegime?.totalTax?.toLocaleString() || '0'}</strong>
            </div>
          </div>

          <div className="deduction-breakdown">
            <h4>Deductions Claimed</h4>
            <ul>
              {oldRegime?.section80C > 0 && (
                <li>Section 80C: ₹{oldRegime.section80C.toLocaleString()}</li>
              )}
              {oldRegime?.section80D > 0 && (
                <li>Section 80D: ₹{oldRegime.section80D.toLocaleString()}</li>
              )}
              {oldRegime?.section80G > 0 && (
                <li>Section 80G: ₹{oldRegime.section80G.toLocaleString()}</li>
              )}
              {oldRegime?.section24B > 0 && (
                <li>Section 24B: ₹{oldRegime.section24B.toLocaleString()}</li>
              )}
              {oldRegime?.standardDeduction > 0 && (
                <li>Standard Deduction: ₹{oldRegime.standardDeduction.toLocaleString()}</li>
              )}
              {oldRegime?.otherDeductions > 0 && (
                <li>Other Deductions: ₹{oldRegime.otherDeductions.toLocaleString()}</li>
              )}
            </ul>
          </div>

          <div className="tax-slabs">
            <h4>Tax Slabs (Old Regime)</h4>
            <ul>
              <li>Up to ₹2.5L: 0%</li>
              <li>₹2.5L - ₹5L: 5%</li>
              <li>₹5L - ₹10L: 20%</li>
              <li>Above ₹10L: 30%</li>
            </ul>
          </div>
        </div>

        {/* New Regime Card */}
        <div className={`regime-card ${recommendedRegime === 'NEW' ? 'recommended' : ''}`}>
          {recommendedRegime === 'NEW' && (
            <div className="recommended-badge">✓ Recommended</div>
          )}
          
          <h3>New Regime</h3>
          <p className="regime-description">
            Lower tax rates with limited deductions
          </p>

          <div className="regime-details">
            <div className="detail-row">
              <span>Gross Total Income</span>
              <strong>₹{newRegime?.grossTotalIncome?.toLocaleString() || '0'}</strong>
            </div>
            <div className="detail-row">
              <span>Total Deductions</span>
              <strong className="deduction">- ₹{newRegime?.totalDeductions?.toLocaleString() || '0'}</strong>
            </div>
            <div className="detail-row">
              <span>Taxable Income</span>
              <strong>₹{newRegime?.taxableIncome?.toLocaleString() || '0'}</strong>
            </div>
            <div className="detail-row highlight">
              <span>Tax Liability</span>
              <strong className="tax-amount">₹{newRegime?.totalTax?.toLocaleString() || '0'}</strong>
            </div>
          </div>

          <div className="deduction-breakdown">
            <h4>Deductions Allowed</h4>
            <ul>
              <li>Standard Deduction: ₹{newRegime?.standardDeduction?.toLocaleString() || '50,000'}</li>
              <li className="note">Most other deductions not available</li>
            </ul>
          </div>

          <div className="tax-slabs">
            <h4>Tax Slabs (New Regime)</h4>
            <ul>
              <li>Up to ₹3L: 0%</li>
              <li>₹3L - ₹6L: 5%</li>
              <li>₹6L - ₹9L: 10%</li>
              <li>₹9L - ₹12L: 15%</li>
              <li>₹12L - ₹15L: 20%</li>
              <li>Above ₹15L: 30%</li>
            </ul>
          </div>
        </div>
      </div>

      {/* Savings Comparison */}
      <div className="savings-comparison">
        <h3>💡 Recommendation</h3>
        <div className="recommendation-content">
          <p className="recommendation-text">
            {recommendedRegime === 'OLD' ? (
              <>
                <strong>Old Regime is better for you!</strong> You can save{' '}
                <span className="savings-amount">₹{savings?.toLocaleString()}</span> ({savingsPercentage?.toFixed(2)}%) 
                by opting for the old regime with deductions.
              </>
            ) : (
              <>
                <strong>New Regime is better for you!</strong> You can save{' '}
                <span className="savings-amount">₹{savings?.toLocaleString()}</span> ({savingsPercentage?.toFixed(2)}%) 
                with the new regime's lower tax rates.
              </>
            )}
          </p>

          <div className="recommendation-details">
            <div className="comparison-metric">
              <label>Tax Difference:</label>
              <span className="positive">₹{savings?.toLocaleString() || '0'}</span>
            </div>
            <div className="comparison-metric">
              <label>Savings Percentage:</label>
              <span className="positive">{savingsPercentage?.toFixed(2) || '0'}%</span>
            </div>
          </div>

          <div className="recommendation-tips">
            <h4>Tips to optimize further:</h4>
            <ul>
              {recommendedRegime === 'OLD' ? (
                <>
                  <li>Maximize Section 80C deductions (up to ₹1.5L)</li>
                  <li>Claim health insurance under Section 80D (up to ₹25K-₹50K)</li>
                  <li>Consider home loan interest deduction under Section 24B (up to ₹2L)</li>
                  <li>Explore other deductions like 80E (education loan), 80G (donations)</li>
                </>
              ) : (
                <>
                  <li>Take advantage of higher basic exemption limit (₹3L)</li>
                  <li>Claim standard deduction of ₹50,000</li>
                  <li>No need to invest in tax-saving instruments unless for other goals</li>
                  <li>Simpler tax filing with fewer deduction proofs required</li>
                </>
              )}
            </ul>
          </div>
        </div>
      </div>

      {/* Visual Comparison */}
      <div className="visual-comparison">
        <h3>Visual Comparison</h3>
        <div className="comparison-bars">
          <div className="bar-group">
            <label>Old Regime</label>
            <div className="bar-container">
              <div 
                className="bar old-regime-bar"
                style={{ width: `${(oldRegime?.totalTax / Math.max(oldRegime?.totalTax, newRegime?.totalTax)) * 100}%` }}
              >
                ₹{oldRegime?.totalTax?.toLocaleString()}
              </div>
            </div>
          </div>
          <div className="bar-group">
            <label>New Regime</label>
            <div className="bar-container">
              <div 
                className="bar new-regime-bar"
                style={{ width: `${(newRegime?.totalTax / Math.max(oldRegime?.totalTax, newRegime?.totalTax)) * 100}%` }}
              >
                ₹{newRegime?.totalTax?.toLocaleString()}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Document Upload Section */}
      <div className="document-upload-section" style={{
        background: '#f9fafb',
        border: '2px dashed #d1d5db',
        borderRadius: '12px',
        padding: '24px',
        marginBottom: '24px',
        textAlign: 'center'
      }}>
        <h3 style={{ marginBottom: '12px', fontSize: '18px' }}>📄 Upload Supporting Documents</h3>
        <p style={{ color: '#6b7280', marginBottom: '16px', fontSize: '14px' }}>
          Upload Form 16, salary slips, investment proofs, or other tax-related documents
        </p>
        
        <div style={{ marginBottom: '16px' }}>
          <input
            type="file"
            id="tax-doc-upload"
            multiple
            accept=".pdf,.jpg,.jpeg,.png,.doc,.docx"
            onChange={handleFileUpload}
            disabled={uploading}
            style={{ display: 'none' }}
          />
          <label
            htmlFor="tax-doc-upload"
            style={{
              display: 'inline-block',
              padding: '12px 24px',
              background: '#4f46e5',
              color: 'white',
              borderRadius: '8px',
              cursor: uploading ? 'not-allowed' : 'pointer',
              fontSize: '15px',
              fontWeight: '600',
              opacity: uploading ? 0.6 : 1,
              transition: 'all 0.2s'
            }}
          >
            {uploading ? '📤 Uploading...' : '📁 Choose Files'}
          </label>
        </div>

        {uploadedDocuments.length > 0 && (
          <div style={{ marginTop: '16px', textAlign: 'left' }}>
            <h4 style={{ fontSize: '14px', fontWeight: '600', marginBottom: '8px', color: '#374151' }}>
              Uploaded Documents ({uploadedDocuments.length})
            </h4>
            <div style={{ display: 'grid', gap: '8px' }}>
              {uploadedDocuments.map((doc, index) => (
                <div key={index} style={{
                  padding: '8px 12px',
                  background: 'white',
                  border: '1px solid #e5e7eb',
                  borderRadius: '6px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  fontSize: '13px'
                }}>
                  <span>✅ {doc.fileName || doc.name || `Document ${index + 1}`}</span>
                  <span style={{ color: '#10b981', fontSize: '12px' }}>Uploaded</span>
                </div>
              ))}
            </div>
          </div>
        )}

        <p style={{ color: '#9ca3af', fontSize: '12px', marginTop: '12px' }}>
          Supported formats: PDF, JPG, PNG, DOC, DOCX (Max 10MB per file)
        </p>
      </div>

      {/* Important Note */}
      <div className="info-box">
        <strong>📌 Important:</strong> This comparison is based on the information you provided. 
        Please consult a tax professional for personalized advice. Tax laws are subject to change.
      </div>
    </div>
  );
}

export default RegimeComparison;
