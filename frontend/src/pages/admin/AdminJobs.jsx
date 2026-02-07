import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Play, RefreshCw, Clock, Tag, ArrowLeft, CheckCircle, XCircle } from 'lucide-react';
import '../../App.css';

const AdminJobs = () => {
    const [jobs, setJobs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [executing, setExecuting] = useState(null);
    const [error, setError] = useState(null);
    const [successMessage, setSuccessMessage] = useState(null);
    const [selectedCategory, setSelectedCategory] = useState('all');
    const navigate = useNavigate();

    const user = JSON.parse(localStorage.getItem('user'));
    const API_BASE = 'http://localhost:8082';

    useEffect(() => {
        if (!user || !user.token) {
            navigate('/login');
            return;
        }
        fetchJobs();
    }, [navigate]);

    const fetchJobs = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await fetch(`${API_BASE}/api/v1/admin/jobs`, {
                headers: {
                    'Authorization': `Bearer ${user.token}`
                }
            });

            if (!response.ok) {
                throw new Error('Failed to fetch jobs');
            }

            const data = await response.json();
            setJobs(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const executeJob = async (jobName) => {
        setExecuting(jobName);
        setError(null);
        setSuccessMessage(null);

        try {
            const response = await fetch(`${API_BASE}/api/v1/admin/jobs/${jobName}/execute`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${user.token}`
                }
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Failed to execute job');
            }

            setSuccessMessage(`Job "${jobName}" executed successfully!`);
            setTimeout(() => setSuccessMessage(null), 5000);
        } catch (err) {
            setError(err.message);
            setTimeout(() => setError(null), 5000);
        } finally {
            setExecuting(null);
        }
    };

    const getCategoryColor = (category) => {
        const colors = {
            'Alerts': '#f44336',
            'Subscriptions': '#2196f3',
            'Budget': '#4caf50',
            'Investments': '#ff9800'
        };
        return colors[category] || '#666';
    };

    const categories = ['all', ...new Set(jobs.map(j => j.category))];
    const filteredJobs = selectedCategory === 'all' 
        ? jobs 
        : jobs.filter(j => j.category === selectedCategory);

    if (loading) {
        return (
            <div style={{ padding: '40px', textAlign: 'center' }}>
                <div className="spinner"></div>
                <p>Loading jobs...</p>
            </div>
        );
    }

    return (
        <div style={{ padding: '30px', maxWidth: '1400px', margin: '0 auto' }}>
            {/* Header */}
            <div style={{ marginBottom: '30px' }}>
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

                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                    <h1 style={{ fontSize: '28px', display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <Clock size={32} />
                        Scheduled Jobs
                    </h1>
                    <button
                        onClick={fetchJobs}
                        style={{
                            padding: '10px 20px',
                            backgroundColor: '#2196f3',
                            color: 'white',
                            border: 'none',
                            borderRadius: '6px',
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '8px'
                        }}
                    >
                        <RefreshCw size={16} />
                        Refresh
                    </button>
                </div>
                <p style={{ color: '#666' }}>Manage and execute scheduled jobs</p>
            </div>

            {/* Success Message */}
            {successMessage && (
                <div style={{
                    padding: '15px',
                    backgroundColor: '#e8f5e9',
                    border: '1px solid #4caf50',
                    borderRadius: '8px',
                    marginBottom: '20px',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '10px'
                }}>
                    <CheckCircle size={20} style={{ color: '#4caf50' }} />
                    <span style={{ color: '#2e7d32' }}>{successMessage}</span>
                </div>
            )}

            {/* Error Message */}
            {error && (
                <div style={{
                    padding: '15px',
                    backgroundColor: '#ffebee',
                    border: '1px solid #f44336',
                    borderRadius: '8px',
                    marginBottom: '20px',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '10px'
                }}>
                    <XCircle size={20} style={{ color: '#f44336' }} />
                    <span style={{ color: '#c62828' }}>{error}</span>
                </div>
            )}

            {/* Category Filter */}
            <div style={{ marginBottom: '20px' }}>
                <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
                    {categories.map(cat => (
                        <button
                            key={cat}
                            onClick={() => setSelectedCategory(cat)}
                            style={{
                                padding: '8px 16px',
                                backgroundColor: selectedCategory === cat ? '#2196f3' : 'white',
                                color: selectedCategory === cat ? 'white' : '#666',
                                border: `1px solid ${selectedCategory === cat ? '#2196f3' : '#e0e0e0'}`,
                                borderRadius: '20px',
                                cursor: 'pointer',
                                fontSize: '14px',
                                fontWeight: '500',
                                transition: 'all 0.2s',
                                textTransform: 'capitalize'
                            }}
                        >
                            {cat} ({cat === 'all' ? jobs.length : jobs.filter(j => j.category === cat).length})
                        </button>
                    ))}
                </div>
            </div>

            {/* Jobs Grid */}
            <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))',
                gap: '20px'
            }}>
                {filteredJobs.map((job) => (
                    <div
                        key={job.jobName}
                        style={{
                            backgroundColor: 'white',
                            border: '1px solid #e0e0e0',
                            borderRadius: '8px',
                            padding: '20px',
                            boxShadow: '0 2px 4px rgba(0,0,0,0.05)',
                            transition: 'transform 0.2s, box-shadow 0.2s'
                        }}
                        onMouseEnter={(e) => {
                            e.currentTarget.style.transform = 'translateY(-2px)';
                            e.currentTarget.style.boxShadow = '0 4px 8px rgba(0,0,0,0.1)';
                        }}
                        onMouseLeave={(e) => {
                            e.currentTarget.style.transform = 'translateY(0)';
                            e.currentTarget.style.boxShadow = '0 2px 4px rgba(0,0,0,0.05)';
                        }}
                    >
                        {/* Category Badge */}
                        <div style={{
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '5px',
                            padding: '4px 10px',
                            backgroundColor: `${getCategoryColor(job.category)}15`,
                            color: getCategoryColor(job.category),
                            borderRadius: '12px',
                            fontSize: '12px',
                            fontWeight: '600',
                            marginBottom: '12px'
                        }}>
                            <Tag size={12} />
                            {job.category}
                        </div>

                        {/* Job Name */}
                        <h3 style={{
                            fontSize: '16px',
                            fontWeight: '600',
                            marginBottom: '8px',
                            color: '#333'
                        }}>
                            {job.jobName.replace(/_/g, ' ')}
                        </h3>

                        {/* Description */}
                        <p style={{
                            fontSize: '14px',
                            color: '#666',
                            marginBottom: '12px',
                            lineHeight: '1.5'
                        }}>
                            {job.description}
                        </p>

                        {/* Schedule */}
                        <div style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: '6px',
                            fontSize: '13px',
                            color: '#999',
                            marginBottom: '15px'
                        }}>
                            <Clock size={14} />
                            <span>{job.schedule}</span>
                        </div>

                        {/* Execute Button */}
                        {job.canRunManually && (
                            <button
                                onClick={() => executeJob(job.jobName)}
                                disabled={executing === job.jobName}
                                style={{
                                    width: '100%',
                                    padding: '10px',
                                    backgroundColor: executing === job.jobName ? '#ccc' : '#4caf50',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '6px',
                                    cursor: executing === job.jobName ? 'not-allowed' : 'pointer',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    gap: '8px',
                                    fontSize: '14px',
                                    fontWeight: '600',
                                    transition: 'background-color 0.2s'
                                }}
                                onMouseEnter={(e) => {
                                    if (executing !== job.jobName) {
                                        e.currentTarget.style.backgroundColor = '#45a049';
                                    }
                                }}
                                onMouseLeave={(e) => {
                                    if (executing !== job.jobName) {
                                        e.currentTarget.style.backgroundColor = '#4caf50';
                                    }
                                }}
                            >
                                <Play size={16} />
                                {executing === job.jobName ? 'Executing...' : 'Run Now'}
                            </button>
                        )}
                    </div>
                ))}
            </div>

            {filteredJobs.length === 0 && (
                <div style={{
                    padding: '60px',
                    textAlign: 'center',
                    color: '#999'
                }}>
                    <Clock size={64} style={{ opacity: 0.3, marginBottom: '20px' }} />
                    <p>No jobs found for this category</p>
                </div>
            )}
        </div>
    );
};

export default AdminJobs;
