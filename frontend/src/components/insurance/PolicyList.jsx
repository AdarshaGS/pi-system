const PolicyList = ({ policies, loading, onEdit, onDelete, onPayPremium, onFileClaim }) => {
    const [filter, setFilter] = useState('ALL');
    const [search, setSearch] = useState('');

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            maximumFractionDigits: 0
        }).format(amount || 0);
    };

    const formatDate = (dateString) => {
        if (!dateString) return 'N/A';
        return new Date(dateString).toLocaleDateString('en-IN');
    };

    const filteredPolicies = policies.filter(policy => {
        const matchesFilter = filter === 'ALL' || policy.policyType === filter;
        const matchesSearch = policy.policyName.toLowerCase().includes(search.toLowerCase()) ||
                             policy.providerName.toLowerCase().includes(search.toLowerCase()) ||
                             policy.policyNumber.toLowerCase().includes(search.toLowerCase());
        return matchesFilter && matchesSearch;
    });

    if (loading) {
        return <div style={{ textAlign: 'center', padding: '40px' }}>Loading policies...</div>;
    }

    return (
        <div>
            {/* Filters */}
            <div style={{ display: 'flex', gap: '16px', marginBottom: '24px' }}>
                <input
                    type="text"
                    placeholder="Search policies..."
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    style={{ flex: 1, padding: '10px', border: '1px solid #ddd', borderRadius: '4px' }}
                />
                <select
                    value={filter}
                    onChange={(e) => setFilter(e.target.value)}
                    style={{ padding: '10px', border: '1px solid #ddd', borderRadius: '4px', minWidth: '150px' }}
                >
                    <option value="ALL">All Types</option>
                    <option value="LIFE">Life</option>
                    <option value="HEALTH">Health</option>
                    <option value="TERM">Term</option>
                    <option value="ENDOWMENT">Endowment</option>
                    <option value="ULIP">ULIP</option>
                    <option value="CRITICAL_ILLNESS">Critical Illness</option>
                </select>
            </div>

            {/* Policies Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '24px' }}>
                {filteredPolicies.map((policy) => (
                    <div key={policy.id} className="stat-card" style={{ position: 'relative' }}>
                        {/* Policy Type Badge */}
                        <span
                            style={{
                                position: 'absolute',
                                top: '16px',
                                right: '16px',
                                padding: '4px 12px',
                                background: getPolicyTypeColor(policy.policyType),
                                color: 'white',
                                borderRadius: '12px',
                                fontSize: '11px',
                                fontWeight: '600'
                            }}
                        >
                            {policy.policyType}
                        </span>

                        {/* Policy Details */}
                        <div style={{ marginBottom: '16px' }}>
                            <h3 style={{ fontSize: '18px', marginBottom: '8px', paddingRight: '80px' }}>
                                {policy.policyName}
                            </h3>
                            <div style={{ fontSize: '14px', color: '#666', marginBottom: '4px' }}>
                                {policy.providerName}
                            </div>
                            <div style={{ fontSize: '13px', color: '#999' }}>
                                Policy #: {policy.policyNumber}
                            </div>
                        </div>

                        {/* Coverage & Premium */}
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
                            <div>
                                <div style={{ fontSize: '12px', color: '#666', marginBottom: '4px' }}>Coverage</div>
                                <div style={{ fontSize: '18px', fontWeight: '600', color: 'var(--primary-color)' }}>
                                    {formatCurrency(policy.coverageAmount)}
                                </div>
                            </div>
                            <div>
                                <div style={{ fontSize: '12px', color: '#666', marginBottom: '4px' }}>Premium</div>
                                <div style={{ fontSize: '16px', fontWeight: '600' }}>
                                    {formatCurrency(policy.premiumAmount)}
                                    <span style={{ fontSize: '11px', fontWeight: '400', color: '#666' }}>
                                        /{policy.premiumFrequency}
                                    </span>
                                </div>
                            </div>
                        </div>

                        {/* Important Dates */}
                        <div style={{ padding: '12px', background: '#f8f9fa', borderRadius: '8px', marginBottom: '16px', fontSize: '13px' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '6px' }}>
                                <span style={{ color: '#666' }}>Next Premium:</span>
                                <span style={{ fontWeight: '600' }}>{formatDate(policy.nextPremiumDate)}</span>
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                <span style={{ color: '#666' }}>Maturity Date:</span>
                                <span style={{ fontWeight: '600' }}>{formatDate(policy.maturityDate)}</span>
                            </div>
                        </div>

                        {/* Status */}
                        <div style={{ marginBottom: '16px' }}>
                            <span className={`status-badge status-${policy.policyStatus.toLowerCase()}`}>
                                {policy.policyStatus}
                            </span>
                        </div>

                        {/* Nominee Info */}
                        {policy.nomineeName && (
                            <div style={{ fontSize: '13px', color: '#666', marginBottom: '16px', paddingTop: '12px', borderTop: '1px solid #eee' }}>
                                <strong>Nominee:</strong> {policy.nomineeName} ({policy.nomineeRelation})
                            </div>
                        )}

                        {/* Actions */}
                        <div style={{ display: 'flex', gap: '8px' }}>
                            <button
                                className="btn-primary"
                                style={{ flex: 1, padding: '10px', fontSize: '14px' }}
                                onClick={() => onPayPremium(policy)}
                            >
                                Pay Premium
                            </button>
                            <button
                                className="btn-secondary"
                                style={{ padding: '10px 16px', fontSize: '14px' }}
                                onClick={() => onEdit(policy)}
                                title="Edit"
                            >
                                ✏️
                            </button>
                            <button
                                className="btn-secondary"
                                style={{ padding: '10px 16px', fontSize: '14px' }}
                                onClick={() => onFileClaim(policy)}
                                title="File Claim"
                            >
                                📋
                            </button>
                            <button
                                className="btn-secondary"
                                style={{ padding: '10px 16px', fontSize: '14px', color: 'var(--danger-color)' }}
                                onClick={() => onDelete(policy.id)}
                                title="Delete"
                            >
                                🗑️
                            </button>
                        </div>
                    </div>
                ))}
            </div>

            {filteredPolicies.length === 0 && (
                <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
                    No policies found. Add your first insurance policy to get started.
                </div>
            )}
        </div>
    );
};

const getPolicyTypeColor = (type) => {
    const colors = {
        'LIFE': '#0066ff',
        'HEALTH': '#00c49f',
        'TERM': '#8884d8',
        'ENDOWMENT': '#ff8042',
        'ULIP': '#ffbb28',
        'CRITICAL_ILLNESS': '#ff6b6b'
    };
    return colors[type] || '#666';
};

import { useState } from 'react';

export default PolicyList;
