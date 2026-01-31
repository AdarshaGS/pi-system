import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import toast, { Toaster } from 'react-hot-toast';
import { budgetApi } from '../api';
import { Plus, X, PieChart, Wallet, Calendar, Tag as TagIcon, FileText, Edit, Trash2, Settings, Filter, ChevronLeft, ChevronRight, Download } from 'lucide-react';
import { PieChart as RechartsPie, Pie, Cell, ResponsiveContainer, Tooltip, Legend, LineChart, Line, XAxis, YAxis, CartesianGrid } from 'recharts';
import ExportModal from '../components/ExportModal';
import TagSelector from '../components/TagSelector';
import TagManagementModal from '../components/TagManagementModal';
import BulkActionsToolbar from '../components/BulkActionsToolbar';
import { downloadFile, generateFilename } from '../utils/fileDownload';

const Budget = () => {
    const [report, setReport] = useState(null);
    const [expenses, setExpenses] = useState({ content: [], totalPages: 0, totalElements: 0, number: 0 });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showModal, setShowModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [showBudgetModal, setShowBudgetModal] = useState(false);
    const [showFilters, setShowFilters] = useState(false);
    const [showExportModal, setShowExportModal] = useState(false);
    const [showTagModal, setShowTagModal] = useState(false);
    const [showBulkCategoryModal, setShowBulkCategoryModal] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [editingExpense, setEditingExpense] = useState(null);
    const [budgetLimits, setBudgetLimits] = useState({});
    const [selectedExpenses, setSelectedExpenses] = useState([]);
    const [bulkCategory, setBulkCategory] = useState('FOOD');
    
    // Pagination and filter states
    const [currentPage, setCurrentPage] = useState(0);
    const [pageSize] = useState(10);
    const [filters, setFilters] = useState({
        category: '',
        startDate: '',
        endDate: '',
        search: '',
        sortBy: 'expenseDate',
        order: 'desc'
    });
    
    const [formData, setFormData] = useState({
        category: 'FOOD',
        description: '',
        amount: '',
        date: new Date().toISOString().split('T')[0],
        notes: '',
        tags: []
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
            setLoading(true);
            
            // Build params from filters and pagination
            const params = {
                page: currentPage,
                size: pageSize,
                sortBy: filters.sortBy,
                order: filters.order
            };
            
            // Add optional filters if they have values
            if (filters.category) params.category = filters.category;
            if (filters.startDate) params.startDate = filters.startDate;
            if (filters.endDate) params.endDate = filters.endDate;
            if (filters.search) params.search = filters.search;
            
            const [reportData, expenseData, budgetData] = await Promise.all([
                budgetApi.getReport(user.userId, user.token),
                budgetApi.getExpenses(user.userId, user.token, params),
                budgetApi.getAllBudgets(user.userId, user.token)
            ]);
            
            setReport(reportData);
            
            // Handle paginated response
            if (expenseData.content) {
                setExpenses(expenseData);
            } else {
                // Fallback for non-paginated response
                setExpenses({
                    content: Array.isArray(expenseData) ? expenseData : [],
                    totalPages: 1,
                    totalElements: Array.isArray(expenseData) ? expenseData.length : 0,
                    number: 0
                });
            }
            
            // Convert budget array to object keyed by category
            const limitsObj = {};
            budgetData.forEach(b => limitsObj[b.category] = b.monthlyLimit);
            setBudgetLimits(limitsObj);
        } catch (err) {
            setError('Failed to fetch budget data');
        } finally {
            setLoading(false);
        }
    }, [navigate, currentPage, pageSize, filters]);

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
                amount: parseFloat(formData.amount),
                tags: formData.tags.map(t => t.id)
            }, user.token);

            setShowModal(false);
            setFormData({
                category: 'FOOD',
                description: '',
                amount: '',
                date: new Date().toISOString().split('T')[0],
                notes: '',
                tags: []
            });
            fetchData();
            toast.success('Expense added successfully!');
        } catch (err) {
            toast.error('Failed to add expense: ' + err.message);
        } finally {
            setSubmitting(false);
        }
    };

    const handleEditExpense = (expense) => {
        setEditingExpense(expense);
        setFormData({
            category: expense.category,
            description: expense.description,
            amount: expense.amount.toString(),
            date: expense.expenseDate,
            notes: expense.notes || '',
            tags: expense.tags || []
        });
        setShowEditModal(true);
    };

    const handleUpdateExpense = async (e) => {
        e.preventDefault();
        const user = JSON.parse(localStorage.getItem('user'));
        if (!user || !user.token || !editingExpense) return;

        setSubmitting(true);
        try {
            await budgetApi.updateExpense(editingExpense.id, {
                ...formData,
                userId: user.userId,
                amount: parseFloat(formData.amount),
                expenseDate: formData.date,
                tags: formData.tags.map(t => t.id)
            }, user.token);

            setShowEditModal(false);
            setEditingExpense(null);
            setFormData({
                category: 'FOOD',
                description: '',
                amount: '',
                date: new Date().toISOString().split('T')[0],
                notes: '',
                tags: []
            });
            fetchData();
            toast.success('Expense updated successfully!');
        } catch (err) {
            toast.error('Failed to update expense: ' + err.message);
        } finally {
            setSubmitting(false);
        }
    };

    const handleDeleteExpense = async (expenseId) => {
        if (!window.confirm('Are you sure you want to delete this expense?')) return;
        
        const user = JSON.parse(localStorage.getItem('user'));
        if (!user || !user.token) return;

        try {
            await budgetApi.deleteExpense(expenseId, user.token);
            toast.success('Expense deleted successfully!');
            fetchData();
        } catch (err) {
            toast.error('Failed to delete expense: ' + err.message);
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
                blob = await budgetApi.exportExpensesCSV(user.userId, user.token, {
                    startDate: params.startDate,
                    endDate: params.endDate,
                    category: params.category
                });
                filename = generateFilename('expenses', 'csv');
            } else if (params.format === 'excel') {
                blob = await budgetApi.exportExpensesExcel(user.userId, user.token, {
                    startDate: params.startDate,
                    endDate: params.endDate,
                    category: params.category
                });
                filename = generateFilename('expenses', 'xlsx');
            } else if (params.format === 'pdf') {
                const monthYear = params.monthYear || new Date().toISOString().slice(0, 7); // YYYY-MM
                blob = await budgetApi.downloadPDFReport(user.userId, user.token, monthYear);
                filename = generateFilename('budget_report', 'pdf');
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

    const handleSaveBudgetLimits = async () => {
        const user = JSON.parse(localStorage.getItem('user'));
        if (!user || !user.token) return;

        setSubmitting(true);
        try {
            const monthYear = new Date().toISOString().slice(0, 7); // YYYY-MM
            
            for (const [category, limit] of Object.entries(budgetLimits)) {
                if (limit && limit > 0) {
                    await budgetApi.setBudget({
                        userId: user.userId,
                        category: category,
                        monthlyLimit: parseFloat(limit),
                        monthYear: monthYear
                    }, user.token);
                }
            }
            
            setShowBudgetModal(false);
            fetchData();
            toast.success('Budget limits saved successfully!');
        } catch (err) {
            toast.error('Failed to save budget limits: ' + err.message);
        } finally {
            setSubmitting(false);
        }
    };

    // Bulk operations handlers
    const handleSelectExpense = (expenseId) => {
        setSelectedExpenses(prev => {
            if (prev.includes(expenseId)) {
                return prev.filter(id => id !== expenseId);
            }
            return [...prev, expenseId];
        });
    };

    const handleSelectAll = () => {
        if (selectedExpenses.length === expenses.content.length) {
            setSelectedExpenses([]);
        } else {
            setSelectedExpenses(expenses.content.map(exp => exp.id));
        }
    };

    const handleBulkDelete = async () => {
        if (selectedExpenses.length === 0) return;
        
        if (!window.confirm(`Delete ${selectedExpenses.length} selected expense(s)?`)) {
            return;
        }

        const user = JSON.parse(localStorage.getItem('user'));
        if (!user || !user.token) return;

        try {
            await budgetApi.bulkDeleteExpenses(user.userId, selectedExpenses, user.token);
            setSelectedExpenses([]);
            fetchData();
            toast.success(`${selectedExpenses.length} expense(s) deleted successfully!`);
        } catch (err) {
            toast.error('Failed to delete expenses: ' + err.message);
        }
    };

    const handleBulkCategoryChange = async () => {
        if (selectedExpenses.length === 0) return;

        const user = JSON.parse(localStorage.getItem('user'));
        if (!user || !user.token) return;

        try {
            await budgetApi.bulkUpdateCategory(user.userId, selectedExpenses, bulkCategory, user.token);
            setSelectedExpenses([]);
            setShowBulkCategoryModal(false);
            fetchData();
            toast.success(`${selectedExpenses.length} expense(s) category updated successfully!`);
        } catch (err) {
            toast.error('Failed to update categories: ' + err.message);
        }
    };

    if (loading) return <div style={{ padding: '40px', textAlign: 'center' }}>Loading Budget Tracker...</div>;

    return (
        <div style={{ position: 'relative' }}>
            <Toaster position="top-right" />
            <div className="page-header">
                <h1 className="page-title">Budget Tracker</h1>
                <div style={{ display: 'flex', gap: '12px' }}>
                    <button
                        onClick={() => setShowBudgetModal(true)}
                        className="auth-button"
                        style={{ 
                            width: 'auto', 
                            padding: '10px 20px', 
                            display: 'flex', 
                            alignItems: 'center', 
                            gap: '8px',
                            background: '#6366f1'
                        }}
                    >
                        <Settings size={18} /> Set Budget
                    </button>
                    <button
                        onClick={() => setShowExportModal(true)}
                        className="auth-button"
                        style={{ 
                            width: 'auto', 
                            padding: '10px 20px', 
                            display: 'flex', 
                            alignItems: 'center', 
                            gap: '8px',
                            background: '#10b981'
                        }}
                    >
                        <Download size={18} /> Export
                    </button>
                    <button
                        onClick={() => setShowModal(true)}
                        className="auth-button"
                        style={{ width: 'auto', padding: '10px 20px', display: 'flex', alignItems: 'center', gap: '8px' }}
                    >
                        <Plus size={18} /> Add Expense
                    </button>
                </div>
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

            {/* Spending Visualization */}
            {report?.categoryBreakdown && Object.keys(report.categoryBreakdown).length > 0 && (
                <section style={{ background: 'white', padding: '24px', borderRadius: '12px', boxShadow: '0 1px 3px rgba(0,0,0,0.1)', marginBottom: '32px' }}>
                    <h2 style={{ fontSize: '18px', marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <PieChart size={20} color="var(--brand-color)" /> Spending Distribution
                    </h2>
                    <ResponsiveContainer width="100%" height={300}>
                        <RechartsPie>
                            <Pie
                                data={Object.entries(report.categoryBreakdown)
                                    .filter(([_, details]) => details.spent > 0)
                                    .map(([cat, details]) => ({
                                        name: cat,
                                        value: details.spent
                                    }))}
                                dataKey="value"
                                nameKey="name"
                                cx="50%"
                                cy="50%"
                                outerRadius={100}
                                label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                            >
                                {Object.entries(report.categoryBreakdown).map((_, index) => (
                                    <Cell key={`cell-${index}`} fill={['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40'][index % 6]} />
                                ))}
                            </Pie>
                            <Tooltip formatter={(value) => formatCurrency(value)} />
                        </RechartsPie>
                    </ResponsiveContainer>
                </section>
            )}

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
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                    <h2 style={{ fontSize: '18px', margin: 0, display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <FileText size={20} color="var(--brand-color)" /> Recent Expenses
                    </h2>
                    <button
                        onClick={() => setShowFilters(!showFilters)}
                        style={{
                            padding: '8px 16px',
                            background: showFilters ? 'var(--brand-color)' : '#f8f9fa',
                            color: showFilters ? 'white' : 'var(--text-primary)',
                            border: '1px solid #e9ecef',
                            borderRadius: '8px',
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '6px',
                            fontSize: '14px',
                            fontWeight: '500'
                        }}
                    >
                        <Filter size={16} /> {showFilters ? 'Hide' : 'Show'} Filters
                    </button>
                </div>

                {/* Filter Section */}
                {showFilters && (
                    <div style={{
                        padding: '20px',
                        background: '#f8f9fa',
                        borderRadius: '8px',
                        marginBottom: '20px',
                        border: '1px solid #e9ecef'
                    }}>
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '16px' }}>
                            <div>
                                <label style={{ display: 'block', fontSize: '13px', marginBottom: '6px', color: 'var(--text-secondary)', fontWeight: '500' }}>
                                    Category
                                </label>
                                <select
                                    value={filters.category}
                                    onChange={(e) => setFilters(prev => ({ ...prev, category: e.target.value }))}
                                    style={{
                                        width: '100%',
                                        padding: '8px 12px',
                                        borderRadius: '6px',
                                        border: '1px solid #e9ecef',
                                        fontSize: '14px'
                                    }}
                                >
                                    <option value="">All Categories</option>
                                    <option value="FOOD">Food & Dining</option>
                                    <option value="ENTERTAINMENT">Entertainment</option>
                                    <option value="TRANSPORT">Transport</option>
                                    <option value="BILL">Bills & Utilities</option>
                                    <option value="SHOPPING">Shopping</option>
                                    <option value="OTHER">Other</option>
                                </select>
                            </div>
                            
                            <div>
                                <label style={{ display: 'block', fontSize: '13px', marginBottom: '6px', color: 'var(--text-secondary)', fontWeight: '500' }}>
                                    Start Date
                                </label>
                                <input
                                    type="date"
                                    value={filters.startDate}
                                    onChange={(e) => setFilters(prev => ({ ...prev, startDate: e.target.value }))}
                                    style={{
                                        width: '100%',
                                        padding: '8px 12px',
                                        borderRadius: '6px',
                                        border: '1px solid #e9ecef',
                                        fontSize: '14px'
                                    }}
                                />
                            </div>
                            
                            <div>
                                <label style={{ display: 'block', fontSize: '13px', marginBottom: '6px', color: 'var(--text-secondary)', fontWeight: '500' }}>
                                    End Date
                                </label>
                                <input
                                    type="date"
                                    value={filters.endDate}
                                    onChange={(e) => setFilters(prev => ({ ...prev, endDate: e.target.value }))}
                                    style={{
                                        width: '100%',
                                        padding: '8px 12px',
                                        borderRadius: '6px',
                                        border: '1px solid #e9ecef',
                                        fontSize: '14px'
                                    }}
                                />
                            </div>
                            
                            <div>
                                <label style={{ display: 'block', fontSize: '13px', marginBottom: '6px', color: 'var(--text-secondary)', fontWeight: '500' }}>
                                    Search Description
                                </label>
                                <input
                                    type="text"
                                    value={filters.search}
                                    onChange={(e) => setFilters(prev => ({ ...prev, search: e.target.value }))}
                                    placeholder="Search..."
                                    style={{
                                        width: '100%',
                                        padding: '8px 12px',
                                        borderRadius: '6px',
                                        border: '1px solid #e9ecef',
                                        fontSize: '14px'
                                    }}
                                />
                            </div>
                        </div>
                        
                        <div style={{ display: 'flex', gap: '12px', marginTop: '16px' }}>
                            <button
                                onClick={() => {
                                    setCurrentPage(0);
                                    fetchData();
                                }}
                                style={{
                                    padding: '8px 20px',
                                    background: 'var(--brand-color)',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '6px',
                                    cursor: 'pointer',
                                    fontSize: '14px',
                                    fontWeight: '500'
                                }}
                            >
                                Apply Filters
                            </button>
                            <button
                                onClick={() => {
                                    setFilters({
                                        category: '',
                                        startDate: '',
                                        endDate: '',
                                        search: '',
                                        sortBy: 'expenseDate',
                                        order: 'desc'
                                    });
                                    setCurrentPage(0);
                                }}
                                style={{
                                    padding: '8px 20px',
                                    background: '#f8f9fa',
                                    color: 'var(--text-primary)',
                                    border: '1px solid #e9ecef',
                                    borderRadius: '6px',
                                    cursor: 'pointer',
                                    fontSize: '14px',
                                    fontWeight: '500'
                                }}
                            >
                                Clear Filters
                            </button>
                        </div>
                    </div>
                )}

                <div className="data-table-container" style={{ boxShadow: 'none' }}>
                    <table>
                        <thead>
                            <tr>
                                <th style={{ width: '40px' }}>
                                    <input
                                        type="checkbox"
                                        checked={expenses.content.length > 0 && selectedExpenses.length === expenses.content.length}
                                        onChange={handleSelectAll}
                                        style={{ cursor: 'pointer' }}
                                    />
                                </th>
                                <th>Category</th>
                                <th>Description</th>
                                <th>Date</th>
                                <th>Amount</th>
                                <th>Tags</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {expenses.content && expenses.content.length > 0 ? expenses.content.map((exp, i) => (
                                <tr key={i}>
                                    <td>
                                        <input
                                            type="checkbox"
                                            checked={selectedExpenses.includes(exp.id)}
                                            onChange={() => handleSelectExpense(exp.id)}
                                            style={{ cursor: 'pointer' }}
                                        />
                                    </td>
                                    <td style={{ fontWeight: '600' }}>
                                        <span className="risk-badge" style={{ background: '#f8f9fa', color: 'var(--text-primary)', border: '1px solid #e9ecef' }}>
                                            {exp.category}
                                        </span>
                                    </td>
                                    <td>{exp.description}</td>
                                    <td>{new Date(exp.date).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })}</td>
                                    <td style={{ color: 'var(--error-color)', fontWeight: '600' }}>{formatCurrency(exp.amount)}</td>
                                    <td>
                                        {exp.tags && exp.tags.length > 0 ? (
                                            <div style={{ display: 'flex', gap: '4px', flexWrap: 'wrap' }}>
                                                {exp.tags.map(tag => (
                                                    <span
                                                        key={tag.id}
                                                        style={{
                                                            padding: '2px 8px',
                                                            borderRadius: '12px',
                                                            fontSize: '11px',
                                                            fontWeight: '500',
                                                            color: 'white',
                                                            backgroundColor: tag.color
                                                        }}
                                                    >
                                                        {tag.name}
                                                    </span>
                                                ))}
                                            </div>
                                        ) : (
                                            <span style={{ color: '#9ca3af', fontSize: '12px' }}>No tags</span>
                                        )}
                                    </td>
                                    <td>
                                        <div style={{ display: 'flex', gap: '8px' }}>
                                            <button
                                                onClick={() => handleEditExpense(exp)}
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
                                                title="Edit expense"
                                            >
                                                <Edit size={14} /> Edit
                                            </button>
                                            <button
                                                onClick={() => handleDeleteExpense(exp.id)}
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
                                                title="Delete expense"
                                            >
                                                <Trash2 size={14} /> Delete
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            )) : (
                                <tr>
                                    <td colSpan="5" style={{ textAlign: 'center', padding: '32px', color: 'var(--text-secondary)' }}>No recent expenses found.</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>

                {/* Pagination Controls */}
                {expenses.content && expenses.content.length > 0 && (
                    <div style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        marginTop: '20px',
                        padding: '16px 0',
                        borderTop: '1px solid #e9ecef'
                    }}>
                        <div style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
                            Showing {(currentPage * pageSize) + 1} to {Math.min((currentPage + 1) * pageSize, expenses.totalElements)} of {expenses.totalElements} expenses
                        </div>
                        
                        <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                            <button
                                onClick={() => setCurrentPage(currentPage - 1)}
                                disabled={currentPage === 0}
                                style={{
                                    padding: '8px 16px',
                                    background: currentPage === 0 ? '#f8f9fa' : 'var(--brand-color)',
                                    color: currentPage === 0 ? '#adb5bd' : 'white',
                                    border: '1px solid #e9ecef',
                                    borderRadius: '6px',
                                    cursor: currentPage === 0 ? 'not-allowed' : 'pointer',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '6px',
                                    fontSize: '14px',
                                    fontWeight: '500'
                                }}
                            >
                                <ChevronLeft size={16} /> Previous
                            </button>
                            
                            <span style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
                                Page {currentPage + 1} of {expenses.totalPages}
                            </span>
                            
                            <button
                                onClick={() => setCurrentPage(currentPage + 1)}
                                disabled={currentPage >= expenses.totalPages - 1}
                                style={{
                                    padding: '8px 16px',
                                    background: currentPage >= expenses.totalPages - 1 ? '#f8f9fa' : 'var(--brand-color)',
                                    color: currentPage >= expenses.totalPages - 1 ? '#adb5bd' : 'white',
                                    border: '1px solid #e9ecef',
                                    borderRadius: '6px',
                                    cursor: currentPage >= expenses.totalPages - 1 ? 'not-allowed' : 'pointer',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '6px',
                                    fontSize: '14px',
                                    fontWeight: '500'
                                }}
                            >
                                Next <ChevronRight size={16} />
                            </button>
                        </div>
                    </div>
                )}
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

                            <div style={{ marginBottom: '20px' }}>
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

                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ display: 'block', fontSize: '14px', marginBottom: '8px', color: 'var(--text-secondary)', fontWeight: '500' }}>Notes (Optional)</label>
                                <textarea
                                    name="notes"
                                    placeholder="Add any additional notes..."
                                    value={formData.notes}
                                    onChange={handleInputChange}
                                    style={{ width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #e9ecef', fontSize: '14px', minHeight: '80px', resize: 'vertical' }}
                                />
                            </div>

                            <div style={{ marginBottom: '32px' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                                    <label style={{ fontSize: '14px', color: 'var(--text-secondary)', fontWeight: '500' }}>Tags (Optional)</label>
                                    <button
                                        type="button"
                                        onClick={() => setShowTagModal(true)}
                                        style={{
                                            padding: '4px 8px',
                                            fontSize: '12px',
                                            background: 'none',
                                            border: '1px solid #d1d5db',
                                            borderRadius: '6px',
                                            cursor: 'pointer',
                                            color: '#6366f1'
                                        }}
                                    >
                                        Manage Tags
                                    </button>
                                </div>
                                <TagSelector
                                    selectedTags={formData.tags}
                                    onTagsChange={(tags) => setFormData({ ...formData, tags })}
                                />
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

            {/* Modal for Edit Expense */}
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
                        maxWidth: '450px',
                        boxShadow: '0 20px 25px -5px rgba(0,0,0,0.1), 0 10px 10px -5px rgba(0,0,0,0.04)'
                    }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                            <h2 style={{ fontSize: '20px', fontWeight: '700', color: 'var(--text-primary)' }}>Edit Expense</h2>
                            <button onClick={() => {
                                setShowEditModal(false);
                                setEditingExpense(null);
                            }} style={{ border: 'none', background: 'none', cursor: 'pointer', color: 'var(--text-secondary)' }}>
                                <X size={24} />
                            </button>
                        </div>

                        <form onSubmit={handleUpdateExpense}>
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

                            <div style={{ marginBottom: '20px' }}>
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

                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ display: 'block', fontSize: '14px', marginBottom: '8px', color: 'var(--text-secondary)', fontWeight: '500' }}>Notes (Optional)</label>
                                <textarea
                                    name="notes"
                                    placeholder="Add any additional notes..."
                                    value={formData.notes}
                                    onChange={handleInputChange}
                                    style={{ width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #e9ecef', fontSize: '14px', minHeight: '80px', resize: 'vertical' }}
                                />
                            </div>

                            <div style={{ marginBottom: '32px' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                                    <label style={{ fontSize: '14px', color: 'var(--text-secondary)', fontWeight: '500' }}>Tags (Optional)</label>
                                    <button
                                        type="button"
                                        onClick={() => setShowTagModal(true)}
                                        style={{
                                            padding: '4px 8px',
                                            fontSize: '12px',
                                            background: 'none',
                                            border: '1px solid #d1d5db',
                                            borderRadius: '6px',
                                            cursor: 'pointer',
                                            color: '#6366f1'
                                        }}
                                    >
                                        Manage Tags
                                    </button>
                                </div>
                                <TagSelector
                                    selectedTags={formData.tags}
                                    onTagsChange={(tags) => setFormData({ ...formData, tags })}
                                />
                            </div>

                            <button
                                type="submit"
                                disabled={submitting}
                                className="auth-button"
                                style={{ background: submitting ? '#ccc' : 'var(--brand-color)' }}
                            >
                                {submitting ? 'Updating...' : 'Update Expense'}
                            </button>
                        </form>
                    </div>
                </div>
            )}

            {/* Modal for Set Budget Limits */}
            {showBudgetModal && (
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
                        maxWidth: '600px',
                        maxHeight: '80vh',
                        overflow: 'auto',
                        boxShadow: '0 20px 25px -5px rgba(0,0,0,0.1), 0 10px 10px -5px rgba(0,0,0,0.04)'
                    }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                            <h2 style={{ fontSize: '20px', fontWeight: '700', color: 'var(--text-primary)' }}>Set Monthly Budget Limits</h2>
                            <button onClick={() => setShowBudgetModal(false)} style={{ border: 'none', background: 'none', cursor: 'pointer', color: 'var(--text-secondary)' }}>
                                <X size={24} />
                            </button>
                        </div>

                        <div style={{ marginBottom: '24px', padding: '12px', background: '#e7f5ff', borderRadius: '8px', fontSize: '14px', color: '#0c5460' }}>
                            💡 Set monthly spending limits for each category. Leave empty to skip a category.
                        </div>

                        <div style={{ display: 'grid', gap: '16px' }}>
                            {['FOOD', 'RENT', 'TRANSPORT', 'ENTERTAINMENT', 'SHOPPING', 'UTILITIES', 'HEALTH', 'EDUCATION', 'INVESTMENT', 'OTHERS'].map(category => (
                                <div key={category}>
                                    <label style={{ 
                                        display: 'block', 
                                        fontSize: '14px', 
                                        marginBottom: '8px', 
                                        color: 'var(--text-primary)', 
                                        fontWeight: '600' 
                                    }}>
                                        {category}
                                    </label>
                                    <div style={{ position: 'relative' }}>
                                        <span style={{ position: 'absolute', left: '12px', top: '12px', color: '#adb5bd', fontWeight: '600' }}>₹</span>
                                        <input
                                            type="number"
                                            value={budgetLimits[category] || ''}
                                            onChange={(e) => setBudgetLimits(prev => ({
                                                ...prev,
                                                [category]: e.target.value ? parseFloat(e.target.value) : ''
                                            }))}
                                            placeholder={`Enter ${category.toLowerCase()} limit`}
                                            style={{ 
                                                width: '100%', 
                                                padding: '12px 12px 12px 32px', 
                                                borderRadius: '8px', 
                                                border: '1px solid #e9ecef', 
                                                fontSize: '14px' 
                                            }}
                                            min="0"
                                            step="100"
                                        />
                                    </div>
                                </div>
                            ))}
                        </div>

                        <div style={{ display: 'flex', gap: '12px', marginTop: '24px' }}>
                            <button
                                onClick={handleSaveBudgetLimits}
                                disabled={submitting}
                                className="auth-button"
                                style={{ 
                                    flex: 1,
                                    background: submitting ? '#ccc' : 'var(--brand-color)' 
                                }}
                            >
                                {submitting ? 'Saving...' : 'Save All Limits'}
                            </button>
                            <button
                                onClick={() => setShowBudgetModal(false)}
                                style={{ 
                                    flex: 1,
                                    padding: '12px 24px',
                                    background: '#f1f3f5',
                                    color: 'var(--text-primary)',
                                    border: 'none',
                                    borderRadius: '8px',
                                    cursor: 'pointer',
                                    fontWeight: '600'
                                }}
                            >
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Export Modal */}
            <ExportModal
                show={showExportModal}
                onClose={() => setShowExportModal(false)}
                onExport={handleExport}
                exportType="expenses"
                categories={[
                    { value: 'FOOD', label: 'Food' },
                    { value: 'TRANSPORT', label: 'Transport' },
                    { value: 'ENTERTAINMENT', label: 'Entertainment' },
                    { value: 'UTILITIES', label: 'Utilities' },
                    { value: 'HEALTHCARE', label: 'Healthcare' },
                    { value: 'EDUCATION', label: 'Education' },
                    { value: 'SHOPPING', label: 'Shopping' },
                    { value: 'INSURANCE', label: 'Insurance' },
                    { value: 'SAVINGS', label: 'Savings' },
                    { value: 'OTHER', label: 'Other' }
                ]}
            />

            {/* Bulk Actions Toolbar */}
            <BulkActionsToolbar
                selectedCount={selectedExpenses.length}
                onDelete={handleBulkDelete}
                onUpdateCategory={() => setShowBulkCategoryModal(true)}
                onClear={() => setSelectedExpenses([])}
            />

            {/* Tag Management Modal */}
            <TagManagementModal
                show={showTagModal}
                onClose={() => setShowTagModal(false)}
            />

            {/* Bulk Category Change Modal */}
            {showBulkCategoryModal && (
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
                    zIndex: 1000
                }}>
                    <div style={{
                        background: 'white',
                        padding: '32px',
                        borderRadius: '16px',
                        width: '100%',
                        maxWidth: '400px',
                        boxShadow: '0 20px 25px -5px rgba(0,0,0,0.1)'
                    }}>
                        <div style={{ marginBottom: '24px' }}>
                            <h3 style={{ fontSize: '18px', fontWeight: '600', marginBottom: '8px' }}>
                                Change Category for {selectedExpenses.length} Expense(s)
                            </h3>
                            <p style={{ color: '#6b7280', fontSize: '14px' }}>
                                Select a new category for the selected expenses
                            </p>
                        </div>

                        <div style={{ marginBottom: '24px' }}>
                            <label style={{ display: 'block', fontSize: '14px', marginBottom: '8px', fontWeight: '500' }}>
                                New Category
                            </label>
                            <select
                                value={bulkCategory}
                                onChange={(e) => setBulkCategory(e.target.value)}
                                style={{
                                    width: '100%',
                                    padding: '12px',
                                    borderRadius: '8px',
                                    border: '1px solid #e9ecef',
                                    fontSize: '14px'
                                }}
                            >
                                <option value="FOOD">Food & Dining</option>
                                <option value="ENTERTAINMENT">Entertainment</option>
                                <option value="TRANSPORT">Transport</option>
                                <option value="BILL">Bills & Utilities</option>
                                <option value="SHOPPING">Shopping</option>
                                <option value="OTHER">Other</option>
                            </select>
                        </div>

                        <div style={{ display: 'flex', gap: '12px' }}>
                            <button
                                onClick={() => setShowBulkCategoryModal(false)}
                                style={{
                                    flex: 1,
                                    padding: '12px',
                                    background: '#f3f4f6',
                                    border: '1px solid #d1d5db',
                                    borderRadius: '8px',
                                    fontWeight: '600',
                                    cursor: 'pointer'
                                }}
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleBulkCategoryChange}
                                style={{
                                    flex: 1,
                                    padding: '12px',
                                    background: '#6366f1',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '8px',
                                    fontWeight: '600',
                                    cursor: 'pointer'
                                }}
                            >
                                Update
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Budget;
