import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { netWorthApi } from '../api';

const Dashboard = () => {
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    const formatCurrency = (value) => {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            maximumFractionDigits: 0
        }).format(value || 0);
    };

    useEffect(() => {
        const user = JSON.parse(localStorage.getItem('user'));
        if (!user || !user.token) {
            navigate('/login');
            return;
        }

        const fetchData = async () => {
            try {
                const nwData = await netWorthApi.getNetWorth(user.userId, user.token);
                setData(nwData);
            } catch (err) {
                setError('Failed to fetch financial data');
                if (err?.message?.includes('Unauthorized')) {
                    navigate('/login');
                }
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [navigate]);

    const timestamp = new Date().toLocaleString('en-IN', {
        day: 'numeric',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });

    if (loading) return <div style={{ padding: '40px', textAlign: 'center' }}>Loading your dashboard...</div>;
    if (error) return <div style={{ padding: '40px', textAlign: 'center', color: 'var(--error-color)' }}>{error}</div>;

    return (
        <div>
            <div className="page-header">
                <h1 className="page-title">Dashboard</h1>
                <div className="last-updated">Last updated: {timestamp}</div>
            </div>

            <section className="hero-card">
                <div className="hero-label">Total Net Worth</div>
                <div className="hero-value">{formatCurrency(data.netWorth)}</div>
                <div className="hero-delta delta-positive">
                    After Tax: {formatCurrency(data.netWorthAfterTax)}
                </div>
            </section>

            <section className="stats-grid">
                <div className="stat-card">
                    <div className="stat-label">Total Assets</div>
                    <div className="stat-value">{formatCurrency(data.totalAssets)}</div>
                    <div style={{ height: '8px', background: '#e9ecef', borderRadius: '4px', marginTop: '16px' }}>
                        <div style={{ height: '100%', width: '100%', background: '#28a745', borderRadius: '4px' }}></div>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Total Liabilities</div>
                    <div className="stat-value">{formatCurrency(data.totalLiabilities)}</div>
                    <div style={{ height: '8px', background: '#e9ecef', borderRadius: '4px', marginTop: '16px' }}>
                        <div style={{
                            height: '100%',
                            width: data.totalAssets > 0 ? `${Math.min((data.totalLiabilities / data.totalAssets) * 100, 100)}%` : '0%',
                            background: '#dc3545',
                            borderRadius: '4px'
                        }}></div>
                    </div>
                </div>
            </section>

            <div className="portfolio-grid">
                <Link to="/portfolio" className="portfolio-card" style={{ textDecoration: 'none', color: 'inherit' }}>
                    <div className="portfolio-card-title">Investments</div>
                    <div className="portfolio-card-value">{formatCurrency(data.portfolioValue)}</div>
                    <div className="portfolio-card-meta">Portfolio Value</div>
                </Link>
                <Link to="/portfolio" className="portfolio-card" style={{ textDecoration: 'none', color: 'inherit' }}>
                    <div className="portfolio-card-title">Savings</div>
                    <div className="portfolio-card-value">{formatCurrency(data.savingsValue)}</div>
                    <div className="portfolio-card-meta">Bank & FDs</div>
                </Link>
                <Link to="/budget" className="portfolio-card" style={{ textDecoration: 'none', color: 'inherit' }}>
                    <div className="portfolio-card-title">Loans</div>
                    <div className="portfolio-card-value">{formatCurrency(data.outstandingLoans)}</div>
                    <div className="portfolio-card-meta" style={{ color: 'var(--error-color)' }}>Outstanding</div>
                </Link>
                <Link to="/portfolio" className="portfolio-card" style={{ textDecoration: 'none', color: 'inherit' }}>
                    <div className="portfolio-card-title">Lendings</div>
                    <div className="portfolio-card-value">{formatCurrency(data.outstandingLendings)}</div>
                    <div className="portfolio-card-meta">Receivable</div>
                </Link>
            </div>

            <section style={{ marginTop: '32px' }}>
                <span style={{ fontSize: '14px', color: 'var(--text-secondary)', marginRight: '12px' }}>Tax Liability:</span>
                <span className="risk-badge" style={{ background: '#ffe8e8', color: '#c53030' }}>
                    {formatCurrency(data.outstandingTaxLiability)}
                </span>
            </section>
        </div>
    );
};

export default Dashboard;
