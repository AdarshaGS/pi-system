import { useState, useEffect } from 'react';
import { loansApi } from '../api';
import { LineChart, Line, BarChart, Bar, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import './Loans.css';

const Loans = () => {
    const [user] = useState(() => JSON.parse(localStorage.getItem('user')));
    const [activeTab, setActiveTab] = useState('dashboard'); // dashboard, add, details, calculator, payments, prepayment
    const [loans, setLoans] = useState([]);
    const [loading, setLoading] = useState(false);
    const [selectedLoan, setSelectedLoan] = useState(null);
    const [filters, setFilters] = useState({
        loanType: 'ALL',
        status: 'ALL',
        searchTerm: ''
    });

    // Form states
    const [loanForm, setLoanForm] = useState({
        loanType: 'HOME',
        provider: '',
        loanAccountNumber: '',
        principalAmount: '',
        interestRate: '',
        tenureMonths: '',
        startDate: new Date().toISOString().split('T')[0],
        emiAmount: ''
    });

    const [paymentForm, setPaymentForm] = useState({
        loanId: '',
        paymentDate: new Date().toISOString().split('T')[0],
        paymentAmount: '',
        paymentType: 'EMI',
        paymentMethod: 'NEFT',
        transactionReference: '',
        notes: ''
    });

    const [calculatorInputs, setCalculatorInputs] = useState({
        principal: '5000000',
        rate: '8.5',
        tenure: '240'
    });

    const [prepaymentInputs, setPrepaymentInputs] = useState({
        currentOutstanding: '',
        prepaymentAmount: '',
        currentEMI: '',
        currentRate: ''
    });

    // Data states
    const [amortizationSchedule, setAmortizationSchedule] = useState(null);
    const [loanAnalysis, setLoanAnalysis] = useState(null);
    const [paymentHistory, setPaymentHistory] = useState(null);
    const [prepaymentSimulation, setPrepaymentSimulation] = useState(null);
    const [foreclosureCalculation, setForeclosureCalculation] = useState(null);

    useEffect(() => {
        fetchLoans();
    }, []);

    const fetchLoans = async () => {
        if (!user?.userId || !user?.token) return;
        
        setLoading(true);
        try {
            const data = await loansApi.getUserLoans(user.userId, user.token);
            setLoans(Array.isArray(data) ? data : []);
        } catch (error) {
            console.error('Error fetching loans:', error);
            setLoans([]);
        } finally {
            setLoading(false);
        }
    };

    const calculateEMI = (principal, rate, tenure) => {
        const p = parseFloat(principal);
        const r = parseFloat(rate) / 1200;
        const n = parseFloat(tenure);
        
        if (isNaN(p) || isNaN(r) || isNaN(n) || n === 0) return 0;
        
        const emi = (p * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
        return emi.toFixed(2);
    };

    const handleCalculateEMI = () => {
        const emi = calculateEMI(calculatorInputs.principal, calculatorInputs.rate, calculatorInputs.tenure);
        alert(`Your EMI would be: ₹${Number(emi).toLocaleString('en-IN')}`);
    };

    const handlePreviewEMI = () => {
        const emi = calculateEMI(loanForm.principalAmount, loanForm.interestRate, loanForm.tenureMonths);
        setLoanForm({ ...loanForm, emiAmount: emi });
    };

    const handleCreateLoan = async (e) => {
        e.preventDefault();
        
        try {
            const loanData = {
                ...loanForm,
                userId: user.userId,
                principalAmount: parseFloat(loanForm.principalAmount),
                interestRate: parseFloat(loanForm.interestRate),
                tenureMonths: parseInt(loanForm.tenureMonths),
                emiAmount: loanForm.emiAmount ? parseFloat(loanForm.emiAmount) : null
            };
            
            await loansApi.createLoan(loanData, user.token);
            alert('Loan created successfully!');
            setLoanForm({
                loanType: 'HOME',
                provider: '',
                loanAccountNumber: '',
                principalAmount: '',
                interestRate: '',
                tenureMonths: '',
                startDate: new Date().toISOString().split('T')[0],
                emiAmount: ''
            });
            fetchLoans();
            setActiveTab('dashboard');
        } catch (error) {
            alert('Error creating loan: ' + error.message);
        }
    };

    const handleRecordPayment = async (e) => {
        e.preventDefault();
        
        try {
            const paymentData = {
                ...paymentForm,
                paymentAmount: parseFloat(paymentForm.paymentAmount)
            };
            
            await loansApi.recordPayment(paymentData, user.token);
            alert('Payment recorded successfully!');
            setPaymentForm({
                loanId: selectedLoan?.id || '',
                paymentDate: new Date().toISOString().split('T')[0],
                paymentAmount: '',
                paymentType: 'EMI',
                paymentMethod: 'NEFT',
                transactionReference: '',
                notes: ''
            });
            fetchPaymentHistory(selectedLoan.id);
        } catch (error) {
            alert('Error recording payment: ' + error.message);
        }
    };

    const handleViewDetails = async (loan) => {
        setSelectedLoan(loan);
        setActiveTab('details');
        
        // Fetch detailed data
        try {
            const [schedule, analysis, payments] = await Promise.all([
                loansApi.getAmortizationSchedule(loan.id, user.token),
                loansApi.analyzeLoan(loan.id, user.token),
                loansApi.getPaymentHistory(loan.id, user.token)
            ]);
            
            setAmortizationSchedule(schedule);
            setLoanAnalysis(analysis);
            setPaymentHistory(payments);
        } catch (error) {
            console.error('Error fetching loan details:', error);
        }
    };

    const fetchPaymentHistory = async (loanId) => {
        try {
            const payments = await loansApi.getPaymentHistory(loanId, user.token);
            setPaymentHistory(payments);
        } catch (error) {
            console.error('Error fetching payment history:', error);
        }
    };

    const handleSimulatePrepayment = async () => {
        if (!selectedLoan) return;
        
        const amount = prompt('Enter prepayment amount:');
        if (!amount) return;
        
        try {
            const simulation = await loansApi.simulatePrepayment(selectedLoan.id, parseFloat(amount), user.token);
            setPrepaymentSimulation(simulation);
            alert(`Prepayment Simulation:\n` +
                  `Original Tenure: ${simulation.originalTenureMonths} months\n` +
                  `New Tenure: ${simulation.newTenureMonths} months\n` +
                  `Interest Saved: ₹${Number(simulation.savedInterest).toLocaleString('en-IN')}`);
        } catch (error) {
            alert('Error simulating prepayment: ' + error.message);
        }
    };

    const handleCalculateForeclosure = async () => {
        if (!selectedLoan) return;
        
        const charges = prompt('Enter foreclosure charges percentage (e.g., 2 for 2%):', '2');
        if (charges === null) return;
        
        try {
            const calculation = await loansApi.calculateForeclosure(selectedLoan.id, parseFloat(charges), user.token);
            setForeclosureCalculation(calculation);
            
            const confirmMsg = `Foreclosure Calculation:\n` +
                              `Outstanding Principal: ₹${Number(calculation.outstandingPrincipal).toLocaleString('en-IN')}\n` +
                              `Outstanding Interest: ₹${Number(calculation.outstandingInterest).toLocaleString('en-IN')}\n` +
                              `Foreclosure Charges (${charges}%): ₹${Number(calculation.foreclosureCharges).toLocaleString('en-IN')}\n` +
                              `Total Amount: ₹${Number(calculation.totalForeclosureAmount).toLocaleString('en-IN')}\n\n` +
                              `Do you want to proceed with foreclosure?`;
            
            if (confirm(confirmMsg)) {
                await loansApi.processForeclosure(selectedLoan.id, parseFloat(charges), user.token);
                alert('Loan foreclosed successfully!');
                fetchLoans();
                setActiveTab('dashboard');
            }
        } catch (error) {
            alert('Error calculating foreclosure: ' + error.message);
        }
    };

    const handleDeleteLoan = async (loanId) => {
        if (!confirm('Are you sure you want to delete this loan?')) return;
        
        try {
            await loansApi.deleteLoan(loanId, user.token);
            alert('Loan deleted successfully!');
            fetchLoans();
            if (selectedLoan?.id === loanId) {
                setActiveTab('dashboard');
            }
        } catch (error) {
            alert('Error deleting loan: ' + error.message);
        }
    };

    // Filter loans
    const filteredLoans = loans.filter(loan => {
        if (filters.loanType !== 'ALL' && loan.loanType !== filters.loanType) return false;
        if (filters.status !== 'ALL') {
            const isActive = loan.outstandingAmount > 0;
            if (filters.status === 'ACTIVE' && !isActive) return false;
            if (filters.status === 'CLOSED' && isActive) return false;
        }
        if (filters.searchTerm && !loan.provider?.toLowerCase().includes(filters.searchTerm.toLowerCase())) return false;
        return true;
    });

    // Calculate summary stats
    const summary = {
        totalLoans: filteredLoans.length,
        activeLoans: filteredLoans.filter(l => l.outstandingAmount > 0).length,
        totalEMI: filteredLoans.filter(l => l.outstandingAmount > 0).reduce((sum, l) => sum + (l.emiAmount || 0), 0),
        totalOutstanding: filteredLoans.reduce((sum, l) => sum + (l.outstandingAmount || 0), 0)
    };

    const COLORS = ['#0066ff', '#00c49f', '#ffbb28', '#ff8042', '#8884d8'];

    return (
        <div className="loans-container">
            <div className="page-header">
                <h1 className="page-title">Loan Management</h1>
                <div className="header-actions">
                    <button className="btn-primary" onClick={() => setActiveTab('dashboard')}>Dashboard</button>
                    <button className="btn-primary" onClick={() => setActiveTab('add')}>+ Add Loan</button>
                    <button className="btn-secondary" onClick={() => setActiveTab('calculator')}>EMI Calculator</button>
                </div>
            </div>

            {/* Dashboard Tab */}
            {activeTab === 'dashboard' && (
                <div>
                    {/* Summary Cards */}
                    <div className="summary-cards">
                        <div className="stat-card">
                            <h3>Total Loans</h3>
                            <div className="stat-value">{summary.totalLoans}</div>
                            <div className="stat-label">{summary.activeLoans} Active</div>
                        </div>
                        <div className="stat-card">
                            <h3>Total EMI</h3>
                            <div className="stat-value">₹{summary.totalEMI.toLocaleString('en-IN', {maximumFractionDigits: 0})}</div>
                            <div className="stat-label">Per Month</div>
                        </div>
                        <div className="stat-card">
                            <h3>Outstanding Amount</h3>
                            <div className="stat-value">₹{summary.totalOutstanding.toLocaleString('en-IN', {maximumFractionDigits: 0})}</div>
                            <div className="stat-label">Total Principal</div>
                        </div>
                    </div>

                    {/* Filters */}
                    <div className="filters-section">
                        <select 
                            value={filters.loanType} 
                            onChange={(e) => setFilters({...filters, loanType: e.target.value})}
                            className="filter-select"
                        >
                            <option value="ALL">All Types</option>
                            <option value="HOME">Home Loan</option>
                            <option value="PERSONAL">Personal Loan</option>
                            <option value="AUTO">Auto Loan</option>
                            <option value="EDUCATION">Education Loan</option>
                            <option value="BUSINESS">Business Loan</option>
                            <option value="OTHER">Other</option>
                        </select>
                        
                        <select 
                            value={filters.status} 
                            onChange={(e) => setFilters({...filters, status: e.target.value})}
                            className="filter-select"
                        >
                            <option value="ALL">All Status</option>
                            <option value="ACTIVE">Active</option>
                            <option value="CLOSED">Closed</option>
                        </select>
                        
                        <input
                            type="text"
                            placeholder="Search by bank/provider..."
                            value={filters.searchTerm}
                            onChange={(e) => setFilters({...filters, searchTerm: e.target.value})}
                            className="filter-input"
                        />
                    </div>

                    {/* Loans List */}
                    <div className="loans-grid">
                        {loading ? (
                            <p>Loading loans...</p>
                        ) : filteredLoans.length === 0 ? (
                            <p>No loans found. Add your first loan to get started!</p>
                        ) : (
                            filteredLoans.map(loan => (
                                <div key={loan.id} className="loan-card">
                                    <div className="loan-card-header">
                                        <h3>{loan.provider || 'Loan'}</h3>
                                        <span className={`loan-badge ${loan.outstandingAmount > 0 ? 'active' : 'closed'}`}>
                                            {loan.outstandingAmount > 0 ? 'Active' : 'Closed'}
                                        </span>
                                    </div>
                                    <div className="loan-type">{loan.loanType}</div>
                                    <div className="loan-details">
                                        <div className="loan-detail-item">
                                            <span className="label">Principal:</span>
                                            <span className="value">₹{loan.principalAmount?.toLocaleString('en-IN')}</span>
                                        </div>
                                        <div className="loan-detail-item">
                                            <span className="label">Outstanding:</span>
                                            <span className="value">₹{loan.outstandingAmount?.toLocaleString('en-IN')}</span>
                                        </div>
                                        <div className="loan-detail-item">
                                            <span className="label">EMI:</span>
                                            <span className="value">₹{loan.emiAmount?.toLocaleString('en-IN')}</span>
                                        </div>
                                        <div className="loan-detail-item">
                                            <span className="label">Rate:</span>
                                            <span className="value">{loan.interestRate}% p.a.</span>
                                        </div>
                                        <div className="loan-detail-item">
                                            <span className="label">Tenure:</span>
                                            <span className="value">{loan.tenureMonths} months</span>
                                        </div>
                                    </div>
                                    <div className="loan-card-actions">
                                        <button className="btn-link" onClick={() => handleViewDetails(loan)}>View Details</button>
                                        <button className="btn-link" onClick={() => {
                                            setSelectedLoan(loan);
                                            setPaymentForm({...paymentForm, loanId: loan.id});
                                            setActiveTab('payments');
                                        }}>Record Payment</button>
                                        <button className="btn-danger-link" onClick={() => handleDeleteLoan(loan.id)}>Delete</button>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                </div>
            )}

            {/* Add Loan Form Tab */}
            {activeTab === 'add' && (
                <div className="form-container">
                    <h2>Add New Loan</h2>
                    <form onSubmit={handleCreateLoan} className="loan-form">
                        <div className="form-row">
                            <div className="form-group">
                                <label>Loan Type*</label>
                                <select 
                                    value={loanForm.loanType} 
                                    onChange={(e) => setLoanForm({...loanForm, loanType: e.target.value})}
                                    required
                                >
                                    <option value="HOME">Home Loan</option>
                                    <option value="PERSONAL">Personal Loan</option>
                                    <option value="AUTO">Auto Loan</option>
                                    <option value="EDUCATION">Education Loan</option>
                                    <option value="BUSINESS">Business Loan</option>
                                    <option value="OTHER">Other</option>
                                </select>
                            </div>
                            
                            <div className="form-group">
                                <label>Bank/Provider*</label>
                                <input 
                                    type="text" 
                                    value={loanForm.provider}
                                    onChange={(e) => setLoanForm({...loanForm, provider: e.target.value})}
                                    required
                                    placeholder="e.g., HDFC Bank"
                                />
                            </div>
                        </div>

                        <div className="form-row">
                            <div className="form-group">
                                <label>Loan Account Number</label>
                                <input 
                                    type="text" 
                                    value={loanForm.loanAccountNumber}
                                    onChange={(e) => setLoanForm({...loanForm, loanAccountNumber: e.target.value})}
                                    placeholder="Optional"
                                />
                            </div>
                            
                            <div className="form-group">
                                <label>Start Date*</label>
                                <input 
                                    type="date" 
                                    value={loanForm.startDate}
                                    onChange={(e) => setLoanForm({...loanForm, startDate: e.target.value})}
                                    required
                                />
                            </div>
                        </div>

                        <div className="form-row">
                            <div className="form-group">
                                <label>Principal Amount (₹)*</label>
                                <input 
                                    type="number" 
                                    value={loanForm.principalAmount}
                                    onChange={(e) => setLoanForm({...loanForm, principalAmount: e.target.value})}
                                    required
                                    placeholder="5000000"
                                    min="1000"
                                />
                            </div>
                            
                            <div className="form-group">
                                <label>Interest Rate (% p.a.)*</label>
                                <input 
                                    type="number" 
                                    step="0.01"
                                    value={loanForm.interestRate}
                                    onChange={(e) => setLoanForm({...loanForm, interestRate: e.target.value})}
                                    required
                                    placeholder="8.5"
                                    min="0.01"
                                />
                            </div>
                        </div>

                        <div className="form-row">
                            <div className="form-group">
                                <label>Tenure (months)*</label>
                                <input 
                                    type="number" 
                                    value={loanForm.tenureMonths}
                                    onChange={(e) => setLoanForm({...loanForm, tenureMonths: e.target.value})}
                                    required
                                    placeholder="240"
                                    min="1"
                                />
                            </div>
                            
                            <div className="form-group">
                                <label>EMI Amount (₹)</label>
                                <div className="input-with-button">
                                    <input 
                                        type="number" 
                                        value={loanForm.emiAmount}
                                        onChange={(e) => setLoanForm({...loanForm, emiAmount: e.target.value})}
                                        placeholder="Auto-calculated"
                                        readOnly
                                    />
                                    <button type="button" onClick={handlePreviewEMI} className="btn-secondary">
                                        Calculate
                                    </button>
                                </div>
                            </div>
                        </div>

                        <div className="form-actions">
                            <button type="button" className="btn-secondary" onClick={() => setActiveTab('dashboard')}>
                                Cancel
                            </button>
                            <button type="submit" className="btn-primary">Create Loan</button>
                        </div>
                    </form>
                </div>
            )}

            {/* Loan Details Tab */}
            {activeTab === 'details' && selectedLoan && (
                <div className="details-container">
                    <div className="details-header">
                        <div>
                            <h2>{selectedLoan.provider}</h2>
                            <p className="details-subtitle">{selectedLoan.loanType} Loan</p>
                        </div>
                        <div className="details-actions">
                            <button className="btn-secondary" onClick={() => setActiveTab('dashboard')}>← Back</button>
                            <button className="btn-secondary" onClick={handleSimulatePrepayment}>Simulate Prepayment</button>
                            <button className="btn-warning" onClick={handleCalculateForeclosure}>Calculate Foreclosure</button>
                            <button className="btn-danger" onClick={() => handleDeleteLoan(selectedLoan.id)}>Delete Loan</button>
                        </div>
                    </div>

                    {/* Loan Summary */}
                    <div className="details-summary">
                        <div className="summary-item">
                            <span className="summary-label">Principal Amount</span>
                            <span className="summary-value">₹{selectedLoan.principalAmount?.toLocaleString('en-IN')}</span>
                        </div>
                        <div className="summary-item">
                            <span className="summary-label">Outstanding</span>
                            <span className="summary-value outstanding">₹{selectedLoan.outstandingAmount?.toLocaleString('en-IN')}</span>
                        </div>
                        <div className="summary-item">
                            <span className="summary-label">EMI</span>
                            <span className="summary-value">₹{selectedLoan.emiAmount?.toLocaleString('en-IN')}</span>
                        </div>
                        <div className="summary-item">
                            <span className="summary-label">Interest Rate</span>
                            <span className="summary-value">{selectedLoan.interestRate}%</span>
                        </div>
                        <div className="summary-item">
                            <span className="summary-label">Tenure</span>
                            <span className="summary-value">{selectedLoan.tenureMonths} months</span>
                        </div>
                    </div>

                    {/* Loan Analysis */}
                    {loanAnalysis && (
                        <div className="analysis-section">
                            <h3>Loan Analysis</h3>
                            <div className="analysis-grid">
                                <div className="analysis-card">
                                    <div className="analysis-label">Total Interest Payable</div>
                                    <div className="analysis-value">₹{loanAnalysis.totalInterestPayable?.toLocaleString('en-IN')}</div>
                                </div>
                                <div className="analysis-card">
                                    <div className="analysis-label">Total Amount Payable</div>
                                    <div className="analysis-value">₹{loanAnalysis.totalAmountPayable?.toLocaleString('en-IN')}</div>
                                </div>
                                <div className="analysis-card">
                                    <div className="analysis-label">Interest to Principal Ratio</div>
                                    <div className="analysis-value">{loanAnalysis.interestToPrincipalRatio?.toFixed(2)}%</div>
                                </div>
                                <div className="analysis-card">
                                    <div className="analysis-label">Completion</div>
                                    <div className="analysis-value">{loanAnalysis.completionPercentage?.toFixed(2)}%</div>
                                </div>
                                <div className="analysis-card">
                                    <div className="analysis-label">Payments Completed</div>
                                    <div className="analysis-value">{loanAnalysis.paymentsCompleted} / {loanAnalysis.totalPayments}</div>
                                </div>
                                <div className="analysis-card">
                                    <div className="analysis-label">Remaining Tenure</div>
                                    <div className="analysis-value">{loanAnalysis.remainingTenureMonths} months</div>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* Amortization Schedule */}
                    {amortizationSchedule && amortizationSchedule.schedule && (
                        <div className="schedule-section">
                            <h3>Amortization Schedule</h3>
                            <div className="schedule-chart">
                                <ResponsiveContainer width="100%" height={300}>
                                    <LineChart data={amortizationSchedule.schedule.slice(0, 60)}>
                                        <CartesianGrid strokeDasharray="3 3" />
                                        <XAxis dataKey="paymentNumber" label={{ value: 'Payment Number', position: 'insideBottom', offset: -5 }} />
                                        <YAxis label={{ value: 'Amount (₹)', angle: -90, position: 'insideLeft' }} />
                                        <Tooltip />
                                        <Legend />
                                        <Line type="monotone" dataKey="principalComponent" stroke="#0066ff" name="Principal" />
                                        <Line type="monotone" dataKey="interestComponent" stroke="#ff8042" name="Interest" />
                                        <Line type="monotone" dataKey="outstandingBalance" stroke="#00c49f" name="Outstanding" />
                                    </LineChart>
                                </ResponsiveContainer>
                            </div>
                            
                            <div className="schedule-table-container">
                                <table className="schedule-table">
                                    <thead>
                                        <tr>
                                            <th>#</th>
                                            <th>Date</th>
                                            <th>EMI</th>
                                            <th>Principal</th>
                                            <th>Interest</th>
                                            <th>Outstanding</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {amortizationSchedule.schedule.slice(0, 12).map((entry, idx) => (
                                            <tr key={idx}>
                                                <td>{entry.paymentNumber}</td>
                                                <td>{entry.paymentDate}</td>
                                                <td>₹{entry.emiAmount?.toLocaleString('en-IN')}</td>
                                                <td>₹{entry.principalComponent?.toLocaleString('en-IN')}</td>
                                                <td>₹{entry.interestComponent?.toLocaleString('en-IN')}</td>
                                                <td>₹{entry.outstandingBalance?.toLocaleString('en-IN')}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                                {amortizationSchedule.schedule.length > 12 && (
                                    <p className="table-note">Showing first 12 payments of {amortizationSchedule.schedule.length}</p>
                                )}
                            </div>
                        </div>
                    )}

                    {/* Payment History */}
                    {paymentHistory && (
                        <div className="payment-history-section">
                            <h3>Payment History</h3>
                            <div className="payment-stats">
                                <div className="stat-item">
                                    <span className="stat-label">Total Payments</span>
                                    <span className="stat-value">{paymentHistory.totalPayments}</span>
                                </div>
                                <div className="stat-item">
                                    <span className="stat-label">Total Paid</span>
                                    <span className="stat-value">₹{paymentHistory.totalPaid?.toLocaleString('en-IN')}</span>
                                </div>
                                <div className="stat-item">
                                    <span className="stat-label">Principal Paid</span>
                                    <span className="stat-value">₹{paymentHistory.totalPrincipalPaid?.toLocaleString('en-IN')}</span>
                                </div>
                                <div className="stat-item">
                                    <span className="stat-label">Interest Paid</span>
                                    <span className="stat-value">₹{paymentHistory.totalInterestPaid?.toLocaleString('en-IN')}</span>
                                </div>
                                <div className="stat-item">
                                    <span className="stat-label">Missed Payments</span>
                                    <span className="stat-value missed">{paymentHistory.missedPayments}</span>
                                </div>
                            </div>
                            
                            {paymentHistory.payments && paymentHistory.payments.length > 0 && (
                                <div className="payments-table-container">
                                    <table className="payments-table">
                                        <thead>
                                            <tr>
                                                <th>Date</th>
                                                <th>Amount</th>
                                                <th>Principal</th>
                                                <th>Interest</th>
                                                <th>Type</th>
                                                <th>Method</th>
                                                <th>Status</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {paymentHistory.payments.map((payment, idx) => (
                                                <tr key={idx}>
                                                    <td>{payment.paymentDate}</td>
                                                    <td>₹{payment.amount?.toLocaleString('en-IN')}</td>
                                                    <td>₹{payment.principalPaid?.toLocaleString('en-IN')}</td>
                                                    <td>₹{payment.interestPaid?.toLocaleString('en-IN')}</td>
                                                    <td><span className={`badge badge-${payment.paymentType?.toLowerCase()}`}>{payment.paymentType}</span></td>
                                                    <td>{payment.paymentMethod}</td>
                                                    <td><span className={`badge badge-${payment.paymentStatus?.toLowerCase()}`}>{payment.paymentStatus}</span></td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </div>
                    )}
                </div>
            )}

            {/* EMI Calculator Tab */}
            {activeTab === 'calculator' && (
                <div className="calculator-container">
                    <h2>EMI Calculator</h2>
                    <div className="calculator-grid">
                        <div className="calculator-inputs">
                            <div className="form-group">
                                <label>Loan Amount (₹)</label>
                                <input 
                                    type="number" 
                                    value={calculatorInputs.principal}
                                    onChange={(e) => setCalculatorInputs({...calculatorInputs, principal: e.target.value})}
                                    placeholder="5000000"
                                />
                            </div>
                            
                            <div className="form-group">
                                <label>Interest Rate (% p.a.)</label>
                                <input 
                                    type="number" 
                                    step="0.01"
                                    value={calculatorInputs.rate}
                                    onChange={(e) => setCalculatorInputs({...calculatorInputs, rate: e.target.value})}
                                    placeholder="8.5"
                                />
                            </div>
                            
                            <div className="form-group">
                                <label>Loan Tenure (months)</label>
                                <input 
                                    type="number" 
                                    value={calculatorInputs.tenure}
                                    onChange={(e) => setCalculatorInputs({...calculatorInputs, tenure: e.target.value})}
                                    placeholder="240"
                                />
                            </div>
                            
                            <button className="btn-primary" onClick={handleCalculateEMI}>Calculate EMI</button>
                        </div>
                        
                        <div className="calculator-results">
                            <h3>Results</h3>
                            <div className="result-item">
                                <span className="result-label">Monthly EMI</span>
                                <span className="result-value">
                                    ₹{Number(calculateEMI(calculatorInputs.principal, calculatorInputs.rate, calculatorInputs.tenure)).toLocaleString('en-IN')}
                                </span>
                            </div>
                            <div className="result-item">
                                <span className="result-label">Total Interest</span>
                                <span className="result-value">
                                    ₹{(
                                        Number(calculateEMI(calculatorInputs.principal, calculatorInputs.rate, calculatorInputs.tenure)) * 
                                        Number(calculatorInputs.tenure) - 
                                        Number(calculatorInputs.principal)
                                    ).toLocaleString('en-IN', {maximumFractionDigits: 0})}
                                </span>
                            </div>
                            <div className="result-item">
                                <span className="result-label">Total Amount</span>
                                <span className="result-value">
                                    ₹{(
                                        Number(calculateEMI(calculatorInputs.principal, calculatorInputs.rate, calculatorInputs.tenure)) * 
                                        Number(calculatorInputs.tenure)
                                    ).toLocaleString('en-IN', {maximumFractionDigits: 0})}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* Payments Tab */}
            {activeTab === 'payments' && selectedLoan && (
                <div className="payments-container">
                    <h2>Record Payment</h2>
                    <p className="payments-subtitle">Recording payment for {selectedLoan.provider}</p>
                    
                    <form onSubmit={handleRecordPayment} className="payment-form">
                        <div className="form-row">
                            <div className="form-group">
                                <label>Payment Date*</label>
                                <input 
                                    type="date" 
                                    value={paymentForm.paymentDate}
                                    onChange={(e) => setPaymentForm({...paymentForm, paymentDate: e.target.value})}
                                    required
                                />
                            </div>
                            
                            <div className="form-group">
                                <label>Payment Amount (₹)*</label>
                                <input 
                                    type="number" 
                                    value={paymentForm.paymentAmount}
                                    onChange={(e) => setPaymentForm({...paymentForm, paymentAmount: e.target.value})}
                                    required
                                    placeholder={`EMI: ${selectedLoan.emiAmount}`}
                                />
                            </div>
                        </div>

                        <div className="form-row">
                            <div className="form-group">
                                <label>Payment Type*</label>
                                <select 
                                    value={paymentForm.paymentType}
                                    onChange={(e) => setPaymentForm({...paymentForm, paymentType: e.target.value})}
                                    required
                                >
                                    <option value="EMI">EMI Payment</option>
                                    <option value="PREPAYMENT">Prepayment</option>
                                    <option value="FORECLOSURE">Foreclosure</option>
                                </select>
                            </div>
                            
                            <div className="form-group">
                                <label>Payment Method*</label>
                                <select 
                                    value={paymentForm.paymentMethod}
                                    onChange={(e) => setPaymentForm({...paymentForm, paymentMethod: e.target.value})}
                                    required
                                >
                                    <option value="NEFT">NEFT</option>
                                    <option value="RTGS">RTGS</option>
                                    <option value="UPI">UPI</option>
                                    <option value="CHEQUE">Cheque</option>
                                    <option value="CASH">Cash</option>
                                    <option value="AUTO_DEBIT">Auto Debit</option>
                                </select>
                            </div>
                        </div>

                        <div className="form-group">
                            <label>Transaction Reference</label>
                            <input 
                                type="text" 
                                value={paymentForm.transactionReference}
                                onChange={(e) => setPaymentForm({...paymentForm, transactionReference: e.target.value})}
                                placeholder="e.g., UTR Number"
                            />
                        </div>

                        <div className="form-group">
                            <label>Notes</label>
                            <textarea 
                                value={paymentForm.notes}
                                onChange={(e) => setPaymentForm({...paymentForm, notes: e.target.value})}
                                placeholder="Any additional notes..."
                                rows="3"
                            />
                        </div>

                        <div className="form-actions">
                            <button type="button" className="btn-secondary" onClick={() => setActiveTab('details')}>
                                Cancel
                            </button>
                            <button type="submit" className="btn-primary">Record Payment</button>
                        </div>
                    </form>
                </div>
            )}
        </div>
    );
};

export default Loans;
