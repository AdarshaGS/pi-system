import { useState, useEffect } from 'react';
import './TransactionModal.css';
import api from '../api';

const TransactionModal = ({ type = 'BUY', symbol = null, onSave, onClose }) => {
    const [formData, setFormData] = useState({
        symbol: symbol || '',
        transactionType: type,
        quantity: '',
        price: '',
        fees: '0',
        transactionDate: new Date().toISOString().split('T')[0],
        notes: ''
    });

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const calculateTotalAmount = () => {
        const quantity = parseFloat(formData.quantity) || 0;
        const price = parseFloat(formData.price) || 0;
        const fees = parseFloat(formData.fees) || 0;
        
        const baseAmount = quantity * price;
        if (formData.transactionType === 'BUY') {
            return baseAmount + fees;
        } else if (formData.transactionType === 'SELL') {
            return baseAmount - fees;
        }
        return baseAmount;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const token = localStorage.getItem('token');
            
            const payload = {
                ...formData,
                quantity: parseInt(formData.quantity),
                price: parseFloat(formData.price),
                fees: parseFloat(formData.fees || 0)
            };

            await api.post('/portfolio/transactions', payload, {
                headers: { Authorization: `Bearer ${token}` }
            });

            onSave();
        } catch (err) {
            console.error('Error recording transaction:', err);
            setError(err.response?.data?.message || 'Failed to record transaction. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2>{type} Stock</h2>
                    <button className="modal-close" onClick={onClose}>&times;</button>
                </div>

                <form onSubmit={handleSubmit} className="transaction-form">
                    {error && (
                        <div className="error-message" style={{ 
                            padding: '12px', 
                            backgroundColor: '#ffebee', 
                            color: '#c62828', 
                            borderRadius: '4px', 
                            marginBottom: '16px',
                            fontSize: '14px'
                        }}>
                            {error}
                        </div>
                    )}

                    <div className="form-group">
                        <label htmlFor="transactionType">Transaction Type *</label>
                        <select
                            id="transactionType"
                            name="transactionType"
                            value={formData.transactionType}
                            onChange={handleChange}
                            required
                        >
                            <option value="BUY">BUY</option>
                            <option value="SELL">SELL</option>
                            <option value="DIVIDEND">DIVIDEND</option>
                            <option value="BONUS">BONUS</option>
                            <option value="SPLIT">SPLIT</option>
                        </select>
                    </div>

                    <div className="form-group">
                        <label htmlFor="symbol">Stock Symbol *</label>
                        <input
                            type="text"
                            id="symbol"
                            name="symbol"
                            value={formData.symbol}
                            onChange={handleChange}
                            placeholder="e.g., RELIANCE, TCS"
                            required
                            style={{ textTransform: 'uppercase' }}
                        />
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="quantity">Quantity *</label>
                            <input
                                type="number"
                                id="quantity"
                                name="quantity"
                                value={formData.quantity}
                                onChange={handleChange}
                                placeholder="Number of shares"
                                min="1"
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="price">Price per Share *</label>
                            <input
                                type="number"
                                id="price"
                                name="price"
                                value={formData.price}
                                onChange={handleChange}
                                placeholder="₹ 0.00"
                                step="0.01"
                                min="0.01"
                                required
                            />
                        </div>
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="fees">Brokerage Fees</label>
                            <input
                                type="number"
                                id="fees"
                                name="fees"
                                value={formData.fees}
                                onChange={handleChange}
                                placeholder="₹ 0.00"
                                step="0.01"
                                min="0"
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="transactionDate">Transaction Date *</label>
                            <input
                                type="date"
                                id="transactionDate"
                                name="transactionDate"
                                value={formData.transactionDate}
                                onChange={handleChange}
                                required
                            />
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="notes">Notes (Optional)</label>
                        <textarea
                            id="notes"
                            name="notes"
                            value={formData.notes}
                            onChange={handleChange}
                            placeholder="Add any notes about this transaction..."
                            rows="3"
                            maxLength="1000"
                        />
                    </div>

                    <div className="transaction-summary">
                        <div className="summary-row">
                            <span>Base Amount:</span>
                            <span>
                                ₹ {((parseFloat(formData.quantity) || 0) * (parseFloat(formData.price) || 0)).toFixed(2)}
                            </span>
                        </div>
                        <div className="summary-row">
                            <span>Fees:</span>
                            <span>₹ {(parseFloat(formData.fees) || 0).toFixed(2)}</span>
                        </div>
                        <div className="summary-row total">
                            <span><strong>Total Amount:</strong></span>
                            <span><strong>₹ {calculateTotalAmount().toFixed(2)}</strong></span>
                        </div>
                    </div>

                    <div className="modal-actions">
                        <button 
                            type="button" 
                            className="btn-secondary" 
                            onClick={onClose}
                            disabled={loading}
                        >
                            Cancel
                        </button>
                        <button 
                            type="submit" 
                            className="btn-primary"
                            disabled={loading}
                        >
                            {loading ? 'Recording...' : `Record ${formData.transactionType}`}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default TransactionModal;
