/**
 * CreateTemplateModal Component
 * 
 * Modal form for creating recurring transaction templates with:
 * - Template name and description
 * - Transaction type (Income/Expense) and category
 * - Amount and frequency
 * - Start/End dates
 * - Auto-generate toggle
 * 
 * Author: Pi-System
 * Date: February 5, 2026
 */

import React, { useState } from 'react';
import { createTemplate } from '../api/recurringTransactionsApi';
import { FaTimes } from 'react-icons/fa';
import './CreateTemplateModal.css';

const CreateTemplateModal = ({ isOpen, onClose, onSuccess }) => {
    const [formData, setFormData] = useState({
        templateName: '',
        description: '',
        transactionType: 'EXPENSE',
        category: 'UTILITIES',
        amount: '',
        frequency: 'MONTHLY',
        startDate: new Date().toISOString().split('T')[0],
        endDate: '',
        autoGenerate: true
    });

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const transactionTypes = [
        { value: 'INCOME', label: 'Income 💰' },
        { value: 'EXPENSE', label: 'Expense 💸' }
    ];

    const categories = {
        INCOME: [
            { value: 'SALARY', label: 'Salary' },
            { value: 'FREELANCE', label: 'Freelance Income' },
            { value: 'BUSINESS', label: 'Business Income' },
            { value: 'INTEREST', label: 'Interest' },
            { value: 'DIVIDEND', label: 'Dividend' },
            { value: 'RENTAL', label: 'Rental Income' },
            { value: 'OTHER', label: 'Other Income' }
        ],
        EXPENSE: [
            { value: 'RENT', label: 'Rent' },
            { value: 'UTILITIES', label: 'Utilities' },
            { value: 'SUBSCRIPTION', label: 'Subscription' },
            { value: 'EMI', label: 'EMI/Loan Payment' },
            { value: 'INSURANCE', label: 'Insurance Premium' },
            { value: 'INVESTMENT', label: 'Investment (SIP)' },
            { value: 'MAINTENANCE', label: 'Maintenance' },
            { value: 'GROCERIES', label: 'Groceries' },
            { value: 'EDUCATION', label: 'Education' },
            { value: 'HEALTHCARE', label: 'Healthcare' },
            { value: 'OTHER', label: 'Other Expense' }
        ]
    };

    const frequencies = [
        { value: 'DAILY', label: 'Daily 📅', description: 'Every day' },
        { value: 'WEEKLY', label: 'Weekly 📆', description: 'Every week' },
        { value: 'MONTHLY', label: 'Monthly 🗓️', description: 'Every month' },
        { value: 'YEARLY', label: 'Yearly 📊', description: 'Every year' }
    ];

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        
        let newFormData = {
            ...formData,
            [name]: type === 'checkbox' ? checked : value
        };

        // Reset category when transaction type changes
        if (name === 'transactionType') {
            newFormData.category = value === 'INCOME' ? 'SALARY' : 'UTILITIES';
        }

        setFormData(newFormData);
    };

    const validateForm = () => {
        if (!formData.templateName.trim()) {
            setError('Please enter a template name');
            return false;
        }
        if (!formData.amount || parseFloat(formData.amount) <= 0) {
            setError('Please enter a valid amount');
            return false;
        }
        if (!formData.startDate) {
            setError('Please select a start date');
            return false;
        }
        if (formData.endDate && new Date(formData.endDate) <= new Date(formData.startDate)) {
            setError('End date must be after start date');
            return false;
        }
        return true;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        if (!validateForm()) {
            return;
        }

        setLoading(true);
        try {
            const userId = localStorage.getItem('userId') || '1';

            const templateData = {
                userId: parseInt(userId),
                templateName: formData.templateName.trim(),
                description: formData.description.trim(),
                transactionType: formData.transactionType,
                category: formData.category,
                amount: parseFloat(formData.amount),
                frequency: formData.frequency,
                startDate: formData.startDate,
                endDate: formData.endDate || null,
                autoGenerate: formData.autoGenerate,
                isActive: true
            };

            await createTemplate(templateData);
            onSuccess();
            handleClose();
        } catch (err) {
            setError('Failed to create template: ' + (err.message || 'Unknown error'));
        } finally {
            setLoading(false);
        }
    };

    const handleClose = () => {
        setFormData({
            templateName: '',
            description: '',
            transactionType: 'EXPENSE',
            category: 'UTILITIES',
            amount: '',
            frequency: 'MONTHLY',
            startDate: new Date().toISOString().split('T')[0],
            endDate: '',
            autoGenerate: true
        });
        setError('');
        onClose();
    };

    if (!isOpen) return null;

    const currentCategories = categories[formData.transactionType];

    return (
        <div className="modal-overlay" onClick={handleClose}>
            <div className="modal-content create-template-modal" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2>Create Recurring Template</h2>
                    <button className="close-button" onClick={handleClose}>
                        <FaTimes />
                    </button>
                </div>

                {error && <div className="error-message">{error}</div>}

                <form onSubmit={handleSubmit}>
                    <div className="form-section">
                        <h3>Template Details</h3>
                        
                        <div className="form-group">
                            <label htmlFor="templateName">Template Name *</label>
                            <input
                                type="text"
                                id="templateName"
                                name="templateName"
                                value={formData.templateName}
                                onChange={handleChange}
                                placeholder="e.g., Monthly Rent, Netflix Subscription"
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="description">Description</label>
                            <textarea
                                id="description"
                                name="description"
                                value={formData.description}
                                onChange={handleChange}
                                placeholder="Optional description or notes..."
                                rows="2"
                            />
                        </div>
                    </div>

                    <div className="form-section">
                        <h3>Transaction Details</h3>

                        <div className="form-row">
                            <div className="form-group">
                                <label htmlFor="transactionType">Type *</label>
                                <select
                                    id="transactionType"
                                    name="transactionType"
                                    value={formData.transactionType}
                                    onChange={handleChange}
                                    required
                                >
                                    {transactionTypes.map(type => (
                                        <option key={type.value} value={type.value}>
                                            {type.label}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div className="form-group">
                                <label htmlFor="category">Category *</label>
                                <select
                                    id="category"
                                    name="category"
                                    value={formData.category}
                                    onChange={handleChange}
                                    required
                                >
                                    {currentCategories.map(cat => (
                                        <option key={cat.value} value={cat.value}>
                                            {cat.label}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        <div className="form-group">
                            <label htmlFor="amount">Amount (₹) *</label>
                            <input
                                type="number"
                                id="amount"
                                name="amount"
                                value={formData.amount}
                                onChange={handleChange}
                                placeholder="5000"
                                min="0"
                                step="0.01"
                                required
                            />
                        </div>
                    </div>

                    <div className="form-section">
                        <h3>Schedule</h3>

                        <div className="form-group">
                            <label htmlFor="frequency">Frequency *</label>
                            <select
                                id="frequency"
                                name="frequency"
                                value={formData.frequency}
                                onChange={handleChange}
                                required
                            >
                                {frequencies.map(freq => (
                                    <option key={freq.value} value={freq.value}>
                                        {freq.label}
                                    </option>
                                ))}
                            </select>
                            <small className="form-hint">
                                {frequencies.find(f => f.value === formData.frequency)?.description}
                            </small>
                        </div>

                        <div className="form-row">
                            <div className="form-group">
                                <label htmlFor="startDate">Start Date *</label>
                                <input
                                    type="date"
                                    id="startDate"
                                    name="startDate"
                                    value={formData.startDate}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="endDate">End Date (Optional)</label>
                                <input
                                    type="date"
                                    id="endDate"
                                    name="endDate"
                                    value={formData.endDate}
                                    onChange={handleChange}
                                />
                                <small className="form-hint">Leave blank for indefinite</small>
                            </div>
                        </div>

                        <div className="form-group checkbox-group">
                            <label>
                                <input
                                    type="checkbox"
                                    name="autoGenerate"
                                    checked={formData.autoGenerate}
                                    onChange={handleChange}
                                />
                                <span>Auto-generate transactions (recommended)</span>
                            </label>
                            <small className="form-hint">
                                Transactions will be automatically created on scheduled dates
                            </small>
                        </div>
                    </div>

                    <div className="modal-actions">
                        <button 
                            type="button" 
                            className="btn-secondary" 
                            onClick={handleClose}
                            disabled={loading}
                        >
                            Cancel
                        </button>
                        <button 
                            type="submit" 
                            className="btn-primary"
                            disabled={loading}
                        >
                            {loading ? 'Creating...' : 'Create Template'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default CreateTemplateModal;
