import { useState, useEffect, useMemo } from 'react';
import { lendingApi } from '../api/lendingApi';
import AddLendingModal from '../components/AddLendingModal';
import LendingDetailModal from '../components/LendingDetailModal';

const Lending = () => {
    const [lendings, setLendings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showAddModal, setShowAddModal] = useState(false);
    const [editingLending, setEditingLending] = useState(null);
    const [selectedLending, setSelectedLending] = useState(null);
    const [filterStatus, setFilterStatus] = useState('ALL');
    const [searchTerm, setSearchTerm] = useState('');

    const user = JSON.parse(localStorage.getItem('user'));
    const userId = useMemo(() => user?.userId, []);

    useEffect(() => {
        if (!userId) {
            setError('User not authenticated');
            setLoading(false);
            return;
        }

        loadLendings();
    }, [userId]);

    const loadLendings = async () => {
        try {
            setLoading(true);
            const data = await lendingApi.getAllLendings(userId);
            setLendings(data || []);
            setError(null);
        } catch (err) {
            console.error('Error loading lendings:', err);
            setError('Failed to load lending records');
        } finally {
            setLoading(false);
        }
    };

    const handleAddLending = async (lendingData) => {
        try {
            if (editingLending) {
                // Update existing lending
                await lendingApi.updateLending(editingLending.id, { ...lendingData, userId });
            } else {
                // Add new lending
                await lendingApi.addLending({ ...lendingData, userId });
            }
            setShowAddModal(false);
            setEditingLending(null);
            loadLendings();
        } catch (err) {
            console.error('Error saving lending:', err);
            alert('Failed to save lending record');
        }
    };

    const handleEditLending = (lending) => {
        setEditingLending(lending);
        setShowAddModal(true);
    };

    const handleViewDetails = (lending) => {
        setSelectedLending(lending);
    };

    const handleCloseDetail = () => {
        setSelectedLending(null);
        loadLendings();
    };

    const handleCloseAddModal = () => {
        setShowAddModal(false);
        setEditingLending(null);
    };

    const formatCurrency = (value) => {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            maximumFractionDigits: 0
        }).format(value || 0);
    };

    const formatDate = (dateString) => {
        if (!dateString) return '-';
        return new Date(dateString).toLocaleDateString('en-IN', {
            day: 'numeric',
            month: 'short',
            year: 'numeric'
        });
    };

    const getStatusBadge = (lending) => {
        const { status, dueDate } = lending;
        
        if (status === 'FULLY_PAID' || status === 'PAID') {
            return <span className="status-badge status-paid">Fully Paid ✓</span>;
        }
        
        if (status === 'OVERDUE') {
            const overdueDays = Math.floor((new Date() - new Date(dueDate)) / (1000 * 60 * 60 * 24));
            return <span className="status-badge status-overdue">Overdue ({overdueDays} days)</span>;
        }
        
        if (status === 'PARTIALLY_PAID') {
            return <span className="status-badge status-partial">Partially Paid</span>;
        }
        
        return <span className="status-badge status-active">Active</span>;
    };

    const filteredLendings = lendings.filter(lending => {
        const matchesStatus = filterStatus === 'ALL' || lending.status === filterStatus;
        const matchesSearch = !searchTerm || 
            lending.borrowerName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            lending.borrowerContact?.toLowerCase().includes(searchTerm.toLowerCase());
        return matchesStatus && matchesSearch;
    });

    const summary = useMemo(() => {
        return lendings.reduce((acc, lending) => {
            const lent = Number(lending.amountLent) || 0;
            const repaid = Number(lending.amountRepaid) || 0;
            const outstanding = Number(lending.outstandingAmount) || 0;
            
            return {
                totalLent: acc.totalLent + lent,
                totalRepaid: acc.totalRepaid + repaid,
                totalOutstanding: acc.totalOutstanding + outstanding,
                activeCount: acc.activeCount + (lending.status !== 'FULLY_PAID' && lending.status !== 'PAID' ? 1 : 0)
            };
        }, { totalLent: 0, totalRepaid: 0, totalOutstanding: 0, activeCount: 0 });
    }, [lendings]);

    if (loading) {
        return <div style={{ padding: '40px', textAlign: 'center' }}>Loading lending records...</div>;
    }

    return (
        <div>
            <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h1 className="page-title">Lending Management</h1>
                <button 
                    className="btn-primary" 
                    onClick={() => setShowAddModal(true)}
                    style={{
                        background: '#4f46e5',
                        color: 'white',
                        border: 'none',
                        padding: '10px 20px',
                        borderRadius: '8px',
                        fontSize: '15px',
                        fontWeight: '600',
                        cursor: 'pointer',
                        boxShadow: '0 2px 4px rgba(79, 70, 229, 0.3)'
                    }}
                >
                    + Add Lending
                </button>
            </div>

            {error && (
                <div style={{ padding: '12px', marginBottom: '20px', background: '#fee', color: '#c33', borderRadius: '8px' }}>
                    {error}
                </div>
            )}

            {/* Summary Cards */}
            <section className="stats-grid" style={{ gridTemplateColumns: 'repeat(4, 1fr)', marginBottom: '24px' }}>
                <div className="stat-card">
                    <div className="stat-label">Total Lent</div>
                    <div className="stat-value">{formatCurrency(summary.totalLent)}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Total Repaid</div>
                    <div className="stat-value" style={{ color: '#28a745' }}>{formatCurrency(summary.totalRepaid)}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Outstanding</div>
                    <div className="stat-value" style={{ color: '#ff6b6b' }}>{formatCurrency(summary.totalOutstanding)}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Active Lendings</div>
                    <div className="stat-value">{summary.activeCount}</div>
                </div>
            </section>

            {/* Filters */}
            <div style={{ display: 'flex', gap: '12px', marginBottom: '20px', alignItems: 'center' }}>
                <div style={{ display: 'flex', gap: '8px' }}>
                    {['ALL', 'ACTIVE', 'PARTIALLY_PAID', 'OVERDUE', 'PAID'].map(status => (
                        <button
                            key={status}
                            onClick={() => setFilterStatus(status)}
                            style={{
                                padding: '8px 16px',
                                border: '1px solid var(--border-color)',
                                borderRadius: '6px',
                                background: filterStatus === status ? 'var(--primary-color)' : 'white',
                                color: filterStatus === status ? 'white' : 'var(--text-color)',
                                cursor: 'pointer',
                                fontSize: '14px'
                            }}
                        >
                            {status === 'ALL' ? 'All' : status.replace('_', ' ')}
                        </button>
                    ))}
                </div>
                <input
                    type="text"
                    placeholder="Search by borrower name..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    style={{
                        marginLeft: 'auto',
                        padding: '8px 16px',
                        border: '1px solid var(--border-color)',
                        borderRadius: '6px',
                        width: '300px'
                    }}
                />
            </div>

            {/* Lendings Table */}
            {filteredLendings.length === 0 ? (
                <div style={{ padding: '60px', textAlign: 'center', color: 'var(--text-secondary)' }}>
                    {searchTerm || filterStatus !== 'ALL' 
                        ? 'No lendings found matching your criteria' 
                        : 'No lending records yet. Click "Add Lending" to get started.'}
                </div>
            ) : (
                <div style={{ background: 'white', borderRadius: '12px', overflow: 'hidden', border: '1px solid var(--border-color)' }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                        <thead style={{ background: '#f8f9fa', borderBottom: '2px solid var(--border-color)' }}>
                            <tr>
                                <th style={{ padding: '16px', textAlign: 'left', fontWeight: '600' }}>Borrower</th>
                                <th style={{ padding: '16px', textAlign: 'right', fontWeight: '600' }}>Amount Lent</th>
                                <th style={{ padding: '16px', textAlign: 'right', fontWeight: '600' }}>Repaid</th>
                                <th style={{ padding: '16px', textAlign: 'right', fontWeight: '600' }}>Outstanding</th>
                                <th style={{ padding: '16px', textAlign: 'left', fontWeight: '600' }}>Due Date</th>
                                <th style={{ padding: '16px', textAlign: 'left', fontWeight: '600' }}>Status</th>
                                <th style={{ padding: '16px', textAlign: 'center', fontWeight: '600' }}>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredLendings.map((lending) => (
                                <tr key={lending.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                                    <td style={{ padding: '16px' }}>
                                        <div style={{ fontWeight: '500' }}>{lending.borrowerName}</div>
                                        {lending.borrowerContact && (
                                            <div style={{ fontSize: '13px', color: 'var(--text-secondary)', marginTop: '4px' }}>
                                                {lending.borrowerContact}
                                            </div>
                                        )}
                                    </td>
                                    <td style={{ padding: '16px', textAlign: 'right', fontWeight: '500' }}>
                                        {formatCurrency(lending.amountLent)}
                                    </td>
                                    <td style={{ padding: '16px', textAlign: 'right', color: '#28a745', fontWeight: '500' }}>
                                        {formatCurrency(lending.amountRepaid)}
                                    </td>
                                    <td style={{ padding: '16px', textAlign: 'right', color: '#ff6b6b', fontWeight: '500' }}>
                                        {formatCurrency(lending.outstandingAmount)}
                                    </td>
                                    <td style={{ padding: '16px' }}>
                                        {formatDate(lending.dueDate)}
                                    </td>
                                    <td style={{ padding: '16px' }}>
                                        {getStatusBadge(lending)}
                                    </td>
                                    <td style={{ padding: '16px', textAlign: 'center' }}>
                                        <button
                                            onClick={() => handleViewDetails(lending)}
                                            className="btn-action"
                                            style={{
                                                padding: '6px 16px',
                                                background: '#4f46e5',
                                                color: 'white',
                                                border: 'none',
                                                borderRadius: '6px',
                                                cursor: 'pointer',
                                                fontSize: '14px',
                                                fontWeight: '500'
                                            }}
                                        >
                                            View Details
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}

            {/* Modals */}
            {showAddModal && (
                <AddLendingModal
                    onClose={handleCloseAddModal}
                    onSubmit={handleAddLending}
                    editData={editingLending}
                />
            )}

            {selectedLending && (
                <LendingDetailModal
                    lending={selectedLending}
                    onClose={handleCloseDetail}
                    onEdit={handleEditLending}
                />
            )}

            <style jsx>{`
                .status-badge {
                    padding: 4px 12px;
                    border-radius: 12px;
                    font-size: 13px;
                    font-weight: 500;
                }
                .status-paid {
                    background: #d4edda;
                    color: #155724;
                }
                .status-active {
                    background: #d1ecf1;
                    color: #0c5460;
                }
                .status-partial {
                    background: #fff3cd;
                    color: #856404;
                }
                .status-overdue {
                    background: #f8d7da;
                    color: #721c24;
                }
                .btn-primary {
                    padding: 10px 20px;
                    background: var(--primary-color);
                    color: white;
                    border: none;
                    border-radius: 8px;
                    cursor: pointer;
                    font-size: 14px;
                    font-weight: 500;
                }
                .btn-primary:hover {
                    opacity: 0.9;
                }
            `}</style>
        </div>
    );
};

export default Lending;
