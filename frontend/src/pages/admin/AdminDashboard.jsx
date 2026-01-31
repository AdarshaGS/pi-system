import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Users, Activity, Shield, Wrench, Database, UserCheck } from 'lucide-react';
import '../../App.css';

const AdminDashboard = () => {
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const user = JSON.parse(localStorage.getItem('user'));
        if (!user || !user.token) {
            navigate('/login');
            return;
        }

        const fetchStats = async () => {
            try {
                const response = await fetch('http://localhost:8082/api/v1/admin/dashboard', {
                    headers: {
                        'Authorization': `Bearer ${user.token}`
                    }
                });

                if (!response.ok) {
                    if (response.status === 403) {
                        throw new Error('Access denied. Admin privileges required.');
                    }
                    throw new Error('Failed to fetch admin data');
                }

                const data = await response.json();
                setStats(data);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchStats();
    }, [navigate]);

    if (loading) {
        return (
            <div style={{ padding: '40px', textAlign: 'center' }}>
                <div className="spinner"></div>
                <p>Loading admin dashboard...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div style={{ padding: '40px' }}>
                <div className="error-message" style={{ 
                    padding: '20px', 
                    backgroundColor: '#fee', 
                    border: '1px solid #fcc',
                    borderRadius: '8px',
                    color: '#c33'
                }}>
                    <Shield size={24} style={{ marginBottom: '10px' }} />
                    <h3>Access Denied</h3>
                    <p>{error}</p>
                </div>
            </div>
        );
    }

    return (
        <div style={{ padding: '30px' }}>
            <div style={{ marginBottom: '30px' }}>
                <h1 style={{ fontSize: '28px', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <Shield size={32} />
                    Admin Dashboard
                </h1>
                <p style={{ color: '#666' }}>System administration and management</p>
            </div>

            {/* Stats Cards */}
            <div style={{ 
                display: 'grid', 
                gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
                gap: '20px',
                marginBottom: '30px'
            }}>
                <div style={{
                    padding: '20px',
                    backgroundColor: '#fff',
                    border: '1px solid #e0e0e0',
                    borderRadius: '8px',
                    boxShadow: '0 2px 4px rgba(0,0,0,0.05)'
                }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div>
                            <p style={{ color: '#666', fontSize: '14px' }}>Status</p>
                            <h3 style={{ fontSize: '24px', margin: '10px 0' }}>{stats?.status || 'N/A'}</h3>
                        </div>
                        <Activity size={40} style={{ color: '#4caf50' }} />
                    </div>
                </div>

                <div style={{
                    padding: '20px',
                    backgroundColor: '#fff',
                    border: '1px solid #e0e0e0',
                    borderRadius: '8px',
                    boxShadow: '0 2px 4px rgba(0,0,0,0.05)'
                }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div>
                            <p style={{ color: '#666', fontSize: '14px' }}>Active Jobs</p>
                            <h3 style={{ fontSize: '24px', margin: '10px 0' }}>{stats?.activeJobs || 0}</h3>
                        </div>
                        <Activity size={40} style={{ color: '#2196f3' }} />
                    </div>
                </div>
            </div>

            {/* Quick Actions */}
            <div style={{ 
                display: 'grid', 
                gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
                gap: '20px'
            }}>
                <Link to="/admin/users" style={{ textDecoration: 'none' }}>
                    <div style={{
                        padding: '25px',
                        backgroundColor: '#fff',
                        border: '2px solid #e0e0e0',
                        borderRadius: '8px',
                        cursor: 'pointer',
                        transition: 'all 0.3s',
                        '&:hover': { borderColor: '#2196f3' }
                    }}>
                        <Users size={32} style={{ color: '#2196f3', marginBottom: '15px' }} />
                        <h3 style={{ fontSize: '18px', marginBottom: '10px', color: '#333' }}>Manage Users</h3>
                        <p style={{ color: '#666', fontSize: '14px' }}>View and manage all system users</p>
                    </div>
                </Link>

                <Link to="/admin/utilities" style={{ textDecoration: 'none' }}>
                    <div style={{
                        padding: '25px',
                        backgroundColor: '#fff',
                        border: '2px solid #e0e0e0',
                        borderRadius: '8px',
                        cursor: 'pointer',
                        transition: 'all 0.3s'
                    }}>
                        <Wrench size={32} style={{ color: '#ff9800', marginBottom: '15px' }} />
                        <h3 style={{ fontSize: '18px', marginBottom: '10px', color: '#333' }}>Critical Logs</h3>
                        <p style={{ color: '#666', fontSize: '14px' }}>View critical errors and system logs</p>
                    </div>
                </Link>

                <Link to="/admin/external-services" style={{ textDecoration: 'none' }}>
                    <div style={{
                        padding: '25px',
                        backgroundColor: '#fff',
                        border: '2px solid #e0e0e0',
                        borderRadius: '8px',
                        cursor: 'pointer',
                        transition: 'all 0.3s'
                    }}>
                        <Database size={32} style={{ color: '#9c27b0', marginBottom: '15px' }} />
                        <h3 style={{ fontSize: '18px', marginBottom: '10px', color: '#333' }}>External Services</h3>
                        <p style={{ color: '#666', fontSize: '14px' }}>Configure external API keys and services</p>
                    </div>
                </Link>

                <Link to="/admin/activity-logs" style={{ textDecoration: 'none' }}>
                    <div style={{
                        padding: '25px',
                        backgroundColor: '#fff',
                        border: '2px solid #e0e0e0',
                        borderRadius: '8px',
                        cursor: 'pointer',
                        transition: 'all 0.3s'
                    }}>
                        <UserCheck size={32} style={{ color: '#00bcd4', marginBottom: '15px' }} />
                        <h3 style={{ fontSize: '18px', marginBottom: '10px', color: '#333' }}>Activity Logs</h3>
                        <p style={{ color: '#666', fontSize: '14px' }}>Track user login, logout, and actions</p>
                    </div>
                </Link>
            </div>

            {/* Welcome Message */}
            {stats?.message && (
                <div style={{
                    marginTop: '30px',
                    padding: '20px',
                    backgroundColor: '#f5f5f5',
                    borderRadius: '8px',
                    borderLeft: '4px solid #2196f3'
                }}>
                    <p style={{ fontSize: '16px', color: '#333' }}>{stats.message}</p>
                </div>
            )}
        </div>
    );
};

export default AdminDashboard;
