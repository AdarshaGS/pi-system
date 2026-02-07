/**
 * Credit Score Tracking Dashboard
 * 
 * Features:
 * - Current credit score display
 * - Score history chart
 * - Trend analysis
 * - Score range indicator
 * - Improvement tips
 * - Record new scores
 * 
 * Author: Pi-System
 * Date: February 6, 2026
 */

import React, { useState, useEffect } from 'react';
import {
    getCreditScoreHistory,
    getLatestCreditScore,
    recordCreditScore,
    deleteCreditScore,
    getCreditScoreTrend,
    getCreditScoreStats
} from '../api/creditScoreApi';
import './CreditScore.css';
import {
    FaChartLine,
    FaArrowUp,
    FaArrowDown,
    FaPlus,
    FaTrophy,
    FaExclamationTriangle,
    FaCheckCircle
} from 'react-icons/fa';

const CreditScore = () => {
    const [loading, setLoading] = useState(true);
    const [latestScore, setLatestScore] = useState(null);
    const [scoreHistory, setScoreHistory] = useState([]);
    const [trend, setTrend] = useState(null);
    const [stats, setStats] = useState(null);
    const [showRecordModal, setShowRecordModal] = useState(false);
    const [newScore, setNewScore] = useState('');
    const [source, setSource] = useState('');
    const [notes, setNotes] = useState('');

    const userId = localStorage.getItem('userId') || '1';

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            setLoading(true);
            const [latest, history, trendData, statsData] = await Promise.all([
                getLatestCreditScore(userId).catch(() => null),
                getCreditScoreHistory(userId).catch(() => []),
                getCreditScoreTrend(userId, 12).catch(() => null),
                getCreditScoreStats(userId).catch(() => null)
            ]);

            setLatestScore(latest);
            setScoreHistory(history);
            setTrend(trendData);
            setStats(statsData);
        } catch (error) {
            console.error('Error fetching credit score data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleRecordScore = async () => {
        if (!newScore || parseInt(newScore) < 300 || parseInt(newScore) > 900) {
            alert('Please enter a valid credit score (300-900)');
            return;
        }

        try {
            await recordCreditScore({
                userId: parseInt(userId),
                score: parseInt(newScore),
                source: source || 'Manual Entry',
                notes: notes
            });
            setShowRecordModal(false);
            setNewScore('');
            setSource('');
            setNotes('');
            fetchData();
        } catch (error) {
            alert('Error recording score: ' + error.message);
        }
    };

    const handleDeleteScore = async (scoreId) => {
        if (window.confirm('Are you sure you want to delete this score record?')) {
            try {
                await deleteCreditScore(scoreId);
                fetchData();
            } catch (error) {
                alert('Error deleting score: ' + error.message);
            }
        }
    };

    const getScoreCategory = (score) => {
        if (score >= 750) return { label: 'Excellent', color: '#10b981', icon: <FaTrophy /> };
        if (score >= 700) return { label: 'Good', color: '#3b82f6', icon: <FaCheckCircle /> };
        if (score >= 650) return { label: 'Fair', color: '#f59e0b', icon: <FaExclamationTriangle /> };
        if (score >= 600) return { label: 'Poor', color: '#ef4444', icon: <FaExclamationTriangle /> };
        return { label: 'Very Poor', color: '#991b1b', icon: <FaExclamationTriangle /> };
    };

    const getScoreTips = (score) => {
        if (score >= 750) {
            return [
                '✅ Maintain your excellent payment history',
                '✅ Keep credit utilization below 30%',
                '✅ Continue diverse credit mix'
            ];
        }
        if (score >= 700) {
            return [
                '💡 Pay all bills on time',
                '💡 Reduce credit utilization to below 30%',
                '💡 Avoid opening too many new accounts'
            ];
        }
        return [
            '⚠️ Make all payments on time',
            '⚠️ Pay down existing debts',
            '⚠️ Check credit report for errors',
            '⚠️ Avoid maxing out credit cards'
        ];
    };

    if (loading) {
        return <div className="loading-container">Loading credit score data...</div>;
    }

    const currentScore = latestScore?.score || 0;
    const scoreCategory = getScoreCategory(currentScore);
    const scoreTips = getScoreTips(currentScore);
    const scoreChange = trend?.changeFromPrevious || 0;

    return (
        <div className="credit-score-page">
            {/* Header */}
            <div className="page-header">
                <div className="page-title">
                    <FaChartLine />
                    <h1>Credit Score Tracking</h1>
                </div>
                <button className="btn-record" onClick={() => setShowRecordModal(true)}>
                    <FaPlus /> Record Score
                </button>
            </div>

            {/* Current Score Display */}
            <div className="score-display-card">
                <div className="score-main">
                    <div className="score-circle" style={{ borderColor: scoreCategory.color }}>
                        <div className="score-value" style={{ color: scoreCategory.color }}>
                            {currentScore}
                        </div>
                        <div className="score-max">/ 900</div>
                    </div>
                    <div className="score-info">
                        <div className="score-category" style={{ color: scoreCategory.color }}>
                            {scoreCategory.icon}
                            <span>{scoreCategory.label}</span>
                        </div>
                        {latestScore && (
                            <div className="score-date">
                                Last updated: {new Date(latestScore.recordedAt).toLocaleDateString()}
                            </div>
                        )}
                        {scoreChange !== 0 && (
                            <div className={`score-change ${scoreChange > 0 ? 'positive' : 'negative'}`}>
                                {scoreChange > 0 ? <FaArrowUp /> : <FaArrowDown />}
                                {Math.abs(scoreChange)} points {scoreChange > 0 ? 'increase' : 'decrease'}
                            </div>
                        )}
                    </div>
                </div>

                {/* Score Range Indicator */}
                <div className="score-range">
                    <div className="range-bar">
                        <div className="range-segment poor"></div>
                        <div className="range-segment fair"></div>
                        <div className="range-segment good"></div>
                        <div className="range-segment excellent"></div>
                        <div 
                            className="score-marker"
                            style={{ left: `${((currentScore - 300) / 600) * 100}%` }}
                        />
                    </div>
                    <div className="range-labels">
                        <span>300</span>
                        <span>600</span>
                        <span>650</span>
                        <span>700</span>
                        <span>750</span>
                        <span>900</span>
                    </div>
                </div>
            </div>

            <div className="content-grid">
                {/* Statistics */}
                <div className="content-card stats-card">
                    <h2>Statistics</h2>
                    {stats ? (
                        <div className="stats-list">
                            <div className="stat-item">
                                <span className="stat-label">Average Score</span>
                                <span className="stat-value">{stats.averageScore || 0}</span>
                            </div>
                            <div className="stat-item">
                                <span className="stat-label">Highest Score</span>
                                <span className="stat-value">{stats.highestScore || 0}</span>
                            </div>
                            <div className="stat-item">
                                <span className="stat-label">Lowest Score</span>
                                <span className="stat-value">{stats.lowestScore || 0}</span>
                            </div>
                            <div className="stat-item">
                                <span className="stat-label">Total Records</span>
                                <span className="stat-value">{stats.totalRecords || 0}</span>
                            </div>
                        </div>
                    ) : (
                        <div className="empty-state">No statistics available</div>
                    )}
                </div>

                {/* Tips */}
                <div className="content-card tips-card">
                    <h2>Improvement Tips</h2>
                    <div className="tips-list">
                        {scoreTips.map((tip, index) => (
                            <div key={index} className="tip-item">
                                {tip}
                            </div>
                        ))}
                    </div>
                </div>
            </div>

            {/* Score History */}
            <div className="history-card">
                <h2>Score History</h2>
                {scoreHistory.length > 0 ? (
                    <>
                        <div className="history-chart">
                            <div className="chart-area">
                                {scoreHistory.map((record, index) => {
                                    const maxScore = Math.max(...scoreHistory.map(r => r.score));
                                    const minScore = Math.min(...scoreHistory.map(r => r.score));
                                    const range = maxScore - minScore || 100;
                                    const height = ((record.score - minScore) / range) * 100;
                                    
                                    return (
                                        <div key={record.id} className="chart-point">
                                            <div 
                                                className="point"
                                                style={{ 
                                                    bottom: `${height}%`,
                                                    backgroundColor: getScoreCategory(record.score).color
                                                }}
                                                title={`${record.score} - ${new Date(record.recordedAt).toLocaleDateString()}`}
                                            />
                                            <div className="point-label">
                                                {new Date(record.recordedAt).toLocaleDateString('en-US', { month: 'short' })}
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        </div>
                        <div className="history-list">
                            {scoreHistory.map((record) => (
                                <div key={record.id} className="history-item">
                                    <div className="history-score" style={{ color: getScoreCategory(record.score).color }}>
                                        {record.score}
                                    </div>
                                    <div className="history-details">
                                        <div className="history-date">
                                            {new Date(record.recordedAt).toLocaleDateString('en-IN', {
                                                year: 'numeric',
                                                month: 'long',
                                                day: 'numeric'
                                            })}
                                        </div>
                                        {record.source && (
                                            <div className="history-source">Source: {record.source}</div>
                                        )}
                                        {record.notes && (
                                            <div className="history-notes">{record.notes}</div>
                                        )}
                                    </div>
                                    <button 
                                        className="btn-delete-small"
                                        onClick={() => handleDeleteScore(record.id)}
                                    >
                                        Delete
                                    </button>
                                </div>
                            ))}
                        </div>
                    </>
                ) : (
                    <div className="empty-state">
                        <p>No credit score history available</p>
                        <button className="btn-record" onClick={() => setShowRecordModal(true)}>
                            <FaPlus /> Record Your First Score
                        </button>
                    </div>
                )}
            </div>

            {/* Record Score Modal */}
            {showRecordModal && (
                <div className="modal-overlay" onClick={() => setShowRecordModal(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <h3>Record Credit Score</h3>
                        <div className="form-group">
                            <label>Credit Score (300-900) *</label>
                            <input
                                type="number"
                                value={newScore}
                                onChange={(e) => setNewScore(e.target.value)}
                                placeholder="750"
                                min="300"
                                max="900"
                            />
                        </div>
                        <div className="form-group">
                            <label>Source</label>
                            <input
                                type="text"
                                value={source}
                                onChange={(e) => setSource(e.target.value)}
                                placeholder="e.g., CIBIL, Experian, Equifax"
                            />
                        </div>
                        <div className="form-group">
                            <label>Notes</label>
                            <textarea
                                value={notes}
                                onChange={(e) => setNotes(e.target.value)}
                                placeholder="Optional notes..."
                                rows="3"
                            />
                        </div>
                        <div className="modal-actions">
                            <button className="btn-primary" onClick={handleRecordScore}>
                                Record Score
                            </button>
                            <button className="btn-secondary" onClick={() => setShowRecordModal(false)}>
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CreditScore;
