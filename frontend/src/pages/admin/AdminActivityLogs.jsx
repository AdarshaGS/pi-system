import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Activity, ArrowLeft, User, Clock, MapPin, Monitor } from 'lucide-react';
import { adminApi } from '../../api';
import '../../App.css';

const AdminActivityLogs = () => {
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedLog, setSelectedLog] = useState(null);
    const [showDetails, setShowDetails] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        const user = JSON.parse(localStorage.getItem('user'));
        if (!user || !user.token) {
            navigate('/login');
            return;
        }

        fetchLogs();
    }, [navigate]);

    const fetchLogs = async () => {
        const user = JSON.parse(localStorage.getItem('user'));
        setLoading(true);

        try {
            const data = await adminApi.getActivityLogs(user.token, { limit: 100 });
            setLogs(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const formatTimestamp = (timestamp) => {
        const date = new Date(timestamp);
        return date.toLocaleString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    };

    const getActionColor = (action) => {
        const colors = {
            'LOGIN': '#4caf50',
            'LOGOUT': '#ff9800',
            'REGISTER': '#2196f3',
            'CREATE': '#00bcd4',
            'UPDATE': '#9c27b0',
            'DELETE': '#f44336'
        };
        return colors[action] || '#666';
    };

    const getStatusColor = (status) => {
        return status === 'SUCCESS' ? '#4caf50' : '#f44336';
    };

    const handleViewDetails = (log) => {
        setSelectedLog(log);
        setShowDetails(true);
    };

    if (loading) {
        return (
            <div style={{ padding: '40px', textAlign: 'center' }}>
                <div className="spinner"></div>
                <p>Loading activity logs...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div style={{ padding: '40px' }}>
                <div style={{
                    padding: '20px',
                    backgroundColor: '#fee',
                    border: '1px solid #fcc',
                    borderRadius: '8px',
                    color: '#c33'
                }}>
                    <h3>Error</h3>
                    <p>{error}</p>
                </div>
            </div>
        );
    }

    return (
        <div style={{ padding: '30px' }}>
            <button
                onClick={() => navigate('/admin')}
                style={{
                    marginBottom: '20px',
                    padding: '10px 16px',
                    backgroundColor: 'white',
                    color: '#666',
                    border: '1px solid #e0e0e0',
                    borderRadius: '6px',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px',
                    fontSize: '14px',
                    fontWeight: '500',
                    transition: 'all 0.2s'
                }}
                onMouseEnter={(e) => {
                    e.currentTarget.style.backgroundColor = '#f5f5f5';
                    e.currentTarget.style.borderColor = '#2196f3';
                }}
                onMouseLeave={(e) => {
                    e.currentTarget.style.backgroundColor = 'white';
                    e.currentTarget.style.borderColor = '#e0e0e0';
                }}
            >
                <ArrowLeft size={18} />
                Back to Admin Dashboard
            </button>

            <div style={{ marginBottom: '30px' }}>
                <h1 style={{ fontSize: '28px', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <Activity size={32} />
                    User Activity Logs
                </h1>
                <p style={{ color: '#666' }}>Track user login, logout, and system actions</p>
            </div>

            {logs.length === 0 ? (
                <div style={{
                    textAlign: 'center',
                    padding: '60px 20px',
                    backgroundColor: 'white',
                    border: '1px solid #e0e0e0',
                    borderRadius: '8px',
                    color: '#666'
                }}>
                    <Activity size={48} style={{ marginBottom: '20px', opacity: 0.5 }} />
                    <p>No activity logs found</p>
                </div>
            ) : (
                <div style={{
                    backgroundColor: '#fff',
                    border: '1px solid #e0e0e0',
                    borderRadius: '8px',
                    overflow: 'hidden'
                }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                        <thead>
                            <tr style={{ backgroundColor: '#f5f5f5', borderBottom: '2px solid #e0e0e0' }}>
                                <th style={{ padding: '15px', textAlign: 'left', fontWeight: '600' }}>Timestamp</th>
                                <th style={{ padding: '15px', textAlign: 'left', fontWeight: '600' }}>User</th>
                                <th style={{ padding: '15px', textAlign: 'left', fontWeight: '600' }}>Action</th>
                                <th style={{ padding: '15px', textAlign: 'left', fontWeight: '600' }}>Description</th>
                                <th style={{ padding: '15px', textAlign: 'left', fontWeight: '600' }}>Status</th>
                                <th style={{ padding: '15px', textAlign: 'center', fontWeight: '600' }}>Details</th>
                            </tr>
                        </thead>
                        <tbody>
                            {logs.map((log) => (
                                <tr key={log.id} style={{ borderBottom: '1px solid #e0e0e0' }}>
                                    <td style={{ padding: '15px' }}>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px' }}>
                                            <Clock size={14} style={{ color: '#666' }} />
                                            {formatTimestamp(log.timestamp)}
                                        </div>
                                    </td>
                                    <td style={{ padding: '15px' }}>
                                        <div>
                                            <div style={{ fontWeight: '600', marginBottom: '4px' }}>{log.username}</div>
                                            <div style={{ fontSize: '12px', color: '#666' }}>{log.userEmail}</div>
                                        </div>
                                    </td>
                                    <td style={{ padding: '15px' }}>
                                        <span style={{
                                            padding: '6px 12px',
                                            backgroundColor: `${getActionColor(log.action)}20`,
                                            color: getActionColor(log.action),
                                            borderRadius: '12px',
                                            fontSize: '12px',
                                            fontWeight: '600'
                                        }}>
                                            {log.action}
                                        </span>
                                    </td>
                                    <td style={{ padding: '15px', maxWidth: '300px' }}>
                                        <div style={{ 
                                            fontSize: '14px',
                                            overflow: 'hidden',
                                            textOverflow: 'ellipsis',
                                            whiteSpace: 'nowrap'
                                        }}>
                                            {log.description || '-'}
                                        </div>
                                    </td>
                                    <td style={{ padding: '15px' }}>
                                        <span style={{
                                            padding: '4px 10px',
                                            backgroundColor: `${getStatusColor(log.status)}20`,
                                            color: getStatusColor(log.status),
                                            borderRadius: '8px',
                                            fontSize: '11px',
                                            fontWeight: '600'
                                        }}>
                                            {log.status}
                                        </span>
                                    </td>
                                    <td style={{ padding: '15px', textAlign: 'center' }}>
                                        <button
                                            onClick={() => handleViewDetails(log)}
                                            style={{
                                                padding: '6px 12px',
                                                backgroundColor: '#2196f3',
                                                color: 'white',
                                                border: 'none',
                                                borderRadius: '4px',
                                                cursor: 'pointer',
                                                fontSize: '12px',
                                                fontWeight: '500'
                                            }}
                                        >
                                            View
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}

            {/* Details Modal */}
            {showDetails && selectedLog && (
                <div style={{
                    position: 'fixed',
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                    backgroundColor: 'rgba(0,0,0,0.5)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    zIndex: 2000
                }} onClick={() => setShowDetails(false)}>
                    <div style={{
                        backgroundColor: 'white',
                        borderRadius: '12px',
                        padding: '30px',
                        maxWidth: '700px',
                        width: '90%',
                        maxHeight: '80vh',
                        overflow: 'auto',
                        boxShadow: '0 10px 40px rgba(0,0,0,0.2)'
                    }} onClick={(e) => e.stopPropagation()}>
                        <div style={{
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center',
                            marginBottom: '25px',
                            paddingBottom: '15px',
                            borderBottom: '2px solid #e0e0e0'
                        }}>
                            <h2 style={{ fontSize: '22px', margin: 0 }}>Activity Details</h2>
                            <button
                                onClick={() => setShowDetails(false)}
                                style={{
                                    background: 'none',
                                    border: 'none',
                                    cursor: 'pointer',
                                    padding: '5px'
                                }}
                            >
                                <span style={{ fontSize: '24px', color: '#666' }}>×</span>
                            </button>
                        </div>

                        <div style={{ marginBottom: '20px' }}>
                            <div style={{ marginBottom: '20px', display: 'grid', gap: '15px' }}>
                                <div>
                                    <div style={{ fontSize: '13px', color: '#999', marginBottom: '5px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                                        <Clock size={14} />
                                        TIMESTAMP
                                    </div>
                                    <div style={{ fontSize: '15px', fontWeight: '600' }}>{formatTimestamp(selectedLog.timestamp)}</div>
                                </div>

                                <div>
                                    <div style={{ fontSize: '13px', color: '#999', marginBottom: '5px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                                        <User size={14} />
                                        USER
                                    </div>
                                    <div style={{ fontSize: '15px', fontWeight: '600' }}>{selectedLog.username}</div>
                                    <div style={{ fontSize: '13px', color: '#666' }}>{selectedLog.userEmail}</div>
                                </div>

                                <div>
                                    <div style={{ fontSize: '13px', color: '#999', marginBottom: '5px' }}>ACTION</div>
                                    <span style={{
                                        padding: '8px 16px',
                                        backgroundColor: `${getActionColor(selectedLog.action)}20`,
                                        color: getActionColor(selectedLog.action),
                                        borderRadius: '12px',
                                        fontSize: '14px',
                                        fontWeight: '600',
                                        display: 'inline-block'
                                    }}>
                                        {selectedLog.action}
                                    </span>
                                </div>

                                {selectedLog.description && (
                                    <div>
                                        <div style={{ fontSize: '13px', color: '#999', marginBottom: '5px' }}>DESCRIPTION</div>
                                        <div style={{ fontSize: '14px', lineHeight: '1.6' }}>{selectedLog.description}</div>
                                    </div>
                                )}

                                {selectedLog.resourceType && (
                                    <div>
                                        <div style={{ fontSize: '13px', color: '#999', marginBottom: '5px' }}>RESOURCE</div>
                                        <div style={{ fontSize: '14px' }}>
                                            <span style={{ fontWeight: '600' }}>{selectedLog.resourceType}</span>
                                            {selectedLog.resourceId && ` (ID: ${selectedLog.resourceId})`}
                                        </div>
                                    </div>
                                )}

                                <div>
                                    <div style={{ fontSize: '13px', color: '#999', marginBottom: '5px' }}>STATUS</div>
                                    <span style={{
                                        padding: '6px 12px',
                                        backgroundColor: `${getStatusColor(selectedLog.status)}20`,
                                        color: getStatusColor(selectedLog.status),
                                        borderRadius: '8px',
                                        fontSize: '13px',
                                        fontWeight: '600',
                                        display: 'inline-block'
                                    }}>
                                        {selectedLog.status}
                                    </span>
                                </div>

                                {selectedLog.errorMessage && (
                                    <div>
                                        <div style={{ fontSize: '13px', color: '#999', marginBottom: '5px' }}>ERROR MESSAGE</div>
                                        <div style={{
                                            padding: '12px',
                                            backgroundColor: '#fee',
                                            border: '1px solid #fcc',
                                            borderRadius: '6px',
                                            fontSize: '13px',
                                            color: '#c33',
                                            fontFamily: 'monospace'
                                        }}>
                                            {selectedLog.errorMessage}
                                        </div>
                                    </div>
                                )}

                                {selectedLog.ipAddress && (
                                    <div>
                                        <div style={{ fontSize: '13px', color: '#999', marginBottom: '5px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                                            <MapPin size={14} />
                                            IP ADDRESS
                                        </div>
                                        <div style={{ fontSize: '14px', fontFamily: 'monospace' }}>{selectedLog.ipAddress}</div>
                                    </div>
                                )}

                                {selectedLog.userAgent && (
                                    <div>
                                        <div style={{ fontSize: '13px', color: '#999', marginBottom: '5px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                                            <Monitor size={14} />
                                            USER AGENT
                                        </div>
                                        <div style={{
                                            fontSize: '12px',
                                            fontFamily: 'monospace',
                                            padding: '10px',
                                            backgroundColor: '#f5f5f5',
                                            borderRadius: '6px',
                                            wordBreak: 'break-all'
                                        }}>
                                            {selectedLog.userAgent}
                                        </div>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AdminActivityLogs;
