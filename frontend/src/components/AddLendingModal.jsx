import { useState, useEffect } from 'react';
import { X } from 'lucide-react';

const AddLendingModal = ({ onClose, onSubmit, editData = null }) => {
    const [formData, setFormData] = useState({
        borrowerName: '',
        borrowerContact: '',
        amountLent: '',
        dateLent: new Date().toISOString().split('T')[0],
        dueDate: '',
        notes: ''
    });

    useEffect(() => {
        if (editData) {
            setFormData({
                borrowerName: editData.borrowerName || '',
                borrowerContact: editData.borrowerContact || '',
                amountLent: editData.amountLent || '',
                dateLent: editData.dateLent ? new Date(editData.dateLent).toISOString().split('T')[0] : '',
                dueDate: editData.dueDate ? new Date(editData.dueDate).toISOString().split('T')[0] : '',
                notes: editData.notes || ''
            });
        }
    }, [editData]);

    const [errors, setErrors] = useState({});

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

        if (!formData.borrowerName.trim()) {
            newErrors.borrowerName = 'Borrower name is required';
        }

        if (!formData.amountLent || Number(formData.amountLent) <= 0) {
            newErrors.amountLent = 'Amount must be greater than 0';
        }

        if (!formData.dateLent) {
            newErrors.dateLent = 'Lending date is required';
        }

        if (!formData.dueDate) {
            newErrors.dueDate = 'Due date is required';
        } else if (new Date(formData.dueDate) <= new Date(formData.dateLent)) {
            newErrors.dueDate = 'Due date must be after lending date';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        
        if (validate()) {
            onSubmit({
                ...formData,
                amountLent: Number(formData.amountLent)
            });
        }
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
            zIndex: 1000
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
                    <h2 style={{ margin: 0, fontSize: '20px', fontWeight: '600' }}>
                        {editData ? 'Edit Lending Record' : 'Add New Lending'}
                    </h2>
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
                    {/* Borrower Name */}
                    <div style={{ marginBottom: '20px' }}>
                        <label style={{ display: 'block', marginBottom: '6px', fontWeight: '500', fontSize: '14px' }}>
                            Borrower Name <span style={{ color: '#ff6b6b' }}>*</span>
                        </label>
                        <input
                            type="text"
                            name="borrowerName"
                            value={formData.borrowerName}
                            onChange={handleChange}
                            placeholder="Enter borrower's full name"
                            style={{
                                width: '100%',
                                padding: '10px 12px',
                                border: `1px solid ${errors.borrowerName ? '#ff6b6b' : 'var(--border-color)'}`,
                                borderRadius: '6px',
                                fontSize: '14px',
                                boxSizing: 'border-box'
                            }}
                        />
                        {errors.borrowerName && (
                            <div style={{ color: '#ff6b6b', fontSize: '13px', marginTop: '4px' }}>
                                {errors.borrowerName}
                            </div>
                        )}
                    </div>

                    {/* Borrower Contact */}
                    <div style={{ marginBottom: '20px' }}>
                        <label style={{ display: 'block', marginBottom: '6px', fontWeight: '500', fontSize: '14px' }}>
                            Contact (Phone/Email)
                        </label>
                        <input
                            type="text"
                            name="borrowerContact"
                            value={formData.borrowerContact}
                            onChange={handleChange}
                            placeholder="Phone number or email address"
                            style={{
                                width: '100%',
                                padding: '10px 12px',
                                border: '1px solid var(--border-color)',
                                borderRadius: '6px',
                                fontSize: '14px',
                                boxSizing: 'border-box'
                            }}
                        />
                    </div>

                    {/* Amount Lent */}
                    <div style={{ marginBottom: '20px' }}>
                        <label style={{ display: 'block', marginBottom: '6px', fontWeight: '500', fontSize: '14px' }}>
                            Amount Lent (₹) <span style={{ color: '#ff6b6b' }}>*</span>
                        </label>
                        <input
                            type="number"
                            name="amountLent"
                            value={formData.amountLent}
                            onChange={handleChange}
                            placeholder="Enter amount"
                            min="0"
                            step="0.01"
                            style={{
                                width: '100%',
                                padding: '10px 12px',
                                border: `1px solid ${errors.amountLent ? '#ff6b6b' : 'var(--border-color)'}`,
                                borderRadius: '6px',
                                fontSize: '14px',
                                boxSizing: 'border-box'
                            }}
                        />
                        {errors.amountLent && (
                            <div style={{ color: '#ff6b6b', fontSize: '13px', marginTop: '4px' }}>
                                {errors.amountLent}
                            </div>
                        )}
                    </div>

                    {/* Dates Row */}
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '20px' }}>
                        {/* Date Lent */}
                        <div>
                            <label style={{ display: 'block', marginBottom: '6px', fontWeight: '500', fontSize: '14px' }}>
                                Date Lent <span style={{ color: '#ff6b6b' }}>*</span>
                            </label>
                            <input
                                type="date"
                                name="dateLent"
                                value={formData.dateLent}
                                onChange={handleChange}
                                style={{
                                    width: '100%',
                                    padding: '10px 12px',
                                    border: `1px solid ${errors.dateLent ? '#ff6b6b' : 'var(--border-color)'}`,
                                    borderRadius: '6px',
                                    fontSize: '14px',
                                    boxSizing: 'border-box'
                                }}
                            />
                            {errors.dateLent && (
                                <div style={{ color: '#ff6b6b', fontSize: '13px', marginTop: '4px' }}>
                                    {errors.dateLent}
                                </div>
                            )}
                        </div>

                        {/* Due Date */}
                        <div>
                            <label style={{ display: 'block', marginBottom: '6px', fontWeight: '500', fontSize: '14px' }}>
                                Due Date <span style={{ color: '#ff6b6b' }}>*</span>
                            </label>
                            <input
                                type="date"
                                name="dueDate"
                                value={formData.dueDate}
                                onChange={handleChange}
                                style={{
                                    width: '100%',
                                    padding: '10px 12px',
                                    border: `1px solid ${errors.dueDate ? '#ff6b6b' : 'var(--border-color)'}`,
                                    borderRadius: '6px',
                                    fontSize: '14px',
                                    boxSizing: 'border-box'
                                }}
                            />
                            {errors.dueDate && (
                                <div style={{ color: '#ff6b6b', fontSize: '13px', marginTop: '4px' }}>
                                    {errors.dueDate}
                                </div>
                            )}
                        </div>
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
                            placeholder="Add any additional notes..."
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
                                background: '#4f46e5',
                                color: 'white',
                                cursor: 'pointer',
                                fontSize: '14px',
                                fontWeight: '500',
                                boxShadow: '0 2px 4px rgba(79, 70, 229, 0.3)'
                            }}
                        >
                            {editData ? 'Update Lending' : 'Add Lending'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default AddLendingModal;
