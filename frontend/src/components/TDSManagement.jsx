import React, { useState, useEffect, useMemo } from 'react';
import taxApi from '../services/taxApi';
import './TDSManagement.css';
import {
  FileText,
  Plus,
  Upload,
  RefreshCw,
  CheckCircle,
  AlertCircle,
  XCircle,
  Download,
  Edit2
} from 'lucide-react';

const TDSManagement = () => {
  const user = JSON.parse(localStorage.getItem('user'));
  const userId = useMemo(() => user?.userId, []);
  const [loading, setLoading] = useState(false);
  const [showAddModal, setShowAddModal] = useState(false);
  const [activeTab, setActiveTab] = useState('entries');
  
  // Financial Year
  const currentYear = new Date().getFullYear();
  const currentMonth = new Date().getMonth();
  const defaultFY = currentMonth >= 3 ? `${currentYear}-${(currentYear + 1) % 100}` : `${currentYear - 1}-${currentYear % 100}`;
  const [selectedFY, setSelectedFY] = useState(defaultFY);
  
  // Data State
  const [tdsEntries, setTdsEntries] = useState([]);
  const [reconciliation, setReconciliation] = useState(null);
  const [selectedQuarter, setSelectedQuarter] = useState('ALL');
  
  // New TDS Entry State
  const [newEntry, setNewEntry] = useState({
    deductorName: '',
    deductorTAN: '',
    tdsAmount: '',
    tdsDate: '',
    quarter: 'Q1',
    certificateNumber: '',
    sectionCode: '194J',
    status: 'PENDING'
  });

  useEffect(() => {
    if (user) {
      loadTDSData();
    }
  }, [selectedFY]);

  const loadTDSData = async () => {
    try {
      setLoading(true);
      
      const [entriesRes, reconRes] = await Promise.all([
        taxApi.getTDSEntries(userId, selectedFY),
        taxApi.getTDSReconciliation(userId, selectedFY)
      ]);
      
      setTdsEntries(entriesRes.data || []);
      setReconciliation(reconRes.data);
      
    } catch (error) {
      console.error('Error loading TDS data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAddEntry = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      
      await taxApi.recordTDSEntry(userId, selectedFY, {
        ...newEntry,
        tdsAmount: parseFloat(newEntry.tdsAmount)
      });
      
      setNewEntry({
        deductorName: '',
        deductorTAN: '',
        tdsAmount: '',
        tdsDate: '',
        quarter: 'Q1',
        certificateNumber: '',
        sectionCode: '194J',
        status: 'PENDING'
      });
      
      setShowAddModal(false);
      await loadTDSData();
      alert('TDS entry recorded successfully!');
      
    } catch (error) {
      console.error('Error recording TDS entry:', error);
      alert('Failed to record TDS entry');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateStatus = async (entryId, newStatus) => {
    try {
      setLoading(true);
      await taxApi.updateTDSStatus(userId, selectedFY, entryId, newStatus);
      await loadTDSData();
      alert('Status updated successfully!');
    } catch (error) {
      console.error('Error updating status:', error);
      alert('Failed to update status');
    } finally {
      setLoading(false);
    }
  };

  const handleImportForm26AS = async (file) => {
    try {
      setLoading(true);
      // In real implementation, you'd upload the file
      alert('Form 26AS import functionality would be implemented here');
      await loadTDSData();
    } catch (error) {
      console.error('Error importing Form 26AS:', error);
      alert('Failed to import Form 26AS');
    } finally {
      setLoading(false);
    }
  };

  const filteredEntries = selectedQuarter === 'ALL' 
    ? tdsEntries 
    : tdsEntries.filter(entry => entry.quarter === selectedQuarter);

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

  const getStatusIcon = (status) => {
    switch (status) {
      case 'MATCHED':
        return <CheckCircle size={18} className="status-icon matched" />;
      case 'CLAIMED':
        return <CheckCircle size={18} className="status-icon claimed" />;
      case 'MISMATCH':
        return <AlertCircle size={18} className="status-icon mismatch" />;
      case 'PENDING':
      default:
        return <XCircle size={18} className="status-icon pending" />;
    }
  };

  const totalTDS = tdsEntries.reduce((sum, entry) => sum + (entry.tdsAmount || 0), 0);
  const matchedTDS = tdsEntries.filter(e => e.status === 'MATCHED').reduce((sum, e) => sum + (e.tdsAmount || 0), 0);
  const pendingTDS = tdsEntries.filter(e => e.status === 'PENDING').reduce((sum, e) => sum + (e.tdsAmount || 0), 0);
  const mismatchTDS = tdsEntries.filter(e => e.status === 'MISMATCH').reduce((sum, e) => sum + (e.tdsAmount || 0), 0);

  return (
    <div className="tds-management">
      {/* Header */}
      <div className="tds-header">
        <div className="header-left">
          <h1>
            <FileText size={32} />
            TDS Management
          </h1>
          <p className="subtitle">Track and reconcile Tax Deducted at Source</p>
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
          <button className="btn-secondary">
            <Upload size={18} />
            Import Form 26AS
          </button>
          <button className="btn-primary" onClick={() => setShowAddModal(true)}>
            <Plus size={18} />
            Add TDS Entry
          </button>
        </div>
      </div>

      {/* Summary Cards */}
      <div className="tds-summary-cards">
        <div className="tds-summary-card total">
          <div className="card-header">
            <h3>Total TDS</h3>
            <FileText size={24} />
          </div>
          <div className="card-value">{formatCurrency(totalTDS)}</div>
          <div className="card-detail">{tdsEntries.length} entries</div>
        </div>

        <div className="tds-summary-card matched">
          <div className="card-header">
            <h3>Matched with 26AS</h3>
            <CheckCircle size={24} />
          </div>
          <div className="card-value">{formatCurrency(matchedTDS)}</div>
          <div className="card-detail">
            {tdsEntries.filter(e => e.status === 'MATCHED').length} entries
          </div>
        </div>

        <div className="tds-summary-card pending">
          <div className="card-header">
            <h3>Pending Verification</h3>
            <XCircle size={24} />
          </div>
          <div className="card-value">{formatCurrency(pendingTDS)}</div>
          <div className="card-detail">
            {tdsEntries.filter(e => e.status === 'PENDING').length} entries
          </div>
        </div>

        <div className="tds-summary-card mismatch">
          <div className="card-header">
            <h3>Mismatch</h3>
            <AlertCircle size={24} />
          </div>
          <div className="card-value">{formatCurrency(mismatchTDS)}</div>
          <div className="card-detail">
            {tdsEntries.filter(e => e.status === 'MISMATCH').length} entries
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="tds-tabs">
        <button 
          className={activeTab === 'entries' ? 'active' : ''}
          onClick={() => setActiveTab('entries')}
        >
          TDS Entries
        </button>
        <button 
          className={activeTab === 'reconciliation' ? 'active' : ''}
          onClick={() => setActiveTab('reconciliation')}
        >
          Reconciliation
        </button>
      </div>

      {/* TDS Entries Tab */}
      {activeTab === 'entries' && (
        <div className="tds-content">
          <div className="content-header">
            <div className="filters">
              <label>Filter by Quarter:</label>
              <select value={selectedQuarter} onChange={(e) => setSelectedQuarter(e.target.value)}>
                <option value="ALL">All Quarters</option>
                <option value="Q1">Q1 (Apr-Jun)</option>
                <option value="Q2">Q2 (Jul-Sep)</option>
                <option value="Q3">Q3 (Oct-Dec)</option>
                <option value="Q4">Q4 (Jan-Mar)</option>
              </select>
            </div>
            <div className="results-count">
              Showing {filteredEntries.length} of {tdsEntries.length} entries
            </div>
          </div>

          {filteredEntries.length === 0 ? (
            <div className="empty-state">
              <FileText size={48} />
              <p>No TDS entries recorded yet</p>
              <button className="btn-primary" onClick={() => setShowAddModal(true)}>
                <Plus size={18} />
                Add First TDS Entry
              </button>
            </div>
          ) : (
            <div className="tds-table">
              <table>
                <thead>
                  <tr>
                    <th>Quarter</th>
                    <th>Deductor Name</th>
                    <th>TAN</th>
                    <th>Section</th>
                    <th>TDS Amount</th>
                    <th>Date</th>
                    <th>Certificate No.</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredEntries.map((entry, index) => (
                    <tr key={index}>
                      <td>
                        <span className="quarter-badge">{entry.quarter}</span>
                      </td>
                      <td>{entry.deductorName}</td>
                      <td>{entry.deductorTAN}</td>
                      <td>
                        <span className="section-badge">{entry.sectionCode}</span>
                      </td>
                      <td className="amount">{formatCurrency(entry.tdsAmount)}</td>
                      <td>{new Date(entry.tdsDate).toLocaleDateString()}</td>
                      <td>{entry.certificateNumber || '-'}</td>
                      <td>
                        <div className="status-cell">
                          {getStatusIcon(entry.status)}
                          <span className={`status-text ${entry.status?.toLowerCase()}`}>
                            {entry.status}
                          </span>
                        </div>
                      </td>
                      <td>
                        <div className="action-buttons">
                          <button 
                            className="action-btn"
                            onClick={() => handleUpdateStatus(entry.id, 'MATCHED')}
                            title="Mark as matched"
                          >
                            <CheckCircle size={16} />
                          </button>
                          <button 
                            className="action-btn"
                            onClick={() => handleUpdateStatus(entry.id, 'MISMATCH')}
                            title="Mark as mismatch"
                          >
                            <AlertCircle size={16} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Reconciliation Tab */}
      {activeTab === 'reconciliation' && reconciliation && (
        <div className="tds-content">
          <div className="reconciliation-panel">
            <h3>TDS Reconciliation Summary</h3>
            
            <div className="recon-grid">
              <div className="recon-card">
                <h4>Total TDS Claimed</h4>
                <div className="recon-value">{formatCurrency(reconciliation.totalTDSClaimed)}</div>
              </div>
              
              <div className="recon-card">
                <h4>Matched with Form 26AS</h4>
                <div className="recon-value success">{formatCurrency(reconciliation.totalTDSMatched)}</div>
              </div>
              
              <div className="recon-card">
                <h4>Mismatch Amount</h4>
                <div className="recon-value danger">{formatCurrency(reconciliation.totalMismatch)}</div>
              </div>
              
              <div className="recon-card">
                <h4>Reconciliation Status</h4>
                <div className={`status-badge ${reconciliation.reconciliationStatus?.toLowerCase()}`}>
                  {reconciliation.reconciliationStatus}
                </div>
              </div>
            </div>

            {reconciliation.totalMismatch > 0 && (
              <div className="alert-box warning">
                <AlertCircle size={20} />
                <div>
                  <strong>Action Required:</strong> There is a mismatch of {formatCurrency(reconciliation.totalMismatch)} between your TDS entries and Form 26AS. Please review and update the entries.
                </div>
              </div>
            )}

            {reconciliation.reconciliationStatus === 'MATCHED' && (
              <div className="alert-box success">
                <CheckCircle size={20} />
                <div>
                  <strong>All Clear:</strong> All TDS entries are matched with Form 26AS. You're ready for ITR filing.
                </div>
              </div>
            )}

            <div className="recon-details">
              <h4>Quarter-wise Breakdown</h4>
              <div className="quarter-breakdown">
                {['Q1', 'Q2', 'Q3', 'Q4'].map(quarter => {
                  const quarterEntries = tdsEntries.filter(e => e.quarter === quarter);
                  const quarterTotal = quarterEntries.reduce((sum, e) => sum + (e.tdsAmount || 0), 0);
                  const quarterMatched = quarterEntries.filter(e => e.status === 'MATCHED').length;
                  
                  return (
                    <div key={quarter} className="quarter-card">
                      <h5>{quarter}</h5>
                      <div className="quarter-stats">
                        <span>Entries: {quarterEntries.length}</span>
                        <span>Matched: {quarterMatched}</span>
                        <span>Amount: {formatCurrency(quarterTotal)}</span>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Add TDS Entry Modal */}
      {showAddModal && (
        <div className="modal-overlay" onClick={() => setShowAddModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Add TDS Entry</h3>
              <button className="close-btn" onClick={() => setShowAddModal(false)}>×</button>
            </div>
            <form onSubmit={handleAddEntry} className="modal-form">
              <div className="form-grid">
                <div className="form-field">
                  <label>Deductor Name *</label>
                  <input
                    type="text"
                    value={newEntry.deductorName}
                    onChange={(e) => setNewEntry({...newEntry, deductorName: e.target.value})}
                    placeholder="Enter deductor name"
                    required
                  />
                </div>

                <div className="form-field">
                  <label>Deductor TAN *</label>
                  <input
                    type="text"
                    value={newEntry.deductorTAN}
                    onChange={(e) => setNewEntry({...newEntry, deductorTAN: e.target.value.toUpperCase()})}
                    placeholder="e.g., ABCD12345E"
                    maxLength="10"
                    required
                  />
                </div>

                <div className="form-field">
                  <label>Section Code</label>
                  <select
                    value={newEntry.sectionCode}
                    onChange={(e) => setNewEntry({...newEntry, sectionCode: e.target.value})}
                  >
                    <option value="194J">194J - Professional Fees</option>
                    <option value="192">192 - Salary</option>
                    <option value="194I">194I - Rent</option>
                    <option value="194C">194C - Contractor</option>
                    <option value="194H">194H - Commission</option>
                    <option value="194A">194A - Interest</option>
                  </select>
                </div>

                <div className="form-field">
                  <label>Quarter *</label>
                  <select
                    value={newEntry.quarter}
                    onChange={(e) => setNewEntry({...newEntry, quarter: e.target.value})}
                    required
                  >
                    <option value="Q1">Q1 (Apr-Jun)</option>
                    <option value="Q2">Q2 (Jul-Sep)</option>
                    <option value="Q3">Q3 (Oct-Dec)</option>
                    <option value="Q4">Q4 (Jan-Mar)</option>
                  </select>
                </div>

                <div className="form-field">
                  <label>TDS Amount *</label>
                  <input
                    type="number"
                    value={newEntry.tdsAmount}
                    onChange={(e) => setNewEntry({...newEntry, tdsAmount: e.target.value})}
                    placeholder="Enter TDS amount"
                    required
                  />
                </div>

                <div className="form-field">
                  <label>TDS Date *</label>
                  <input
                    type="date"
                    value={newEntry.tdsDate}
                    onChange={(e) => setNewEntry({...newEntry, tdsDate: e.target.value})}
                    required
                  />
                </div>

                <div className="form-field">
                  <label>Certificate Number</label>
                  <input
                    type="text"
                    value={newEntry.certificateNumber}
                    onChange={(e) => setNewEntry({...newEntry, certificateNumber: e.target.value})}
                    placeholder="Optional"
                  />
                </div>

                <div className="form-field">
                  <label>Status</label>
                  <select
                    value={newEntry.status}
                    onChange={(e) => setNewEntry({...newEntry, status: e.target.value})}
                  >
                    <option value="PENDING">Pending</option>
                    <option value="CLAIMED">Claimed</option>
                    <option value="MATCHED">Matched</option>
                    <option value="MISMATCH">Mismatch</option>
                  </select>
                </div>
              </div>

              <div className="modal-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowAddModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn-primary" disabled={loading}>
                  <Plus size={18} />
                  {loading ? 'Adding...' : 'Add TDS Entry'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default TDSManagement;
