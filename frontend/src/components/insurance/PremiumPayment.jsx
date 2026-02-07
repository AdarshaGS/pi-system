import { useState } from 'react';
import * as insuranceApi from '../../api/insuranceApi';

const PremiumPayment = ({ policy, onClose, onSave, token }) => {
    const [formData, setFormData] = useState({
        paymentDate: new Date().toISOString().split('T')[0],
        premiumAmount: policy?.premiumAmount || '',
        paymentMode: 'ONLINE',
        transactionReference: '',
        coverageStartDate: '',
        coverageEndDate: '',
        paymentStatus: 'PAID',
        isLatePayment: false,
        lateFee: '0',
        gracePeriodUsed: false,
        receiptNumber: '',
        notes: ''
    });

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            const paymentData = {
                amount: formData.premiumAmount,
                date: formData.paymentDate,
                method: formData.paymentMode,
                transactionReference: formData.transactionReference,
                receiptNumber: formData.receiptNumber,
                notes: formData.notes
            };
            await insuranceApi.recordPremiumPayment(policy.id, paymentData);
            onSave();
        } catch (err) {
            setError(err.message || 'Error recording payment');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '600px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                    <h2 style={{ margin: 0 }}>Record Premium Payment</h2>
                    <button onClick={onClose} style={{ background: 'none', border: 'none', fontSize: '24px', cursor: 'pointer' }}>
                        ×
                    </button>
                </div>

                {/* Policy Info */}
                <div style={{ padding: '16px', background: '#f8f9fa', borderRadius: '8px', marginBottom: '24px' }}>
                    <h3 style={{ fontSize: '16px', marginBottom: '8px' }}>{policy?.policyName}</h3>
                    <div style={{ fontSize: '14px', color: '#666' }}>
                        Policy #: {policy?.policyNumber} • Premium: ₹{policy?.premiumAmount}
                    </div>
                </div>

                {error && (
                    <div style={{ padding: '12px', background: '#fee', color: '#c00', borderRadius: '4px', marginBottom: '16px' }}>
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
                        <div>
                            <label className="form-label">Payment Date *</label>
                            <input
                                type="date"
                                name="paymentDate"
                                value={formData.paymentDate}
                                onChange={handleChange}
                                className="form-input"
                                required
                            />
                        </div>
                        <div>
                            <label className="form-label">Premium Amount *</label>
                            <input
                                type="number"
                                name="premiumAmount"
                                value={formData.premiumAmount}
                                onChange={handleChange}
                                className="form-input"
                                required
                                step="0.01"
                            />
                        </div>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
                        <div>
                            <label className="form-label">Payment Mode *</label>
                            <select name="paymentMode" value={formData.paymentMode} onChange={handleChange} className="form-input" required>
                                <option value="ONLINE">Online</option>
                                <option value="CHEQUE">Cheque</option>
                                <option value="CASH">Cash</option>
                                <option value="AUTO_DEBIT">Auto Debit</option>
                            </select>
                        </div>
                        <div>
                            <label className="form-label">Transaction Reference</label>
                            <input
                                type="text"
                                name="transactionReference"
                                value={formData.transactionReference}
                                onChange={handleChange}
                                className="form-input"
                            />
                        </div>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
                        <div>
                            <label className="form-label">Coverage Start Date *</label>
                            <input
                                type="date"
                                name="coverageStartDate"
                                value={formData.coverageStartDate}
                                onChange={handleChange}
                                className="form-input"
                                required
                            />
                        </div>
                        <div>
                            <label className="form-label">Coverage End Date *</label>
                            <input
                                type="date"
                                name="coverageEndDate"
                                value={formData.coverageEndDate}
                                onChange={handleChange}
                                className="form-input"
                                required
                            />
                        </div>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
                        <div>
                            <label className="form-label">Receipt Number</label>
                            <input
                                type="text"
                                name="receiptNumber"
                                value={formData.receiptNumber}
                                onChange={handleChange}
                                className="form-input"
                            />
                        </div>
                        <div>
                            <label className="form-label">Payment Status</label>
                            <select name="paymentStatus" value={formData.paymentStatus} onChange={handleChange} className="form-input">
                                <option value="PAID">Paid</option>
                                <option value="PENDING">Pending</option>
                                <option value="FAILED">Failed</option>
                                <option value="REFUNDED">Refunded</option>
                            </select>
                        </div>
                    </div>

                    <div style={{ marginBottom: '16px' }}>
                        <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                            <input
                                type="checkbox"
                                name="isLatePayment"
                                checked={formData.isLatePayment}
                                onChange={handleChange}
                            />
                            <span>Late Payment</span>
                        </label>
                    </div>

                    {formData.isLatePayment && (
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
                            <div>
                                <label className="form-label">Late Fee</label>
                                <input
                                    type="number"
                                    name="lateFee"
                                    value={formData.lateFee}
                                    onChange={handleChange}
                                    className="form-input"
                                    step="0.01"
                                />
                            </div>
                            <div>
                                <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', marginTop: '24px' }}>
                                    <input
                                        type="checkbox"
                                        name="gracePeriodUsed"
                                        checked={formData.gracePeriodUsed}
                                        onChange={handleChange}
                                    />
                                    <span>Grace Period Used</span>
                                </label>
                            </div>
                        </div>
                    )}

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

                    <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
                        <button type="button" className="btn-secondary" onClick={onClose} disabled={loading}>
                            Cancel
                        </button>
                        <button type="submit" className="btn-primary" disabled={loading}>
                            {loading ? 'Recording...' : 'Record Payment'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default PremiumPayment;
