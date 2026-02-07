import React, { useState, useEffect, useMemo } from 'react';
import taxApi from '../services/taxApi';
import './ITRFilingAssistant.css';
import {
  FileText,
  Download,
  Upload,
  CheckCircle,
  XCircle,
  AlertCircle,
  RefreshCw,
  ExternalLink
} from 'lucide-react';

const ITRFilingAssistant = () => {
  const user = JSON.parse(localStorage.getItem('user'));
  const userId = useMemo(() => user?.userId, []);
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('checklist');
  
  // Financial Year
  const currentYear = new Date().getFullYear();
  const currentMonth = new Date().getMonth();
  const defaultFY = currentMonth >= 3 ? `${currentYear}-${(currentYear + 1) % 100}` : `${currentYear - 1}-${currentYear % 100}`;
  const [selectedFY, setSelectedFY] = useState(defaultFY);
  
  // Data State
  const [itr1Data, setItr1Data] = useState(null);
  const [itr2Data, setItr2Data] = useState(null);
  const [selectedITRForm, setSelectedITRForm] = useState('ITR-1');
  const [checklistItems, setChecklistItems] = useState([
    { id: 1, title: 'PAN Card', description: 'Permanent Account Number', completed: true },
    { id: 2, title: 'Aadhaar Card', description: 'Linked with PAN', completed: true },
    { id: 3, title: 'Bank Account Details', description: 'For tax refund', completed: false },
    { id: 4, title: 'Form 16', description: 'From employer (if applicable)', completed: false },
    { id: 5, title: 'Form 26AS', description: 'Tax credit statement', completed: false },
    { id: 6, title: 'Investment Proofs', description: '80C, 80D documents', completed: false },
    { id: 7, title: 'Capital Gains Details', description: 'Stock/MF transactions', completed: false },
    { id: 8, title: 'Interest Certificates', description: 'From banks/FDs', completed: false },
    { id: 9, title: 'House Property Details', description: 'If applicable', completed: false },
    { id: 10, title: 'Previous Year ITR', description: 'For reference', completed: false }
  ]);

  useEffect(() => {
    if (user) {
      loadITRData();
    }
  }, [selectedFY]);

  const loadITRData = async () => {
    try {
      setLoading(true);
      
      // Load ITR-1 data
      try {
        const itr1Res = await taxApi.buildITR1Data(userId, selectedFY);
        setItr1Data(itr1Res.data);
      } catch (error) {
        console.log('ITR-1 data not available');
      }
      
      // Load ITR-2 data
      try {
        const itr2Res = await taxApi.buildITR2Data(userId, selectedFY);
        setItr2Data(itr2Res.data);
      } catch (error) {
        console.log('ITR-2 data not available');
      }
      
    } catch (error) {
      console.error('Error loading ITR data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadITR = async (formType) => {
    try {
      setLoading(true);
      
      let response;
      if (formType === 'ITR-1') {
        response = await taxApi.generateITR1JSON(userId, selectedFY);
      } else {
        response = await taxApi.generateITR2JSON(userId, selectedFY);
      }
      
      // Create a download link
      const blob = new Blob([JSON.stringify(response.data, null, 2)], { type: 'application/json' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `${formType}-${selectedFY}.json`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      
      alert(`${formType} JSON downloaded successfully!`);
      
    } catch (error) {
      console.error(`Error downloading ${formType}:`, error);
      alert(`Failed to download ${formType}`);
    } finally {
      setLoading(false);
    }
  };

  const handleImportForm16 = async (file) => {
    try {
      setLoading(true);
      // In real implementation, you'd upload the file
      alert('Form 16 import functionality would be implemented here');
      await loadITRData();
    } catch (error) {
      console.error('Error importing Form 16:', error);
      alert('Failed to import Form 16');
    } finally {
      setLoading(false);
    }
  };

  const handleSyncAIS = async () => {
    try {
      setLoading(true);
      await taxApi.syncAIS(userId, selectedFY);
      await loadITRData();
      alert('AIS synced successfully!');
    } catch (error) {
      console.error('Error syncing AIS:', error);
      alert('Failed to sync AIS');
    } finally {
      setLoading(false);
    }
  };

  const toggleChecklistItem = (id) => {
    setChecklistItems(items =>
      items.map(item =>
        item.id === id ? { ...item, completed: !item.completed } : item
      )
    );
  };

  const completedCount = checklistItems.filter(item => item.completed).length;
  const completionPercentage = (completedCount / checklistItems.length) * 100;

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
    <div className="itr-filing-assistant">
      {/* Header */}
      <div className="filing-header">
        <div className="header-left">
          <h1>
            <FileText size={32} />
            ITR Filing Assistant
          </h1>
          <p className="subtitle">Simplified income tax return filing with pre-filled data</p>
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

      {/* Filing Status Banner */}
      <div className="filing-status-banner">
        <div className="status-content">
          <div className="status-icon">
            {completionPercentage === 100 ? (
              <CheckCircle size={32} className="success" />
            ) : (
              <AlertCircle size={32} className="warning" />
            )}
          </div>
          <div className="status-info">
            <h3>Filing Readiness: {completionPercentage.toFixed(0)}%</h3>
            <p>
              {completionPercentage === 100
                ? 'You\'re ready to file your ITR!'
                : `${completedCount} of ${checklistItems.length} items completed`}
            </p>
            <div className="progress-bar">
              <div className="progress-fill" style={{width: `${completionPercentage}%`}}></div>
            </div>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="filing-tabs">
        <button 
          className={activeTab === 'checklist' ? 'active' : ''}
          onClick={() => setActiveTab('checklist')}
        >
          Filing Checklist
        </button>
        <button 
          className={activeTab === 'form-selection' ? 'active' : ''}
          onClick={() => setActiveTab('form-selection')}
        >
          Form Selection
        </button>
        <button 
          className={activeTab === 'data-review' ? 'active' : ''}
          onClick={() => setActiveTab('data-review')}
        >
          Data Review
        </button>
        <button 
          className={activeTab === 'download' ? 'active' : ''}
          onClick={() => setActiveTab('download')}
        >
          Download & File
        </button>
      </div>

      {/* Checklist Tab */}
      {activeTab === 'checklist' && (
        <div className="filing-content">
          <div className="checklist-container">
            <h3>Pre-Filing Checklist</h3>
            <p className="checklist-desc">
              Make sure you have all required documents before filing your ITR
            </p>
            
            <div className="checklist-items">
              {checklistItems.map(item => (
                <div
                  key={item.id}
                  className={`checklist-item ${item.completed ? 'completed' : ''}`}
                  onClick={() => toggleChecklistItem(item.id)}
                >
                  <div className="checkbox">
                    {item.completed && <CheckCircle size={20} />}
                  </div>
                  <div className="item-content">
                    <h4>{item.title}</h4>
                    <p>{item.description}</p>
                  </div>
                </div>
              ))}
            </div>

            <div className="import-actions">
              <h4>Import Documents</h4>
              <div className="action-buttons">
                <button className="action-btn" onClick={handleImportForm16}>
                  <Upload size={18} />
                  Import Form 16
                </button>
                <button className="action-btn">
                  <Upload size={18} />
                  Import Form 26AS
                </button>
                <button className="action-btn" onClick={handleSyncAIS}>
                  <RefreshCw size={18} />
                  Sync with AIS
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Form Selection Tab */}
      {activeTab === 'form-selection' && (
        <div className="filing-content">
          <div className="form-selection">
            <h3>Choose Your ITR Form</h3>
            <p className="selection-desc">
              Select the appropriate ITR form based on your income sources
            </p>
            
            <div className="form-cards">
              <div
                className={`form-card ${selectedITRForm === 'ITR-1' ? 'selected' : ''}`}
                onClick={() => setSelectedITRForm('ITR-1')}
              >
                <div className="form-badge">ITR-1 (SAHAJ)</div>
                <h4>Most Common Form</h4>
                <div className="form-criteria">
                  <h5>Eligible if you have:</h5>
                  <ul>
                    <li>Salary income</li>
                    <li>One house property</li>
                    <li>Income from other sources (interest, etc.)</li>
                    <li>Total income up to ₹50 lakh</li>
                  </ul>
                  <h5>Not eligible if you have:</h5>
                  <ul className="not-eligible">
                    <li>Business or professional income</li>
                    <li>Capital gains</li>
                    <li>More than one house property</li>
                    <li>Agricultural income &gt; ₹5,000</li>
                  </ul>
                </div>
              </div>

              <div
                className={`form-card ${selectedITRForm === 'ITR-2' ? 'selected' : ''}`}
                onClick={() => setSelectedITRForm('ITR-2')}
              >
                <div className="form-badge">ITR-2</div>
                <h4>For Capital Gains & Multiple Properties</h4>
                <div className="form-criteria">
                  <h5>Eligible if you have:</h5>
                  <ul>
                    <li>Salary income</li>
                    <li>Capital gains (stocks, property)</li>
                    <li>Multiple house properties</li>
                    <li>Income from other sources</li>
                    <li>Agricultural income &gt; ₹5,000</li>
                  </ul>
                  <h5>Not eligible if you have:</h5>
                  <ul className="not-eligible">
                    <li>Business or professional income</li>
                    <li>Income from partnership firm</li>
                  </ul>
                </div>
              </div>
            </div>

            <div className="form-recommendation">
              <AlertCircle size={20} />
              <div>
                <strong>Recommendation:</strong> Based on your profile, we recommend{' '}
                <strong>{itr2Data ? 'ITR-2' : 'ITR-1'}</strong>
                {itr2Data && ' because you have capital gains or multiple income sources.'}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Data Review Tab */}
      {activeTab === 'data-review' && (
        <div className="filing-content">
          <div className="data-review">
            <h3>Review Pre-Filled Data</h3>
            <p className="review-desc">
              Verify all information before downloading your ITR
            </p>
            
            {selectedITRForm === 'ITR-1' && itr1Data ? (
              <div className="data-sections">
                <div className="data-section">
                  <h4>Personal Information</h4>
                  <div className="data-grid">
                    <div className="data-item">
                      <span className="data-label">Name:</span>
                      <span className="data-value">{itr1Data.name || user.name}</span>
                    </div>
                    <div className="data-item">
                      <span className="data-label">PAN:</span>
                      <span className="data-value">{itr1Data.pan || 'Not provided'}</span>
                    </div>
                    <div className="data-item">
                      <span className="data-label">Assessment Year:</span>
                      <span className="data-value">{itr1Data.assessmentYear}</span>
                    </div>
                  </div>
                </div>

                <div className="data-section">
                  <h4>Income Details</h4>
                  <div className="data-grid">
                    <div className="data-item">
                      <span className="data-label">Gross Salary:</span>
                      <span className="data-value">{formatCurrency(itr1Data.grossSalary)}</span>
                    </div>
                    <div className="data-item">
                      <span className="data-label">Deductions u/s 80C:</span>
                      <span className="data-value">{formatCurrency(itr1Data.section80C)}</span>
                    </div>
                    <div className="data-item">
                      <span className="data-label">Total Income:</span>
                      <span className="data-value">{formatCurrency(itr1Data.totalIncome)}</span>
                    </div>
                  </div>
                </div>

                <div className="data-section">
                  <h4>Tax Summary</h4>
                  <div className="data-grid">
                    <div className="data-item">
                      <span className="data-label">Tax Payable:</span>
                      <span className="data-value">{formatCurrency(itr1Data.taxPayable)}</span>
                    </div>
                    <div className="data-item">
                      <span className="data-label">TDS:</span>
                      <span className="data-value">{formatCurrency(itr1Data.tdsDeducted)}</span>
                    </div>
                    <div className="data-item">
                      <span className="data-label">Tax Refund/Payable:</span>
                      <span className={`data-value ${(itr1Data.refund || 0) > 0 ? 'success' : 'danger'}`}>
                        {formatCurrency(itr1Data.refund || itr1Data.taxDue)}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            ) : selectedITRForm === 'ITR-2' && itr2Data ? (
              <div className="data-sections">
                <div className="data-section">
                  <h4>Income Summary</h4>
                  <div className="data-grid">
                    <div className="data-item">
                      <span className="data-label">Salary:</span>
                      <span className="data-value">{formatCurrency(itr2Data.salaryIncome)}</span>
                    </div>
                    <div className="data-item">
                      <span className="data-label">House Property:</span>
                      <span className="data-value">{formatCurrency(itr2Data.housePropertyIncome)}</span>
                    </div>
                    <div className="data-item">
                      <span className="data-label">Capital Gains:</span>
                      <span className="data-value">{formatCurrency(itr2Data.capitalGains)}</span>
                    </div>
                    <div className="data-item">
                      <span className="data-label">Other Sources:</span>
                      <span className="data-value">{formatCurrency(itr2Data.otherIncome)}</span>
                    </div>
                  </div>
                </div>

                <div className="data-section">
                  <h4>Tax Computation</h4>
                  <div className="data-grid">
                    <div className="data-item">
                      <span className="data-label">Total Tax:</span>
                      <span className="data-value">{formatCurrency(itr2Data.totalTax)}</span>
                    </div>
                    <div className="data-item">
                      <span className="data-label">Tax Paid:</span>
                      <span className="data-value">{formatCurrency(itr2Data.taxPaid)}</span>
                    </div>
                  </div>
                </div>
              </div>
            ) : (
              <div className="no-data">
                <FileText size={48} />
                <p>No data available for {selectedITRForm}</p>
                <p className="small">Please complete your income and deduction details first</p>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Download Tab */}
      {activeTab === 'download' && (
        <div className="filing-content">
          <div className="download-section">
            <h3>Download ITR JSON</h3>
            <p className="download-desc">
              Download the pre-filled ITR JSON file and upload it to the Income Tax e-filing portal
            </p>
            
            <div className="download-cards">
              <div className="download-card">
                <FileText size={48} />
                <h4>{selectedITRForm}</h4>
                <p>Pre-filled with your tax data</p>
                <button
                  className="btn-primary"
                  onClick={() => handleDownloadITR(selectedITRForm)}
                  disabled={loading}
                >
                  <Download size={18} />
                  {loading ? 'Generating...' : `Download ${selectedITRForm} JSON`}
                </button>
              </div>
            </div>

            <div className="filing-steps">
              <h4>Next Steps:</h4>
              <ol>
                <li>
                  <strong>Download JSON:</strong> Click the download button above to get your pre-filled ITR JSON file
                </li>
                <li>
                  <strong>Visit e-Filing Portal:</strong> Go to{' '}
                  <a href="https://www.incometax.gov.in/iec/foportal" target="_blank" rel="noopener noreferrer">
                    incometax.gov.in
                    <ExternalLink size={14} />
                  </a>
                </li>
                <li>
                  <strong>Upload JSON:</strong> Login and upload the downloaded JSON file
                </li>
                <li>
                  <strong>Verify Data:</strong> Review all details and make corrections if needed
                </li>
                <li>
                  <strong>Submit Return:</strong> E-verify using Aadhaar OTP, EVC, or DSC
                </li>
              </ol>
            </div>

            <div className="important-dates">
              <h4>Important Dates:</h4>
              <div className="dates-grid">
                <div className="date-card">
                  <Calendar size={24} />
                  <div className="date-info">
                    <strong>31 July {currentYear}</strong>
                    <span>Deadline for individuals (non-audit cases)</span>
                  </div>
                </div>
                <div className="date-card">
                  <Calendar size={24} />
                  <div className="date-info">
                    <strong>31 December {currentYear}</strong>
                    <span>Revised return filing deadline</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ITRFilingAssistant;
