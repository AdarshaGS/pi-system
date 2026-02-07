/**
 * GoalDetails Page
 * 
 * Detailed view of a financial goal with:
 * - Goal information and progress
 * - Projection chart showing future value
 * - What-if calculator
 * - Milestone timeline
 * - Contribution history
 * - Edit and delete actions
 * 
 * Author: Pi-System
 * Date: February 5, 2026
 */

import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
    getGoalById,
    getGoalProjection,
    calculateWhatIf,
    getMilestones,
    getContributions,
    recordContribution,
    addMilestone,
    updateGoal,
    deleteGoal
} from '../api/goalsApi';
import {
    FaArrowLeft,
    FaBullseye,
    FaChartLine,
    FaCalculator,
    FaFlag,
    FaMoneyBillWave,
    FaEdit,
    FaTrash,
    FaPlus,
    FaCheckCircle
} from 'react-icons/fa';
import './GoalDetails.css';

const GoalDetails = () => {
    const { goalId } = useParams();
    const navigate = useNavigate();

    const [goal, setGoal] = useState(null);
    const [projection, setProjection] = useState(null);
    const [milestones, setMilestones] = useState([]);
    const [contributions, setContributions] = useState([]);
    const [loading, setLoading] = useState(true);

    // What-if calculator state
    const [whatIfAmount, setWhatIfAmount] = useState('');
    const [whatIfResult, setWhatIfResult] = useState(null);

    // Modal states
    const [showContributionModal, setShowContributionModal] = useState(false);
    const [contributionAmount, setContributionAmount] = useState('');
    const [contributionNotes, setContributionNotes] = useState('');

    const [showMilestoneModal, setShowMilestoneModal] = useState(false);
    const [milestoneTitle, setMilestoneTitle] = useState('');
    const [milestoneAmount, setMilestoneAmount] = useState('');
    const [milestoneDate, setMilestoneDate] = useState('');

    useEffect(() => {
        fetchGoalData();
    }, [goalId]);

    const fetchGoalData = async () => {
        try {
            setLoading(true);
            const [goalData, projectionData, milestonesData, contributionsData] = await Promise.all([
                getGoalById(goalId),
                getGoalProjection(goalId).catch(() => null),
                getMilestones(goalId).catch(() => []),
                getContributions(goalId).catch(() => [])
            ]);

            setGoal(goalData);
            setProjection(projectionData);
            setMilestones(milestonesData);
            setContributions(contributionsData);
        } catch (error) {
            console.error('Error fetching goal data:', error);
            alert('Failed to load goal details');
        } finally {
            setLoading(false);
        }
    };

    const handleWhatIfCalculate = async () => {
        if (!whatIfAmount || parseFloat(whatIfAmount) <= 0) {
            alert('Please enter a valid monthly contribution');
            return;
        }

        try {
            const result = await calculateWhatIf(goalId, parseFloat(whatIfAmount));
            setWhatIfResult(result);
        } catch (error) {
            alert('Error calculating what-if scenario: ' + error.message);
        }
    };

    const handleAddContribution = async () => {
        if (!contributionAmount || parseFloat(contributionAmount) <= 0) {
            alert('Please enter a valid contribution amount');
            return;
        }

        try {
            await recordContribution(goalId, {
                amount: parseFloat(contributionAmount),
                date: new Date().toISOString().split('T')[0],
                notes: contributionNotes
            });
            setShowContributionModal(false);
            setContributionAmount('');
            setContributionNotes('');
            fetchGoalData();
        } catch (error) {
            alert('Error recording contribution: ' + error.message);
        }
    };

    const handleAddMilestone = async () => {
        if (!milestoneTitle || !milestoneAmount || !milestoneDate) {
            alert('Please fill all milestone fields');
            return;
        }

        try {
            await addMilestone(goalId, {
                title: milestoneTitle,
                targetAmount: parseFloat(milestoneAmount),
                targetDate: milestoneDate,
                completed: false
            });
            setShowMilestoneModal(false);
            setMilestoneTitle('');
            setMilestoneAmount('');
            setMilestoneDate('');
            fetchGoalData();
        } catch (error) {
            alert('Error adding milestone: ' + error.message);
        }
    };

    const handleDeleteGoal = async () => {
        if (window.confirm('Are you sure you want to delete this goal? This action cannot be undone.')) {
            try {
                await deleteGoal(goalId);
                navigate('/financial-goals');
            } catch (error) {
                alert('Error deleting goal: ' + error.message);
            }
        }
    };

    const handleStatusChange = async (newStatus) => {
        try {
            await updateGoal(goalId, { ...goal, status: newStatus });
            fetchGoalData();
        } catch (error) {
            alert('Error updating goal status: ' + error.message);
        }
    };

    if (loading) {
        return <div className="loading-container">Loading goal details...</div>;
    }

    if (!goal) {
        return <div className="error-container">Goal not found</div>;
    }

    const progressPercentage = Math.min(
        ((goal.currentAmount || 0) / (goal.targetAmount || 1)) * 100,
        100
    );

    return (
        <div className="goal-details-page">
            {/* Header */}
            <div className="page-header">
                <button className="back-button" onClick={() => navigate('/financial-goals')}>
                    <FaArrowLeft /> Back to Goals
                </button>
                <div className="header-actions">
                    <button className="btn-action btn-edit">
                        <FaEdit /> Edit Goal
                    </button>
                    <button className="btn-action btn-delete" onClick={handleDeleteGoal}>
                        <FaTrash /> Delete Goal
                    </button>
                </div>
            </div>

            {/* Goal Overview Card */}
            <div className="goal-overview-card">
                <div className="goal-header-section">
                    <div className="goal-icon-large">
                        {getGoalTypeEmoji(goal.goalType)}
                    </div>
                    <div className="goal-title-section">
                        <h1>{goal.goalName}</h1>
                        <div className="goal-meta">
                            <span className={`status-badge status-${goal.status.toLowerCase()}`}>
                                {goal.status}
                            </span>
                            <span className={`priority-badge priority-${goal.priority.toLowerCase()}`}>
                                {goal.priority} Priority
                            </span>
                            <span className="type-badge">{goal.goalType.replace('_', ' ')}</span>
                        </div>
                    </div>
                </div>

                {goal.notes && (
                    <div className="goal-notes">
                        <p>{goal.notes}</p>
                    </div>
                )}

                <div className="goal-stats-grid">
                    <div className="stat-card">
                        <div className="stat-label">Current Amount</div>
                        <div className="stat-value">₹{(goal.currentAmount || 0).toLocaleString()}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Target Amount</div>
                        <div className="stat-value">₹{(goal.targetAmount || 0).toLocaleString()}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Remaining</div>
                        <div className="stat-value">
                            ₹{((goal.targetAmount || 0) - (goal.currentAmount || 0)).toLocaleString()}
                        </div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Target Date</div>
                        <div className="stat-value">{new Date(goal.targetDate).toLocaleDateString()}</div>
                    </div>
                </div>

                <div className="progress-section">
                    <div className="progress-header">
                        <span>Progress</span>
                        <span className="progress-percentage">{progressPercentage.toFixed(1)}%</span>
                    </div>
                    <div className="progress-bar">
                        <div className="progress-fill" style={{ width: `${progressPercentage}%` }} />
                    </div>
                </div>
            </div>

            <div className="details-content-grid">
                {/* Projection Section */}
                {projection && (
                    <div className="detail-card projection-card">
                        <div className="card-header">
                            <FaChartLine />
                            <h2>Goal Projection</h2>
                        </div>
                        <div className="projection-content">
                            {projection.onTrack ? (
                                <div className="projection-success">
                                    <FaCheckCircle />
                                    <h3>On Track! 🎉</h3>
                                    <p>You're projected to reach your goal on time!</p>
                                    <div className="projection-details">
                                        <div className="projection-item">
                                            <span className="label">Projected Amount:</span>
                                            <span className="value">₹{(projection.projectedAmount || 0).toLocaleString()}</span>
                                        </div>
                                        <div className="projection-item">
                                            <span className="label">Monthly Contribution:</span>
                                            <span className="value">₹{(goal.monthlyContribution || 0).toLocaleString()}</span>
                                        </div>
                                    </div>
                                </div>
                            ) : (
                                <div className="projection-warning">
                                    <h3>Needs Adjustment</h3>
                                    <p>Current trajectory will fall short of your target.</p>
                                    <div className="projection-details">
                                        <div className="projection-item alert">
                                            <span className="label">Projected Shortfall:</span>
                                            <span className="value">₹{(projection.shortfall || 0).toLocaleString()}</span>
                                        </div>
                                        <div className="projection-item success">
                                            <span className="label">Required Monthly:</span>
                                            <span className="value">₹{(projection.requiredMonthlyContribution || 0).toLocaleString()}</span>
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                )}

                {/* What-If Calculator */}
                <div className="detail-card whatif-card">
                    <div className="card-header">
                        <FaCalculator />
                        <h2>What-If Calculator</h2>
                    </div>
                    <div className="whatif-content">
                        <p>See how changing your monthly contribution affects your goal.</p>
                        <div className="whatif-input-group">
                            <input
                                type="number"
                                placeholder="Monthly contribution"
                                value={whatIfAmount}
                                onChange={(e) => setWhatIfAmount(e.target.value)}
                                min="0"
                                step="100"
                            />
                            <button className="btn-calculate" onClick={handleWhatIfCalculate}>
                                Calculate
                            </button>
                        </div>
                        {whatIfResult && (
                            <div className="whatif-result">
                                <div className="result-item">
                                    <span className="label">Projected Amount:</span>
                                    <span className="value">₹{(whatIfResult.projectedAmount || 0).toLocaleString()}</span>
                                </div>
                                <div className="result-item">
                                    <span className="label">Will Reach Target:</span>
                                    <span className={`value ${whatIfResult.onTrack ? 'success' : 'warning'}`}>
                                        {whatIfResult.onTrack ? 'Yes ✓' : 'No ✗'}
                                    </span>
                                </div>
                                {!whatIfResult.onTrack && (
                                    <div className="result-item">
                                        <span className="label">Shortfall:</span>
                                        <span className="value warning">₹{(whatIfResult.shortfall || 0).toLocaleString()}</span>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                </div>

                {/* Milestones */}
                <div className="detail-card milestones-card">
                    <div className="card-header">
                        <FaFlag />
                        <h2>Milestones</h2>
                        <button className="btn-add-small" onClick={() => setShowMilestoneModal(true)}>
                            <FaPlus /> Add
                        </button>
                    </div>
                    <div className="milestones-content">
                        {milestones.length === 0 ? (
                            <p className="empty-state">No milestones yet. Add one to track your progress!</p>
                        ) : (
                            <div className="milestones-timeline">
                                {milestones.map((milestone, index) => (
                                    <div key={index} className={`milestone-item ${milestone.completed ? 'completed' : ''}`}>
                                        <div className="milestone-marker">
                                            {milestone.completed ? <FaCheckCircle /> : <div className="marker-dot" />}
                                        </div>
                                        <div className="milestone-content">
                                            <h4>{milestone.title}</h4>
                                            <div className="milestone-meta">
                                                <span>₹{(milestone.targetAmount || 0).toLocaleString()}</span>
                                                <span>{new Date(milestone.targetDate).toLocaleDateString()}</span>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </div>

                {/* Contributions */}
                <div className="detail-card contributions-card">
                    <div className="card-header">
                        <FaMoneyBillWave />
                        <h2>Contribution History</h2>
                        <button className="btn-add-small" onClick={() => setShowContributionModal(true)}>
                            <FaPlus /> Add
                        </button>
                    </div>
                    <div className="contributions-content">
                        {contributions.length === 0 ? (
                            <p className="empty-state">No contributions recorded yet.</p>
                        ) : (
                            <div className="contributions-list">
                                {contributions.map((contribution, index) => (
                                    <div key={index} className="contribution-item">
                                        <div className="contribution-icon">💰</div>
                                        <div className="contribution-details">
                                            <div className="contribution-amount">
                                                ₹{(contribution.amount || 0).toLocaleString()}
                                            </div>
                                            <div className="contribution-meta">
                                                <span>{new Date(contribution.date).toLocaleDateString()}</span>
                                                {contribution.notes && <span className="notes">{contribution.notes}</span>}
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* Modals */}
            {showContributionModal && (
                <div className="modal-overlay" onClick={() => setShowContributionModal(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <h3>Record Contribution</h3>
                        <input
                            type="number"
                            placeholder="Amount"
                            value={contributionAmount}
                            onChange={(e) => setContributionAmount(e.target.value)}
                            min="0"
                            step="100"
                        />
                        <input
                            type="text"
                            placeholder="Notes (optional)"
                            value={contributionNotes}
                            onChange={(e) => setContributionNotes(e.target.value)}
                        />
                        <div className="modal-actions">
                            <button className="btn-primary" onClick={handleAddContribution}>
                                Record
                            </button>
                            <button className="btn-secondary" onClick={() => setShowContributionModal(false)}>
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {showMilestoneModal && (
                <div className="modal-overlay" onClick={() => setShowMilestoneModal(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <h3>Add Milestone</h3>
                        <input
                            type="text"
                            placeholder="Milestone title"
                            value={milestoneTitle}
                            onChange={(e) => setMilestoneTitle(e.target.value)}
                        />
                        <input
                            type="number"
                            placeholder="Target amount"
                            value={milestoneAmount}
                            onChange={(e) => setMilestoneAmount(e.target.value)}
                            min="0"
                            step="1000"
                        />
                        <input
                            type="date"
                            value={milestoneDate}
                            onChange={(e) => setMilestoneDate(e.target.value)}
                        />
                        <div className="modal-actions">
                            <button className="btn-primary" onClick={handleAddMilestone}>
                                Add Milestone
                            </button>
                            <button className="btn-secondary" onClick={() => setShowMilestoneModal(false)}>
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

const getGoalTypeEmoji = (type) => {
    const emojis = {
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
    return emojis[type] || '🎯';
};

export default GoalDetails;
