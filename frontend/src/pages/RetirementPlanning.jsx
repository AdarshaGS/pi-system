/**
 * Retirement Planning Calculator
 * 
 * Features:
 * - Retirement corpus calculation
 * - Future value projection
 * - Monthly income estimation
 * - Inflation adjustment
 * - Retirement readiness indicator
 * - Visual projections
 * 
 * Author: Pi-System
 * Date: February 6, 2026
 */

import React, { useState, useEffect } from 'react';
import {
    createRetirementPlan,
    getRetirementPlanByUser,
    updateRetirementPlan,
    calculateRetirementCorpus,
    getRetirementProjection,
    calculateRetirementIncome,
    getRetirementReadiness
} from '../api/retirementPlanningApi';
import './RetirementPlanning.css';
import {
    FaPiggyBank,
    FaCalculator,
    FaChartLine,
    FaCheckCircle,
    FaExclamationTriangle,
    FaSave
} from 'react-icons/fa';

const RetirementPlanning = () => {
    const [loading, setLoading] = useState(false);
    const [existingPlan, setExistingPlan] = useState(null);
    const [calculation, setCalculation] = useState(null);
    const [projection, setProjection] = useState(null);
    const [readiness, setReadiness] = useState(null);
    
    // Form inputs
    const [currentAge, setCurrentAge] = useState(30);
    const [retirementAge, setRetirementAge] = useState(60);
    const [lifeExpectancy, setLifeExpectancy] = useState(85);
    const [currentSavings, setCurrentSavings] = useState(500000);
    const [monthlyContribution, setMonthlyContribution] = useState(10000);
    const [expectedReturn, setExpectedReturn] = useState(10);
    const [inflationRate, setInflationRate] = useState(6);
    const [monthlyExpenseAfterRetirement, setMonthlyExpenseAfterRetirement] = useState(30000);

    const userId = localStorage.getItem('userId') || '1';

    useEffect(() => {
        fetchExistingPlan();
    }, []);

    const fetchExistingPlan = async () => {
        try {
            const plan = await getRetirementPlanByUser(userId);
            if (plan) {
                setExistingPlan(plan);
                // Populate form with existing data
                setCurrentAge(plan.currentAge || 30);
                setRetirementAge(plan.retirementAge || 60);
                setLifeExpectancy(plan.lifeExpectancy || 85);
                setCurrentSavings(plan.currentSavings || 0);
                setMonthlyContribution(plan.monthlyContribution || 0);
                setExpectedReturn(plan.expectedAnnualReturn || 10);
                setInflationRate(plan.inflationRate || 6);
                setMonthlyExpenseAfterRetirement(plan.monthlyExpenseAfterRetirement || 30000);
                
                // Fetch projection and readiness
                const [proj, ready] = await Promise.all([
                    getRetirementProjection(plan.id).catch(() => null),
                    getRetirementReadiness(plan.id).catch(() => null)
                ]);
                setProjection(proj);
                setReadiness(ready);
            }
        } catch (error) {
            console.log('No existing plan found');
        }
    };

    const handleCalculate = async () => {
        try {
            setLoading(true);
            
            const calculationData = {
                currentAge: parseInt(currentAge),
                retirementAge: parseInt(retirementAge),
                lifeExpectancy: parseInt(lifeExpectancy),
                currentSavings: parseFloat(currentSavings),
                monthlyContribution: parseFloat(monthlyContribution),
                expectedAnnualReturn: parseFloat(expectedReturn),
                inflationRate: parseFloat(inflationRate),
                monthlyExpenseAfterRetirement: parseFloat(monthlyExpenseAfterRetirement)
            };

            const result = await calculateRetirementCorpus(calculationData);
            setCalculation(result);
            
            // Calculate retirement income
            const incomeResult = await calculateRetirementIncome({
                retirementCorpus: result.requiredCorpus,
                yearsInRetirement: parseInt(lifeExpectancy) - parseInt(retirementAge),
                expectedReturn: parseFloat(expectedReturn),
                inflationRate: parseFloat(inflationRate)
            });
            
            setCalculation(prev => ({ ...prev, monthlyIncome: incomeResult.monthlyIncome }));
        } catch (error) {
            alert('Error calculating retirement plan: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

    const handleSavePlan = async () => {
        try {
            setLoading(true);
            
            const planData = {
                userId: parseInt(userId),
                currentAge: parseInt(currentAge),
                retirementAge: parseInt(retirementAge),
                lifeExpectancy: parseInt(lifeExpectancy),
                currentSavings: parseFloat(currentSavings),
                monthlyContribution: parseFloat(monthlyContribution),
                expectedAnnualReturn: parseFloat(expectedReturn),
                inflationRate: parseFloat(inflationRate),
                monthlyExpenseAfterRetirement: parseFloat(monthlyExpenseAfterRetirement),
                targetCorpus: calculation?.requiredCorpus || 0
            };

            if (existingPlan) {
                await updateRetirementPlan(existingPlan.id, planData);
            } else {
                await createRetirementPlan(planData);
            }
            
            alert('Retirement plan saved successfully!');
            await fetchExistingPlan();
        } catch (error) {
            alert('Error saving plan: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

    const formatCurrency = (value) => {
        if (value >= 10000000) {
            return `₹${(value / 10000000).toFixed(2)} Cr`;
        } else if (value >= 100000) {
            return `₹${(value / 100000).toFixed(2)} L`;
        }
        return `₹${value.toLocaleString('en-IN')}`;
    };

    const yearsToRetirement = parseInt(retirementAge) - parseInt(currentAge);
    const yearsInRetirement = parseInt(lifeExpectancy) - parseInt(retirementAge);
    const isReady = calculation && calculation.projectedCorpus >= calculation.requiredCorpus;

    return (
        <div className="retirement-planning-page">
            {/* Header */}
            <div className="page-header">
                <div className="page-title">
                    <FaPiggyBank />
                    <h1>Retirement Planning</h1>
                </div>
            </div>

            <div className="planning-grid">
                {/* Input Form */}
                <div className="input-card">
                    <h2>
                        <FaCalculator /> Plan Your Retirement
                    </h2>
                    
                    <div className="form-section">
                        <h3>Personal Details</h3>
                        <div className="form-row">
                            <div className="form-group">
                                <label>Current Age</label>
                                <input
                                    type="number"
                                    value={currentAge}
                                    onChange={(e) => setCurrentAge(e.target.value)}
                                    min="18"
                                    max="100"
                                />
                            </div>
                            <div className="form-group">
                                <label>Retirement Age</label>
                                <input
                                    type="number"
                                    value={retirementAge}
                                    onChange={(e) => setRetirementAge(e.target.value)}
                                    min="40"
                                    max="80"
                                />
                            </div>
                        </div>
                        <div className="form-group">
                            <label>Life Expectancy</label>
                            <input
                                type="number"
                                value={lifeExpectancy}
                                onChange={(e) => setLifeExpectancy(e.target.value)}
                                min="60"
                                max="120"
                            />
                        </div>
                        <div className="info-badge">
                            <span>Years to Retirement: <strong>{yearsToRetirement}</strong></span>
                            <span>Years in Retirement: <strong>{yearsInRetirement}</strong></span>
                        </div>
                    </div>

                    <div className="form-section">
                        <h3>Financial Details</h3>
                        <div className="form-group">
                            <label>Current Savings (₹)</label>
                            <input
                                type="number"
                                value={currentSavings}
                                onChange={(e) => setCurrentSavings(e.target.value)}
                                step="10000"
                            />
                        </div>
                        <div className="form-group">
                            <label>Monthly Contribution (₹)</label>
                            <input
                                type="number"
                                value={monthlyContribution}
                                onChange={(e) => setMonthlyContribution(e.target.value)}
                                step="1000"
                            />
                        </div>
                        <div className="form-row">
                            <div className="form-group">
                                <label>Expected Return (%)</label>
                                <input
                                    type="number"
                                    value={expectedReturn}
                                    onChange={(e) => setExpectedReturn(e.target.value)}
                                    step="0.5"
                                    min="0"
                                    max="30"
                                />
                            </div>
                            <div className="form-group">
                                <label>Inflation Rate (%)</label>
                                <input
                                    type="number"
                                    value={inflationRate}
                                    onChange={(e) => setInflationRate(e.target.value)}
                                    step="0.5"
                                    min="0"
                                    max="20"
                                />
                            </div>
                        </div>
                        <div className="form-group">
                            <label>Monthly Expense After Retirement (₹)</label>
                            <input
                                type="number"
                                value={monthlyExpenseAfterRetirement}
                                onChange={(e) => setMonthlyExpenseAfterRetirement(e.target.value)}
                                step="5000"
                            />
                        </div>
                    </div>

                    <div className="form-actions">
                        <button 
                            className="btn-calculate"
                            onClick={handleCalculate}
                            disabled={loading}
                        >
                            <FaCalculator /> Calculate
                        </button>
                        {calculation && (
                            <button 
                                className="btn-save"
                                onClick={handleSavePlan}
                                disabled={loading}
                            >
                                <FaSave /> Save Plan
                            </button>
                        )}
                    </div>
                </div>

                {/* Results */}
                {calculation && (
                    <div className="results-card">
                        <h2>
                            <FaChartLine /> Retirement Analysis
                        </h2>

                        {/* Readiness Indicator */}
                        <div className={`readiness-badge ${isReady ? 'ready' : 'not-ready'}`}>
                            {isReady ? <FaCheckCircle /> : <FaExclamationTriangle />}
                            <span>
                                {isReady ? 'On Track for Retirement!' : 'Need More Savings'}
                            </span>
                        </div>

                        {/* Key Metrics */}
                        <div className="metrics-grid">
                            <div className="metric-card primary">
                                <div className="metric-label">Required Corpus</div>
                                <div className="metric-value">
                                    {formatCurrency(calculation.requiredCorpus)}
                                </div>
                                <div className="metric-sublabel">Needed at retirement</div>
                            </div>
                            <div className="metric-card success">
                                <div className="metric-label">Projected Corpus</div>
                                <div className="metric-value">
                                    {formatCurrency(calculation.projectedCorpus)}
                                </div>
                                <div className="metric-sublabel">Based on current plan</div>
                            </div>
                        </div>

                        {/* Gap Analysis */}
                        {!isReady && (
                            <div className="gap-card">
                                <h3>Savings Gap</h3>
                                <div className="gap-amount">
                                    {formatCurrency(calculation.requiredCorpus - calculation.projectedCorpus)}
                                </div>
                                <div className="gap-suggestion">
                                    Increase monthly contribution or extend working years
                                </div>
                            </div>
                        )}

                        {/* Additional Metrics */}
                        <div className="additional-metrics">
                            <div className="metric-row">
                                <span className="metric-label">Monthly Income in Retirement</span>
                                <span className="metric-value">
                                    {calculation.monthlyIncome 
                                        ? formatCurrency(calculation.monthlyIncome)
                                        : formatCurrency(monthlyExpenseAfterRetirement)}
                                </span>
                            </div>
                            <div className="metric-row">
                                <span className="metric-label">Total Contributions</span>
                                <span className="metric-value">
                                    {formatCurrency(
                                        currentSavings + (monthlyContribution * 12 * yearsToRetirement)
                                    )}
                                </span>
                            </div>
                            <div className="metric-row">
                                <span className="metric-label">Investment Growth</span>
                                <span className="metric-value success-text">
                                    {formatCurrency(
                                        calculation.projectedCorpus - 
                                        (currentSavings + (monthlyContribution * 12 * yearsToRetirement))
                                    )}
                                </span>
                            </div>
                        </div>

                        {/* Projection Chart */}
                        <div className="projection-section">
                            <h3>Retirement Fund Projection</h3>
                            <div className="projection-chart">
                                <div className="chart-bars">
                                    <div className="bar-group">
                                        <div 
                                            className="bar current"
                                            style={{ 
                                                height: `${(currentSavings / calculation.projectedCorpus) * 100}%` 
                                            }}
                                        />
                                        <div className="bar-label">Current</div>
                                    </div>
                                    <div className="bar-group">
                                        <div 
                                            className="bar projected"
                                            style={{ height: '100%' }}
                                        />
                                        <div className="bar-label">Projected</div>
                                    </div>
                                    <div className="bar-group">
                                        <div 
                                            className="bar required"
                                            style={{ 
                                                height: `${(calculation.requiredCorpus / calculation.projectedCorpus) * 100}%` 
                                            }}
                                        />
                                        <div className="bar-label">Required</div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Tips */}
                        <div className="tips-section">
                            <h3>Retirement Tips</h3>
                            <div className="tips-list">
                                <div className="tip-item">
                                    💡 Start early to benefit from compound interest
                                </div>
                                <div className="tip-item">
                                    💡 Diversify investments to manage risk
                                </div>
                                <div className="tip-item">
                                    💡 Review and adjust plan annually
                                </div>
                                <div className="tip-item">
                                    💡 Consider inflation impact on expenses
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                {/* Empty State */}
                {!calculation && (
                    <div className="empty-state-card">
                        <FaCalculator size={60} color="#cbd5e1" />
                        <h3>Calculate Your Retirement Plan</h3>
                        <p>Enter your details and click Calculate to see projections</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default RetirementPlanning;
