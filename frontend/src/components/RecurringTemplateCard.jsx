/**
 * RecurringTemplateCard Component
 * 
 * Displays a single recurring transaction template with:
 * - Template name and category
 * - Frequency and schedule
 * - Amount and type
 * - Active/Paused status
 * - Quick actions (Pause/Resume, Delete)
 * 
 * Author: Pi-System
 * Date: February 5, 2026
 */

import React, { useState } from 'react';
import { getUpcomingTransactions } from '../api/recurringTransactionsApi';
import {
    FaClock,
    FaCalendarAlt,
    FaPause,
    FaPlay,
    FaTrash,
    FaCheckCircle,
    FaTimesCircle
} from 'react-icons/fa';
import './RecurringTemplateCard.css';

const RecurringTemplateCard = ({ template, onDelete, onPause, onResume, onRefresh }) => {
    const [showDetails, setShowDetails] = useState(false);

    const getFrequencyIcon = (frequency) => {
        const icons = {
            DAILY: '📅',
            WEEKLY: '📆',
            MONTHLY: '🗓️',
            YEARLY: '📊'
        };
        return icons[frequency] || '🔄';
    };

    const getFrequencyLabel = (frequency) => {
        return frequency.charAt(0) + frequency.slice(1).toLowerCase();
    };

    const getCategoryColor = (category) => {
        const colors = {
            'SALARY': '#10b981',
            'RENT': '#ef4444',
            'UTILITIES': '#f59e0b',
            'SUBSCRIPTION': '#6366f1',
            'EMI': '#ef4444',
            'INVESTMENT': '#8b5cf6',
            'INSURANCE': '#3b82f6'
        };
        return colors[category] || '#6b7280';
    };

    const getNextOccurrence = () => {
        if (!template.nextOccurrenceDate) return 'Not scheduled';
        const date = new Date(template.nextOccurrenceDate);
        const today = new Date();
        const diffTime = date - today;
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

        if (diffDays === 0) return 'Today';
        if (diffDays === 1) return 'Tomorrow';
        if (diffDays < 0) return 'Overdue';
        return `in ${diffDays} days`;
    };

    return (
        <div className={`template-card ${!template.isActive ? 'paused' : ''}`}>
            {/* Header */}
            <div className="template-header">
                <div className="template-icon">
                    <span className="icon-emoji">{getFrequencyIcon(template.frequency)}</span>
                </div>
                <div className="template-title-section">
                    <h3>{template.templateName}</h3>
                    <div className="template-badges">
                        <span 
                            className="category-badge"
                            style={{ backgroundColor: getCategoryColor(template.category) + '20', 
                                     color: getCategoryColor(template.category) }}
                        >
                            {template.category}
                        </span>
                        <span className={`status-badge ${template.isActive ? 'active' : 'paused'}`}>
                            {template.isActive ? <FaCheckCircle /> : <FaTimesCircle />}
                            {template.isActive ? 'Active' : 'Paused'}
                        </span>
                    </div>
                </div>
            </div>

            {/* Amount */}
            <div className="template-amount-section">
                <div className={`amount ${template.transactionType === 'INCOME' ? 'income' : 'expense'}`}>
                    {template.transactionType === 'INCOME' ? '+' : '-'}₹{(template.amount || 0).toLocaleString()}
                </div>
                <span className="amount-label">{template.transactionType}</span>
            </div>

            {/* Schedule Info */}
            <div className="template-schedule">
                <div className="schedule-item">
                    <FaClock />
                    <span>Every {getFrequencyLabel(template.frequency)}</span>
                </div>
                {template.nextOccurrenceDate && (
                    <div className="schedule-item">
                        <FaCalendarAlt />
                        <span>Next: {getNextOccurrence()}</span>
                    </div>
                )}
            </div>

            {/* Date Range */}
            <div className="template-dates">
                <div className="date-info">
                    <span className="date-label">Start:</span>
                    <span className="date-value">
                        {new Date(template.startDate).toLocaleDateString()}
                    </span>
                </div>
                {template.endDate && (
                    <div className="date-info">
                        <span className="date-label">End:</span>
                        <span className="date-value">
                            {new Date(template.endDate).toLocaleDateString()}
                        </span>
                    </div>
                )}
            </div>

            {/* Auto-generate indicator */}
            {template.autoGenerate && (
                <div className="auto-generate-badge">
                    <FaCheckCircle /> Auto-generate enabled
                </div>
            )}

            {/* Description */}
            {template.description && (
                <div className="template-description">
                    <p>{template.description}</p>
                </div>
            )}

            {/* Stats */}
            <div className="template-stats">
                <div className="stat-item">
                    <span className="stat-label">Generated:</span>
                    <span className="stat-value">{template.generatedCount || 0}</span>
                </div>
                <div className="stat-item">
                    <span className="stat-label">Last:</span>
                    <span className="stat-value">
                        {template.lastGeneratedDate 
                            ? new Date(template.lastGeneratedDate).toLocaleDateString()
                            : 'Never'}
                    </span>
                </div>
            </div>

            {/* Actions */}
            <div className="template-actions">
                {template.isActive ? (
                    <button 
                        className="btn-action btn-pause"
                        onClick={onPause}
                        title="Pause Template"
                    >
                        <FaPause /> Pause
                    </button>
                ) : (
                    <button 
                        className="btn-action btn-resume"
                        onClick={onResume}
                        title="Resume Template"
                    >
                        <FaPlay /> Resume
                    </button>
                )}
                <button 
                    className="btn-action btn-delete"
                    onClick={onDelete}
                    title="Delete Template"
                >
                    <FaTrash /> Delete
                </button>
            </div>
        </div>
    );
};

export default RecurringTemplateCard;
