/**
 * GoalCard Component
 * 
 * Displays a single financial goal with:
 * - Goal icon and name
 * - Progress bar
 * - Current vs Target amounts
 * - On-track indicator
 * - Quick actions
 * 
 * Author: Pi-System
 * Date: February 5, 2026
 */

import React, { useState, useEffect } from 'react';
import { getGoalProjection, recordContribution } from '../api/goalsApi';
import { 
    FaCheckCircle, 
    FaExclamationTriangle, 
    FaEdit, 
    FaTrash,
    FaPlus,
    FaCalendarAlt
} from 'react-icons/fa';
import './GoalCard.css';

const GoalCard = ({ goal, onViewDetails, onDelete, onRefresh }) => {
    const [projection, setProjection] = useState(null);
    const [showContributionModal, setShowContributionModal] = useState(false);
    const [contributionAmount, setContributionAmount] = useState('');

    useEffect(() => {
        if (goal.status === 'ACTIVE') {
            fetchProjection();
        }
    }, [goal.id]);

    const fetchProjection = async () => {
        try {
            const proj = await getGoalProjection(goal.id);
            setProjection(proj);
        } catch (error) {
            console.error('Error fetching projection:', error);
        }
    };

    const handleAddContribution = async () => {
        if (!contributionAmount || parseFloat(contributionAmount) <= 0) {
            alert('Please enter a valid contribution amount');
            return;
        }

        try {
            await recordContribution(goal.id, {
                amount: parseFloat(contributionAmount),
                date: new Date().toISOString().split('T')[0],
                notes: 'Manual contribution'
            });
            setShowContributionModal(false);
            setContributionAmount('');
            onRefresh();
        } catch (error) {
            alert('Error recording contribution: ' + error.message);
        }
    };

    const progressPercentage = Math.min(
        ((goal.currentAmount || 0) / (goal.targetAmount || 1)) * 100,
        100
    );

    const getGoalTypeIcon = (type) => {
        const icons = {
            RETIREMENT: '🏖️',
            PROPERTY: '🏠',
            EDUCATION: '🎓',
            EMERGENCY_FUND: '🚨',
            TRAVEL: '✈️',
            VEHICLE: '🚗',
            BUSINESS: '💼',
            WEDDING: '💍',
            CUSTOM: '🎯'
        };
        return icons[type] || '🎯';
    };

    const getPriorityClass = (priority) => {
        const classes = {
            HIGH: 'priority-high',
            MEDIUM: 'priority-medium',
            LOW: 'priority-low'
        };
        return classes[priority] || 'priority-medium';
    };

    const getStatusClass = (status) => {
        const classes = {
            ACTIVE: 'status-active',
            COMPLETED: 'status-completed',
            PAUSED: 'status-paused',
            ABANDONED: 'status-abandoned'
        };
        return classes[status] || 'status-active';
    };

    const formatDate = (dateString) => {
        if (!dateString) return 'N/A';
        const date = new Date(dateString);
        return date.toLocaleDateString('en-IN', { 
            year: 'numeric', 
            month: 'short', 
            day: 'numeric' 
        });
    };

    const daysUntilTarget = () => {
        if (!goal.targetDate) return null;
        const today = new Date();
        const target = new Date(goal.targetDate);
        const diffTime = target - today;
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        return diffDays;
    };

    const days = daysUntilTarget();

    return (
        <div className={`goal-card ${getPriorityClass(goal.priority)}`}>
            {/* Goal Header */}
            <div className="goal-card-header">
                <div className="goal-icon">
                    <span className="icon-emoji">{getGoalTypeIcon(goal.goalType)}</span>
                </div>
                <div className="goal-title-section">
                    <h3>{goal.goalName}</h3>
                    <div className="goal-badges">
                        <span className={`status-badge ${getStatusClass(goal.status)}`}>
                            {goal.status}
                        </span>
                        <span className={`priority-badge ${getPriorityClass(goal.priority)}`}>
                            {goal.priority}
                        </span>
                    </div>
                </div>
            </div>

            {/* Progress Section */}
            <div className="goal-progress-section">
                <div className="amounts">
                    <div className="amount-current">
                        <span className="label">Current</span>
                        <span className="value">₹{(goal.currentAmount || 0).toLocaleString()}</span>
                    </div>
                    <div className="amount-target">
                        <span className="label">Target</span>
                        <span className="value">₹{(goal.targetAmount || 0).toLocaleString()}</span>
                    </div>
                </div>

                <div className="progress-bar-container">
                    <div className="progress-bar">
                        <div 
                            className="progress-fill"
                            style={{ width: `${progressPercentage}%` }}
                        />
                    </div>
                    <span className="progress-text">{progressPercentage.toFixed(1)}% Complete</span>
                </div>
            </div>

            {/* Target Date */}
            <div className="goal-target-date">
                <FaCalendarAlt />
                <span>Target: {formatDate(goal.targetDate)}</span>
                {days !== null && days > 0 && (
                    <span className="days-remaining">({days} days)</span>
                )}
                {days !== null && days <= 0 && (
                    <span className="days-overdue">(Overdue)</span>
                )}
            </div>

            {/* Projection Info */}
            {projection && goal.status === 'ACTIVE' && (
                <div className="goal-projection">
                    {projection.onTrack ? (
                        <div className="projection-on-track">
                            <FaCheckCircle /> 
                            <span>On Track!</span>
                        </div>
                    ) : (
                        <div className="projection-off-track">
                            <FaExclamationTriangle /> 
                            <div className="shortfall-info">
                                <span>Shortfall: ₹{(projection.shortfall || 0).toLocaleString()}</span>
                                <span className="suggestion">
                                    Increase monthly to ₹{(projection.requiredMonthlyContribution || 0).toLocaleString()}
                                </span>
                            </div>
                        </div>
                    )}
                </div>
            )}

            {/* Monthly Contribution Info */}
            {goal.monthlyContribution > 0 && (
                <div className="goal-contribution-info">
                    <span>Monthly Contribution: ₹{goal.monthlyContribution.toLocaleString()}</span>
                    {goal.autoContribute && (
                        <span className="auto-badge">AUTO</span>
                    )}
                </div>
            )}

            {/* Actions */}
            <div className="goal-card-actions">
                <button 
                    className="btn-action btn-view"
                    onClick={onViewDetails}
                >
                    View Details
                </button>
                <button 
                    className="btn-action btn-add"
                    onClick={() => setShowContributionModal(true)}
                    disabled={goal.status !== 'ACTIVE'}
                >
                    <FaPlus /> Add
                </button>
                <button 
                    className="btn-action btn-delete"
                    onClick={(e) => {
                        e.stopPropagation();
                        onDelete();
                    }}
                    title="Delete Goal"
                >
                    <FaTrash />
                </button>
            </div>

            {/* Contribution Modal */}
            {showContributionModal && (
                <div className="modal-overlay" onClick={() => setShowContributionModal(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <h3>Add Contribution</h3>
                        <p>Goal: {goal.goalName}</p>
                        <input
                            type="number"
                            placeholder="Amount"
                            value={contributionAmount}
                            onChange={(e) => setContributionAmount(e.target.value)}
                            min="0"
                            step="100"
                        />
                        <div className="modal-actions">
                            <button 
                                className="btn-primary"
                                onClick={handleAddContribution}
                            >
                                Record Contribution
                            </button>
                            <button 
                                className="btn-secondary"
                                onClick={() => setShowContributionModal(false)}
                            >
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default GoalCard;
