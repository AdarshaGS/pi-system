import { useState, useEffect } from 'react';
import { Wallet, TrendingUp, Calendar, Plus, Edit2, X } from 'lucide-react';
import './Banking.css';

const Banking = () => {
    const [activeTab, setActiveTab] = useState('fd'); // fd, rd, savings
    const [user] = useState(() => JSON.parse(localStorage.getItem('user')));
    const [loading, setLoading] = useState(false);
    const [showModal, setShowModal] = useState(false);
    const [editingItem, setEditingItem] = useState(null);

    // Fixed Deposits State
    const [fixedDeposits, setFixedDeposits] = useState([]);
    const [fdForm, setFdForm] = useState({
        bankName: '',
        accountNumber: '',
        depositAmount: '',
        interestRate: '',
        tenureMonths: '',
        startDate: new Date().toISOString().split('T')[0],
        maturityAmount: '',
        autoRenew: false,
        nomineeDetails: ''
    });

    // Recurring Deposits State
    const [recurringDeposits, setRecurringDeposits] = useState([]);
    const [rdForm, setRdForm] = useState({
        bankName: '',
        accountNumber: '',
        monthlyDepositAmount: '',
        interestRate: '',
        tenureMonths: '',
        startDate: new Date().toISOString().split('T')[0],
        maturityAmount: '',
        autoRenew: false,
        nomineeDetails: ''
    });

    // Savings Accounts State (placeholder for future)
    const [savingsAccounts, setSavingsAccounts] = useState([]);

    useEffect(() => {
        if (activeTab === 'fd') {
            loadFixedDeposits();
        } else if (activeTab === 'rd') {
            loadRecurringDeposits();
        }
    }, [activeTab]);

    // Fixed Deposit Functions
    const loadFixedDeposits = async () => {
        setLoading(true);
        try {
            const response = await fetch(`http://localhost:8082/api/v1/fixed-deposit?userId=${user.userId}`, {
                headers: { 'Authorization': `Bearer ${user.token}` }
            });
            if (response.ok) {
                const data = await response.json();
                setFixedDeposits(data || []);
            }
        } catch (error) {
            console.error('Error loading FDs:', error);
        } finally {
            setLoading(false);
        }
    };

    const calculateFDMaturity = (principal, rate, months) => {
        const r = rate / 100;
        const n = 4; // Quarterly compounding
        const t = months / 12;
        return principal * Math.pow((1 + r / n), n * t);
    };

    const handleFDFormChange = (e) => {
        const { name, value, type, checked } = e.target;
        const newValue = type === 'checkbox' ? checked : value;
        
        const updatedForm = { ...fdForm, [name]: newValue };
        
        // Auto-calculate maturity amount
        if (updatedForm.depositAmount && updatedForm.interestRate && updatedForm.tenureMonths) {
            const maturity = calculateFDMaturity(
                parseFloat(updatedForm.depositAmount),
                parseFloat(updatedForm.interestRate),
                parseInt(updatedForm.tenureMonths)
            );
            updatedForm.maturityAmount = maturity.toFixed(2);
        }
        
        setFdForm(updatedForm);
    };

    const handleFDSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const url = editingItem 
                ? `http://localhost:8082/api/v1/fixed-deposit/${editingItem.id}`
                : `http://localhost:8082/api/v1/fixed-deposit`;
            
            const method = editingItem ? 'PUT' : 'POST';
            
            const response = await fetch(url, {
                method,
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${user.token}`
                },
                body: JSON.stringify({ ...fdForm, userId: user.userId })
            });

            if (response.ok) {
                alert(`Fixed Deposit ${editingItem ? 'updated' : 'created'} successfully!`);
                setShowModal(false);
                setEditingItem(null);
                resetFDForm();
                loadFixedDeposits();
            } else {
                alert('Failed to save Fixed Deposit');
            }
        } catch (error) {
            console.error('Error saving FD:', error);
            alert('Error saving Fixed Deposit');
        } finally {
            setLoading(false);
        }
    };

    const resetFDForm = () => {
        setFdForm({
            bankName: '',
            accountNumber: '',
            depositAmount: '',
            interestRate: '',
            tenureMonths: '',
            startDate: new Date().toISOString().split('T')[0],
            maturityAmount: '',
            autoRenew: false,
            nomineeDetails: ''
        });
    };

    // Recurring Deposit Functions
    const loadRecurringDeposits = async () => {
        setLoading(true);
        try {
            const response = await fetch(`http://localhost:8082/api/v1/recurring-deposit?userId=${user.userId}`, {
                headers: { 'Authorization': `Bearer ${user.token}` }
            });
            if (response.ok) {
                const data = await response.json();
                setRecurringDeposits(data || []);
            }
        } catch (error) {
            console.error('Error loading RDs:', error);
        } finally {
            setLoading(false);
        }
    };

    const calculateRDMaturity = (monthlyDeposit, rate, months) => {
        const r = rate / (12 * 100);
        const n = months;
        // RD Maturity = P * n + P * n * (n + 1) * r / (2 * 12)
        const maturity = (monthlyDeposit * n) + (monthlyDeposit * n * (n + 1) * (rate / 100)) / (2 * 12);
        return maturity;
    };

    const handleRDFormChange = (e) => {
        const { name, value, type, checked } = e.target;
        const newValue = type === 'checkbox' ? checked : value;
        
        const updatedForm = { ...rdForm, [name]: newValue };
        
        // Auto-calculate maturity amount
        if (updatedForm.monthlyDepositAmount && updatedForm.interestRate && updatedForm.tenureMonths) {
            const maturity = calculateRDMaturity(
                parseFloat(updatedForm.monthlyDepositAmount),
                parseFloat(updatedForm.interestRate),
                parseInt(updatedForm.tenureMonths)
            );
            updatedForm.maturityAmount = maturity.toFixed(2);
        }
        
        setRdForm(updatedForm);
    };

    const handleRDSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const url = editingItem 
                ? `http://localhost:8082/api/v1/recurring-deposit/${editingItem.id}`
                : `http://localhost:8082/api/v1/recurring-deposit`;
            
            const method = editingItem ? 'PUT' : 'POST';
            
            const response = await fetch(url, {
                method,
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${user.token}`
                },
                body: JSON.stringify({ ...rdForm, userId: user.userId })
            });

            if (response.ok) {
                alert(`Recurring Deposit ${editingItem ? 'updated' : 'created'} successfully!`);
                setShowModal(false);
                setEditingItem(null);
                resetRDForm();
                loadRecurringDeposits();
            } else {
                alert('Failed to save Recurring Deposit');
            }
        } catch (error) {
            console.error('Error saving RD:', error);
            alert('Error saving Recurring Deposit');
        } finally {
            setLoading(false);
        }
    };

    const resetRDForm = () => {
        setRdForm({
            bankName: '',
            accountNumber: '',
            monthlyDepositAmount: '',
            interestRate: '',
            tenureMonths: '',
            startDate: new Date().toISOString().split('T')[0],
            maturityAmount: '',
            autoRenew: false,
            nomineeDetails: ''
        });
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            maximumFractionDigits: 0
        }).format(amount || 0);
    };

    const formatDate = (dateString) => {
        if (!dateString) return '-';
        return new Date(dateString).toLocaleDateString('en-IN', {
            day: 'numeric',
            month: 'short',
            year: 'numeric'
        });
    };

    const calculateMaturityDate = (startDate, months) => {
        const date = new Date(startDate);
        date.setMonth(date.getMonth() + months);
        return date;
    };

    const getDaysToMaturity = (maturityDate) => {
        const today = new Date();
        const maturity = new Date(maturityDate);
        const diff = maturity - today;
        return Math.ceil(diff / (1000 * 60 * 60 * 24));
    };

    const handleEdit = (item) => {
        setEditingItem(item);
        if (activeTab === 'fd') {
            setFdForm(item);
        } else if (activeTab === 'rd') {
            setRdForm(item);
        }
        setShowModal(true);
    };

    const handleCloseModal = () => {
        setShowModal(false);
        setEditingItem(null);
        if (activeTab === 'fd') {
            resetFDForm();
        } else if (activeTab === 'rd') {
            resetRDForm();
        }
    };

    return (
        <div className="banking-container">
            <div className="banking-header">
                <h1>Banking & Deposits</h1>
                <button 
                    className="btn-primary"
                    onClick={() => setShowModal(true)}
                    style={{
                        background: '#4f46e5',
                        color: 'white',
                        border: 'none',
                        padding: '10px 20px',
                        borderRadius: '8px',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '8px',
                        cursor: 'pointer',
                        fontSize: '15px',
                        fontWeight: '600'
                    }}
                >
                    <Plus size={20} />
                    Add {activeTab === 'fd' ? 'Fixed Deposit' : activeTab === 'rd' ? 'Recurring Deposit' : 'Account'}
                </button>
            </div>

            {/* Tabs */}
            <div className="banking-tabs" style={{ display: 'flex', gap: '8px', marginBottom: '24px', borderBottom: '2px solid #e5e7eb' }}>
                <button
                    className={activeTab === 'fd' ? 'tab-active' : 'tab-inactive'}
                    onClick={() => setActiveTab('fd')}
                    style={{
                        padding: '12px 24px',
                        border: 'none',
                        background: 'none',
                        cursor: 'pointer',
                        fontSize: '15px',
                        fontWeight: '600',
                        borderBottom: activeTab === 'fd' ? '3px solid #4f46e5' : '3px solid transparent',
                        color: activeTab === 'fd' ? '#4f46e5' : '#6b7280'
                    }}
                >
                    <TrendingUp size={18} style={{ display: 'inline', marginRight: '8px' }} />
                    Fixed Deposits
                </button>
                <button
                    className={activeTab === 'rd' ? 'tab-active' : 'tab-inactive'}
                    onClick={() => setActiveTab('rd')}
                    style={{
                        padding: '12px 24px',
                        border: 'none',
                        background: 'none',
                        cursor: 'pointer',
                        fontSize: '15px',
                        fontWeight: '600',
                        borderBottom: activeTab === 'rd' ? '3px solid #4f46e5' : '3px solid transparent',
                        color: activeTab === 'rd' ? '#4f46e5' : '#6b7280'
                    }}
                >
                    <Calendar size={18} style={{ display: 'inline', marginRight: '8px' }} />
                    Recurring Deposits
                </button>
                <button
                    className={activeTab === 'savings' ? 'tab-active' : 'tab-inactive'}
                    onClick={() => setActiveTab('savings')}
                    style={{
                        padding: '12px 24px',
                        border: 'none',
                        background: 'none',
                        cursor: 'pointer',
                        fontSize: '15px',
                        fontWeight: '600',
                        borderBottom: activeTab === 'savings' ? '3px solid #4f46e5' : '3px solid transparent',
                        color: activeTab === 'savings' ? '#4f46e5' : '#6b7280'
                    }}
                >
                    <Wallet size={18} style={{ display: 'inline', marginRight: '8px' }} />
                    Savings Accounts
                </button>
            </div>

            {/* Content */}
            {loading && <div style={{ textAlign: 'center', padding: '40px' }}>Loading...</div>}

            {/* Fixed Deposits Tab */}
            {activeTab === 'fd' && !loading && (
                <div className="deposits-list">
                    {fixedDeposits.length === 0 ? (
                        <div style={{ 
                            textAlign: 'center', 
                            padding: '60px 20px', 
                            background: '#f9fafb', 
                            borderRadius: '12px',
                            color: '#6b7280'
                        }}>
                            <TrendingUp size={48} style={{ marginBottom: '16px', opacity: 0.5 }} />
                            <p style={{ fontSize: '18px', marginBottom: '8px' }}>No Fixed Deposits Yet</p>
                            <p>Start tracking your Fixed Deposits to monitor maturity and returns</p>
                        </div>
                    ) : (
                        <div style={{ display: 'grid', gap: '16px' }}>
                            {fixedDeposits.map((fd) => {
                                const maturityDate = calculateMaturityDate(fd.startDate, fd.tenureMonths);
                                const daysToMaturity = getDaysToMaturity(maturityDate);
                                const isMaturing = daysToMaturity <= 30 && daysToMaturity > 0;
                                
                                return (
                                    <div key={fd.id} style={{
                                        background: 'white',
                                        border: '1px solid #e5e7eb',
                                        borderRadius: '12px',
                                        padding: '20px',
                                        borderLeft: isMaturing ? '4px solid #fbbf24' : '4px solid #4f46e5'
                                    }}>
                                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', marginBottom: '16px' }}>
                                            <div>
                                                <h3 style={{ margin: '0 0 4px 0', fontSize: '18px', fontWeight: '600' }}>
                                                    {fd.bankName}
                                                </h3>
                                                <p style={{ margin: 0, color: '#6b7280', fontSize: '14px' }}>
                                                    A/C: {fd.accountNumber}
                                                </p>
                                            </div>
                                            <button
                                                onClick={() => handleEdit(fd)}
                                                style={{
                                                    background: 'none',
                                                    border: 'none',
                                                    cursor: 'pointer',
                                                    padding: '8px',
                                                    borderRadius: '6px',
                                                    display: 'flex',
                                                    alignItems: 'center'
                                                }}
                                            >
                                                <Edit2 size={18} color="#4f46e5" />
                                            </button>
                                        </div>

                                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px', marginBottom: '16px' }}>
                                            <div>
                                                <div style={{ fontSize: '13px', color: '#6b7280', marginBottom: '4px' }}>Deposit Amount</div>
                                                <div style={{ fontSize: '20px', fontWeight: '600', color: '#4f46e5' }}>
                                                    {formatCurrency(fd.depositAmount)}
                                                </div>
                                            </div>
                                            <div>
                                                <div style={{ fontSize: '13px', color: '#6b7280', marginBottom: '4px' }}>Interest Rate</div>
                                                <div style={{ fontSize: '20px', fontWeight: '600' }}>{fd.interestRate}% p.a.</div>
                                            </div>
                                            <div>
                                                <div style={{ fontSize: '13px', color: '#6b7280', marginBottom: '4px' }}>Tenure</div>
                                                <div style={{ fontSize: '20px', fontWeight: '600' }}>{fd.tenureMonths} months</div>
                                            </div>
                                            <div>
                                                <div style={{ fontSize: '13px', color: '#6b7280', marginBottom: '4px' }}>Maturity Amount</div>
                                                <div style={{ fontSize: '20px', fontWeight: '600', color: '#10b981' }}>
                                                    {formatCurrency(fd.maturityAmount)}
                                                </div>
                                            </div>
                                        </div>

                                        <div style={{ display: 'flex', justifyContent: 'space-between', paddingTop: '16px', borderTop: '1px solid #e5e7eb' }}>
                                            <div>
                                                <span style={{ fontSize: '13px', color: '#6b7280' }}>Start Date: </span>
                                                <span style={{ fontSize: '14px', fontWeight: '500' }}>{formatDate(fd.startDate)}</span>
                                            </div>
                                            <div>
                                                <span style={{ fontSize: '13px', color: '#6b7280' }}>Maturity Date: </span>
                                                <span style={{ fontSize: '14px', fontWeight: '500' }}>{formatDate(maturityDate)}</span>
                                            </div>
                                            {isMaturing && (
                                                <div style={{
                                                    padding: '4px 12px',
                                                    background: '#fef3c7',
                                                    color: '#92400e',
                                                    borderRadius: '6px',
                                                    fontSize: '13px',
                                                    fontWeight: '500'
                                                }}>
                                                    ⚠️ Maturing in {daysToMaturity} days
                                                </div>
                                            )}
                                        </div>

                                        {fd.nomineeDetails && (
                                            <div style={{ marginTop: '12px', padding: '8px 12px', background: '#f3f4f6', borderRadius: '6px', fontSize: '13px' }}>
                                                <strong>Nominee:</strong> {fd.nomineeDetails}
                                            </div>
                                        )}
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </div>
            )}

            {/* Recurring Deposits Tab */}
            {activeTab === 'rd' && !loading && (
                <div className="deposits-list">
                    {recurringDeposits.length === 0 ? (
                        <div style={{ 
                            textAlign: 'center', 
                            padding: '60px 20px', 
                            background: '#f9fafb', 
                            borderRadius: '12px',
                            color: '#6b7280'
                        }}>
                            <Calendar size={48} style={{ marginBottom: '16px', opacity: 0.5 }} />
                            <p style={{ fontSize: '18px', marginBottom: '8px' }}>No Recurring Deposits Yet</p>
                            <p>Start tracking your RDs to monitor monthly deposits and maturity</p>
                        </div>
                    ) : (
                        <div style={{ display: 'grid', gap: '16px' }}>
                            {recurringDeposits.map((rd) => {
                                const maturityDate = calculateMaturityDate(rd.startDate, rd.tenureMonths);
                                const daysToMaturity = getDaysToMaturity(maturityDate);
                                const isMaturing = daysToMaturity <= 30 && daysToMaturity > 0;
                                const totalDeposited = rd.monthlyDepositAmount * rd.tenureMonths;
                                
                                return (
                                    <div key={rd.id} style={{
                                        background: 'white',
                                        border: '1px solid #e5e7eb',
                                        borderRadius: '12px',
                                        padding: '20px',
                                        borderLeft: isMaturing ? '4px solid #fbbf24' : '4px solid #10b981'
                                    }}>
                                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', marginBottom: '16px' }}>
                                            <div>
                                                <h3 style={{ margin: '0 0 4px 0', fontSize: '18px', fontWeight: '600' }}>
                                                    {rd.bankName}
                                                </h3>
                                                <p style={{ margin: 0, color: '#6b7280', fontSize: '14px' }}>
                                                    A/C: {rd.accountNumber}
                                                </p>
                                            </div>
                                            <button
                                                onClick={() => handleEdit(rd)}
                                                style={{
                                                    background: 'none',
                                                    border: 'none',
                                                    cursor: 'pointer',
                                                    padding: '8px',
                                                    borderRadius: '6px',
                                                    display: 'flex',
                                                    alignItems: 'center'
                                                }}
                                            >
                                                <Edit2 size={18} color="#10b981" />
                                            </button>
                                        </div>

                                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '16px', marginBottom: '16px' }}>
                                            <div>
                                                <div style={{ fontSize: '13px', color: '#6b7280', marginBottom: '4px' }}>Monthly Deposit</div>
                                                <div style={{ fontSize: '20px', fontWeight: '600', color: '#10b981' }}>
                                                    {formatCurrency(rd.monthlyDepositAmount)}
                                                </div>
                                            </div>
                                            <div>
                                                <div style={{ fontSize: '13px', color: '#6b7280', marginBottom: '4px' }}>Interest Rate</div>
                                                <div style={{ fontSize: '20px', fontWeight: '600' }}>{rd.interestRate}% p.a.</div>
                                            </div>
                                            <div>
                                                <div style={{ fontSize: '13px', color: '#6b7280', marginBottom: '4px' }}>Tenure</div>
                                                <div style={{ fontSize: '20px', fontWeight: '600' }}>{rd.tenureMonths} months</div>
                                            </div>
                                            <div>
                                                <div style={{ fontSize: '13px', color: '#6b7280', marginBottom: '4px' }}>Total Deposited</div>
                                                <div style={{ fontSize: '18px', fontWeight: '600' }}>
                                                    {formatCurrency(totalDeposited)}
                                                </div>
                                            </div>
                                            <div>
                                                <div style={{ fontSize: '13px', color: '#6b7280', marginBottom: '4px' }}>Maturity Amount</div>
                                                <div style={{ fontSize: '20px', fontWeight: '600', color: '#4f46e5' }}>
                                                    {formatCurrency(rd.maturityAmount)}
                                                </div>
                                            </div>
                                        </div>

                                        <div style={{ display: 'flex', justifyContent: 'space-between', paddingTop: '16px', borderTop: '1px solid #e5e7eb' }}>
                                            <div>
                                                <span style={{ fontSize: '13px', color: '#6b7280' }}>Start Date: </span>
                                                <span style={{ fontSize: '14px', fontWeight: '500' }}>{formatDate(rd.startDate)}</span>
                                            </div>
                                            <div>
                                                <span style={{ fontSize: '13px', color: '#6b7280' }}>Maturity Date: </span>
                                                <span style={{ fontSize: '14px', fontWeight: '500' }}>{formatDate(maturityDate)}</span>
                                            </div>
                                            {isMaturing && (
                                                <div style={{
                                                    padding: '4px 12px',
                                                    background: '#fef3c7',
                                                    color: '#92400e',
                                                    borderRadius: '6px',
                                                    fontSize: '13px',
                                                    fontWeight: '500'
                                                }}>
                                                    ⚠️ Maturing in {daysToMaturity} days
                                                </div>
                                            )}
                                        </div>

                                        {rd.nomineeDetails && (
                                            <div style={{ marginTop: '12px', padding: '8px 12px', background: '#f3f4f6', borderRadius: '6px', fontSize: '13px' }}>
                                                <strong>Nominee:</strong> {rd.nomineeDetails}
                                            </div>
                                        )}
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </div>
            )}

            {/* Savings Accounts Tab */}
            {activeTab === 'savings' && !loading && (
                <div style={{ 
                    textAlign: 'center', 
                    padding: '80px 20px', 
                    background: '#f9fafb', 
                    borderRadius: '12px',
                    color: '#6b7280'
                }}>
                    <Wallet size={64} style={{ marginBottom: '20px', opacity: 0.5 }} />
                    <h3 style={{ fontSize: '20px', marginBottom: '12px', color: '#374151' }}>Savings Accounts Coming Soon</h3>
                    <p>This feature is under development and will be available soon</p>
                </div>
            )}

            {/* Add/Edit Modal */}
            {showModal && (activeTab === 'fd' || activeTab === 'rd') && (
                <div style={{
                    position: 'fixed',
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                    background: 'rgba(0, 0, 0, 0.5)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    zIndex: 1000
                }}>
                    <div style={{
                        background: 'white',
                        borderRadius: '12px',
                        width: '90%',
                        maxWidth: '600px',
                        maxHeight: '90vh',
                        overflow: 'auto',
                        boxShadow: '0 4px 20px rgba(0, 0, 0, 0.15)'
                    }}>
                        <div style={{
                            padding: '20px 24px',
                            borderBottom: '1px solid #e5e7eb',
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center'
                        }}>
                            <h2 style={{ margin: 0, fontSize: '20px', fontWeight: '600' }}>
                                {editingItem ? 'Edit' : 'Add'} {activeTab === 'fd' ? 'Fixed Deposit' : 'Recurring Deposit'}
                            </h2>
                            <button
                                onClick={handleCloseModal}
                                style={{
                                    background: 'none',
                                    border: 'none',
                                    cursor: 'pointer',
                                    padding: '4px'
                                }}
                            >
                                <X size={24} color="#666" />
                            </button>
                        </div>

                        <form onSubmit={activeTab === 'fd' ? handleFDSubmit : handleRDSubmit} style={{ padding: '24px' }}>
                            <div style={{ display: 'grid', gap: '20px' }}>
                                <div>
                                    <label style={{ display: 'block', marginBottom: '6px', fontWeight: '500', fontSize: '14px' }}>
                                        Bank Name <span style={{ color: '#ef4444' }}>*</span>
                                    </label>
                                    <input
                                        type="text"
                                        name="bankName"
                                        value={activeTab === 'fd' ? fdForm.bankName : rdForm.bankName}
                                        onChange={activeTab === 'fd' ? handleFDFormChange : handleRDFormChange}
                                        required
                                        style={{
                                            width: '100%',
                                            padding: '10px 12px',
                                            border: '1px solid #d1d5db',
                                            borderRadius: '6px',
                                            fontSize: '14px',
                                            boxSizing: 'border-box'
                                        }}
                                    />
                                </div>

                                <div>
                                    <label style={{ display: 'block', marginBottom: '6px', fontWeight: '500', fontSize: '14px' }}>
                                        Account Number <span style={{ color: '#ef4444' }}>*</span>
                                    </label>
                                    <input
                                        type="text"
                                        name="accountNumber"
                                        value={activeTab === 'fd' ? fdForm.accountNumber : rdForm.accountNumber}
                                        onChange={activeTab === 'fd' ? handleFDFormChange : handleRDFormChange}
                                        required
                                        style={{
                                            width: '100%',
                                            padding: '10px 12px',
                                            border: '1px solid #d1d5db',
                                            borderRadius: '6px',
                                            fontSize: '14px',
                                            boxSizing: 'border-box'
                                        }}
                                    />
                                </div>

                                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                                    <div>
                                        <label style={{ display: 'block', marginBottom: '6px', fontWeight: '500', fontSize: '14px' }}>
                                            {activeTab === 'fd' ? 'Deposit Amount' : 'Monthly Deposit'} (₹) <span style={{ color: '#ef4444' }}>*</span>
                                        </label>
                                        <input
                                            type="number"
                                            name={activeTab === 'fd' ? 'depositAmount' : 'monthlyDepositAmount'}
                                            value={activeTab === 'fd' ? fdForm.depositAmount : rdForm.monthlyDepositAmount}
                                            onChange={activeTab === 'fd' ? handleFDFormChange : handleRDFormChange}
                                            required
                                            min="0"
                                            step="0.01"
                                            style={{
                                                width: '100%',
                                                padding: '10px 12px',
                                                border: '1px solid #d1d5db',
                                                borderRadius: '6px',
                                                fontSize: '14px',
                                                boxSizing: 'border-box'
                                            }}
                                        />
                                    </div>
                                    <div>
                                        <label style={{ display: 'block', marginBottom: '6px', fontWeight: '500', fontSize: '14px' }}>
                                            Interest Rate (% p.a.) <span style={{ color: '#ef4444' }}>*</span>
                                        </label>
                                        <input
                                            type="number"
                                            name="interestRate"
                                            value={activeTab === 'fd' ? fdForm.interestRate : rdForm.interestRate}
                                            onChange={activeTab === 'fd' ? handleFDFormChange : handleRDFormChange}
                                            required
                                            min="0"
                                            max="100"
                                            step="0.01"
                                            style={{
                                                width: '100%',
                                                padding: '10px 12px',
                                                border: '1px solid #d1d5db',
                                                borderRadius: '6px',
                                                fontSize: '14px',
                                                boxSizing: 'border-box'
                                            }}
                                        />
                                    </div>
                                </div>

                                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                                    <div>
                                        <label style={{ display: 'block', marginBottom: '6px', fontWeight: '500', fontSize: '14px' }}>
                                            Tenure (Months) <span style={{ color: '#ef4444' }}>*</span>
                                        </label>
                                        <input
                                            type="number"
                                            name="tenureMonths"
                                            value={activeTab === 'fd' ? fdForm.tenureMonths : rdForm.tenureMonths}
                                            onChange={activeTab === 'fd' ? handleFDFormChange : handleRDFormChange}
                                            required
                                            min="1"
                                            style={{
                                                width: '100%',
                                                padding: '10px 12px',
                                                border: '1px solid #d1d5db',
                                                borderRadius: '6px',
                                                fontSize: '14px',
                                                boxSizing: 'border-box'
                                            }}
                                        />
                                    </div>
                                    <div>
                                        <label style={{ display: 'block', marginBottom: '6px', fontWeight: '500', fontSize: '14px' }}>
                                            Start Date <span style={{ color: '#ef4444' }}>*</span>
                                        </label>
                                        <input
                                            type="date"
                                            name="startDate"
                                            value={activeTab === 'fd' ? fdForm.startDate : rdForm.startDate}
                                            onChange={activeTab === 'fd' ? handleFDFormChange : handleRDFormChange}
                                            required
                                            style={{
                                                width: '100%',
                                                padding: '10px 12px',
                                                border: '1px solid #d1d5db',
                                                borderRadius: '6px',
                                                fontSize: '14px',
                                                boxSizing: 'border-box'
                                            }}
                                        />
                                    </div>
                                </div>

                                <div style={{
                                    padding: '16px',
                                    background: '#f0fdf4',
                                    border: '1px solid #86efac',
                                    borderRadius: '8px'
                                }}>
                                    <div style={{ fontSize: '13px', color: '#166534', marginBottom: '4px' }}>
                                        Estimated Maturity Amount
                                    </div>
                                    <div style={{ fontSize: '24px', fontWeight: '700', color: '#15803d' }}>
                                        {formatCurrency((activeTab === 'fd' ? fdForm.maturityAmount : rdForm.maturityAmount) || 0)}
                                    </div>
                                </div>

                                <div>
                                    <label style={{ display: 'block', marginBottom: '6px', fontWeight: '500', fontSize: '14px' }}>
                                        Nominee Details (Optional)
                                    </label>
                                    <input
                                        type="text"
                                        name="nomineeDetails"
                                        value={activeTab === 'fd' ? fdForm.nomineeDetails : rdForm.nomineeDetails}
                                        onChange={activeTab === 'fd' ? handleFDFormChange : handleRDFormChange}
                                        placeholder="Enter nominee name and relationship"
                                        style={{
                                            width: '100%',
                                            padding: '10px 12px',
                                            border: '1px solid #d1d5db',
                                            borderRadius: '6px',
                                            fontSize: '14px',
                                            boxSizing: 'border-box'
                                        }}
                                    />
                                </div>

                                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                    <input
                                        type="checkbox"
                                        id="autoRenew"
                                        name="autoRenew"
                                        checked={activeTab === 'fd' ? fdForm.autoRenew : rdForm.autoRenew}
                                        onChange={activeTab === 'fd' ? handleFDFormChange : handleRDFormChange}
                                        style={{ width: '18px', height: '18px', cursor: 'pointer' }}
                                    />
                                    <label htmlFor="autoRenew" style={{ fontSize: '14px', cursor: 'pointer' }}>
                                        Auto-renew on maturity
                                    </label>
                                </div>
                            </div>

                            <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end', marginTop: '24px' }}>
                                <button
                                    type="button"
                                    onClick={handleCloseModal}
                                    style={{
                                        padding: '10px 20px',
                                        border: '1px solid #d1d5db',
                                        borderRadius: '6px',
                                        background: 'white',
                                        cursor: 'pointer',
                                        fontSize: '14px',
                                        fontWeight: '500'
                                    }}
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    disabled={loading}
                                    style={{
                                        padding: '10px 20px',
                                        border: 'none',
                                        borderRadius: '6px',
                                        background: '#4f46e5',
                                        color: 'white',
                                        cursor: loading ? 'not-allowed' : 'pointer',
                                        fontSize: '14px',
                                        fontWeight: '500',
                                        opacity: loading ? 0.6 : 1
                                    }}
                                >
                                    {loading ? 'Saving...' : editingItem ? 'Update' : 'Create'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Banking;
