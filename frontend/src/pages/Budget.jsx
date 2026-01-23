import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { budgetApi } from '../api';
import { Plus, X, PieChart, Wallet, Calendar, Tag, FileText } from 'lucide-react';

const Budget = () => {
    const [report, setReport] = useState(null);
    const [expenses, setExpenses] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showModal, setShowModal] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [formData, setFormData] = useState({
        category: 'FOOD',
        description: '',
        amount: '',
        date: new Date().toISOString().split('T')[0]
    });

    const navigate = useNavigate();

    const formatCurrency = (value) => {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            maximumFractionDigits: 0
        }).format(value || 0);
    };

    const fetchData = useCallback(async () => {
        const user = JSON.parse(localStorage.getItem('user'));
        if (!user || !user.token) {
            navigate('/login');
            return;
        }

        try {
            const [reportData, expenseData] = await Promise.all([
                budgetApi.getReport(user.userId, user.token),
                budgetApi.getExpenses(user.userId, user.token)
            ]);
            setReport(reportData);
            setExpenses(expenseData);
        } catch (err) {
            setError('Failed to fetch budget data');
        } finally {
            setLoading(false);
        }
    }, [navigate]);

    useEffect(() => {
        fetchData();
    }, [fetchData]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const user = JSON.parse(localStorage.getItem('user'));
        if (!user || !user.token) return;

        setSubmitting(true);
        try {
            await budgetApi.addExpense({
                ...formData,
                userId: user.userId,
                amount: parseFloat(formData.amount)
            }, user.token);

            setShowModal(false);
            setFormData({
                category: 'FOOD',
                description: '',
                amount: '',
                date: new Date().toISOString().split('T')[0]
            });
            fetchData();
        } catch (err) {
            alert('Failed to add expense: ' + err.message);
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) return <div style={{ padding: '40px', textAlign: 'center' }}>Loading Budget Tracker...</div>;

    return (
        <div style={{ position: 'relative' }}>
            <div className="page-header">
                <h1 className="page-title">Budget Tracker</h1>
                <button
                    onClick={() => setShowModal(true)}
                    className="auth-button"
                    style={{ width: 'auto', padding: '10px 20px', display: 'flex', alignItems: 'center', gap: '8px' }}
                >
                    <Plus size={18} /> Add Expense
                </button>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '24px', marginBottom: '32px' }}>
                <div className="stat-card">
                    <div className="stat-label">Monthly Limit</div>
                    <div className="stat-value">{formatCurrency(report?.totalBudget)}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Spent So Far</div>
                    <div className="stat-value" style={{ color: 'var(--error-color)' }}>{formatCurrency(report?.totalSpent)}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Net Balance</div>
                    <div className="stat-value" style={{ color: (report?.balance || 0) >= 0 ? 'var(--success-color)' : 'var(--error-color)' }}>
                        {formatCurrency(report?.balance)}
                    </div>
                </div>
            </div>

            {report?.categoryBreakdown && Object.keys(report.categoryBreakdown).length > 0 && (
                <section style={{ marginBottom: '32px' }}>
                    <h2 style={{ fontSize: '18px', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <PieChart size={20} color="var(--brand-color)" /> Category Breakdown
                    </h2>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '20px' }}>
                        {Object.entries(report.categoryBreakdown).map(([cat, details]) => (
                            <div key={cat} className="portfolio-card" style={{ padding: '16px' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '12px', alignItems: 'center' }}>
                                    <span style={{ fontWeight: '600', fontSize: '14px', color: 'var(--text-primary)' }}>{cat}</span>
                                    <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>{details.percentageUsed?.toFixed(1)}% used</span>
                                </div>
                                <div style={{ height: '6px', background: '#f1f3f5', borderRadius: '3px', marginBottom: '12px', overflow: 'hidden' }}>
                                    <div style={{
                                        height: '100%',
                                        width: `${Math.min(details.percentageUsed || 0, 100)}%`,
                                        background: (details.percentageUsed || 0) > 90 ? 'var(--error-color)' : 'var(--brand-color)',
                                        borderRadius: '3px'
                                    }}></div>
                                </div>
                                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px' }}>
                                    <span>Spent: <b>{formatCurrency(details.spent)}</b></span>
                                    <span style={{ color: 'var(--text-secondary)' }}>Limit: {formatCurrency(details.limit)}</span>
                                </div>
                            </div>
                        ))}
                    </div>
                </section>
            )}

            <section style={{ background: 'white', padding: '24px', borderRadius: '12px', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
                <h2 style={{ fontSize: '18px', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <FileText size={20} color="var(--brand-color)" /> Recent Expenses
                </h2>
                <div className="data-table-container" style={{ boxShadow: 'none' }}>
                    <table>
                        <thead>
                            <tr>
                                <th>Category</th>
                                <th>Description</th>
                                <th>Date</th>
                                <th>Amount</th>
                            </tr>
                        </thead>
                        <tbody>
                            {expenses.length > 0 ? expenses.map((exp, i) => (
                                <tr key={i}>
                                    <td style={{ fontWeight: '600' }}>
                                        <span className="risk-badge" style={{ background: '#f8f9fa', color: 'var(--text-primary)', border: '1px solid #e9ecef' }}>
                                            {exp.category}
                                        </span>
                                    </td>
                                    <td>{exp.description}</td>
                                    <td>{new Date(exp.date).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })}</td>
                                    <td style={{ color: 'var(--error-color)', fontWeight: '600' }}>{formatCurrency(exp.amount)}</td>
                                </tr>
                            )) : (
                                <tr>
                                    <td colSpan="4" style={{ textAlign: 'center', padding: '32px', color: 'var(--text-secondary)' }}>No recent expenses found.</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </section>

            {/* Modal for Add Expense */}
            {showModal && (
                <div style={{
                    position: 'fixed',
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                    background: 'rgba(0,0,0,0.5)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    zIndex: 1000,
                    backdropFilter: 'blur(4px)'
                }}>
                    <div style={{
                        background: 'white',
                        padding: '32px',
                        borderRadius: '16px',
                        width: '100%',
                        maxWidth: '450px',
                        boxShadow: '0 20px 25px -5px rgba(0,0,0,0.1), 0 10px 10px -5px rgba(0,0,0,0.04)'
                    }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                            <h2 style={{ fontSize: '20px', fontWeight: '700', color: 'var(--text-primary)' }}>Add New Expense</h2>
                            <button onClick={() => setShowModal(false)} style={{ border: 'none', background: 'none', cursor: 'pointer', color: 'var(--text-secondary)' }}>
                                <X size={24} />
                            </button>
                        </div>

                        <form onSubmit={handleSubmit}>
                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ display: 'block', fontSize: '14px', marginBottom: '8px', color: 'var(--text-secondary)', fontWeight: '500' }}>Category</label>
                                <div style={{ position: 'relative' }}>
                                    <Tag size={16} style={{ position: 'absolute', left: '12px', top: '14px', color: '#adb5bd' }} />
                                    <select
                                        name="category"
                                        value={formData.category}
                                        onChange={handleInputChange}
                                        style={{ width: '100%', padding: '12px 12px 12px 40px', borderRadius: '8px', border: '1px solid #e9ecef', fontSize: '14px' }}
                                        required
                                    >
                                        <option value="FOOD">Food & Dining</option>
                                        <option value="ENTERTAINMENT">Entertainment</option>
                                        <option value="TRANSPORT">Transport</option>
                                        <option value="BILL">Bills & Utilities</option>
                                        <option value="SHOPPING">Shopping</option>
                                        <option value="OTHER">Other</option>
                                    </select>
                                </div>
                            </div>

                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ display: 'block', fontSize: '14px', marginBottom: '8px', color: 'var(--text-secondary)', fontWeight: '500' }}>Amount (₹)</label>
                                <div style={{ position: 'relative' }}>
                                    <Wallet size={16} style={{ position: 'absolute', left: '12px', top: '14px', color: '#adb5bd' }} />
                                    <input
                                        name="amount"
                                        type="number"
                                        placeholder="0.00"
                                        value={formData.amount}
                                        onChange={handleInputChange}
                                        style={{ width: '100%', padding: '12px 12px 12px 40px', borderRadius: '8px', border: '1px solid #e9ecef', fontSize: '14px' }}
                                        required
                                    />
                                </div>
                            </div>

                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ display: 'block', fontSize: '14px', marginBottom: '8px', color: 'var(--text-secondary)', fontWeight: '500' }}>Date</label>
                                <div style={{ position: 'relative' }}>
                                    <Calendar size={16} style={{ position: 'absolute', left: '12px', top: '14px', color: '#adb5bd' }} />
                                    <input
                                        name="date"
                                        type="date"
                                        value={formData.date}
                                        onChange={handleInputChange}
                                        style={{ width: '100%', padding: '12px 12px 12px 40px', borderRadius: '8px', border: '1px solid #e9ecef', fontSize: '14px' }}
                                        required
                                    />
                                </div>
                            </div>

                            <div style={{ marginBottom: '32px' }}>
                                <label style={{ display: 'block', fontSize: '14px', marginBottom: '8px', color: 'var(--text-secondary)', fontWeight: '500' }}>Description</label>
                                <div style={{ position: 'relative' }}>
                                    <FileText size={16} style={{ position: 'absolute', left: '12px', top: '14px', color: '#adb5bd' }} />
                                    <input
                                        name="description"
                                        placeholder="What was this for?"
                                        value={formData.description}
                                        onChange={handleInputChange}
                                        style={{ width: '100%', padding: '12px 12px 12px 40px', borderRadius: '8px', border: '1px solid #e9ecef', fontSize: '14px' }}
                                        required
                                    />
                                </div>
                            </div>

                            <button
                                type="submit"
                                disabled={submitting}
                                className="auth-button"
                                style={{ background: submitting ? '#ccc' : 'var(--brand-color)' }}
                            >
                                {submitting ? 'Adding...' : 'Save Expense'}
                            </button>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Budget;
