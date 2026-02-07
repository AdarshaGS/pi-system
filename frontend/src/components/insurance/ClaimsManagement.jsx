import { useState, useEffect } from 'react';
import * as insuranceApi from '../../api/insuranceApi';

const ClaimsManagement = ({ token, policies, preSelectedPolicy }) => {
    const userId = localStorage.getItem('userId') || 1;
    const [claims, setClaims] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showClaimForm, setShowClaimForm] = useState(false);
    const [selectedClaim, setSelectedClaim] = useState(null);
    const [filter, setFilter] = useState('ALL');

    const [formData, setFormData] = useState({
        policyId: preSelectedPolicy?.id || '',
        claimNumber: '',
        claimType: 'HOSPITALIZATION',
        claimAmount: '',
        claimDate: new Date().toISOString().split('T')[0],
        incidentDate: '',
        reason: '',
        hospitalName: '',
        doctorName: '',
        diagnosis: ''
    });

    useEffect(() => {
        fetchClaims();
    }, []);

    useEffect(() => {
        if (preSelectedPolicy) {
            setFormData(prev => ({ ...prev, policyId: preSelectedPolicy.id }));
            setShowClaimForm(true);
        }
    }, [preSelectedPolicy]);

    const fetchClaims = async () => {
        try {
            setLoading(true);
            // Fetch claims for all policies
            const allClaims = [];
            for (const policy of policies) {
                try {
                    const policyClaims = await insuranceApi.getPolicyClaims(policy.id);
                    allClaims.push(...(Array.isArray(policyClaims) ? policyClaims : []));
                } catch (err) {
                    console.error(`Error fetching claims for policy ${policy.id}:`, err);
                }
            }
            setClaims(allClaims);
        } catch (error) {
            console.error('Error fetching claims:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const claimData = {
                amount: formData.claimAmount,
                date: formData.claimDate,
                reason: formData.reason,
                documents: [] // Add document handling if needed
            };
            await insuranceApi.fileInsuranceClaim(formData.policyId, claimData);
            setShowClaimForm(false);
            resetForm();
            fetchClaims();
        } catch (error) {
            alert('Error filing claim: ' + error.message);
        }
    };

    const handleUpdateStatus = async (claimId, newStatus) => {
        try {
            const claim = claims.find(c => c.id === claimId);
            await insuranceApi.updateClaim(claimId, { ...claim, claimStatus: newStatus }, token);
            fetchClaims();
        } catch (error) {
            alert('Error updating claim: ' + error.message);
        }
    };

    const resetForm = () => {
        setFormData({
            policyId: '',
            claimNumber: '',
            claimType: 'HOSPITALIZATION',
            claimAmount: '',
            claimDate: new Date().toISOString().split('T')[0],
            incidentDate: '',
            reason: '',
            hospitalName: '',
            doctorName: '',
            diagnosis: ''
        });
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            maximumFractionDigits: 0
        }).format(amount || 0);
    };

    const formatDate = (dateString) => {
        if (!dateString) return 'N/A';
        return new Date(dateString).toLocaleDateString('en-IN');
    };

    const getStatusColor = (status) => {
        const colors = {
            'SUBMITTED': '#3498db',
            'UNDER_REVIEW': '#f39c12',
            'APPROVED': '#27ae60',
            'REJECTED': '#e74c3c',
            'SETTLED': '#2ecc71',
            'WITHDRAWN': '#95a5a6'
        };
        return colors[status] || '#666';
    };

    const filteredClaims = claims.filter(claim => {
        if (filter === 'ALL') return true;
        return claim.claimStatus === filter;
    });

    if (loading) {
        return <div style={{ textAlign: 'center', padding: '40px' }}>Loading claims...</div>;
    }

    return (
        <div>
            {/* Header */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                <div style={{ display: 'flex', gap: '12px' }}>
                    <button
                        onClick={() => setFilter('ALL')}
                        className={filter === 'ALL' ? 'btn-primary' : 'btn-secondary'}
                        style={{ padding: '8px 16px' }}
                    >
                        All ({claims.length})
                    </button>
                    <button
                        onClick={() => setFilter('SUBMITTED')}
                        className={filter === 'SUBMITTED' ? 'btn-primary' : 'btn-secondary'}
                        style={{ padding: '8px 16px' }}
                    >
                        Submitted
                    </button>
                    <button
                        onClick={() => setFilter('UNDER_REVIEW')}
                        className={filter === 'UNDER_REVIEW' ? 'btn-primary' : 'btn-secondary'}
                        style={{ padding: '8px 16px' }}
                    >
                        Under Review
                    </button>
                    <button
                        onClick={() => setFilter('APPROVED')}
                        className={filter === 'APPROVED' ? 'btn-primary' : 'btn-secondary'}
                        style={{ padding: '8px 16px' }}
                    >
                        Approved
                    </button>
                    <button
                        onClick={() => setFilter('SETTLED')}
                        className={filter === 'SETTLED' ? 'btn-primary' : 'btn-secondary'}
                        style={{ padding: '8px 16px' }}
                    >
                        Settled
                    </button>
                </div>
                <button className="btn-primary" onClick={() => setShowClaimForm(true)}>
                    + File New Claim
                </button>
            </div>

            {/* Claims List */}
            <div className="data-table-container">
                <table>
                    <thead>
                        <tr>
                            <th>Claim #</th>
                            <th>Policy</th>
                            <th>Type</th>
                            <th>Claim Amount</th>
                            <th>Approved Amount</th>
                            <th>Date</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filteredClaims.map((claim) => {
                            const policy = policies.find(p => p.id === claim.policy?.id);
                            return (
                                <tr key={claim.id}>
                                    <td style={{ fontWeight: '600' }}>{claim.claimNumber}</td>
                                    <td>{policy?.policyName || 'N/A'}</td>
                                    <td>{claim.claimType}</td>
                                    <td>{formatCurrency(claim.claimAmount)}</td>
                                    <td>{claim.approvedAmount ? formatCurrency(claim.approvedAmount) : '-'}</td>
                                    <td>{formatDate(claim.claimDate)}</td>
                                    <td>
                                        <span
                                            style={{
                                                padding: '4px 12px',
                                                borderRadius: '12px',
                                                fontSize: '12px',
                                                fontWeight: '600',
                                                background: getStatusColor(claim.claimStatus) + '20',
                                                color: getStatusColor(claim.claimStatus)
                                            }}
                                        >
                                            {claim.claimStatus}
                                        </span>
                                    </td>
                                    <td>
                                        {claim.claimStatus === 'SUBMITTED' && (
                                            <select
                                                onChange={(e) => handleUpdateStatus(claim.id, e.target.value)}
                                                className="form-input"
                                                style={{ padding: '6px', fontSize: '13px' }}
                                                defaultValue=""
                                            >
                                                <option value="" disabled>Update Status</option>
                                                <option value="UNDER_REVIEW">Under Review</option>
                                                <option value="APPROVED">Approve</option>
                                                <option value="REJECTED">Reject</option>
                                            </select>
                                        )}
                                    </td>
                                </tr>
                            );
                        })}
                    </tbody>
                </table>
            </div>

            {filteredClaims.length === 0 && (
                <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
                    No claims found.
                </div>
            )}

            {/* Claim Form Modal */}
            {showClaimForm && (
                <div className="modal-overlay" onClick={() => setShowClaimForm(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '600px' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                            <h2 style={{ margin: 0 }}>File Insurance Claim</h2>
                            <button
                                onClick={() => setShowClaimForm(false)}
                                style={{ background: 'none', border: 'none', fontSize: '24px', cursor: 'pointer' }}
                            >
                                ×
                            </button>
                        </div>

                        <form onSubmit={handleSubmit}>
                            <div style={{ marginBottom: '16px' }}>
                                <label className="form-label">Select Policy *</label>
                                <select
                                    name="policyId"
                                    value={formData.policyId}
                                    onChange={handleChange}
                                    className="form-input"
                                    required
                                >
                                    <option value="">Choose a policy...</option>
                                    {policies.map(policy => (
                                        <option key={policy.id} value={policy.id}>
                                            {policy.policyName} - {policy.policyNumber}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
                                <div>
                                    <label className="form-label">Claim Number *</label>
                                    <input
                                        type="text"
                                        name="claimNumber"
                                        value={formData.claimNumber}
                                        onChange={handleChange}
                                        className="form-input"
                                        required
                                    />
                                </div>
                                <div>
                                    <label className="form-label">Claim Type *</label>
                                    <select name="claimType" value={formData.claimType} onChange={handleChange} className="form-input" required>
                                        <option value="HOSPITALIZATION">Hospitalization</option>
                                        <option value="SURGERY">Surgery</option>
                                        <option value="CRITICAL_ILLNESS">Critical Illness</option>
                                        <option value="ACCIDENT">Accident</option>
                                        <option value="DEATH">Death</option>
                                        <option value="MATURITY">Maturity</option>
                                        <option value="DISABILITY">Disability</option>
                                    </select>
                                </div>
                            </div>

                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
                                <div>
                                    <label className="form-label">Claim Amount *</label>
                                    <input
                                        type="number"
                                        name="claimAmount"
                                        value={formData.claimAmount}
                                        onChange={handleChange}
                                        className="form-input"
                                        required
                                        step="0.01"
                                    />
                                </div>
                                <div>
                                    <label className="form-label">Incident Date</label>
                                    <input
                                        type="date"
                                        name="incidentDate"
                                        value={formData.incidentDate}
                                        onChange={handleChange}
                                        className="form-input"
                                    />
                                </div>
                            </div>

                            <div style={{ marginBottom: '16px' }}>
                                <label className="form-label">Hospital Name</label>
                                <input
                                    type="text"
                                    name="hospitalName"
                                    value={formData.hospitalName}
                                    onChange={handleChange}
                                    className="form-input"
                                />
                            </div>

                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
                                <div>
                                    <label className="form-label">Doctor Name</label>
                                    <input
                                        type="text"
                                        name="doctorName"
                                        value={formData.doctorName}
                                        onChange={handleChange}
                                        className="form-input"
                                    />
                                </div>
                                <div>
                                    <label className="form-label">Diagnosis</label>
                                    <input
                                        type="text"
                                        name="diagnosis"
                                        value={formData.diagnosis}
                                        onChange={handleChange}
                                        className="form-input"
                                    />
                                </div>
                            </div>

                            <div style={{ marginBottom: '24px' }}>
                                <label className="form-label">Reason *</label>
                                <textarea
                                    name="reason"
                                    value={formData.reason}
                                    onChange={handleChange}
                                    className="form-input"
                                    rows="3"
                                    required
                                />
                            </div>

                            <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
                                <button type="button" className="btn-secondary" onClick={() => setShowClaimForm(false)}>
                                    Cancel
                                </button>
                                <button type="submit" className="btn-primary">
                                    File Claim
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ClaimsManagement;
