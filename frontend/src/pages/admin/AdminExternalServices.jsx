import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Settings, Save, RefreshCw, Database, Eye, EyeOff, ArrowLeft, Plus, Edit, X } from 'lucide-react';
import { externalServicesApi } from '../../api';
import '../../App.css';

const AdminExternalServices = () => {
    const [services, setServices] = useState([]);
    const [selectedService, setSelectedService] = useState(null);
    const [properties, setProperties] = useState([]);
    const [loading, setLoading] = useState(true);
    const [loadingProperties, setLoadingProperties] = useState(false);
    const [error, setError] = useState(null);
    const [saving, setSaving] = useState(false);
    const [editedProperties, setEditedProperties] = useState({});
    const [showPassword, setShowPassword] = useState({});
    const [showCreateServiceModal, setShowCreateServiceModal] = useState(false);
    const [showAddPropertyModal, setShowAddPropertyModal] = useState(false);
    const [newServiceName, setNewServiceName] = useState('');
    const [newProperty, setNewProperty] = useState({ name: '', value: '' });
    const navigate = useNavigate();

    useEffect(() => {
        const user = JSON.parse(localStorage.getItem('user'));
        if (!user || !user.token) {
            navigate('/login');
            return;
        }

        fetchServices(user.token);
    }, [navigate]);

    const fetchServices = async (token) => {
        setLoading(true);
        setError(null);

        try {
            const data = await externalServicesApi.getAllServices(token);
            // Transform backend data to include display name and description
            const transformedServices = data.map(service => ({
                id: service.id,
                name: service.serviceName,
                displayName: formatServiceName(service.serviceName),
                description: `Configuration for ${formatServiceName(service.serviceName)}`
            }));
            setServices(transformedServices);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const formatServiceName = (serviceName) => {
        return serviceName
            .split('_')
            .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
            .join(' ');
    };

    const fetchServiceProperties = async (serviceName) => {
        const user = JSON.parse(localStorage.getItem('user'));
        setLoadingProperties(true);
        setError(null);

        try {
            const response = await fetch(`http://localhost:8082/api/v1/external-services/${serviceName}`, {
                headers: {
                    'Authorization': `Bearer ${user.token}`
                }
            });

            if (!response.ok) {
                throw new Error('Failed to fetch service properties');
            }

            const data = await response.json();
            setProperties(data);
            
            // Initialize edited properties with current values
            const initialEdits = {};
            data.forEach(prop => {
                initialEdits[prop.id] = prop.value;
            });
            setEditedProperties(initialEdits);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoadingProperties(false);
        }
    };

    const handleServiceSelect = (service) => {
        setSelectedService(service);
        fetchServiceProperties(service.name);
    };

    const handlePropertyChange = (propertyId, newValue) => {
        setEditedProperties(prev => ({
            ...prev,
            [propertyId]: newValue
        }));
    };

    const handleSaveProperties = async () => {
        const user = JSON.parse(localStorage.getItem('user'));
        setSaving(true);
        setError(null);

        try {
            // Update each modified property
            const updatePromises = properties.map(async (prop) => {
                if (editedProperties[prop.id] !== prop.value) {
                    return await externalServicesApi.updateProperty(prop.id, editedProperties[prop.id], user.token);
                }
                return null;
            });

            const results = await Promise.all(updatePromises);
            const changes = results.filter(r => r !== null);

            if (changes.length > 0) {
                alert('Properties updated successfully!');
                // Refresh properties
                await fetchServiceProperties(selectedService.name);
            } else {
                alert('No changes detected');
            }
        } catch (err) {
            alert('Failed to save properties: ' + err.message);
            setError(err.message);
        } finally {
            setSaving(false);
        }
    };

    const handleCreateService = async () => {
        const user = JSON.parse(localStorage.getItem('user'));
        if (!newServiceName.trim()) {
            alert('Please enter a service name');
            return;
        }

        try {
            await externalServicesApi.createService(newServiceName, user.token);
            setNewServiceName('');
            setShowCreateServiceModal(false);
            await fetchServices(user.token);
            alert('Service created successfully!');
        } catch (err) {
            alert('Failed to create service: ' + err.message);
        }
    };

    const handleAddProperty = async () => {
        const user = JSON.parse(localStorage.getItem('user'));
        if (!newProperty.name.trim() || !newProperty.value.trim()) {
            alert('Please enter both property name and value');
            return;
        }

        try {
            const serviceEntity = services.find(s => s.name === selectedService.name);
            await externalServicesApi.createProperty(serviceEntity.id, newProperty.name, newProperty.value, user.token);
            setNewProperty({ name: '', value: '' });
            setShowAddPropertyModal(false);
            await fetchServiceProperties(selectedService.name);
            alert('Property added successfully!');
        } catch (err) {
            alert('Failed to add property: ' + err.message);
        }
    };

    const togglePasswordVisibility = (propertyId) => {
        setShowPassword(prev => ({
            ...prev,
            [propertyId]: !prev[propertyId]
        }));
    };

    const isPasswordField = (propertyName) => {
        const passwordKeywords = ['password', 'secret', 'key', 'token', 'api_key', 'apikey'];
        return passwordKeywords.some(keyword => propertyName.toLowerCase().includes(keyword));
    };

    if (loading) {
        return (
            <div style={{ padding: '40px', textAlign: 'center' }}>
                <div className="spinner"></div>
                <p>Loading services...</p>
            </div>
        );
    }

    return (
        <div style={{ padding: '30px' }}>
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
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                        <h1 style={{ fontSize: '28px', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '10px' }}>
                            <Database size={32} />
                            External Services Configuration
                        </h1>
                        <p style={{ color: '#666' }}>Manage external service API keys and configuration</p>
                    </div>
                    <button
                        onClick={() => setShowCreateServiceModal(true)}
                        style={{
                            padding: '10px 20px',
                            backgroundColor: '#4caf50',
                            color: 'white',
                            border: 'none',
                            borderRadius: '6px',
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '8px',
                            fontSize: '14px',
                            fontWeight: '600'
                        }}
                    >
                        <Plus size={18} />
                        Add New Service
                    </button>
                </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '20px' }}>
                {/* Services List */}
                <div style={{
                    backgroundColor: '#fff',
                    border: '1px solid #e0e0e0',
                    borderRadius: '8px',
                    overflow: 'hidden',
                    boxShadow: '0 2px 4px rgba(0,0,0,0.05)',
                    height: 'fit-content'
                }}>
                    <div style={{
                        padding: '15px',
                        backgroundColor: '#f5f5f5',
                        borderBottom: '2px solid #e0e0e0',
                        fontWeight: '600',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '8px'
                    }}>
                        <Settings size={18} />
                        Available Services
                    </div>
                    <div>
                        {services.map((service) => (
                            <div
                                key={service.name}
                                onClick={() => handleServiceSelect(service)}
                                style={{
                                    padding: '15px',
                                    cursor: 'pointer',
                                    borderBottom: '1px solid #e0e0e0',
                                    backgroundColor: selectedService?.name === service.name ? '#e3f2fd' : 'white',
                                    transition: 'background-color 0.2s'
                                }}
                                onMouseEnter={(e) => {
                                    if (selectedService?.name !== service.name) {
                                        e.currentTarget.style.backgroundColor = '#f5f5f5';
                                    }
                                }}
                                onMouseLeave={(e) => {
                                    if (selectedService?.name !== service.name) {
                                        e.currentTarget.style.backgroundColor = 'white';
                                    }
                                }}
                            >
                                <div style={{ fontWeight: '600', marginBottom: '4px' }}>
                                    {service.displayName}
                                </div>
                                <div style={{ fontSize: '12px', color: '#666' }}>
                                    {service.description}
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Properties Panel */}
                <div style={{
                    backgroundColor: '#fff',
                    border: '1px solid #e0e0e0',
                    borderRadius: '8px',
                    overflow: 'hidden',
                    boxShadow: '0 2px 4px rgba(0,0,0,0.05)'
                }}>
                    {!selectedService ? (
                        <div style={{ 
                            padding: '60px', 
                            textAlign: 'center', 
                            color: '#999',
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: 'center',
                            gap: '15px'
                        }}>
                            <Database size={64} style={{ opacity: 0.3 }} />
                            <p style={{ fontSize: '16px' }}>Select a service to view and edit its configuration</p>
                        </div>
                    ) : (
                        <>
                            <div style={{
                                padding: '15px',
                                backgroundColor: '#f5f5f5',
                                borderBottom: '2px solid #e0e0e0',
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center'
                            }}>
                                <div>
                                    <div style={{ fontWeight: '600', fontSize: '18px' }}>
                                        {selectedService.displayName}
                                    </div>
                                    <div style={{ fontSize: '12px', color: '#666', marginTop: '4px' }}>
                                        {selectedService.description}
                                    </div>
                                </div>
                                <div style={{ display: 'flex', gap: '10px' }}>
                                    <button
                                        onClick={() => setShowAddPropertyModal(true)}
                                        style={{
                                            padding: '8px 16px',
                                            backgroundColor: '#4caf50',
                                            color: 'white',
                                            border: 'none',
                                            borderRadius: '6px',
                                            cursor: 'pointer',
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: '6px',
                                            fontSize: '13px',
                                            fontWeight: '500'
                                        }}
                                    >
                                        <Plus size={14} />
                                        Add Property
                                    </button>
                                    <button
                                        onClick={() => fetchServiceProperties(selectedService.name)}
                                        disabled={loadingProperties}
                                        style={{
                                            padding: '8px 16px',
                                            backgroundColor: '#2196f3',
                                            color: 'white',
                                            border: 'none',
                                            borderRadius: '6px',
                                            cursor: loadingProperties ? 'not-allowed' : 'pointer',
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: '6px',
                                            fontSize: '13px',
                                            fontWeight: '500'
                                        }}
                                    >
                                        <RefreshCw size={14} />
                                        Refresh
                                    </button>
                                </div>
                            </div>

                            {loadingProperties ? (
                                <div style={{ padding: '40px', textAlign: 'center' }}>
                                    <div className="spinner"></div>
                                    <p>Loading properties...</p>
                                </div>
                            ) : error ? (
                                <div style={{ padding: '20px' }}>
                                    <div style={{
                                        padding: '15px',
                                        backgroundColor: '#fee',
                                        border: '1px solid #fcc',
                                        borderRadius: '6px',
                                        color: '#c33'
                                    }}>
                                        {error}
                                    </div>
                                </div>
                            ) : properties.length === 0 ? (
                                <div style={{ padding: '40px', textAlign: 'center', color: '#999' }}>
                                    <p>No properties configured for this service</p>
                                </div>
                            ) : (
                                <>
                                    <div style={{ padding: '20px' }}>
                                        {properties.map((prop) => (
                                            <div key={prop.id} style={{ marginBottom: '20px' }}>
                                                <label style={{
                                                    display: 'block',
                                                    fontSize: '14px',
                                                    fontWeight: '600',
                                                    marginBottom: '8px',
                                                    textTransform: 'capitalize'
                                                }}>
                                                    {prop.name.replace(/_/g, ' ')}
                                                </label>
                                                <div style={{ position: 'relative' }}>
                                                    <input
                                                        type={isPasswordField(prop.name) && !showPassword[prop.id] ? 'password' : 'text'}
                                                        value={editedProperties[prop.id] || ''}
                                                        onChange={(e) => handlePropertyChange(prop.id, e.target.value)}
                                                        style={{
                                                            width: '100%',
                                                            padding: '12px',
                                                            paddingRight: isPasswordField(prop.name) ? '45px' : '12px',
                                                            border: '1px solid #e0e0e0',
                                                            borderRadius: '6px',
                                                            fontSize: '14px',
                                                            fontFamily: isPasswordField(prop.name) && !showPassword[prop.id] ? 'monospace' : 'inherit'
                                                        }}
                                                    />
                                                    {isPasswordField(prop.name) && (
                                                        <button
                                                            onClick={() => togglePasswordVisibility(prop.id)}
                                                            style={{
                                                                position: 'absolute',
                                                                right: '10px',
                                                                top: '50%',
                                                                transform: 'translateY(-50%)',
                                                                background: 'none',
                                                                border: 'none',
                                                                cursor: 'pointer',
                                                                padding: '5px',
                                                                display: 'flex',
                                                                alignItems: 'center'
                                                            }}
                                                        >
                                                            {showPassword[prop.id] ? <EyeOff size={18} color="#666" /> : <Eye size={18} color="#666" />}
                                                        </button>
                                                    )}
                                                </div>
                                            </div>
                                        ))}
                                    </div>

                                    <div style={{
                                        padding: '15px 20px',
                                        borderTop: '1px solid #e0e0e0',
                                        backgroundColor: '#f9f9f9',
                                        display: 'flex',
                                        justifyContent: 'flex-end'
                                    }}>
                                        <button
                                            onClick={handleSaveProperties}
                                            disabled={saving}
                                            style={{
                                                padding: '12px 24px',
                                                backgroundColor: saving ? '#ccc' : '#4caf50',
                                                color: 'white',
                                                border: 'none',
                                                borderRadius: '6px',
                                                fontSize: '14px',
                                                fontWeight: '600',
                                                cursor: saving ? 'not-allowed' : 'pointer',
                                                display: 'flex',
                                                alignItems: 'center',
                                                gap: '8px'
                                            }}
                                        >
                                            <Save size={16} />
                                            {saving ? 'Saving...' : 'Save Configuration'}
                                        </button>
                                    </div>
                                </>
                            )}
                        </>
                    )}
                </div>
            </div>

            {/* Create Service Modal */}
            {showCreateServiceModal && (
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
                    zIndex: 1000
                }}>
                    <div style={{
                        backgroundColor: 'white',
                        borderRadius: '8px',
                        padding: '30px',
                        width: '90%',
                        maxWidth: '500px',
                        boxShadow: '0 10px 40px rgba(0,0,0,0.2)'
                    }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                            <h2 style={{ margin: 0, fontSize: '22px' }}>Create New Service</h2>
                            <button
                                onClick={() => {
                                    setShowCreateServiceModal(false);
                                    setNewServiceName('');
                                }}
                                style={{
                                    background: 'none',
                                    border: 'none',
                                    cursor: 'pointer',
                                    padding: '5px'
                                }}
                            >
                                <X size={24} color="#666" />
                            </button>
                        </div>
                        
                        <div style={{ marginBottom: '20px' }}>
                            <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600' }}>
                                Service Name
                            </label>
                            <input
                                type="text"
                                value={newServiceName}
                                onChange={(e) => setNewServiceName(e.target.value)}
                                placeholder="e.g., INDIAN_API, ALPHA_VANTAGE"
                                style={{
                                    width: '100%',
                                    padding: '12px',
                                    border: '1px solid #e0e0e0',
                                    borderRadius: '6px',
                                    fontSize: '14px'
                                }}
                            />
                        </div>

                        <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
                            <button
                                onClick={() => {
                                    setShowCreateServiceModal(false);
                                    setNewServiceName('');
                                }}
                                style={{
                                    padding: '10px 20px',
                                    backgroundColor: '#f5f5f5',
                                    color: '#666',
                                    border: 'none',
                                    borderRadius: '6px',
                                    cursor: 'pointer',
                                    fontSize: '14px',
                                    fontWeight: '600'
                                }}
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleCreateService}
                                style={{
                                    padding: '10px 20px',
                                    backgroundColor: '#4caf50',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '6px',
                                    cursor: 'pointer',
                                    fontSize: '14px',
                                    fontWeight: '600'
                                }}
                            >
                                Create Service
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Add Property Modal */}
            {showAddPropertyModal && (
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
                    zIndex: 1000
                }}>
                    <div style={{
                        backgroundColor: 'white',
                        borderRadius: '8px',
                        padding: '30px',
                        width: '90%',
                        maxWidth: '500px',
                        boxShadow: '0 10px 40px rgba(0,0,0,0.2)'
                    }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                            <h2 style={{ margin: 0, fontSize: '22px' }}>Add Property to {selectedService?.displayName}</h2>
                            <button
                                onClick={() => {
                                    setShowAddPropertyModal(false);
                                    setNewProperty({ name: '', value: '' });
                                }}
                                style={{
                                    background: 'none',
                                    border: 'none',
                                    cursor: 'pointer',
                                    padding: '5px'
                                }}
                            >
                                <X size={24} color="#666" />
                            </button>
                        </div>
                        
                        <div style={{ marginBottom: '20px' }}>
                            <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600' }}>
                                Property Name
                            </label>
                            <input
                                type="text"
                                value={newProperty.name}
                                onChange={(e) => setNewProperty({ ...newProperty, name: e.target.value })}
                                placeholder="e.g., API_KEY, BASE_URL"
                                style={{
                                    width: '100%',
                                    padding: '12px',
                                    border: '1px solid #e0e0e0',
                                    borderRadius: '6px',
                                    fontSize: '14px'
                                }}
                            />
                        </div>

                        <div style={{ marginBottom: '20px' }}>
                            <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600' }}>
                                Property Value
                            </label>
                            <input
                                type="text"
                                value={newProperty.value}
                                onChange={(e) => setNewProperty({ ...newProperty, value: e.target.value })}
                                placeholder="Enter property value"
                                style={{
                                    width: '100%',
                                    padding: '12px',
                                    border: '1px solid #e0e0e0',
                                    borderRadius: '6px',
                                    fontSize: '14px'
                                }}
                            />
                        </div>

                        <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
                            <button
                                onClick={() => {
                                    setShowAddPropertyModal(false);
                                    setNewProperty({ name: '', value: '' });
                                }}
                                style={{
                                    padding: '10px 20px',
                                    backgroundColor: '#f5f5f5',
                                    color: '#666',
                                    border: 'none',
                                    borderRadius: '6px',
                                    cursor: 'pointer',
                                    fontSize: '14px',
                                    fontWeight: '600'
                                }}
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleAddProperty}
                                style={{
                                    padding: '10px 20px',
                                    backgroundColor: '#2196f3',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '6px',
                                    cursor: 'pointer',
                                    fontSize: '14px',
                                    fontWeight: '600'
                                }}
                            >
                                Add Property
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AdminExternalServices;
