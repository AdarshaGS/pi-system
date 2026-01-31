import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { budgetApi } from '../api';
import { Plus, Edit, Trash2, Calendar, RefreshCw, ToggleLeft, ToggleRight } from 'lucide-react';
import './RecurringTransactions.css';

const RecurringTransactions = () => {
    const [templates, setTemplates] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [editingTemplate, setEditingTemplate] = useState(null);
    const [submitting, setSubmitting] = useState(false);
    const [filterType, setFilterType] = useState('all'); // all, EXPENSE, INCOME
    const [filterStatus, setFilterStatus] = useState('all'); // all, active, inactive
    
    const [formData, setFormData] = useState({
        name: '',
        type: 'EXPENSE',
        category: 'FOOD',
        source: 'SALARY',
        amount: '',
        pattern: 'MONTHLY',
        startDate: new Date().toISOString().split('T')[0],
        endDate: '',
        description: '',
        isActive: true
    });

    const navigate = useNavigate();

    const formatCurrency = (value) => {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            maximumFractionDigits: 0
        }).format(value || 0);
    };

    const fetchTemplates = useCallback(async () => {
        const user = JSON.parse(localStorage.getItem('user'));
        if (!user || !user.token) {
            navigate('/login');
            return;
        }

        try {
            setLoading(true);
            const data = await budgetApi.getRecurringTemplates(user.userId, user.token);
            setTemplates(data);
        } catch (err) {
            console.error('Failed to fetch recurring templates:', err);
            alert('Failed to load recurring templates');
        } finally {
            setLoading(false);
        }
    }, [navigate]);

    useEffect(() => {
        fetchTemplates();
    }, [fetchTemplates]);

    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
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
            const payload = {
                ...formData,
                userId: user.userId,
                amount: parseFloat(formData.amount),
                endDate: formData.endDate || null
            };

            if (editingTemplate) {
                await budgetApi.updateRecurringTemplate(editingTemplate.id, payload, user.token);
            } else {
                await budgetApi.createRecurringTemplate(payload, user.token);
            }

            setShowModal(false);
            setEditingTemplate(null);
            resetForm();
            fetchTemplates();
            alert(editingTemplate ? 'Template updated successfully!' : 'Template created successfully!');
        } catch (err) {
            alert('Failed to save template: ' + err.message);
        } finally {
            setSubmitting(false);
        }
    };

    const handleEdit = (template) => {
        setEditingTemplate(template);
        setFormData({
            name: template.name,
            type: template.type,
            category: template.category || 'FOOD',
            source: template.source || 'SALARY',
            amount: template.amount.toString(),
            pattern: template.pattern,
            startDate: template.startDate,
            endDate: template.endDate || '',
            description: template.description || '',
            isActive: template.isActive
        });
        setShowModal(true);
    };

    const handleDelete = async (templateId) => {
        if (!window.confirm('Are you sure you want to delete this recurring template?')) return;

        const user = JSON.parse(localStorage.getItem('user'));
        if (!user || !user.token) return;

        try {
            await budgetApi.deleteRecurringTemplate(templateId, user.token);
            fetchTemplates();
            alert('Template deleted successfully!');
        } catch (err) {
            alert('Failed to delete template: ' + err.message);
        }
    };

    const handleToggleActive = async (templateId) => {
        const user = JSON.parse(localStorage.getItem('user'));
        if (!user || !user.token) return;

        try {
            await budgetApi.toggleRecurringTemplate(templateId, user.token);
            fetchTemplates();
        } catch (err) {
            alert('Failed to toggle template: ' + err.message);
        }
    };

    const resetForm = () => {
        setFormData({
            name: '',
            type: 'EXPENSE',
            category: 'FOOD',
            source: 'SALARY',
            amount: '',
            pattern: 'MONTHLY',
            startDate: new Date().toISOString().split('T')[0],
            endDate: '',
            description: '',
            isActive: true
        });
    };

    const filteredTemplates = templates.filter(template => {
        const typeMatch = filterType === 'all' || template.type === filterType;
        const statusMatch = filterStatus === 'all' || 
            (filterStatus === 'active' && template.isActive) ||
            (filterStatus === 'inactive' && !template.isActive);
        return typeMatch && statusMatch;
    });

    const getPatternBadgeColor = (pattern) => {
        const colors = {
            DAILY: '#ef4444',
            WEEKLY: '#f59e0b',
            MONTHLY: '#10b981',
            QUARTERLY: '#3b82f6',
            YEARLY: '#8b5cf6'
        };
        return colors[pattern] || '#6b7280';
    };

    if (loading) {
        return <div className="loading-container">Loading recurring templates...</div>;
    }

    return (
        <div className="recurring-container">
            <div className="recurring-header">
                <div>
                    <h1>Recurring Transactions</h1>
                    <p className="subtitle">Manage automated recurring expenses and income</p>
                </div>
                <button
                    onClick={() => {
                        setEditingTemplate(null);
                        resetForm();
                        setShowModal(true);
                    }}
                    className="btn-primary"
                >
                    <Plus size={18} /> Add Recurring Template
                </button>
            </div>

            {/* Filters */}
            <div className="filters-section">
                <div className="filter-group">
                    <label>Type:</label>
                    <select value={filterType} onChange={(e) => setFilterType(e.target.value)}>
                        <option value="all">All</option>
                        <option value="EXPENSE">Expenses</option>
                        <option value="INCOME">Income</option>
                    </select>
                </div>
                <div className="filter-group">
                    <label>Status:</label>
                    <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
                        <option value="all">All</option>
                        <option value="active">Active</option>
                        <option value="inactive">Inactive</option>
                    </select>
                </div>
                <div className="filter-stats">
                    Total: {filteredTemplates.length} | Active: {filteredTemplates.filter(t => t.isActive).length}
                </div>
            </div>

            {/* Templates Grid */}
            <div className="templates-grid">
                {filteredTemplates.length === 0 ? (
                    <div className="empty-state">
                        <RefreshCw size={48} color="#ccc" />
                        <h3>No recurring templates found</h3>
                        <p>Create your first recurring template to automate your finances</p>
                    </div>
                ) : (
                    filteredTemplates.map(template => (
                        <div key={template.id} className={`template-card ${!template.isActive ? 'inactive' : ''}`}>
                            <div className="template-header">
                                <div className="template-title">
                                    <h3>{template.name}</h3>
                                    <span className={`type-badge ${template.type.toLowerCase()}`}>
                                        {template.type}
                                    </span>
                                </div>
                                <div className="template-actions">
                                    <button
                                        onClick={() => handleToggleActive(template.id)}
                                        className="icon-btn"
                                        title={template.isActive ? 'Deactivate' : 'Activate'}
                                    >
                                        {template.isActive ? 
                                            <ToggleRight size={20} color="#10b981" /> : 
                                            <ToggleLeft size={20} color="#9ca3af" />
                                        }
                                    </button>
                                    <button onClick={() => handleEdit(template)} className="icon-btn">
                                        <Edit size={16} />
                                    </button>
                                    <button onClick={() => handleDelete(template.id)} className="icon-btn danger">
                                        <Trash2 size={16} />
                                    </button>
                                </div>
                            </div>

                            <div className="template-body">
                                <div className="template-amount">
                                    <span>{formatCurrency(template.amount)}</span>
                                </div>
                                
                                <div className="template-details">
                                    <div className="detail-item">
                                        <span className="detail-label">Category:</span>
                                        <span>{template.type === 'EXPENSE' ? 
                                            (template.customCategoryName || template.category) : 
                                            template.source
                                        }</span>
                                    </div>
                                    <div className="detail-item">
                                        <span className="detail-label">Frequency:</span>
                                        <span 
                                            className="pattern-badge"
                                            style={{ backgroundColor: getPatternBadgeColor(template.pattern) }}
                                        >
                                            {template.pattern}
                                        </span>
                                    </div>
                                    <div className="detail-item">
                                        <span className="detail-label">Start Date:</span>
                                        <span><Calendar size={14} /> {template.startDate}</span>
                                    </div>
                                    {template.endDate && (
                                        <div className="detail-item">
                                            <span className="detail-label">End Date:</span>
                                            <span><Calendar size={14} /> {template.endDate}</span>
                                        </div>
                                    )}
                                    {template.lastGenerated && (
                                        <div className="detail-item">
                                            <span className="detail-label">Last Generated:</span>
                                            <span>{template.lastGenerated}</span>
                                        </div>
                                    )}
                                </div>

                                {template.description && (
                                    <p className="template-description">{template.description}</p>
                                )}
                            </div>
                        </div>
                    ))
                )}
            </div>

            {/* Modal */}
            {showModal && (
                <div className="modal-overlay" onClick={() => setShowModal(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>{editingTemplate ? 'Edit' : 'Add'} Recurring Template</h2>
                            <button className="close-button" onClick={() => setShowModal(false)}>×</button>
                        </div>

                        <form onSubmit={handleSubmit}>
                            <div className="modal-body">
                                <div className="form-group">
                                    <label>Template Name *</label>
                                    <input
                                        type="text"
                                        name="name"
                                        value={formData.name}
                                        onChange={handleInputChange}
                                        placeholder="e.g., Monthly Rent, Weekly Groceries"
                                        required
                                    />
                                </div>

                                <div className="form-row">
                                    <div className="form-group">
                                        <label>Type *</label>
                                        <select name="type" value={formData.type} onChange={handleInputChange}>
                                            <option value="EXPENSE">Expense</option>
                                            <option value="INCOME">Income</option>
                                        </select>
                                    </div>

                                    <div className="form-group">
                                        <label>Amount *</label>
                                        <input
                                            type="number"
                                            name="amount"
                                            value={formData.amount}
                                            onChange={handleInputChange}
                                            placeholder="0.00"
                                            step="0.01"
                                            min="0"
                                            required
                                        />
                                    </div>
                                </div>

                                {formData.type === 'EXPENSE' ? (
                                    <div className="form-group">
                                        <label>Category *</label>
                                        <select name="category" value={formData.category} onChange={handleInputChange}>
                                            <option value="FOOD">Food</option>
                                            <option value="TRANSPORT">Transport</option>
                                            <option value="ENTERTAINMENT">Entertainment</option>
                                            <option value="UTILITIES">Utilities</option>
                                            <option value="HEALTHCARE">Healthcare</option>
                                            <option value="EDUCATION">Education</option>
                                            <option value="SHOPPING">Shopping</option>
                                            <option value="INSURANCE">Insurance</option>
                                            <option value="SAVINGS">Savings</option>
                                            <option value="OTHERS">Others</option>
                                        </select>
                                    </div>
                                ) : (
                                    <div className="form-group">
                                        <label>Source *</label>
                                        <input
                                            type="text"
                                            name="source"
                                            value={formData.source}
                                            onChange={handleInputChange}
                                            placeholder="e.g., Salary, Freelance"
                                            required
                                        />
                                    </div>
                                )}

                                <div className="form-group">
                                    <label>Frequency *</label>
                                    <select name="pattern" value={formData.pattern} onChange={handleInputChange}>
                                        <option value="DAILY">Daily</option>
                                        <option value="WEEKLY">Weekly</option>
                                        <option value="MONTHLY">Monthly</option>
                                        <option value="QUARTERLY">Quarterly</option>
                                        <option value="YEARLY">Yearly</option>
                                    </select>
                                </div>

                                <div className="form-row">
                                    <div className="form-group">
                                        <label>Start Date *</label>
                                        <input
                                            type="date"
                                            name="startDate"
                                            value={formData.startDate}
                                            onChange={handleInputChange}
                                            required
                                        />
                                    </div>

                                    <div className="form-group">
                                        <label>End Date (Optional)</label>
                                        <input
                                            type="date"
                                            name="endDate"
                                            value={formData.endDate}
                                            onChange={handleInputChange}
                                        />
                                    </div>
                                </div>

                                <div className="form-group">
                                    <label>Description (Optional)</label>
                                    <textarea
                                        name="description"
                                        value={formData.description}
                                        onChange={handleInputChange}
                                        placeholder="Additional notes about this recurring transaction"
                                        rows="3"
                                    />
                                </div>

                                <div className="form-group">
                                    <label className="checkbox-label">
                                        <input
                                            type="checkbox"
                                            name="isActive"
                                            checked={formData.isActive}
                                            onChange={handleInputChange}
                                        />
                                        <span>Active (generate transactions automatically)</span>
                                    </label>
                                </div>
                            </div>

                            <div className="modal-footer">
                                <button
                                    type="button"
                                    onClick={() => setShowModal(false)}
                                    className="btn-secondary"
                                    disabled={submitting}
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    className="btn-primary"
                                    disabled={submitting}
                                >
                                    {submitting ? 'Saving...' : editingTemplate ? 'Update' : 'Create'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default RecurringTransactions;
