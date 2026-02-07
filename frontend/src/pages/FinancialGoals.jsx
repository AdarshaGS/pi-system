/**
 * Financial Goals Dashboard - Main Page
 * 
 * Features:
 * - List all financial goals with progress tracking
 * - Summary cards showing total targets, current amounts, on-track goals
 * - Filter by goal type and status
 * - Create new goals
 * - View goal details with projections
 * 
 * Author: Pi-System
 * Date: February 5, 2026
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
    getUserGoals, 
    getGoalsByStatus,
    deleteGoal 
} from '../api/goalsApi';
import GoalCard from '../components/GoalCard';
import CreateGoalModal from '../components/CreateGoalModal';
import './FinancialGoals.css';
import { 
    FaBullseye, 
    FaPlus, 
    FaMoneyBillWave, 
    FaChartLine,
    FaCheckCircle,
    FaFilter
} from 'react-icons/fa';

const FinancialGoals = () => {
    // State management
    const [goals, setGoals] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filterType, setFilterType] = useState('ALL');
    const [filterStatus, setFilterStatus] = useState('ACTIVE');
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [summary, setSummary] = useState({
        totalGoals: 0,
        activeGoals: 0,
        totalTarget: 0,
        totalCurrent: 0,
        onTrackCount: 0,
        completedCount: 0
    });

    const navigate = useNavigate();
    const userId = localStorage.getItem('userId') || 1;

    // Fetch data on mount
    useEffect(() => {
        fetchGoals();
    }, [filterStatus]);

    const fetchGoals = async () => {
        setLoading(true);
        try {
            let goalsData;
            if (filterStatus === 'ALL') {
                goalsData = await getUserGoals(userId);
            } else {
                goalsData = await getGoalsByStatus(userId, filterStatus);
            }
            
            setGoals(goalsData || []);
            calculateSummary(goalsData || []);
        } catch (error) {
            console.error('Error fetching goals:', error);
            setGoals([]);
        } finally {
            setLoading(false);
        }
    };

    const calculateSummary = (goalsData) => {
        const summary = {
            totalGoals: goalsData.length,
            activeGoals: goalsData.filter(g => g.status === 'ACTIVE').length,
            totalTarget: goalsData.reduce((sum, g) => sum + (g.targetAmount || 0), 0),
            totalCurrent: goalsData.reduce((sum, g) => sum + (g.currentAmount || 0), 0),
            onTrackCount: goalsData.filter(g => g.isOnTrack).length,
            completedCount: goalsData.filter(g => g.status === 'COMPLETED').length
        };
        setSummary(summary);
    };

    // Filter goals by type
    const filteredGoals = filterType === 'ALL' 
        ? goals 
        : goals.filter(g => g.goalType === filterType);

    const handleCreateGoal = () => {
        setShowCreateModal(true);
    };

    const handleViewDetails = (goalId) => {
        navigate(`/goals/${goalId}`);
    };

    const handleDeleteGoal = async (goalId) => {
        if (window.confirm('Are you sure you want to delete this goal?')) {
            try {
                await deleteGoal(goalId);
                fetchGoals();
            } catch (error) {
                console.error('Error deleting goal:', error);
                alert('Failed to delete goal');
            }
        }
    };

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

    if (loading) {
        return <div className="loading-spinner">Loading financial goals...</div>;
    }

    return (
        <div className="financial-goals-page">
            {/* Header */}
            <div className="goals-header">
                <div className="header-left">
                    <h1><FaBullseye /> Financial Goals</h1>
                    <p>Track your financial targets and achieve your dreams</p>
                </div>
                <button className="btn-create-goal" onClick={handleCreateGoal}>
                    <FaPlus /> Create Goal
                </button>
            </div>

            {/* Summary Cards */}
            <div className="goals-summary-cards">
                <div className="summary-card">
                    <div className="card-icon">
                        <FaBullseye />
                    </div>
                    <div className="card-content">
                        <h3>Total Goals</h3>
                        <p className="card-value">{summary.totalGoals}</p>
                        <span className="card-subtitle">{summary.activeGoals} active</span>
                    </div>
                </div>

                <div className="summary-card">
                    <div className="card-icon">
                        <FaMoneyBillWave />
                    </div>
                    <div className="card-content">
                        <h3>Total Target</h3>
                        <p className="card-value">₹{summary.totalTarget.toLocaleString()}</p>
                        <span className="card-subtitle">Across all goals</span>
                    </div>
                </div>

                <div className="summary-card">
                    <div className="card-icon">
                        <FaChartLine />
                    </div>
                    <div className="card-content">
                        <h3>Current Progress</h3>
                        <p className="card-value">₹{summary.totalCurrent.toLocaleString()}</p>
                        <span className="card-subtitle">
                            {((summary.totalCurrent / summary.totalTarget) * 100 || 0).toFixed(1)}% achieved
                        </span>
                    </div>
                </div>

                <div className="summary-card success">
                    <div className="card-icon">
                        <FaCheckCircle />
                    </div>
                    <div className="card-content">
                        <h3>On Track</h3>
                        <p className="card-value">{summary.onTrackCount}</p>
                        <span className="card-subtitle">{summary.completedCount} completed</span>
                    </div>
                </div>
            </div>

            {/* Filters */}
            <div className="goals-filters">
                <div className="filter-group">
                    <FaFilter />
                    <select 
                        value={filterType} 
                        onChange={(e) => setFilterType(e.target.value)}
                        className="filter-select"
                    >
                        <option value="ALL">All Types</option>
                        <option value="RETIREMENT">🏖️ Retirement</option>
                        <option value="PROPERTY">🏠 Property</option>
                        <option value="EDUCATION">🎓 Education</option>
                        <option value="EMERGENCY_FUND">🚨 Emergency Fund</option>
                        <option value="TRAVEL">✈️ Travel</option>
                        <option value="VEHICLE">🚗 Vehicle</option>
                        <option value="BUSINESS">💼 Business</option>
                        <option value="WEDDING">💍 Wedding</option>
                        <option value="CUSTOM">🎯 Custom</option>
                    </select>
                </div>

                <div className="filter-group">
                    <select 
                        value={filterStatus} 
                        onChange={(e) => setFilterStatus(e.target.value)}
                        className="filter-select"
                    >
                        <option value="ALL">All Status</option>
                        <option value="ACTIVE">Active</option>
                        <option value="COMPLETED">Completed</option>
                        <option value="PAUSED">Paused</option>
                        <option value="ABANDONED">Abandoned</option>
                    </select>
                </div>
            </div>

            {/* Goals Grid */}
            <div className="goals-section">
                <h2>
                    Your Goals ({filteredGoals.length})
                </h2>
                
                {filteredGoals.length === 0 ? (
                    <div className="no-data">
                        <FaBullseye size={64} color="#ccc" />
                        <p>No financial goals found.</p>
                        <button onClick={handleCreateGoal}>Create Your First Goal</button>
                    </div>
                ) : (
                    <div className="goals-grid">
                        {filteredGoals.map(goal => (
                            <GoalCard
                                key={goal.id}
                                goal={goal}
                                onViewDetails={() => handleViewDetails(goal.id)}
                                onDelete={() => handleDeleteGoal(goal.id)}
                                onRefresh={fetchGoals}
                            />
                        ))}
                    </div>
                )}
            </div>

            {/* Create Goal Modal */}
            {showCreateModal && (
                <CreateGoalModal
                    onClose={() => setShowCreateModal(false)}
                    onSave={() => {
                        setShowCreateModal(false);
                        fetchGoals();
                    }}
                />
            )}
        </div>
    );
};

export default FinancialGoals;
