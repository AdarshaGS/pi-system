import { useState, useEffect, useMemo } from 'react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';
import PolicyList from '../components/insurance/PolicyList';
import PolicyForm from '../components/insurance/PolicyForm';
import PremiumPayment from '../components/insurance/PremiumPayment';
import ClaimsManagement from '../components/insurance/ClaimsManagement';
import * as insuranceApi from '../api/insuranceApi';

const Insurance = () => {
    const [activeTab, setActiveTab] = useState('overview');
    const [policies, setPolicies] = useState([]);
    const [summary, setSummary] = useState(null);
    const [analytics, setAnalytics] = useState(null);
    const [loading, setLoading] = useState(true);
    const [showPolicyForm, setShowPolicyForm] = useState(false);
    const [showPremiumForm, setShowPremiumForm] = useState(false);
    const [showClaimsForm, setShowClaimsForm] = useState(false);
    const [selectedPolicy, setSelectedPolicy] = useState(null);

    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const userId = user?.id || localStorage.getItem('userId') || 1;
    const token = useMemo(() => user?.token, []);

    useEffect(() => {
        if (activeTab === 'overview') {
            fetchSummaryData();
        } else if (activeTab === 'policies') {
            fetchPolicies();
        }
    }, [activeTab]);

    const fetchSummaryData = async () => {
        try {
            setLoading(true);
            const [summaryRes, policiesRes] = await Promise.all([
                insuranceApi.getInsuranceSummary(userId),
                insuranceApi.getInsurancePolicies(userId)
            ]);
            setSummary(summaryRes || {});
            setPolicies(Array.isArray(policiesRes) ? policiesRes : []);
            
            // Calculate analytics from policies
            const analyticsData = calculateAnalytics(policiesRes || []);
            setAnalytics(analyticsData);
        } catch (error) {
            console.error('Error fetching summary:', error);
            // Set empty data on error
            setSummary({});
            setAnalytics({});
            setPolicies([]);
        } finally {
            setLoading(false);
        }
    };

    const calculateAnalytics = (policies) => {
        const coverageByType = {};
        const policiesByType = {};
        
        policies.forEach(policy => {
            const type = policy.policyType;
            coverageByType[type] = (coverageByType[type] || 0) + (policy.coverageAmount || 0);
            policiesByType[type] = (policiesByType[type] || 0) + 1;
        });
        
        return { coverageByType, policiesByType };
    };

    const fetchPolicies = async () => {
        try {
            setLoading(true);
            const response = await insuranceApi.getInsurancePolicies(userId);
            setPolicies(Array.isArray(response) ? response : []);
        } catch (error) {
            console.error('Error fetching policies:', error);
            setPolicies([]);
        } finally {
            setLoading(false);
        }
    };

    const handleAddPolicy = () => {
        setSelectedPolicy(null);
        setShowPolicyForm(true);
    };

    const handleEditPolicy = (policy) => {
        setSelectedPolicy(policy);
        setShowPolicyForm(true);
    };

    const handleDeletePolicy = async (policyId) => {
        if (confirm('Are you sure you want to delete this policy?')) {
            try {
                await insuranceApi.deleteInsurancePolicy(policyId);
                fetchPolicies();
                fetchSummaryData();
            } catch (error) {
                alert('Error deleting policy: ' + error.message);
            }
        }
    };

    const handlePolicySaved = () => {
        setShowPolicyForm(false);
        setSelectedPolicy(null);
        fetchPolicies();
        fetchSummaryData();
    };

    const handlePayPremium = (policy) => {
        setSelectedPolicy(policy);
        setShowPremiumForm(true);
    };

    const handleFileClaim = (policy) => {
        setSelectedPolicy(policy);
        setShowClaimsForm(true);
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            maximumFractionDigits: 0
        }).format(amount || 0);
    };

    const COLORS = ['#0066ff', '#00c49f', '#ffbb28', '#ff8042', '#8884d8', '#82ca9d'];

    const renderOverview = () => {
        if (loading || !summary) {
            return <div style={{ textAlign: 'center', padding: '40px' }}>Loading...</div>;
        }

        // Prepare chart data
        const coverageByType = analytics?.coverageByType || {};
        const coverageData = Object.entries(coverageByType).map(([name, value]) => ({
            name,
            value: value
        }));

        const policiesByType = analytics?.policiesByType || {};
        const policyCountData = Object.entries(policiesByType).map(([name, value]) => ({
            name,
            value: value
        }));

        const upcomingPremiums = policies.filter(p => 
            p.nextPremiumDate && new Date(p.nextPremiumDate) <= new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)
        );

        const maturingSoon = policies.filter(p =>
            p.maturityDate && new Date(p.maturityDate) <= new Date(Date.now() + 90 * 24 * 60 * 60 * 1000)
        );

        return (
            <>
                {/* Hero Stats */}
                <section className="hero-card" style={{ marginBottom: '32px' }}>
                    <div className="hero-label">Total Coverage</div>
                    <div className="hero-value">{formatCurrency(summary.totalCoverage)}</div>
                    <div className="hero-delta">
                        {summary.activePolicies} Active Policies • Yearly Premium: {formatCurrency(summary.yearlyPremium)}
                    </div>
                </section>

                {/* Stats Grid */}
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '24px', marginBottom: '32px' }}>
                    <div className="stat-card">
                        <div className="stat-label">Total Policies</div>
                        <div className="stat-value">{summary.totalPolicies}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Active Policies</div>
                        <div className="stat-value">{summary.activePolicies}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Premiums Paid (2026)</div>
                        <div className="stat-value">{formatCurrency(summary.premiumsPaidThisYear)}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Claims Filed</div>
                        <div className="stat-value">{summary.totalClaims}</div>
                    </div>
                </div>

                {/* Charts */}
                {coverageData.length > 0 && (
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '24px', marginBottom: '32px' }}>
                        <div className="stat-card" style={{ height: '320px' }}>
                            <h3 style={{ fontSize: '16px', marginBottom: '16px' }}>Coverage by Type</h3>
                            <ResponsiveContainer width="100%" height="90%">
                                <PieChart>
                                    <Pie
                                        data={coverageData}
                                        innerRadius={60}
                                        outerRadius={90}
                                        paddingAngle={5}
                                        dataKey="value"
                                        label={(entry) => `${entry.name}: ${formatCurrency(entry.value)}`}
                                    >
                                        {coverageData.map((entry, index) => (
                                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                        ))}
                                    </Pie>
                                    <Tooltip formatter={(value) => formatCurrency(value)} />
                                </PieChart>
                            </ResponsiveContainer>
                        </div>

                        <div className="stat-card" style={{ height: '320px' }}>
                            <h3 style={{ fontSize: '16px', marginBottom: '16px' }}>Policies by Type</h3>
                            <ResponsiveContainer width="100%" height="90%">
                                <PieChart>
                                    <Pie
                                        data={policyCountData}
                                        innerRadius={60}
                                        outerRadius={90}
                                        paddingAngle={5}
                                        dataKey="value"
                                        label={(entry) => `${entry.name}: ${entry.value}`}
                                    >
                                        {policyCountData.map((entry, index) => (
                                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                        ))}
                                    </Pie>
                                    <Tooltip />
                                </PieChart>
                            </ResponsiveContainer>
                        </div>
                    </div>
                )}

                {/* Alerts Section */}
                {(upcomingPremiums.length > 0 || maturingSoon.length > 0) && (
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '24px', marginBottom: '32px' }}>
                        {upcomingPremiums.length > 0 && (
                            <div className="stat-card" style={{ borderLeft: '4px solid var(--warning-color)' }}>
                                <h3 style={{ fontSize: '16px', marginBottom: '12px', color: 'var(--warning-color)' }}>
                                    ⚠️ Upcoming Premiums ({upcomingPremiums.length})
                                </h3>
                                {upcomingPremiums.slice(0, 3).map(policy => (
                                    <div key={policy.id} style={{ padding: '8px 0', borderBottom: '1px solid #eee' }}>
                                        <div style={{ fontWeight: '600' }}>{policy.policyName}</div>
                                        <div style={{ fontSize: '13px', color: '#666' }}>
                                            Due: {new Date(policy.nextPremiumDate).toLocaleDateString('en-IN')} • {formatCurrency(policy.premiumAmount)}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}

                        {maturingSoon.length > 0 && (
                            <div className="stat-card" style={{ borderLeft: '4px solid var(--success-color)' }}>
                                <h3 style={{ fontSize: '16px', marginBottom: '12px', color: 'var(--success-color)' }}>
                                    🎉 Maturing Soon ({maturingSoon.length})
                                </h3>
                                {maturingSoon.slice(0, 3).map(policy => (
                                    <div key={policy.id} style={{ padding: '8px 0', borderBottom: '1px solid #eee' }}>
                                        <div style={{ fontWeight: '600' }}>{policy.policyName}</div>
                                        <div style={{ fontSize: '13px', color: '#666' }}>
                                            Maturity: {new Date(policy.maturityDate).toLocaleDateString('en-IN')} • {formatCurrency(policy.maturityAmount)}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                )}

                {/* Recent Policies */}
                <h2 style={{ fontSize: '18px', marginBottom: '16px' }}>Recent Policies</h2>
                <div className="data-table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>Policy Name</th>
                                <th>Type</th>
                                <th>Provider</th>
                                <th>Coverage</th>
                                <th>Premium</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {policies.slice(0, 5).map((policy) => (
                                <tr key={policy.id}>
                                    <td style={{ fontWeight: '600' }}>{policy.policyName}</td>
                                    <td>{policy.policyType}</td>
                                    <td>{policy.providerName}</td>
                                    <td>{formatCurrency(policy.coverageAmount)}</td>
                                    <td>{formatCurrency(policy.premiumAmount)} / {policy.premiumFrequency}</td>
                                    <td>
                                        <span className={`status-badge status-${policy.policyStatus.toLowerCase()}`}>
                                            {policy.policyStatus}
                                        </span>
                                    </td>
                                    <td>
                                        <button
                                            className="btn-secondary"
                                            style={{ padding: '6px 12px', fontSize: '13px' }}
                                            onClick={() => handlePayPremium(policy)}
                                        >
                                            Pay Premium
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </>
        );
    };

    return (
        <div>
            <div className="page-header" style={{ marginBottom: '24px' }}>
                <h1 className="page-title">Insurance Management</h1>
                <button className="btn-primary" onClick={handleAddPolicy}>
                    + Add Policy
                </button>
            </div>

            {/* Tabs */}
            <div className="tabs" style={{ marginBottom: '24px' }}>
                <button
                    className={`tab ${activeTab === 'overview' ? 'active' : ''}`}
                    onClick={() => setActiveTab('overview')}
                >
                    Overview
                </button>
                <button
                    className={`tab ${activeTab === 'policies' ? 'active' : ''}`}
                    onClick={() => setActiveTab('policies')}
                >
                    All Policies
                </button>
                <button
                    className={`tab ${activeTab === 'claims' ? 'active' : ''}`}
                    onClick={() => setActiveTab('claims')}
                >
                    Claims
                </button>
            </div>

            {/* Tab Content */}
            {activeTab === 'overview' && renderOverview()}
            
            {activeTab === 'policies' && (
                <PolicyList
                    policies={policies}
                    loading={loading}
                    onEdit={handleEditPolicy}
                    onDelete={handleDeletePolicy}
                    onPayPremium={handlePayPremium}
                    onFileClaim={handleFileClaim}
                />
            )}

            {activeTab === 'claims' && (
                <ClaimsManagement token={token} policies={policies} />
            )}

            {/* Modals */}
            {showPolicyForm && (
                <PolicyForm
                    policy={selectedPolicy}
                    onClose={() => setShowPolicyForm(false)}
                    onSave={handlePolicySaved}
                    token={token}
                />
            )}

            {showPremiumForm && selectedPolicy && (
                <PremiumPayment
                    policy={selectedPolicy}
                    onClose={() => setShowPremiumForm(false)}
                    onSave={() => {
                        setShowPremiumForm(false);
                        fetchSummaryData();
                        fetchPolicies();
                    }}
                    token={token}
                />
            )}

            {showClaimsForm && selectedPolicy && (
                <ClaimsManagement
                    token={token}
                    policies={policies}
                    preSelectedPolicy={selectedPolicy}
                />
            )}
        </div>
    );
};

export default Insurance;
