/**
 * CreateGoalModal Component
 * 
 * Modal form for creating new financial goals with:
 * - Goal name and type selection
 * - Target amount and date
 * - Monthly contribution settings
 * - Priority and auto-contribute options
 * 
 * Author: Pi-System
 * Date: February 5, 2026
 */

import React, { useState } from 'react';
import { createGoal } from '../api/goalsApi';
import { FaTimes } from 'react-icons/fa';
import './CreateGoalModal.css';

const CreateGoalModal = ({ isOpen, onClose, onSuccess }) => {
    const [formData, setFormData] = useState({
        goalName: '',
        goalType: 'CUSTOM',
        targetAmount: '',
        currentAmount: '',
        targetDate: '',
        monthlyContribution: '',
        expectedReturn: '8',
        priority: 'MEDIUM',
        notes: '',
        autoContribute: false
    });

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const goalTypes = [
        { value: 'RETIREMENT', label: 'Retirement 🏖️', description: 'Long-term retirement planning' },
        { value: 'PROPERTY', label: 'Property 🏠', description: 'Home or real estate purchase' },
        { value: 'EDUCATION', label: 'Education 🎓', description: 'Education expenses' },
        { value: 'EMERGENCY_FUND', label: 'Emergency Fund 🚨', description: '3-6 months expenses' },
        { value: 'TRAVEL', label: 'Travel ✈️', description: 'Vacation or travel plans' },
        { value: 'VEHICLE', label: 'Vehicle 🚗', description: 'Car or vehicle purchase' },
        { value: 'BUSINESS', label: 'Business 💼', description: 'Business startup or investment' },
        { value: 'WEDDING', label: 'Wedding 💍', description: 'Wedding expenses' },
        { value: 'CUSTOM', label: 'Custom 🎯', description: 'Custom financial goal' }
    ];

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData({
            ...formData,
            [name]: type === 'checkbox' ? checked : value
        });
    };

    const validateForm = () => {
        if (!formData.goalName.trim()) {
            setError('Please enter a goal name');
            return false;
        }
        if (!formData.targetAmount || parseFloat(formData.targetAmount) <= 0) {
            setError('Please enter a valid target amount');
            return false;
        }
        if (!formData.targetDate) {
            setError('Please select a target date');
            return false;
        }

        const targetDate = new Date(formData.targetDate);
        const today = new Date();
        if (targetDate <= today) {
            setError('Target date must be in the future');
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
            // Get userId from localStorage/session (adjust based on your auth implementation)
            const userId = localStorage.getItem('userId') || '1';

            const goalData = {
                userId: parseInt(userId),
                goalName: formData.goalName.trim(),
                goalType: formData.goalType,
                targetAmount: parseFloat(formData.targetAmount),
                currentAmount: formData.currentAmount ? parseFloat(formData.currentAmount) : 0,
                targetDate: formData.targetDate,
                monthlyContribution: formData.monthlyContribution ? parseFloat(formData.monthlyContribution) : 0,
                expectedReturn: parseFloat(formData.expectedReturn),
                priority: formData.priority,
                notes: formData.notes.trim(),
                autoContribute: formData.autoContribute,
                status: 'ACTIVE'
            };

            await createGoal(goalData);
            onSuccess();
            handleClose();
        } catch (err) {
            setError('Failed to create goal: ' + (err.message || 'Unknown error'));
        } finally {
            setLoading(false);
        }
    };

    const handleClose = () => {
        setFormData({
            goalName: '',
            goalType: 'CUSTOM',
            targetAmount: '',
            currentAmount: '',
            targetDate: '',
            monthlyContribution: '',
            expectedReturn: '8',
            priority: 'MEDIUM',
            notes: '',
            autoContribute: false
        });
        setError('');
        onClose();
    };

    if (!isOpen) return null;

    return (
        <div className="modal-overlay" onClick={handleClose}>
            <div className="modal-content create-goal-modal" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2>Create New Financial Goal</h2>
                    <button className="close-button" onClick={handleClose}>
                        <FaTimes />
                    </button>
                </div>

                {error && <div className="error-message">{error}</div>}

                <form onSubmit={handleSubmit}>
                    <div className="form-section">
                        <h3>Goal Details</h3>
                        
                        <div className="form-group">
                            <label htmlFor="goalName">Goal Name *</label>
                            <input
                                type="text"
                                id="goalName"
                                name="goalName"
                                value={formData.goalName}
                                onChange={handleChange}
                                placeholder="e.g., Dream Home, Retirement Fund"
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="goalType">Goal Type *</label>
                            <select
                                id="goalType"
                                name="goalType"
                                value={formData.goalType}
                                onChange={handleChange}
                                required
                            >
                                {goalTypes.map(type => (
                                    <option key={type.value} value={type.value}>
                                        {type.label}
                                    </option>
                                ))}
                            </select>
                            <small className="form-hint">
                                {goalTypes.find(t => t.value === formData.goalType)?.description}
                            </small>
                        </div>

                        <div className="form-group">
                            <label htmlFor="priority">Priority</label>
                            <select
                                id="priority"
                                name="priority"
                                value={formData.priority}
                                onChange={handleChange}
                            >
                                <option value="LOW">Low Priority</option>
                                <option value="MEDIUM">Medium Priority</option>
                                <option value="HIGH">High Priority</option>
                            </select>
                        </div>
                    </div>

                    <div className="form-section">
                        <h3>Financial Details</h3>

                        <div className="form-row">
                            <div className="form-group">
                                <label htmlFor="targetAmount">Target Amount (₹) *</label>
                                <input
                                    type="number"
                                    id="targetAmount"
                                    name="targetAmount"
                                    value={formData.targetAmount}
                                    onChange={handleChange}
                                    placeholder="1000000"
                                    min="0"
                                    step="1000"
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="currentAmount">Current Amount (₹)</label>
                                <input
                                    type="number"
                                    id="currentAmount"
                                    name="currentAmount"
                                    value={formData.currentAmount}
                                    onChange={handleChange}
                                    placeholder="0"
                                    min="0"
                                    step="1000"
                                />
                            </div>
                        </div>

                        <div className="form-group">
                            <label htmlFor="targetDate">Target Date *</label>
                            <input
                                type="date"
                                id="targetDate"
                                name="targetDate"
                                value={formData.targetDate}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        <div className="form-row">
                            <div className="form-group">
                                <label htmlFor="monthlyContribution">Monthly Contribution (₹)</label>
                                <input
                                    type="number"
                                    id="monthlyContribution"
                                    name="monthlyContribution"
                                    value={formData.monthlyContribution}
                                    onChange={handleChange}
                                    placeholder="5000"
                                    min="0"
                                    step="100"
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="expectedReturn">Expected Annual Return (%)</label>
                                <input
                                    type="number"
                                    id="expectedReturn"
                                    name="expectedReturn"
                                    value={formData.expectedReturn}
                                    onChange={handleChange}
                                    placeholder="8"
                                    min="0"
                                    max="30"
                                    step="0.1"
                                />
                            </div>
                        </div>

                        <div className="form-group checkbox-group">
                            <label>
                                <input
                                    type="checkbox"
                                    name="autoContribute"
                                    checked={formData.autoContribute}
                                    onChange={handleChange}
                                />
                                <span>Auto-contribute monthly (from primary account)</span>
                            </label>
                        </div>
                    </div>

                    <div className="form-section">
                        <h3>Additional Notes</h3>
                        
                        <div className="form-group">
                            <label htmlFor="notes">Notes</label>
                            <textarea
                                id="notes"
                                name="notes"
                                value={formData.notes}
                                onChange={handleChange}
                                placeholder="Any additional details or reminders about this goal..."
                                rows="3"
                            />
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
                            {loading ? 'Creating...' : 'Create Goal'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default CreateGoalModal;
