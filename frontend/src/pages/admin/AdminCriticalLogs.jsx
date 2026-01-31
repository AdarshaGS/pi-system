import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { AlertCircle, Clock, Code, FileText, X, ArrowLeft } from 'lucide-react';
import '../../App.css';

const AdminCriticalLogs = () => {
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedLog, setSelectedLog] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const user = JSON.parse(localStorage.getItem('user'));
        if (!user || !user.token) {
            navigate('/login');
            return;
        }

        const fetchLogs = async () => {
            try {
                const response = await fetch('http://localhost:8082/api/v1/admin/utilities/critical-logs', {
                    headers: {
                        'Authorization': `Bearer ${user.token}`
                    }
                });

                if (!response.ok) {
                    if (response.status === 403) {
                        throw new Error('Access denied. Admin privileges required.');
                    }
                    throw new Error('Failed to fetch critical logs');
                }

                const data = await response.json();
                setLogs(data);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchLogs();
    }, [navigate]);

    const formatDateTime = (timestamp) => {
        return new Date(timestamp).toLocaleString('en-IN', {
            day: '2-digit',
            month: 'short',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    };

    const getStatusColor = (status) => {
        if (status >= 500) return '#f44336';
        if (status >= 400) return '#ff9800';
        return '#666';
    };

    if (loading) {
        return (
            <div style={{ padding: '40px', textAlign: 'center' }}>
                <div className="spinner"></div>
                <p>Loading critical logs...</p>
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
            {/* Back Navigation Button */}
            <button
                onClick={() => navigate('/admin')}
                style={{
                    marginBottom: '20px',
                    padding: '10px 18px',
                    backgroundColor: '#f5f5f5',
                    border: '1px solid #ddd',
                    borderRadius: '6px',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px',
                    fontSize: '14px',
                    fontWeight: '500',
                    color: '#333',
                    transition: 'all 0.2s'
                }}
                onMouseEnter={(e) => {
                    e.currentTarget.style.backgroundColor = '#e0e0e0';
                    e.currentTarget.style.borderColor = '#bbb';
                }}
                onMouseLeave={(e) => {
                    e.currentTarget.style.backgroundColor = '#f5f5f5';
                    e.currentTarget.style.borderColor = '#ddd';
                }}
            >
                <ArrowLeft size={18} />
                Back to Admin Dashboard
            </button>

            <div style={{ marginBottom: '30px' }}>
                <h1 style={{ fontSize: '28px', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <AlertCircle size={32} style={{ color: '#f44336' }} />
                    Critical Error Logs
                </h1>
                <p style={{ color: '#666' }}>Latest 10 critical errors in the system</p>
            </div>

            {logs.length === 0 ? (
                <div style={{
                    textAlign: 'center',
                    padding: '60px 20px',
                    backgroundColor: '#fff',
                    borderRadius: '8px',
                    border: '1px solid #e0e0e0'
                }}>
                    <AlertCircle size={48} style={{ marginBottom: '20px', opacity: 0.5, color: '#4caf50' }} />
                    <h3 style={{ color: '#4caf50', marginBottom: '10px' }}>No Critical Errors!</h3>
                    <p style={{ color: '#666' }}>System is running smoothly with no critical errors.</p>
                </div>
            ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                    {logs.map((log) => (
                        <div
                            key={log.id}
                            onClick={() => setSelectedLog(log)}
                            style={{
                                backgroundColor: '#fff',
                                border: `1px solid ${getStatusColor(log.status)}20`,
                                borderLeft: `4px solid ${getStatusColor(log.status)}`,
                                borderRadius: '8px',
                                padding: '20px',
                                cursor: 'pointer',
                                transition: 'all 0.2s',
                                boxShadow: '0 2px 4px rgba(0,0,0,0.05)'
                            }}
                            onMouseEnter={(e) => {
                                e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.1)';
                                e.currentTarget.style.transform = 'translateY(-2px)';
                            }}
                            onMouseLeave={(e) => {
                                e.currentTarget.style.boxShadow = '0 2px 4px rgba(0,0,0,0.05)';
                                e.currentTarget.style.transform = 'translateY(0)';
                            }}
                        >
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '10px' }}>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                                    <span style={{
                                        padding: '4px 12px',
                                        backgroundColor: `${getStatusColor(log.status)}20`,
                                        color: getStatusColor(log.status),
                                        borderRadius: '4px',
                                        fontWeight: '600',
                                        fontSize: '14px'
                                    }}>
                                        {log.status}
                                    </span>
                                    <span style={{
                                        padding: '4px 12px',
                                        backgroundColor: '#f5f5f5',
                                        borderRadius: '4px',
                                        fontSize: '12px',
                                        fontWeight: '500',
                                        color: '#666'
                                    }}>
                                        {log.method}
                                    </span>
                                </div>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#666', fontSize: '14px' }}>
                                    <Clock size={14} />
                                    {formatDateTime(log.timestamp)}
                                </div>
                            </div>

                            <div style={{ marginBottom: '8px' }}>
                                <p style={{ fontSize: '16px', fontWeight: '600', color: '#333', marginBottom: '5px' }}>
                                    {log.errorCode || 'ERROR'}
                                </p>
                                <p style={{ fontSize: '14px', color: '#666', marginBottom: '8px' }}>
                                    {log.message}
                                </p>
                            </div>

                            <div style={{ display: 'flex', gap: '15px', fontSize: '13px', color: '#666' }}>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                                    <FileText size={14} />
                                    <span>{log.path}</span>
                                </div>
                                {log.requestId && (
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                                        <Code size={14} />
                                        <span>ID: {log.requestId}</span>
                                    </div>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* Log Details Modal */}
            {selectedLog && (
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
                }} onClick={() => setSelectedLog(null)}>
                    <div style={{
                        backgroundColor: 'white',
                        borderRadius: '12px',
                        padding: '30px',
                        maxWidth: '800px',
                        width: '90%',
                        maxHeight: '80vh',
                        overflow: 'auto',
                        boxShadow: '0 10px 40px rgba(0,0,0,0.2)'
                    }} onClick={(e) => e.stopPropagation()}>
                        <div style={{ 
                            display: 'flex', 
                            justifyContent: 'space-between', 
                            alignItems: 'center',
                            marginBottom: '20px',
                            paddingBottom: '15px',
                            borderBottom: '2px solid #e0e0e0'
                        }}>
                            <h2 style={{ fontSize: '24px', margin: 0 }}>Error Details</h2>
                            <button 
                                onClick={() => setSelectedLog(null)}
                                style={{
                                    background: 'none',
                                    border: 'none',
                                    cursor: 'pointer',
                                    padding: '5px'
                                }}
                            >
                                <X size={24} style={{ color: '#666' }} />
                            </button>
                        </div>

                        <div style={{ marginBottom: '30px' }}>
                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ 
                                    display: 'block', 
                                    fontSize: '12px', 
                                    color: '#666', 
                                    marginBottom: '5px',
                                    textTransform: 'uppercase',
                                    fontWeight: '600'
                                }}>Status & Method</label>
                                <div style={{ display: 'flex', gap: '10px' }}>
                                    <span style={{
                                        padding: '8px 16px',
                                        backgroundColor: `${getStatusColor(selectedLog.status)}20`,
                                        color: getStatusColor(selectedLog.status),
                                        borderRadius: '6px',
                                        fontWeight: '600'
                                    }}>
                                        {selectedLog.status}
                                    </span>
                                    <span style={{
                                        padding: '8px 16px',
                                        backgroundColor: '#f5f5f5',
                                        borderRadius: '6px',
                                        fontWeight: '600'
                                    }}>
                                        {selectedLog.method}
                                    </span>
                                </div>
                            </div>

                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ 
                                    display: 'block', 
                                    fontSize: '12px', 
                                    color: '#666', 
                                    marginBottom: '5px',
                                    textTransform: 'uppercase',
                                    fontWeight: '600'
                                }}>Timestamp</label>
                                <div style={{ 
                                    padding: '12px', 
                                    backgroundColor: '#f5f5f5', 
                                    borderRadius: '6px'
                                }}>
                                    {formatDateTime(selectedLog.timestamp)}
                                </div>
                            </div>

                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ 
                                    display: 'block', 
                                    fontSize: '12px', 
                                    color: '#666', 
                                    marginBottom: '5px',
                                    textTransform: 'uppercase',
                                    fontWeight: '600'
                                }}>Error Code</label>
                                <div style={{ 
                                    padding: '12px', 
                                    backgroundColor: '#f5f5f5', 
                                    borderRadius: '6px',
                                    fontWeight: '600',
                                    color: getStatusColor(selectedLog.status)
                                }}>
                                    {selectedLog.errorCode || 'INTERNAL_ERROR'}
                                </div>
                            </div>

                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ 
                                    display: 'block', 
                                    fontSize: '12px', 
                                    color: '#666', 
                                    marginBottom: '5px',
                                    textTransform: 'uppercase',
                                    fontWeight: '600'
                                }}>Message</label>
                                <div style={{ 
                                    padding: '12px', 
                                    backgroundColor: '#fff3e0', 
                                    borderRadius: '6px',
                                    border: '1px solid #ffe0b2'
                                }}>
                                    {selectedLog.message}
                                </div>
                            </div>

                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ 
                                    display: 'block', 
                                    fontSize: '12px', 
                                    color: '#666', 
                                    marginBottom: '5px',
                                    textTransform: 'uppercase',
                                    fontWeight: '600'
                                }}>Path</label>
                                <div style={{ 
                                    padding: '12px', 
                                    backgroundColor: '#f5f5f5', 
                                    borderRadius: '6px',
                                    fontFamily: 'monospace',
                                    fontSize: '14px'
                                }}>
                                    {selectedLog.path}
                                </div>
                            </div>

                            {selectedLog.requestId && (
                                <div style={{ marginBottom: '20px' }}>
                                    <label style={{ 
                                        display: 'block', 
                                        fontSize: '12px', 
                                        color: '#666', 
                                        marginBottom: '5px',
                                        textTransform: 'uppercase',
                                        fontWeight: '600'
                                    }}>Request ID</label>
                                    <div style={{ 
                                        padding: '12px', 
                                        backgroundColor: '#f5f5f5', 
                                        borderRadius: '6px',
                                        fontFamily: 'monospace',
                                        fontSize: '14px'
                                    }}>
                                        {selectedLog.requestId}
                                    </div>
                                </div>
                            )}

                            {selectedLog.stackTrace && (
                                <div style={{ marginBottom: '20px' }}>
                                    <label style={{ 
                                        display: 'block', 
                                        fontSize: '12px', 
                                        color: '#666', 
                                        marginBottom: '5px',
                                        textTransform: 'uppercase',
                                        fontWeight: '600'
                                    }}>Stack Trace</label>
                                    <div style={{ 
                                        padding: '12px', 
                                        backgroundColor: '#1e1e1e', 
                                        color: '#d4d4d4',
                                        borderRadius: '6px',
                                        fontFamily: 'monospace',
                                        fontSize: '12px',
                                        maxHeight: '300px',
                                        overflow: 'auto',
                                        whiteSpace: 'pre-wrap',
                                        wordBreak: 'break-word'
                                    }}>
                                        {selectedLog.stackTrace}
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AdminCriticalLogs;
