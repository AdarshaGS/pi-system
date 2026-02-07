/**
 * Portfolio Rebalancing Dashboard
 * 
 * Features:
 * - Current vs target allocation comparison
 * - Drift analysis with visual indicators
 * - Rebalancing suggestions (buy/sell)
 * - One-click rebalance execution
 * - Asset allocation breakdown
 * - Rebalancing history
 * 
 * Author: Pi-System
 * Date: February 6, 2026
 */

import React, { useState, useEffect } from 'react';
import {
    getRebalancingSuggestions,
    getCurrentAllocation,
    getTargetAllocation,
    executeRebalancing,
    getAllocationDrift,
    getRebalancingHistory
} from '../api/rebalancingApi';
import './PortfolioRebalancing.css';
import {
    FaBalanceScale,
    FaChartPie,
    FaExclamationTriangle,
    FaCheckCircle,
    FaArrowUp,
    FaArrowDown,
    FaHistory,
    FaRedo
} from 'react-icons/fa';

const PortfolioRebalancing = () => {
    const [loading, setLoading] = useState(true);
    const [currentAllocation, setCurrentAllocation] = useState([]);
    const [targetAllocation, setTargetAllocation] = useState([]);
    const [suggestions, setSuggestions] = useState(null);
    const [drift, setDrift] = useState(null);
    const [history, setHistory] = useState([]);
    const [threshold, setThreshold] = useState(5);
    const [showHistory, setShowHistory] = useState(false);

    const portfolioId = localStorage.getItem('portfolioId') || '1';

    useEffect(() => {
        fetchData();
    }, [threshold]);

    const fetchData = async () => {
        try {
            setLoading(true);
            const [current, target, sug, driftData, hist] = await Promise.all([
                getCurrentAllocation(portfolioId).catch(() => []),
                getTargetAllocation(portfolioId).catch(() => []),
                getRebalancingSuggestions(portfolioId, threshold).catch(() => null),
                getAllocationDrift(portfolioId).catch(() => null),
                getRebalancingHistory(portfolioId).catch(() => [])
            ]);

            setCurrentAllocation(current);
            setTargetAllocation(target);
            setSuggestions(sug);
            setDrift(driftData);
            setHistory(hist);
        } catch (error) {
            console.error('Error fetching rebalancing data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleRebalance = async () => {
        if (!suggestions || suggestions.actions?.length === 0) {
            alert('No rebalancing needed');
            return;
        }

        if (!window.confirm('Are you sure you want to execute this rebalancing?')) {
            return;
        }

        try {
            setLoading(true);
            await executeRebalancing(portfolioId, { actions: suggestions.actions });
            alert('Portfolio rebalanced successfully!');
            await fetchData();
        } catch (error) {
            alert('Error rebalancing portfolio: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

    const getAssetColor = (index) => {
        const colors = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#14b8a6'];
        return colors[index % colors.length];
    };

    const calculatePieChart = (allocation) => {
        let cumulativePercentage = 0;
        return allocation.map((item, index) => {
            const startAngle = (cumulativePercentage / 100) * 360;
            cumulativePercentage += item.percentage;
            const endAngle = (cumulativePercentage / 100) * 360;
            return {
                ...item,
                color: getAssetColor(index),
                startAngle,
                endAngle
            };
        });
    };

    const formatCurrency = (value) => {
        if (value >= 10000000) {
            return `₹${(value / 10000000).toFixed(2)} Cr`;
        } else if (value >= 100000) {
            return `₹${(value / 100000).toFixed(2)} L`;
        }
        return `₹${value.toLocaleString('en-IN')}`;
    };

    const needsRebalancing = drift && Math.abs(drift.maxDrift) > threshold;
    const currentPieData = calculatePieChart(currentAllocation);
    const targetPieData = calculatePieChart(targetAllocation);

    if (loading) {
        return <div className="loading-container">Loading rebalancing data...</div>;
    }

    return (
        <div className="rebalancing-page">
            {/* Header */}
            <div className="page-header">
                <div className="page-title">
                    <FaBalanceScale />
                    <h1>Portfolio Rebalancing</h1>
                </div>
                <div className="header-actions">
                    <button 
                        className="btn-history"
                        onClick={() => setShowHistory(!showHistory)}
                    >
                        <FaHistory /> History
                    </button>
                    {needsRebalancing && (
                        <button 
                            className="btn-rebalance"
                            onClick={handleRebalance}
                            disabled={loading}
                        >
                            <FaRedo /> Rebalance Now
                        </button>
                    )}
                </div>
            </div>

            {/* Status Banner */}
            <div className={`status-banner ${needsRebalancing ? 'warning' : 'success'}`}>
                {needsRebalancing ? <FaExclamationTriangle /> : <FaCheckCircle />}
                <span>
                    {needsRebalancing 
                        ? `Portfolio drift detected: ${Math.abs(drift.maxDrift).toFixed(2)}%`
                        : 'Portfolio is balanced'}
                </span>
                {needsRebalancing && (
                    <div className="threshold-control">
                        <label>Threshold:</label>
                        <input
                            type="number"
                            value={threshold}
                            onChange={(e) => setThreshold(Number(e.target.value))}
                            min="1"
                            max="20"
                            step="1"
                        />
                        <span>%</span>
                    </div>
                )}
            </div>

            <div className="content-grid">
                {/* Allocation Comparison */}
                <div className="allocation-card">
                    <h2>
                        <FaChartPie /> Asset Allocation
                    </h2>
                    
                    <div className="allocation-comparison">
                        {/* Current Allocation */}
                        <div className="allocation-section">
                            <h3>Current Allocation</h3>
                            <div className="pie-chart-container">
                                <svg viewBox="0 0 100 100" className="pie-chart">
                                    {currentPieData.map((item, index) => {
                                        const x1 = 50 + 40 * Math.cos((item.startAngle - 90) * Math.PI / 180);
                                        const y1 = 50 + 40 * Math.sin((item.startAngle - 90) * Math.PI / 180);
                                        const x2 = 50 + 40 * Math.cos((item.endAngle - 90) * Math.PI / 180);
                                        const y2 = 50 + 40 * Math.sin((item.endAngle - 90) * Math.PI / 180);
                                        const largeArc = item.percentage > 50 ? 1 : 0;
                                        
                                        return (
                                            <path
                                                key={index}
                                                d={`M 50 50 L ${x1} ${y1} A 40 40 0 ${largeArc} 1 ${x2} ${y2} Z`}
                                                fill={item.color}
                                                stroke="white"
                                                strokeWidth="0.5"
                                            />
                                        );
                                    })}
                                </svg>
                            </div>
                        </div>

                        {/* Target Allocation */}
                        <div className="allocation-section">
                            <h3>Target Allocation</h3>
                            <div className="pie-chart-container">
                                <svg viewBox="0 0 100 100" className="pie-chart">
                                    {targetPieData.map((item, index) => {
                                        const x1 = 50 + 40 * Math.cos((item.startAngle - 90) * Math.PI / 180);
                                        const y1 = 50 + 40 * Math.sin((item.startAngle - 90) * Math.PI / 180);
                                        const x2 = 50 + 40 * Math.cos((item.endAngle - 90) * Math.PI / 180);
                                        const y2 = 50 + 40 * Math.sin((item.endAngle - 90) * Math.PI / 180);
                                        const largeArc = item.percentage > 50 ? 1 : 0;
                                        
                                        return (
                                            <path
                                                key={index}
                                                d={`M 50 50 L ${x1} ${y1} A 40 40 0 ${largeArc} 1 ${x2} ${y2} Z`}
                                                fill={item.color}
                                                stroke="white"
                                                strokeWidth="0.5"
                                            />
                                        );
                                    })}
                                </svg>
                            </div>
                        </div>
                    </div>

                    {/* Legend */}
                    <div className="allocation-legend">
                        {currentPieData.map((item, index) => (
                            <div key={index} className="legend-item">
                                <div 
                                    className="legend-color"
                                    style={{ backgroundColor: item.color }}
                                />
                                <span className="legend-label">{item.assetClass}</span>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Drift Analysis */}
                <div className="drift-card">
                    <h2>Drift Analysis</h2>
                    <div className="drift-table">
                        {currentAllocation.map((current, index) => {
                            const target = targetAllocation.find(t => t.assetClass === current.assetClass);
                            const driftPercentage = current.percentage - (target?.percentage || 0);
                            const needsAdjustment = Math.abs(driftPercentage) > threshold;

                            return (
                                <div key={index} className={`drift-row ${needsAdjustment ? 'needs-adjustment' : ''}`}>
                                    <div className="asset-info">
                                        <div 
                                            className="asset-color"
                                            style={{ backgroundColor: getAssetColor(index) }}
                                        />
                                        <span className="asset-name">{current.assetClass}</span>
                                    </div>
                                    <div className="allocation-bars">
                                        <div className="bar-row">
                                            <span className="bar-label">Current:</span>
                                            <div className="bar-container">
                                                <div 
                                                    className="bar current"
                                                    style={{ 
                                                        width: `${current.percentage}%`,
                                                        backgroundColor: getAssetColor(index)
                                                    }}
                                                />
                                                <span className="bar-value">{current.percentage.toFixed(1)}%</span>
                                            </div>
                                        </div>
                                        <div className="bar-row">
                                            <span className="bar-label">Target:</span>
                                            <div className="bar-container">
                                                <div 
                                                    className="bar target"
                                                    style={{ 
                                                        width: `${target?.percentage || 0}%`,
                                                        backgroundColor: getAssetColor(index),
                                                        opacity: 0.5
                                                    }}
                                                />
                                                <span className="bar-value">{target?.percentage.toFixed(1) || 0}%</span>
                                            </div>
                                        </div>
                                    </div>
                                    <div className={`drift-value ${driftPercentage > 0 ? 'positive' : 'negative'}`}>
                                        {driftPercentage > 0 ? <FaArrowUp /> : <FaArrowDown />}
                                        {Math.abs(driftPercentage).toFixed(2)}%
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                </div>
            </div>

            {/* Rebalancing Suggestions */}
            {suggestions && suggestions.actions?.length > 0 && (
                <div className="suggestions-card">
                    <h2>Rebalancing Suggestions</h2>
                    <div className="suggestions-list">
                        {suggestions.actions.map((action, index) => (
                            <div key={index} className={`suggestion-item ${action.action.toLowerCase()}`}>
                                <div className="action-icon">
                                    {action.action === 'BUY' ? <FaArrowUp /> : <FaArrowDown />}
                                </div>
                                <div className="action-details">
                                    <div className="action-type">{action.action}</div>
                                    <div className="action-asset">{action.assetClass}</div>
                                </div>
                                <div className="action-amount">
                                    {formatCurrency(action.amount)}
                                </div>
                                <div className="action-units">
                                    {action.units?.toFixed(2)} units
                                </div>
                            </div>
                        ))}
                    </div>
                    <div className="suggestions-footer">
                        <p>Estimated transaction cost: {formatCurrency(suggestions.estimatedCost || 0)}</p>
                    </div>
                </div>
            )}

            {/* Rebalancing History */}
            {showHistory && history.length > 0 && (
                <div className="history-card">
                    <h2>
                        <FaHistory /> Rebalancing History
                    </h2>
                    <div className="history-list">
                        {history.map((record) => (
                            <div key={record.id} className="history-item">
                                <div className="history-date">
                                    {new Date(record.rebalancedAt).toLocaleDateString('en-IN', {
                                        year: 'numeric',
                                        month: 'long',
                                        day: 'numeric'
                                    })}
                                </div>
                                <div className="history-details">
                                    <span>Actions: {record.actionsCount || 0}</span>
                                    <span>Cost: {formatCurrency(record.totalCost || 0)}</span>
                                </div>
                                <div className="history-status success">Completed</div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* Empty State */}
            {currentAllocation.length === 0 && (
                <div className="empty-state">
                    <FaChartPie size={60} color="#cbd5e1" />
                    <h3>No Portfolio Data</h3>
                    <p>Add assets to your portfolio to see rebalancing suggestions</p>
                </div>
            )}
        </div>
    );
};

export default PortfolioRebalancing;
