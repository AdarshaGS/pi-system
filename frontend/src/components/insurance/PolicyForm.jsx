import { useState, useEffect } from 'react';
import * as insuranceApi from '../../api/insuranceApi';

const PolicyForm = ({ policy, onClose, onSave, token }) => {
    const userId = localStorage.getItem('userId') || 1;
    const [formData, setFormData] = useState({
        policyNumber: '',
        policyType: 'LIFE',
        providerName: '',
        policyName: '',
        sumAssured: '',
        coverageAmount: '',
        bonusAmount: '0',
        premiumAmount: '',
        premiumFrequency: 'YEARLY',
        premiumPayingTerm: '',
        policyTerm: '',
        policyStartDate: '',
        policyEndDate: '',
        nextPremiumDate: '',
        maturityAmount: '',
        maturityDate: '',
        nomineeName: '',
        nomineeRelation: '',
        nomineeDob: '',
        agentName: '',
        agentContact: '',
        notes: '',
        policyStatus: 'ACTIVE'
    });

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        if (policy) {
            setFormData({
                policyNumber: policy.policyNumber || '',
                policyType: policy.policyType || 'LIFE',
                providerName: policy.providerName || '',
                policyName: policy.policyName || '',
                sumAssured: policy.sumAssured || '',
                coverageAmount: policy.coverageAmount || '',
                bonusAmount: policy.bonusAmount || '0',
                premiumAmount: policy.premiumAmount || '',
                premiumFrequency: policy.premiumFrequency || 'YEARLY',
                premiumPayingTerm: policy.premiumPayingTerm || '',
                policyTerm: policy.policyTerm || '',
                policyStartDate: policy.policyStartDate || '',
                policyEndDate: policy.policyEndDate || '',
                nextPremiumDate: policy.nextPremiumDate || '',
                maturityAmount: policy.maturityAmount || '',
                maturityDate: policy.maturityDate || '',
                nomineeName: policy.nomineeName || '',
                nomineeRelation: policy.nomineeRelation || '',
                nomineeDob: policy.nomineeDob || '',
                agentName: policy.agentName || '',
                agentContact: policy.agentContact || '',
                notes: policy.notes || '',
                policyStatus: policy.policyStatus || 'ACTIVE'
            });
        }
    }, [policy]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            // Add userId to formData
            const policyData = {
                ...formData,
                userId: parseInt(userId)
            };
            
            if (policy) {
                await insuranceApi.updateInsurancePolicy(policy.id, policyData);
            } else {
                await insuranceApi.createInsurancePolicy(policyData);
            }
            onSave();
        } catch (err) {
            setError(err.message || 'Error saving policy');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '800px', maxHeight: '90vh', overflowY: 'auto' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                    <h2 style={{ margin: 0 }}>{policy ? 'Edit Policy' : 'Add New Policy'}</h2>
                    <button onClick={onClose} style={{ background: 'none', border: 'none', fontSize: '24px', cursor: 'pointer' }}>
                        ×
                    </button>
                </div>

                {error && (
                    <div style={{ padding: '12px', background: '#fee', color: '#c00', borderRadius: '4px', marginBottom: '16px' }}>
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    {/* Basic Details */}
                    <div style={{ marginBottom: '24px' }}>
                        <h3 style={{ fontSize: '16px', marginBottom: '16px', color: '#666' }}>Basic Details</h3>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                            <div>
                                <label className="form-label">Policy Number *</label>
                                <input
                                    type="text"
                                    name="policyNumber"
                                    value={formData.policyNumber}
                                    onChange={handleChange}
                                    className="form-input"
                                    required
                                />
                            </div>
                            <div>
                                <label className="form-label">Policy Type *</label>
                                <select name="policyType" value={formData.policyType} onChange={handleChange} className="form-input" required>
                                    <option value="LIFE">Life</option>
                                    <option value="HEALTH">Health</option>
                                    <option value="TERM">Term</option>
                                    <option value="ENDOWMENT">Endowment</option>
                                    <option value="ULIP">ULIP</option>
                                    <option value="CRITICAL_ILLNESS">Critical Illness</option>
                                </select>
                            </div>
                            <div>
                                <label className="form-label">Provider Name *</label>
                                <input
                                    type="text"
                                    name="providerName"
                                    value={formData.providerName}
                                    onChange={handleChange}
                                    className="form-input"
                                    required
                                />
                            </div>
                            <div>
                                <label className="form-label">Policy Name *</label>
                                <input
                                    type="text"
                                    name="policyName"
                                    value={formData.policyName}
                                    onChange={handleChange}
                                    className="form-input"
                                    required
                                />
                            </div>
                        </div>
                    </div>

                    {/* Coverage Details */}
                    <div style={{ marginBottom: '24px' }}>
                        <h3 style={{ fontSize: '16px', marginBottom: '16px', color: '#666' }}>Coverage Details</h3>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                            <div>
                                <label className="form-label">Sum Assured *</label>
                                <input
                                    type="number"
                                    name="sumAssured"
                                    value={formData.sumAssured}
                                    onChange={handleChange}
                                    className="form-input"
                                    required
                                />
                            </div>
                            <div>
                                <label className="form-label">Coverage Amount *</label>
                                <input
                                    type="number"
                                    name="coverageAmount"
                                    value={formData.coverageAmount}
                                    onChange={handleChange}
                                    className="form-input"
                                    required
                                />
                            </div>
                            <div>
                                <label className="form-label">Bonus Amount</label>
                                <input
                                    type="number"
                                    name="bonusAmount"
                                    value={formData.bonusAmount}
                                    onChange={handleChange}
                                    className="form-input"
                                />
                            </div>
                        </div>
                    </div>

                    {/* Premium Details */}
                    <div style={{ marginBottom: '24px' }}>
                        <h3 style={{ fontSize: '16px', marginBottom: '16px', color: '#666' }}>Premium Details</h3>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                            <div>
                                <label className="form-label">Premium Amount *</label>
                                <input
                                    type="number"
                                    name="premiumAmount"
                                    value={formData.premiumAmount}
                                    onChange={handleChange}
                                    className="form-input"
                                    required
                                />
                            </div>
                            <div>
                                <label className="form-label">Premium Frequency *</label>
                                <select name="premiumFrequency" value={formData.premiumFrequency} onChange={handleChange} className="form-input" required>
                                    <option value="MONTHLY">Monthly</option>
                                    <option value="QUARTERLY">Quarterly</option>
                                    <option value="HALF_YEARLY">Half Yearly</option>
                                    <option value="YEARLY">Yearly</option>
                                </select>
                            </div>
                            <div>
                                <label className="form-label">Premium Paying Term (Years) *</label>
                                <input
                                    type="number"
                                    name="premiumPayingTerm"
                                    value={formData.premiumPayingTerm}
                                    onChange={handleChange}
                                    className="form-input"
                                    required
                                />
                            </div>
                            <div>
                                <label className="form-label">Policy Term (Years) *</label>
                                <input
                                    type="number"
                                    name="policyTerm"
                                    value={formData.policyTerm}
                                    onChange={handleChange}
                                    className="form-input"
                                    required
                                />
                            </div>
                        </div>
                    </div>

                    {/* Important Dates */}
                    <div style={{ marginBottom: '24px' }}>
                        <h3 style={{ fontSize: '16px', marginBottom: '16px', color: '#666' }}>Important Dates</h3>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                            <div>
                                <label className="form-label">Policy Start Date *</label>
                                <input
                                    type="date"
                                    name="policyStartDate"
                                    value={formData.policyStartDate}
                                    onChange={handleChange}
                                    className="form-input"
                                    required
                                />
                            </div>
                            <div>
                                <label className="form-label">Policy End Date *</label>
                                <input
                                    type="date"
                                    name="policyEndDate"
                                    value={formData.policyEndDate}
                                    onChange={handleChange}
                                    className="form-input"
                                    required
                                />
                            </div>
                            <div>
                                <label className="form-label">Next Premium Date</label>
                                <input
                                    type="date"
                                    name="nextPremiumDate"
                                    value={formData.nextPremiumDate}
                                    onChange={handleChange}
                                    className="form-input"
                                />
                            </div>
                            <div>
                                <label className="form-label">Maturity Date</label>
                                <input
                                    type="date"
                                    name="maturityDate"
                                    value={formData.maturityDate}
                                    onChange={handleChange}
                                    className="form-input"
                                />
                            </div>
                            <div>
                                <label className="form-label">Maturity Amount</label>
                                <input
                                    type="number"
                                    name="maturityAmount"
                                    value={formData.maturityAmount}
                                    onChange={handleChange}
                                    className="form-input"
                                />
                            </div>
                        </div>
                    </div>

                    {/* Nominee Details */}
                    <div style={{ marginBottom: '24px' }}>
                        <h3 style={{ fontSize: '16px', marginBottom: '16px', color: '#666' }}>Nominee Details</h3>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                            <div>
                                <label className="form-label">Nominee Name</label>
                                <input
                                    type="text"
                                    name="nomineeName"
                                    value={formData.nomineeName}
                                    onChange={handleChange}
                                    className="form-input"
                                />
                            </div>
                            <div>
                                <label className="form-label">Nominee Relation</label>
                                <input
                                    type="text"
                                    name="nomineeRelation"
                                    value={formData.nomineeRelation}
                                    onChange={handleChange}
                                    className="form-input"
                                />
                            </div>
                            <div>
                                <label className="form-label">Nominee Date of Birth</label>
                                <input
                                    type="date"
                                    name="nomineeDob"
                                    value={formData.nomineeDob}
                                    onChange={handleChange}
                                    className="form-input"
                                />
                            </div>
                        </div>
                    </div>

                    {/* Agent Details */}
                    <div style={{ marginBottom: '24px' }}>
                        <h3 style={{ fontSize: '16px', marginBottom: '16px', color: '#666' }}>Agent Details</h3>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                            <div>
                                <label className="form-label">Agent Name</label>
                                <input
                                    type="text"
                                    name="agentName"
                                    value={formData.agentName}
                                    onChange={handleChange}
                                    className="form-input"
                                />
                            </div>
                            <div>
                                <label className="form-label">Agent Contact</label>
                                <input
                                    type="text"
                                    name="agentContact"
                                    value={formData.agentContact}
                                    onChange={handleChange}
                                    className="form-input"
                                />
                            </div>
                        </div>
                    </div>

                    {/* Additional Info */}
                    <div style={{ marginBottom: '24px' }}>
                        <label className="form-label">Notes</label>
                        <textarea
                            name="notes"
                            value={formData.notes}
                            onChange={handleChange}
                            className="form-input"
                            rows="3"
                        />
                    </div>

                    {/* Status */}
                    <div style={{ marginBottom: '24px' }}>
                        <label className="form-label">Policy Status</label>
                        <select name="policyStatus" value={formData.policyStatus} onChange={handleChange} className="form-input">
                            <option value="ACTIVE">Active</option>
                            <option value="LAPSED">Lapsed</option>
                            <option value="MATURED">Matured</option>
                            <option value="SURRENDERED">Surrendered</option>
                            <option value="CLAIMED">Claimed</option>
                        </select>
                    </div>

                    {/* Submit Buttons */}
                    <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
                        <button type="button" className="btn-secondary" onClick={onClose} disabled={loading}>
                            Cancel
                        </button>
                        <button type="submit" className="btn-primary" disabled={loading}>
                            {loading ? 'Saving...' : (policy ? 'Update Policy' : 'Add Policy')}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default PolicyForm;
