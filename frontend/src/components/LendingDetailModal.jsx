import { useState, useEffect } from 'react';
import { X, Edit2 } from 'lucide-react';
import { lendingApi } from '../api/lendingApi';
import AddRepaymentModal from './AddRepaymentModal';

const LendingDetailModal = ({ lending, onClose, onEdit }) => {
    const [lendingData, setLendingData] = useState(lending);
    const [showRepaymentModal, setShowRepaymentModal] = useState(false);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        // Refresh lending data to get latest repayments
        refreshLendingData();
    }, []);

    const refreshLendingData = async () => {
        try {
            const data = await lendingApi.getLendingById(lending.id);
            setLendingData(data);
        } catch (err) {
            console.error('Error refreshing lending data:', err);
        }
    };

    const handleAddRepayment = async (repaymentData) => {
        try {
            setLoading(true);
            await lendingApi.addRepayment(lendingData.id, repaymentData);
            setShowRepaymentModal(false);
            await refreshLendingData();
        } catch (err) {
            console.error('Error adding repayment:', err);
            alert('Failed to add repayment');
        } finally {
            setLoading(false);
        }
    };

    const handleMarkAsPaid = async () => {
        if (!confirm('Mark this lending as fully paid? This will close the lending record.')) {
            return;
        }

        try {
            setLoading(true);
            await lendingApi.closeLending(lendingData.id);
            await refreshLendingData();
            alert('Lending marked as fully paid');
        } catch (err) {
            console.error('Error marking as paid:', err);
            alert('Failed to mark as paid');
        } finally {
            setLoading(false);
        }
    };

    const handleSendReminder = async () => {
        try {
            setLoading(true);
            await lendingApi.sendReminder(lendingData.id);
            alert('Payment reminder notification sent!');
        } catch (err) {
            console.error('Error sending reminder:', err);
            alert(err.response?.data?.message || 'Failed to send reminder');
        } finally {
            setLoading(false);
        }
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

    const getProgressPercentage = () => {
        const lent = Number(lendingData.amountLent) || 0;
        const repaid = Number(lendingData.amountRepaid) || 0;
        if (lent === 0) return 0;
        return Math.min((repaid / lent) * 100, 100);
    };

    const getStatusColor = () => {
        switch (lendingData.status) {
            case 'FULLY_PAID':
            case 'PAID': return '#28a745';
            case 'OVERDUE': return '#dc3545';
            case 'PARTIALLY_PAID': return '#ffc107';
            default: return '#17a2b8';
        }
    };

    return (
        <>
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
                    maxWidth: '700px',
                    maxHeight: '90vh',
                    overflow: 'auto',
                    boxShadow: '0 4px 20px rgba(0, 0, 0, 0.15)'
                }}>
                    {/* Header */}
                    <div style={{
                        padding: '20px 24px',
                        borderBottom: '1px solid var(--border-color)',
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center'
                    }}>
                        <h2 style={{ margin: 0, fontSize: '20px', fontWeight: '600' }}>
                            Lending Details - {lendingData.borrowerName}
                        </h2>
                        <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                            <button
                                onClick={() => {
                                    onEdit(lendingData);
                                    onClose();
                                }}
                                style={{
                                    background: 'none',
                                    border: 'none',
                                    cursor: 'pointer',
                                    padding: '6px',
                                    display: 'flex',
                                    alignItems: 'center',
                                    borderRadius: '4px',
                                    transition: 'background 0.2s'
                                }}
                                onMouseEnter={(e) => e.currentTarget.style.background = '#f0f0f0'}
                                onMouseLeave={(e) => e.currentTarget.style.background = 'none'}
                                title="Edit Lending"
                            >
                                <Edit2 size={20} color="#4f46e5" />
                            </button>
                            <button
                                onClick={onClose}
                                style={{
                                    background: 'none',
                                    border: 'none',
                                    cursor: 'pointer',
                                    padding: '4px',
                                    display: 'flex',
                                    alignItems: 'center'
                                }}
                            >
                                <X size={24} color="#666" />
                            </button>
                        </div>
                    </div>

                    {/* Content */}
                    <div style={{ padding: '24px' }}>
                        {/* Borrower Info */}
                        <div style={{ marginBottom: '24px' }}>
                            <h3 style={{ margin: '0 0 12px 0', fontSize: '16px', fontWeight: '600' }}>
                                Borrower Information
                            </h3>
                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                                <div>
                                    <div style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '4px' }}>Name</div>
                                    <div style={{ fontWeight: '500' }}>{lendingData.borrowerName}</div>
                                </div>
                                <div>
                                    <div style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '4px' }}>Contact</div>
                                    <div style={{ fontWeight: '500' }}>{lendingData.borrowerContact || '-'}</div>
                                </div>
                            </div>
                        </div>

                        {/* Lending Details */}
                        <div style={{ marginBottom: '24px' }}>
                            <h3 style={{ margin: '0 0 12px 0', fontSize: '16px', fontWeight: '600' }}>
                                Lending Information
                            </h3>
                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                                <div>
                                    <div style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '4px' }}>Amount Lent</div>
                                    <div style={{ fontWeight: '600', fontSize: '18px', color: '#000' }}>
                                        {formatCurrency(lendingData.amountLent)}
                                    </div>
                                </div>
                                <div>
                                    <div style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '4px' }}>Status</div>
                                    <div style={{
                                        display: 'inline-block',
                                        padding: '4px 12px',
                                        borderRadius: '12px',
                                        background: getStatusColor() + '20',
                                        color: getStatusColor(),
                                        fontSize: '14px',
                                        fontWeight: '500'
                                    }}>
                                        {lendingData.status?.replace('_', ' ')}
                                    </div>
                                </div>
                                <div>
                                    <div style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '4px' }}>Date Lent</div>
                                    <div style={{ fontWeight: '500' }}>{formatDate(lendingData.dateLent)}</div>
                                </div>
                                <div>
                                    <div style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '4px' }}>Due Date</div>
                                    <div style={{ fontWeight: '500' }}>{formatDate(lendingData.dueDate)}</div>
                                </div>
                            </div>
                        </div>

                        {/* Progress */}
                        <div style={{ marginBottom: '24px' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                                <span style={{ fontSize: '14px', fontWeight: '500' }}>Repayment Progress</span>
                                <span style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
                                    {getProgressPercentage().toFixed(0)}%
                                </span>
                            </div>
                            <div style={{
                                height: '12px',
                                background: '#e9ecef',
                                borderRadius: '6px',
                                overflow: 'hidden'
                            }}>
                                <div style={{
                                    height: '100%',
                                    width: `${getProgressPercentage()}%`,
                                    background: '#28a745',
                                    borderRadius: '6px',
                                    transition: 'width 0.3s ease'
                                }} />
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '8px', fontSize: '14px' }}>
                                <span style={{ color: '#28a745', fontWeight: '500' }}>
                                    Repaid: {formatCurrency(lendingData.amountRepaid)}
                                </span>
                                <span style={{ color: '#ff6b6b', fontWeight: '500' }}>
                                    Outstanding: {formatCurrency(lendingData.outstandingAmount)}
                                </span>
                            </div>
                        </div>

                        {/* Notes */}
                        {lendingData.notes && (
                            <div style={{ marginBottom: '24px' }}>
                                <h3 style={{ margin: '0 0 8px 0', fontSize: '16px', fontWeight: '600' }}>Notes</h3>
                                <div style={{
                                    padding: '12px',
                                    background: '#f8f9fa',
                                    borderRadius: '6px',
                                    fontSize: '14px',
                                    color: 'var(--text-secondary)'
                                }}>
                                    {lendingData.notes}
                                </div>
                            </div>
                        )}

                        {/* Repayment History */}
                        <div style={{ marginBottom: '24px' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                                <h3 style={{ margin: 0, fontSize: '16px', fontWeight: '600' }}>Repayment History</h3>
                                {lendingData.status !== 'FULLY_PAID' && lendingData.status !== 'PAID' && (
                                    <button
                                        onClick={() => setShowRepaymentModal(true)}
                                        disabled={loading}
                                        style={{
                                            padding: '8px 16px',
                                            background: '#4f46e5',
                                            color: 'white',
                                            border: 'none',
                                            borderRadius: '6px',
                                            cursor: loading ? 'not-allowed' : 'pointer',
                                            fontSize: '14px',
                                            fontWeight: '500',
                                            opacity: loading ? 0.6 : 1,
                                            boxShadow: '0 2px 4px rgba(79, 70, 229, 0.2)'
                                        }}
                                    >
                                        + Add Repayment
                                    </button>
                                )}
                            </div>

                            {lendingData.repayments && lendingData.repayments.length > 0 ? (
                                <div style={{
                                    border: '1px solid var(--border-color)',
                                    borderRadius: '8px',
                                    overflow: 'hidden'
                                }}>
                                    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                                        <thead style={{ background: '#f8f9fa' }}>
                                            <tr>
                                                <th style={{ padding: '12px', textAlign: 'left', fontSize: '13px', fontWeight: '600' }}>Date</th>
                                                <th style={{ padding: '12px', textAlign: 'right', fontSize: '13px', fontWeight: '600' }}>Amount</th>
                                                <th style={{ padding: '12px', textAlign: 'left', fontSize: '13px', fontWeight: '600' }}>Method</th>
                                                <th style={{ padding: '12px', textAlign: 'left', fontSize: '13px', fontWeight: '600' }}>Notes</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {lendingData.repayments.map((repayment, index) => (
                                                <tr key={repayment.id || index} style={{ borderTop: '1px solid var(--border-color)' }}>
                                                    <td style={{ padding: '12px', fontSize: '14px' }}>
                                                        {formatDate(repayment.repaymentDate)}
                                                    </td>
                                                    <td style={{ padding: '12px', textAlign: 'right', fontSize: '14px', fontWeight: '500', color: '#28a745' }}>
                                                        {formatCurrency(repayment.amount)}
                                                    </td>
                                                    <td style={{ padding: '12px', fontSize: '14px' }}>
                                                        {repayment.repaymentMethod?.replace('_', ' ') || '-'}
                                                    </td>
                                                    <td style={{ padding: '12px', fontSize: '14px', color: 'var(--text-secondary)' }}>
                                                        {repayment.notes || '-'}
                                                    </td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                            ) : (
                                <div style={{
                                    padding: '40px',
                                    textAlign: 'center',
                                    color: 'var(--text-secondary)',
                                    fontSize: '14px',
                                    border: '1px dashed var(--border-color)',
                                    borderRadius: '8px'
                                }}>
                                    No repayments recorded yet
                                </div>
                            )}
                        </div>

                        {/* Actions */}
                        <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
                            {lendingData.status !== 'FULLY_PAID' && lendingData.status !== 'PAID' && (
                                <>
                                    <button
                                        onClick={handleSendReminder}
                                        disabled={loading}
                                        style={{
                                            padding: '10px 20px',
                                            border: '1px solid #4f46e5',
                                            borderRadius: '6px',
                                            background: 'white',
                                            color: '#4f46e5',
                                            cursor: loading ? 'not-allowed' : 'pointer',
                                            fontSize: '14px',
                                            fontWeight: '500',
                                            opacity: loading ? 0.6 : 1
                                        }}
                                    >
                                        📩 Send Reminder
                                    </button>
                                    <button
                                        onClick={handleMarkAsPaid}
                                        disabled={loading}
                                        style={{
                                            padding: '10px 20px',
                                            border: '1px solid #28a745',
                                            borderRadius: '6px',
                                            background: 'white',
                                            color: '#28a745',
                                            cursor: loading ? 'not-allowed' : 'pointer',
                                            fontSize: '14px',
                                            fontWeight: '500',
                                            opacity: loading ? 0.6 : 1
                                        }}
                                    >
                                        Mark as Fully Paid
                                    </button>
                                </>
                            )}
                            <button
                                onClick={onClose}
                                style={{
                                    padding: '10px 20px',
                                    border: 'none',
                                    borderRadius: '6px',
                                    background: '#6b7280',
                                    color: 'white',
                                    cursor: 'pointer',
                                    fontSize: '14px',
                                    fontWeight: '500'
                                }}
                            >
                                Close
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* Repayment Modal */}
            {showRepaymentModal && (
                <AddRepaymentModal
                    onClose={() => setShowRepaymentModal(false)}
                    onSubmit={handleAddRepayment}
                    maxAmount={lendingData.outstandingAmount}
                />
            )}
        </>
    );
};

export default LendingDetailModal;
