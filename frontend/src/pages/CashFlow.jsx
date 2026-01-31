import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import toast, { Toaster } from 'react-hot-toast';
import { budgetApi } from '../api';
import { Plus, X, PieChart, TrendingUp, TrendingDown, DollarSign, Wallet, AlertCircle, Edit, Trash2, Download } from 'lucide-react';
import ExportModal from '../components/ExportModal';
import { downloadFile, generateFilename } from '../utils/fileDownload';

const CashFlow = () => {
    const [cashFlow, setCashFlow] = useState(null);
    const [incomes, setIncomes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showIncomeModal, setShowIncomeModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [showExportModal, setShowExportModal] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [editingIncome, setEditingIncome] = useState(null);
    const [incomeForm, setIncomeForm] = useState({
        source: 'SALARY',
        description: '',
        amount: '',
        date: new Date().toISOString().split('T')[0],
        isRecurring: true,
        isStable: true
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
            const [cashFlowData, incomeData] = await Promise.all([
                budgetApi.getCashFlow(user.userId, user.token),
                budgetApi.getIncomes(user.userId, user.token)
            ]);
            setCashFlow(cashFlowData);
            setIncomes(incomeData);
        } catch (err) {
            console.error('Failed to fetch cash flow data:', err);
        } finally {
            setLoading(false);
        }
    }, [navigate]);

    useEffect(() => {
        fetchData();
    }, [fetchData]);

    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setIncomeForm(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const user = JSON.parse(localStorage.getItem('user'));
        if (!user || !user.token) return;

        setSubmitting(true);
        try {
            await budgetApi.addIncome({
                ...incomeForm,
                userId: user.userId,
                amount: parseFloat(incomeForm.amount)
            }, user.token);

            setShowIncomeModal(false);
            setIncomeForm({
                source: 'SALARY',
                description: '',
                amount: '',
                date: new Date().toISOString().split('T')[0],
                isRecurring: true,
                isStable: true
            });
            fetchData();
            toast.success('Income added successfully!');
        } catch (err) {
            toast.error('Failed to add income: ' + err.message);
        } finally {
            setSubmitting(false);
        }
    };

    const handleEditIncome = (income) => {
        setEditingIncome(income);
        setIncomeForm({
            source: income.source,
            description: income.description || '',
            amount: income.amount.toString(),
            date: income.date,
            isRecurring: income.isRecurring,
            isStable: income.isStable
        });
        setShowEditModal(true);
    };

    const handleUpdateIncome = async (e) => {
        e.preventDefault();
        const user = JSON.parse(localStorage.getItem('user'));
        if (!user || !user.token || !editingIncome) return;

        setSubmitting(true);
        try {
            await budgetApi.updateIncome(editingIncome.id, {
                ...incomeForm,
                userId: user.userId,
                amount: parseFloat(incomeForm.amount)
            }, user.token);

            setShowEditModal(false);
            setEditingIncome(null);
            setIncomeForm({
                source: 'SALARY',
                description: '',
                amount: '',
                date: new Date().toISOString().split('T')[0],
                isRecurring: true,
                isStable: true
            });
            fetchData();
            toast.success('Income updated successfully!');
        } catch (err) {
            toast.error('Failed to update income: ' + err.message);
        } finally {
            setSubmitting(false);
        }
    };

    const handleDeleteIncome = async (incomeId) => {
        if (!window.confirm('Are you sure you want to delete this income entry?')) return;
        
        const user = JSON.parse(localStorage.getItem('user'));
        if (!user || !user.token) return;

        try {
            await budgetApi.deleteIncome(incomeId, user.token);
            fetchData();
            toast.success('Income deleted successfully!');
        } catch (err) {
            toast.error('Failed to delete income: ' + err.message);
        }
    };
    const handleExport = async (params) => {
        const user = JSON.parse(localStorage.getItem('user'));
        if (!user || !user.token) return;

        const loadingToast = toast.loading('Exporting data...');
        
        try {
            let blob;
            let filename;
            
            if (params.format === 'csv') {
                blob = await budgetApi.exportIncomesCSV(user.userId, user.token, {
                    startDate: params.startDate,
                    endDate: params.endDate,
                    source: params.categories && params.categories.length > 0 ? params.categories[0] : null
                });
                filename = generateFilename('incomes', 'csv');
            } else if (params.format === 'excel') {
                blob = await budgetApi.exportIncomesExcel(user.userId, user.token, {
                    startDate: params.startDate,
                    endDate: params.endDate,
                    source: params.categories && params.categories.length > 0 ? params.categories[0] : null
                });
                filename = generateFilename('incomes', 'xlsx');
            } else if (params.format === 'pdf') {
                const monthYear = params.monthYear || new Date().toISOString().slice(0, 7);
                blob = await budgetApi.downloadPDFReport(user.userId, user.token, monthYear);
                filename = generateFilename('cash_flow_report', 'pdf');
            }

            if (blob) {
                downloadFile(blob, filename);
                toast.success('Export completed successfully!', { id: loadingToast });
            }
        } catch (err) {
            console.error('Export failed:', err);
            toast.error('Export failed: ' + err.message, { id: loadingToast });
            throw err;
        }
    };
    if (loading) return <div style={{ padding: '40px', textAlign: 'center' }}>Loading Cash Flow Analysis...</div>;

    return (
        <div style={{ position: 'relative' }}>
            <Toaster position="top-right" />
            <div className="page-header">
                <h1 className="page-title">Cash Flow & Income Tracker</h1>
                <div style={{ display: 'flex', gap: '12px' }}>
                    <button
                        onClick={() => setShowExportModal(true)}
                        className="auth-button"
                        style={{ width: 'auto', padding: '10px 20px', display: 'flex', alignItems: 'center', gap: '8px', background: '#10b981' }}
                    >
                        <Download size={18} /> Export
                    </button>
                    <button
                        onClick={() => setShowIncomeModal(true)}
                        className="auth-button"
                        style={{ width: 'auto', padding: '10px 20px', display: 'flex', alignItems: 'center', gap: '8px' }}
                    >
                        <Plus size={18} /> Add Income
                    </button>
                </div>
            </div>

            {/* Key Metrics */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '24px', marginBottom: '32px' }}>
                <div className="stat-card">
                    <div className="stat-label">Total Income</div>
                    <div className="stat-value" style={{ color: 'var(--success-color)' }}>
                        {formatCurrency(cashFlow?.totalIncome)}
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Total Expenses</div>
                    <div className="stat-value" style={{ color: 'var(--error-color)' }}>
                        {formatCurrency(cashFlow?.totalExpenses)}
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Net Cash Flow</div>
                    <div className="stat-value" style={{ 
                        color: (cashFlow?.netCashFlow || 0) >= 0 ? 'var(--success-color)' : 'var(--error-color)',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '8px'
                    }}>
                        {(cashFlow?.netCashFlow || 0) >= 0 ? <TrendingUp size={24} /> : <TrendingDown size={24} />}
                        {formatCurrency(cashFlow?.netCashFlow)}
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Savings Rate</div>
                    <div className="stat-value">{(cashFlow?.savingsRate || 0).toFixed(1)}%</div>
                    <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '4px' }}>
                        Target: 20%+
                    </div>
                </div>
            </div>

            {/* Income Breakdown */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px', marginBottom: '32px' }}>
                <section className="portfolio-card">
                    <h2 style={{ fontSize: '18px', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <DollarSign size={20} color="var(--brand-color)" /> Income Breakdown
                    </h2>
                    <div style={{ display: 'grid', gap: '12px' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', padding: '12px', background: '#f8f9fa', borderRadius: '8px' }}>
                            <span style={{ fontWeight: '600' }}>Stable Income</span>
                            <span style={{ color: 'var(--success-color)', fontWeight: '600' }}>
                                {formatCurrency(cashFlow?.stableIncome)}
                            </span>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', padding: '12px', background: '#f8f9fa', borderRadius: '8px' }}>
                            <span style={{ fontWeight: '600' }}>Variable Income</span>
                            <span style={{ color: 'var(--warning-color)', fontWeight: '600' }}>
                                {formatCurrency(cashFlow?.variableIncome)}
                            </span>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', padding: '12px', background: '#f8f9fa', borderRadius: '8px' }}>
                            <span style={{ fontWeight: '600' }}>Recurring Income</span>
                            <span style={{ fontWeight: '600' }}>
                                {formatCurrency(cashFlow?.recurringIncome)}
                            </span>
                        </div>
                    </div>
                    <div style={{ marginTop: '16px', padding: '12px', background: '#e7f5ff', borderRadius: '8px', borderLeft: '4px solid var(--brand-color)' }}>
                        <div style={{ fontSize: '12px', fontWeight: '600', marginBottom: '4px' }}>Income Stability</div>
                        <div style={{ fontSize: '20px', fontWeight: '700', color: 'var(--brand-color)' }}>
                            {(cashFlow?.incomeStability || 0).toFixed(1)}%
                        </div>
                    </div>
                </section>

                <section className="portfolio-card">
                    <h2 style={{ fontSize: '18px', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <PieChart size={20} color="var(--brand-color)" /> Income by Source
                    </h2>
                    <div style={{ display: 'grid', gap: '10px' }}>
                        {cashFlow?.incomeBySource && Object.entries(cashFlow.incomeBySource).map(([source, amount]) => (
                            <div key={source} style={{ 
                                display: 'flex', 
                                justifyContent: 'space-between', 
                                padding: '10px 12px',
                                background: '#f8f9fa',
                                borderRadius: '6px'
                            }}>
                                <span className="risk-badge" style={{ 
                                    background: 'white', 
                                    color: 'var(--text-primary)',
                                    border: '1px solid #e9ecef',
                                    fontSize: '12px'
                                }}>
                                    {source}
                                </span>
                                <span style={{ fontWeight: '600' }}>{formatCurrency(amount)}</span>
                            </div>
                        ))}
                    </div>
                </section>
            </div>

            {/* Recommendations */}
            {cashFlow?.recommendations && cashFlow.recommendations.length > 0 && (
                <section className="portfolio-card" style={{ marginBottom: '32px', background: '#fff3cd', border: '1px solid #ffc107' }}>
                    <h2 style={{ 
                        fontSize: '18px', 
                        marginBottom: '16px', 
                        display: 'flex', 
                        alignItems: 'center', 
                        gap: '8px',
                        color: '#856404'
                    }}>
                        <AlertCircle size={20} /> Recommendations
                    </h2>
                    <ul style={{ margin: 0, paddingLeft: '20px', color: '#856404' }}>
                        {cashFlow.recommendations.map((rec, i) => (
                            <li key={i} style={{ marginBottom: '8px' }}>{rec}</li>
                        ))}
                    </ul>
                </section>
            )}

            {/* Historical Trends */}
            {cashFlow?.last6Months && cashFlow.last6Months.length > 0 && (
                <section className="portfolio-card" style={{ marginBottom: '32px' }}>
                    <h2 style={{ fontSize: '18px', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <TrendingUp size={20} color="var(--brand-color)" /> 6-Month Trend
                    </h2>
                    <div className="data-table-container" style={{ boxShadow: 'none' }}>
                        <table>
                            <thead>
                                <tr>
                                    <th>Month</th>
                                    <th>Income</th>
                                    <th>Expenses</th>
                                    <th>Savings</th>
                                    <th>Savings Rate</th>
                                </tr>
                            </thead>
                            <tbody>
                                {cashFlow.last6Months.map((trend, i) => (
                                    <tr key={i}>
                                        <td style={{ fontWeight: '600' }}>{trend.monthYear}</td>
                                        <td style={{ color: 'var(--success-color)' }}>{formatCurrency(trend.income)}</td>
                                        <td style={{ color: 'var(--error-color)' }}>{formatCurrency(trend.expenses)}</td>
                                        <td style={{ fontWeight: '600', color: trend.savings >= 0 ? 'var(--success-color)' : 'var(--error-color)' }}>
                                            {formatCurrency(trend.savings)}
                                        </td>
                                        <td>
                                            <span style={{ 
                                                padding: '4px 8px', 
                                                borderRadius: '4px',
                                                background: trend.savingsRate >= 20 ? '#d4edda' : '#f8d7da',
                                                color: trend.savingsRate >= 20 ? '#155724' : '#721c24',
                                                fontSize: '12px',
                                                fontWeight: '600'
                                            }}>
                                                {(trend.savingsRate || 0).toFixed(1)}%
                                            </span>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </section>
            )}

            {/* Recent Income Entries */}
            <section className="portfolio-card">
                <h2 style={{ fontSize: '18px', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <Wallet size={20} color="var(--brand-color)" /> Recent Income Entries
                </h2>
                <div className="data-table-container" style={{ boxShadow: 'none' }}>
                    <table>
                        <thead>
                            <tr>
                                <th>Source</th>
                                <th>Date</th>
                                <th>Amount</th>
                                <th>Type</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {incomes.length > 0 ? incomes.map((inc, i) => (
                                <tr key={i}>
                                    <td style={{ fontWeight: '600' }}>
                                        <span className="risk-badge" style={{ 
                                            background: inc.isStable ? '#d4edda' : '#fff3cd',
                                            color: inc.isStable ? '#155724' : '#856404',
                                            border: 'none'
                                        }}>
                                            {inc.source}
                                        </span>
                                    </td>
                                    <td>{new Date(inc.date).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })}</td>
                                    <td style={{ color: 'var(--success-color)', fontWeight: '600' }}>{formatCurrency(inc.amount)}</td>
                                    <td>
                                        <div style={{ display: 'flex', gap: '6px', fontSize: '11px' }}>
                                            {inc.isRecurring && (
                                                <span style={{ 
                                                    padding: '2px 6px', 
                                                    background: '#e7f5ff', 
                                                    color: '#0c5460',
                                                    borderRadius: '3px'
                                                }}>
                                                    Recurring
                                                </span>
                                            )}
                                            {inc.isStable && (
                                                <span style={{ 
                                                    padding: '2px 6px', 
                                                    background: '#d4edda', 
                                                    color: '#155724',
                                                    borderRadius: '3px'
                                                }}>
                                                    Stable
                                                </span>
                                            )}
                                        </div>
                                    </td>
                                    <td>
                                        <div style={{ display: 'flex', gap: '8px' }}>
                                            <button
                                                onClick={() => handleEditIncome(inc)}
                                                style={{
                                                    padding: '6px 10px',
                                                    background: '#2196f3',
                                                    color: 'white',
                                                    border: 'none',
                                                    borderRadius: '6px',
                                                    cursor: 'pointer',
                                                    display: 'flex',
                                                    alignItems: 'center',
                                                    gap: '4px',
                                                    fontSize: '12px',
                                                    fontWeight: '500'
                                                }}
                                                title="Edit income"
                                            >
                                                <Edit size={14} /> Edit
                                            </button>
                                            <button
                                                onClick={() => handleDeleteIncome(inc.id)}
                                                style={{
                                                    padding: '6px 10px',
                                                    background: '#f44336',
                                                    color: 'white',
                                                    border: 'none',
                                                    borderRadius: '6px',
                                                    cursor: 'pointer',
                                                    display: 'flex',
                                                    alignItems: 'center',
                                                    gap: '4px',
                                                    fontSize: '12px',
                                                    fontWeight: '500'
                                                }}
                                                title="Delete income"
                                            >
                                                <Trash2 size={14} /> Delete
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            )) : (
                                <tr>
                                    <td colSpan="5" style={{ textAlign: 'center', padding: '32px', color: 'var(--text-secondary)' }}>
                                        No income entries found. Add your first income source!
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </section>

            {/* Add Income Modal */}
            {showIncomeModal && (
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
                        maxWidth: '500px',
                        boxShadow: '0 10px 40px rgba(0,0,0,0.2)'
                    }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                            <h2 style={{ fontSize: '20px', fontWeight: '700', margin: 0 }}>Add Income</h2>
                            <button
                                onClick={() => setShowIncomeModal(false)}
                                style={{ background: 'none', border: 'none', cursor: 'pointer', padding: '4px' }}
                            >
                                <X size={24} color="var(--text-secondary)" />
                            </button>
                        </div>

                        <form onSubmit={handleSubmit}>
                            <div style={{ marginBottom: '16px' }}>
                                <label className="auth-label">Income Source</label>
                                <select name="source" value={incomeForm.source} onChange={handleInputChange} className="auth-input">
                                    <option value="SALARY">Salary</option>
                                    <option value="DIVIDEND">Dividend</option>
                                    <option value="RENTAL">Rental Income</option>
                                    <option value="FREELANCE">Freelance</option>
                                    <option value="BONUS">Bonus</option>
                                    <option value="INTEREST">Interest</option>
                                    <option value="OTHER">Other</option>
                                </select>
                            </div>

                            <div style={{ marginBottom: '16px' }}>
                                <label className="auth-label">Amount (₹)</label>
                                <input
                                    type="number"
                                    name="amount"
                                    value={incomeForm.amount}
                                    onChange={handleInputChange}
                                    className="auth-input"
                                    required
                                    min="0"
                                    step="0.01"
                                />
                            </div>

                            <div style={{ marginBottom: '16px' }}>
                                <label className="auth-label">Date</label>
                                <input
                                    type="date"
                                    name="date"
                                    value={incomeForm.date}
                                    onChange={handleInputChange}
                                    className="auth-input"
                                    required
                                />
                            </div>

                            <div style={{ display: 'flex', gap: '16px', marginBottom: '24px' }}>
                                <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                                    <input
                                        type="checkbox"
                                        name="isRecurring"
                                        checked={incomeForm.isRecurring}
                                        onChange={handleInputChange}
                                    />
                                    <span style={{ fontSize: '14px' }}>Recurring Income</span>
                                </label>
                                <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                                    <input
                                        type="checkbox"
                                        name="isStable"
                                        checked={incomeForm.isStable}
                                        onChange={handleInputChange}
                                    />
                                    <span style={{ fontSize: '14px' }}>Stable Income</span>
                                </label>
                            </div>

                            <button type="submit" className="auth-button" disabled={submitting}>
                                {submitting ? 'Adding...' : 'Add Income'}
                            </button>
                        </form>
                    </div>
                </div>
            )}

            {/* Edit Income Modal */}
            {showEditModal && (
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
                        maxWidth: '500px',
                        boxShadow: '0 10px 40px rgba(0,0,0,0.2)'
                    }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                            <h2 style={{ fontSize: '20px', fontWeight: '700', margin: 0 }}>Edit Income</h2>
                            <button
                                onClick={() => {
                                    setShowEditModal(false);
                                    setEditingIncome(null);
                                }}
                                style={{ background: 'none', border: 'none', cursor: 'pointer', padding: '4px' }}
                            >
                                <X size={24} color="var(--text-secondary)" />
                            </button>
                        </div>

                        <form onSubmit={handleUpdateIncome}>
                            <div style={{ marginBottom: '16px' }}>
                                <label className="auth-label">Income Source</label>
                                <select name="source" value={incomeForm.source} onChange={handleInputChange} className="auth-input">
                                    <option value="SALARY">Salary</option>
                                    <option value="DIVIDEND">Dividend</option>
                                    <option value="RENTAL">Rental Income</option>
                                    <option value="FREELANCE">Freelance</option>
                                    <option value="BONUS">Bonus</option>
                                    <option value="INTEREST">Interest</option>
                                    <option value="OTHER">Other</option>
                                </select>
                            </div>

                            <div style={{ marginBottom: '16px' }}>
                                <label className="auth-label">Amount (₹)</label>
                                <input
                                    type="number"
                                    name="amount"
                                    value={incomeForm.amount}
                                    onChange={handleInputChange}
                                    className="auth-input"
                                    required
                                    min="0"
                                    step="0.01"
                                />
                            </div>

                            <div style={{ marginBottom: '16px' }}>
                                <label className="auth-label">Date</label>
                                <input
                                    type="date"
                                    name="date"
                                    value={incomeForm.date}
                                    onChange={handleInputChange}
                                    className="auth-input"
                                    required
                                />
                            </div>

                            <div style={{ display: 'flex', gap: '16px', marginBottom: '24px' }}>
                                <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                                    <input
                                        type="checkbox"
                                        name="isRecurring"
                                        checked={incomeForm.isRecurring}
                                        onChange={handleInputChange}
                                    />
                                    <span style={{ fontSize: '14px' }}>Recurring Income</span>
                                </label>
                                <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                                    <input
                                        type="checkbox"
                                        name="isStable"
                                        checked={incomeForm.isStable}
                                        onChange={handleInputChange}
                                    />
                                    <span style={{ fontSize: '14px' }}>Stable Income</span>
                                </label>
                            </div>

                            <button type="submit" className="auth-button" disabled={submitting}>
                                {submitting ? 'Updating...' : 'Update Income'}
                            </button>
                        </form>
                    </div>
                </div>
            )}

            {/* Export Modal */}
            <ExportModal
                show={showExportModal}
                onClose={() => setShowExportModal(false)}
                onExport={handleExport}
                exportType="incomes"
                categories={[
                    { value: 'SALARY', label: 'Salary' },
                    { value: 'FREELANCE', label: 'Freelance' },
                    { value: 'INVESTMENT', label: 'Investment' },
                    { value: 'BUSINESS', label: 'Business' },
                    { value: 'RENTAL', label: 'Rental' },
                    { value: 'GIFT', label: 'Gift' },
                    { value: 'OTHER', label: 'Other' }
                ]}
            />
        </div>
    );
};

export default CashFlow;
