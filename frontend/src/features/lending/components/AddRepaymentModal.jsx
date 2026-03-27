import { useState } from 'react';
import { X } from 'lucide-react';

const AddRepaymentModal = ({ onClose, onSubmit, maxAmount }) => {
    const [formData, setFormData] = useState({
        amount: '',
        repaymentDate: new Date().toISOString().split('T')[0],
        repaymentMethod: 'CASH',
        notes: ''
    });

    const [errors, setErrors] = useState({});

    const paymentMethods = [
        { value: 'CASH', label: 'Cash' },
        { value: 'BANK_TRANSFER', label: 'Bank Transfer' },
        { value: 'UPI', label: 'UPI' },
        { value: 'CHEQUE', label: 'Cheque' },
        { value: 'OTHER', label: 'Other' }
    ];

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
        // Clear error for this field
        if (errors[name]) {
            setErrors(prev => ({ ...prev, [name]: null }));
        }
    };

    const validate = () => {
        const newErrors = {};

        if (!formData.amount || Number(formData.amount) <= 0) {
            newErrors.amount = 'Amount must be greater than 0';
        } else if (maxAmount && Number(formData.amount) > maxAmount) {
            newErrors.amount = `Amount cannot exceed outstanding balance of ₹${maxAmount.toFixed(2)}`;
        }

        if (!formData.repaymentDate) {
            newErrors.repaymentDate = 'Repayment date is required';
        }

        if (!formData.repaymentMethod) {
            newErrors.repaymentMethod = 'Payment method is required';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        
        if (validate()) {
            onSubmit({
                ...formData,
                amount: Number(formData.amount)
            });
        }
    };

    const formatCurrency = (value) => {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            maximumFractionDigits: 2
        }).format(value || 0);
    };

    return (
        <div style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'rgba(0, 0, 0, 0.5)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1001
        }}>
            <div style={{
                background: 'white',
                borderRadius: '12px',
                width: '90%',
                maxWidth: '500px',
                maxHeight: '90vh',
                overflow: 'auto',
                boxShadow: '0 4px 20px rgba(0, 0, 0, 0.15)'
            }}>
                {/* Header */}
                <div style={{
                    padding: '20px 24px',
                    borderBottom: '1px solid var(--border-color)',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center'
                }}>
                    <h2 style={{ margin: 0, fontSize: '20px', fontWeight: '600' }}>Record Repayment</h2>
                    <button
                        onClick={onClose}
                        style={{
                            background: 'none',
                            border: 'none',
                            cursor: 'pointer',
                            padding: '4px',
                            display: 'flex',
                            alignItems: 'center'
                        }}
                    >
                        <X size={24} color="#666" />
                    </button>
                </div>

                {/* Form */}
                <form onSubmit={handleSubmit} style={{ padding: '24px' }}>
                    {maxAmount && (
                        <div style={{
                            padding: '12px',
                            background: '#e3f2fd',
                            borderRadius: '6px',
                            marginBottom: '20px',
                            fontSize: '14px',
                            color: '#0c5460'
                        }}>
                            <strong>Outstanding Balance:</strong> {formatCurrency(maxAmount)}
                        </div>
                    )}

                    {/* Amount */}
                    <div style={{ marginBottom: '20px' }}>
                        <label style={{ display: 'block', marginBottom: '6px', fontWeight: '500', fontSize: '14px' }}>
                            Repayment Amount (₹) <span style={{ color: '#ff6b6b' }}>*</span>
                        </label>
                        <input
                            type="number"
                            name="amount"
                            value={formData.amount}
                            onChange={handleChange}
                            placeholder="Enter amount"
                            min="0"
                            step="0.01"
                            max={maxAmount || undefined}
                            style={{
                                width: '100%',
                                padding: '10px 12px',
                                border: `1px solid ${errors.amount ? '#ff6b6b' : 'var(--border-color)'}`,
                                borderRadius: '6px',
                                fontSize: '14px',
                                boxSizing: 'border-box'
                            }}
                        />
                        {errors.amount && (
                            <div style={{ color: '#ff6b6b', fontSize: '13px', marginTop: '4px' }}>
                                {errors.amount}
                            </div>
                        )}
                        {formData.amount && maxAmount && Number(formData.amount) === maxAmount && (
                            <div style={{ color: '#28a745', fontSize: '13px', marginTop: '4px' }}>
                                ✓ Full repayment - this will close the lending record
                            </div>
                        )}
                    </div>

                    {/* Repayment Date */}
                    <div style={{ marginBottom: '20px' }}>
                        <label style={{ display: 'block', marginBottom: '6px', fontWeight: '500', fontSize: '14px' }}>
                            Repayment Date <span style={{ color: '#ff6b6b' }}>*</span>
                        </label>
                        <input
                            type="date"
                            name="repaymentDate"
                            value={formData.repaymentDate}
                            onChange={handleChange}
                            style={{
                                width: '100%',
                                padding: '10px 12px',
                                border: `1px solid ${errors.repaymentDate ? '#ff6b6b' : 'var(--border-color)'}`,
                                borderRadius: '6px',
                                fontSize: '14px',
                                boxSizing: 'border-box'
                            }}
                        />
                        {errors.repaymentDate && (
                            <div style={{ color: '#ff6b6b', fontSize: '13px', marginTop: '4px' }}>
                                {errors.repaymentDate}
                            </div>
                        )}
                    </div>

                    {/* Payment Method */}
                    <div style={{ marginBottom: '20px' }}>
                        <label style={{ display: 'block', marginBottom: '6px', fontWeight: '500', fontSize: '14px' }}>
                            Payment Method <span style={{ color: '#ff6b6b' }}>*</span>
                        </label>
                        <select
                            name="repaymentMethod"
                            value={formData.repaymentMethod}
                            onChange={handleChange}
                            style={{
                                width: '100%',
                                padding: '10px 12px',
                                border: `1px solid ${errors.repaymentMethod ? '#ff6b6b' : 'var(--border-color)'}`,
                                borderRadius: '6px',
                                fontSize: '14px',
                                boxSizing: 'border-box',
                                cursor: 'pointer'
                            }}
                        >
                            {paymentMethods.map(method => (
                                <option key={method.value} value={method.value}>
                                    {method.label}
                                </option>
                            ))}
                        </select>
                        {errors.repaymentMethod && (
                            <div style={{ color: '#ff6b6b', fontSize: '13px', marginTop: '4px' }}>
                                {errors.repaymentMethod}
                            </div>
                        )}
                    </div>

                    {/* Notes */}
                    <div style={{ marginBottom: '24px' }}>
                        <label style={{ display: 'block', marginBottom: '6px', fontWeight: '500', fontSize: '14px' }}>
                            Notes
                        </label>
                        <textarea
                            name="notes"
                            value={formData.notes}
                            onChange={handleChange}
                            placeholder="Add any notes about this repayment..."
                            rows={3}
                            style={{
                                width: '100%',
                                padding: '10px 12px',
                                border: '1px solid var(--border-color)',
                                borderRadius: '6px',
                                fontSize: '14px',
                                boxSizing: 'border-box',
                                fontFamily: 'inherit',
                                resize: 'vertical'
                            }}
                        />
                    </div>

                    {/* Actions */}
                    <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
                        <button
                            type="button"
                            onClick={onClose}
                            style={{
                                padding: '10px 20px',
                                border: '1px solid var(--border-color)',
                                borderRadius: '6px',
                                background: 'white',
                                cursor: 'pointer',
                                fontSize: '14px',
                                fontWeight: '500'
                            }}
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            style={{
                                padding: '10px 20px',
                                border: 'none',
                                borderRadius: '6px',
                                background: '#28a745',
                                color: 'white',
                                cursor: 'pointer',
                                fontSize: '14px',
                                fontWeight: '500'
                            }}
                        >
                            Record Repayment
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default AddRepaymentModal;
