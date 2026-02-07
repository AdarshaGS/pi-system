import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
    ToggleLeft, 
    ToggleRight, 
    Filter, 
    Search, 
    AlertCircle,
    CheckCircle,
    Loader,
    RefreshCw,
    ArrowLeft
} from 'lucide-react';
import { useFeatures } from '../../contexts/FeatureContext';
import '../../App.css';

const AdminFeatures = () => {
    const [features, setFeatures] = useState([]);
    const [filteredFeatures, setFilteredFeatures] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedCategory, setSelectedCategory] = useState('all');
    const [searchTerm, setSearchTerm] = useState('');
    const [categories, setCategories] = useState([]);
    const [updating, setUpdating] = useState(null);
    const [successMessage, setSuccessMessage] = useState(null);
    const navigate = useNavigate();
    const { refreshFeatures } = useFeatures();

    const user = JSON.parse(localStorage.getItem('user'));
    const API_BASE = 'http://localhost:8082';

    useEffect(() => {
        if (!user || !user.token) {
            navigate('/login');
            return;
        }
        fetchFeatures();
    }, [navigate]);

    useEffect(() => {
        filterFeatures();
    }, [features, selectedCategory, searchTerm]);

    const fetchFeatures = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await fetch(`${API_BASE}/api/v1/admin/features`, {
                headers: {
                    'Authorization': `Bearer ${user.token}`
                }
            });

            if (!response.ok) {
                if (response.status === 403) {
                    throw new Error('Access denied. Admin privileges required.');
                }
                throw new Error('Failed to fetch features');
            }

            const data = await response.json();
            setFeatures(data);
            
            // Extract unique categories
            const uniqueCategories = [...new Set(data.map(f => f.category))].sort();
            setCategories(uniqueCategories);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const filterFeatures = () => {
        let filtered = features;

        // Filter by category
        if (selectedCategory !== 'all') {
            filtered = filtered.filter(f => f.category === selectedCategory);
        }

        // Filter by search term
        if (searchTerm) {
            filtered = filtered.filter(f => 
                f.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                f.displayName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                f.description.toLowerCase().includes(searchTerm.toLowerCase())
            );
        }

        setFilteredFeatures(filtered);
    };

    const toggleFeature = async (featureName, currentEnabled) => {
        setUpdating(featureName);
        setError(null);
        
        try {
            const action = currentEnabled ? 'disable' : 'enable';
            const response = await fetch(
                `${API_BASE}/api/v1/admin/features/${featureName}/${action}`,
                {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${user.token}`
                    }
                }
            );

            if (!response.ok) {
                throw new Error(`Failed to ${action} feature`);
            }

            const result = await response.json();
            
            // Update local state
            setFeatures(prevFeatures => 
                prevFeatures.map(f => 
                    f.name === featureName 
                        ? { ...f, enabled: !currentEnabled }
                        : f
                )
            );

            // Refresh global feature context
            await refreshFeatures();

            // Show success message
            setSuccessMessage(`${result.displayName} ${action}d successfully`);
            setTimeout(() => setSuccessMessage(null), 3000);
        } catch (err) {
            setError(err.message);
        } finally {
            setUpdating(null);
        }
    };

    const getCategoryColor = (category) => {
        const colors = {
            'budget': '#4caf50',
            'tax': '#2196f3',
            'investments': '#ff9800',
            'banking': '#9c27b0',
            'insurance': '#e91e63',
            'networth': '#00bcd4',
            'admin': '#f44336',
            'planning': '#795548',
            'core': '#607d8b'
        };
        return colors[category] || '#666';
    };

    if (loading) {
        return (
            <div style={{ padding: '40px', textAlign: 'center' }}>
                <Loader className="spinner" size={40} />
                <p style={{ marginTop: '20px' }}>Loading features...</p>
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
                        <ToggleRight size={32} />
                        Feature Management
                    </h1>
                    <button
                        onClick={fetchFeatures}
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
                <p style={{ color: '#666' }}>Enable or disable features across the application</p>
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
                    <AlertCircle size={20} style={{ color: '#f44336' }} />
                    <span style={{ color: '#c62828' }}>{error}</span>
                </div>
            )}

            {/* Filters */}
            <div style={{
                display: 'flex',
                gap: '15px',
                marginBottom: '30px',
                flexWrap: 'wrap'
            }}>
                {/* Category Filter */}
                <div style={{ flex: '1', minWidth: '200px' }}>
                    <label style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px', color: '#666' }}>
                        <Filter size={16} />
                        Category
                    </label>
                    <select
                        value={selectedCategory}
                        onChange={(e) => setSelectedCategory(e.target.value)}
                        style={{
                            width: '100%',
                            padding: '10px',
                            border: '1px solid #ddd',
                            borderRadius: '6px',
                            fontSize: '14px'
                        }}
                    >
                        <option value="all">All Categories ({features.length})</option>
                        {categories.map(cat => (
                            <option key={cat} value={cat}>
                                {cat.charAt(0).toUpperCase() + cat.slice(1)} 
                                ({features.filter(f => f.category === cat).length})
                            </option>
                        ))}
                    </select>
                </div>

                {/* Search */}
                <div style={{ flex: '2', minWidth: '300px' }}>
                    <label style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px', color: '#666' }}>
                        <Search size={16} />
                        Search
                    </label>
                    <input
                        type="text"
                        placeholder="Search features..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        style={{
                            width: '100%',
                            padding: '10px',
                            border: '1px solid #ddd',
                            borderRadius: '6px',
                            fontSize: '14px'
                        }}
                    />
                </div>
            </div>

            {/* Stats */}
            <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
                gap: '15px',
                marginBottom: '30px'
            }}>
                <div style={{
                    padding: '15px',
                    backgroundColor: '#e8f5e9',
                    borderRadius: '8px',
                    textAlign: 'center'
                }}>
                    <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#4caf50' }}>
                        {features.filter(f => f.enabled).length}
                    </div>
                    <div style={{ fontSize: '12px', color: '#666', marginTop: '5px' }}>Enabled</div>
                </div>
                <div style={{
                    padding: '15px',
                    backgroundColor: '#ffebee',
                    borderRadius: '8px',
                    textAlign: 'center'
                }}>
                    <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#f44336' }}>
                        {features.filter(f => !f.enabled).length}
                    </div>
                    <div style={{ fontSize: '12px', color: '#666', marginTop: '5px' }}>Disabled</div>
                </div>
                <div style={{
                    padding: '15px',
                    backgroundColor: '#e3f2fd',
                    borderRadius: '8px',
                    textAlign: 'center'
                }}>
                    <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#2196f3' }}>
                        {categories.length}
                    </div>
                    <div style={{ fontSize: '12px', color: '#666', marginTop: '5px' }}>Categories</div>
                </div>
                <div style={{
                    padding: '15px',
                    backgroundColor: '#f3e5f5',
                    borderRadius: '8px',
                    textAlign: 'center'
                }}>
                    <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#9c27b0' }}>
                        {filteredFeatures.length}
                    </div>
                    <div style={{ fontSize: '12px', color: '#666', marginTop: '5px' }}>Showing</div>
                </div>
            </div>

            {/* Features Table */}
            <div style={{
                backgroundColor: '#fff',
                borderRadius: '8px',
                border: '1px solid #e0e0e0',
                overflow: 'hidden'
            }}>
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                    <thead>
                        <tr style={{ backgroundColor: '#f5f5f5', borderBottom: '2px solid #e0e0e0' }}>
                            <th style={{ padding: '15px', textAlign: 'left', fontWeight: '600' }}>Feature</th>
                            <th style={{ padding: '15px', textAlign: 'left', fontWeight: '600' }}>Category</th>
                            <th style={{ padding: '15px', textAlign: 'left', fontWeight: '600' }}>Description</th>
                            <th style={{ padding: '15px', textAlign: 'center', fontWeight: '600' }}>Status</th>
                            <th style={{ padding: '15px', textAlign: 'center', fontWeight: '600' }}>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filteredFeatures.length === 0 ? (
                            <tr>
                                <td colSpan="5" style={{ padding: '40px', textAlign: 'center', color: '#999' }}>
                                    No features found matching your filters
                                </td>
                            </tr>
                        ) : (
                            filteredFeatures.map((feature) => (
                                <tr 
                                    key={feature.name}
                                    style={{ 
                                        borderBottom: '1px solid #f0f0f0',
                                        transition: 'background-color 0.2s'
                                    }}
                                    onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#fafafa'}
                                    onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                                >
                                    <td style={{ padding: '15px' }}>
                                        <div style={{ fontWeight: '500', marginBottom: '4px' }}>
                                            {feature.displayName}
                                        </div>
                                        <div style={{ fontSize: '12px', color: '#999' }}>
                                            {feature.name}
                                        </div>
                                    </td>
                                    <td style={{ padding: '15px' }}>
                                        <span style={{
                                            padding: '4px 12px',
                                            backgroundColor: getCategoryColor(feature.category) + '20',
                                            color: getCategoryColor(feature.category),
                                            borderRadius: '12px',
                                            fontSize: '12px',
                                            fontWeight: '500'
                                        }}>
                                            {feature.category}
                                        </span>
                                    </td>
                                    <td style={{ padding: '15px', color: '#666', fontSize: '14px' }}>
                                        {feature.description}
                                    </td>
                                    <td style={{ padding: '15px', textAlign: 'center' }}>
                                        <span style={{
                                            padding: '6px 12px',
                                            backgroundColor: feature.enabled ? '#e8f5e9' : '#ffebee',
                                            color: feature.enabled ? '#4caf50' : '#f44336',
                                            borderRadius: '6px',
                                            fontSize: '12px',
                                            fontWeight: '600',
                                            display: 'inline-flex',
                                            alignItems: 'center',
                                            gap: '6px'
                                        }}>
                                            {feature.enabled ? (
                                                <>
                                                    <CheckCircle size={14} />
                                                    Enabled
                                                </>
                                            ) : (
                                                <>
                                                    <AlertCircle size={14} />
                                                    Disabled
                                                </>
                                            )}
                                        </span>
                                    </td>
                                    <td style={{ padding: '15px', textAlign: 'center' }}>
                                        <button
                                            onClick={() => toggleFeature(feature.name, feature.enabled)}
                                            disabled={updating === feature.name}
                                            style={{
                                                padding: '8px 16px',
                                                backgroundColor: feature.enabled ? '#f44336' : '#4caf50',
                                                color: 'white',
                                                border: 'none',
                                                borderRadius: '6px',
                                                cursor: updating === feature.name ? 'not-allowed' : 'pointer',
                                                fontSize: '13px',
                                                fontWeight: '500',
                                                display: 'inline-flex',
                                                alignItems: 'center',
                                                gap: '6px',
                                                opacity: updating === feature.name ? 0.6 : 1,
                                                transition: 'all 0.2s'
                                            }}
                                        >
                                            {updating === feature.name ? (
                                                <>
                                                    <Loader size={14} className="spinner" />
                                                    Updating...
                                                </>
                                            ) : feature.enabled ? (
                                                <>
                                                    <ToggleLeft size={14} />
                                                    Disable
                                                </>
                                            ) : (
                                                <>
                                                    <ToggleRight size={14} />
                                                    Enable
                                                </>
                                            )}
                                        </button>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            {/* Info Box */}
            <div style={{
                marginTop: '30px',
                padding: '20px',
                backgroundColor: '#e3f2fd',
                borderRadius: '8px',
                borderLeft: '4px solid #2196f3'
            }}>
                <h4 style={{ marginBottom: '10px', color: '#1976d2' }}>ℹ️ About Feature Toggles</h4>
                <ul style={{ margin: 0, paddingLeft: '20px', color: '#666', lineHeight: '1.8' }}>
                    <li>Changes take effect immediately - no restart required</li>
                    <li>Disabled features will return 403 Forbidden on API calls</li>
                    <li>UI components can query feature status and hide disabled features</li>
                    <li>All configuration is stored in the database</li>
                </ul>
            </div>
        </div>
    );
};

export default AdminFeatures;
