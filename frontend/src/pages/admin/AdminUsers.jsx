import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Users, Mail, Phone, Shield, MoreVertical, Edit, Trash2, X, Save, UserPlus, Filter, Search, ChevronLeft, ChevronRight, ArrowLeft } from 'lucide-react';
import { adminApi } from '../../api';
import '../../App.css';

const AdminUsers = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [activeMenu, setActiveMenu] = useState(null);
    const [selectedUser, setSelectedUser] = useState(null);
    const [showUserDetails, setShowUserDetails] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [editFormData, setEditFormData] = useState({});
    const [saving, setSaving] = useState(false);
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [createFormData, setCreateFormData] = useState({
        name: '',
        email: '',
        mobileNumber: '',
        password: ''
    });
    const [creating, setCreating] = useState(false);
    
    // Search and filter state
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedRole, setSelectedRole] = useState('');
    const [availableRoles, setAvailableRoles] = useState([]);
    
    // Pagination state
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [pageSize] = useState(10);
    
    // Role management
    const [showRoleModal, setShowRoleModal] = useState(false);
    const [roleUser, setRoleUser] = useState(null);
    const [userRoles, setUserRoles] = useState([]);
    const [addingRole, setAddingRole] = useState(false);
    
    const navigate = useNavigate();

    useEffect(() => {
        const user = JSON.parse(localStorage.getItem('user'));
        if (!user || !user.token) {
            navigate('/login');
            return;
        }

        fetchUsers();
        fetchRoles();
    }, [navigate, currentPage, searchTerm, selectedRole]);

    const fetchUsers = async () => {
        const user = JSON.parse(localStorage.getItem('user'));
        setLoading(true);
        
        try {
            const params = {
                page: currentPage,
                size: pageSize
            };
            
            if (searchTerm) params.search = searchTerm;
            if (selectedRole) params.role = selectedRole;
            
            const data = await adminApi.getAllUsers(user.token, params);
            setUsers(data.users || []);
            setTotalPages(data.totalPages || 0);
            setTotalElements(data.totalElements || 0);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const fetchRoles = async () => {
        const user = JSON.parse(localStorage.getItem('user'));
        try {
            const roles = await adminApi.getAllRoles(user.token);
            setAvailableRoles(roles);
        } catch (err) {
            console.error('Failed to fetch roles:', err);
        }
    };

    const handleSearch = (value) => {
        setSearchTerm(value);
        setCurrentPage(0);
    };

    const handleRoleFilter = (role) => {
        setSelectedRole(role);
        setCurrentPage(0);
    };

    const handlePageChange = (newPage) => {
        if (newPage >= 0 && newPage < totalPages) {
            setCurrentPage(newPage);
        }
    };

    const toggleMenu = (userId) => {
        setActiveMenu(activeMenu === userId ? null : userId);
    };

    const handleEditUser = (user) => {
        setEditFormData({
            name: user.name,
            email: user.email,
            mobileNumber: user.mobileNumber,
            roles: user.roles || []
        });
        setSelectedUser(user);
        setShowEditModal(true);
        setActiveMenu(null);
    };

    const handleSaveEdit = async () => {
        const user = JSON.parse(localStorage.getItem('user'));
        setSaving(true);
        
        try {
            await adminApi.updateUser(selectedUser.id, editFormData, user.token);
            
            // Refresh the users list
            await fetchUsers();
            
            setShowEditModal(false);
            setSelectedUser(null);
            alert('User updated successfully!');
        } catch (err) {
            alert('Failed to update user: ' + err.message);
        } finally {
            setSaving(false);
        }
    };

    const handleManageRoles = (user) => {
        setRoleUser(user);
        setUserRoles(user.roles || []);
        setShowRoleModal(true);
        setActiveMenu(null);
    };

    const handleAddRole = async (roleName) => {
        const user = JSON.parse(localStorage.getItem('user'));
        setAddingRole(true);
        
        try {
            await adminApi.addRoleToUser(roleUser.id, roleName, user.token);
            setUserRoles([...userRoles, roleName]);
            await fetchUsers();
            alert('Role added successfully!');
        } catch (err) {
            alert('Failed to add role: ' + err.message);
        } finally {
            setAddingRole(false);
        }
    };

    const handleRemoveRole = async (roleName) => {
        const user = JSON.parse(localStorage.getItem('user'));
        
        try {
            await adminApi.removeRoleFromUser(roleUser.id, roleName, user.token);
            setUserRoles(userRoles.filter(r => r !== roleName));
            await fetchUsers();
            alert('Role removed successfully!');
        } catch (err) {
            alert('Failed to remove role: ' + err.message);
        }
    };

    const handleCreateUser = async () => {
        const user = JSON.parse(localStorage.getItem('user'));
        setCreating(true);
        
        try {
            const response = await fetch('http://localhost:8082/api/v1/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(createFormData)
            });
            
            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.message || 'Failed to create user');
            }
            
            // Refresh the users list
            const updatedUsers = await adminApi.getAllUsers(user.token);
            setUsers(updatedUsers);
            
            setShowCreateModal(false);
            setCreateFormData({
                name: '',
                email: '',
                mobileNumber: '',
                password: ''
            });
            alert('User created successfully!');
        } catch (err) {
            alert('Failed to create user: ' + err.message);
        } finally {
            setCreating(false);
        }
    };

    const handleDeleteUser = async (user) => {
        if (!window.confirm(`Are you sure you want to delete user: ${user.name}?\n\nThis action cannot be undone.`)) {
            setActiveMenu(null);
            return;
        }
        
        const currentUser = JSON.parse(localStorage.getItem('user'));
        
        try {
            await adminApi.deleteUser(user.id, currentUser.token);
            
            // Remove user from the list
            setUsers(users.filter(u => u.id !== user.id));
            alert('User deleted successfully!');
        } catch (err) {
            alert('Failed to delete user: ' + err.message);
        }
        
        setActiveMenu(null);
    };

    const handleViewDetails = (user) => {
        setSelectedUser(user);
        setShowUserDetails(true);
    };

    const closeDetailsModal = () => {
        setShowUserDetails(false);
        setSelectedUser(null);
    };

    if (loading) {
        return (
            <div style={{ padding: '40px', textAlign: 'center' }}>
                <div className="spinner"></div>
                <p>Loading users...</p>
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

            <div style={{ marginBottom: '30px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                    <h1 style={{ fontSize: '28px', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <Users size={32} />
                        User Management
                    </h1>
                    <p style={{ color: '#666' }}>View and manage all system users</p>
                </div>
                <div style={{ display: 'flex', gap: '15px', alignItems: 'center' }}>
                    <button
                        onClick={() => setShowCreateModal(true)}
                        style={{
                            padding: '12px 20px',
                            backgroundColor: '#4caf50',
                            color: 'white',
                            border: 'none',
                            borderRadius: '6px',
                            fontWeight: '600',
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '8px',
                            fontSize: '14px',
                            transition: 'background-color 0.2s'
                        }}
                        onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#45a049'}
                        onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#4caf50'}
                    >
                        <UserPlus size={18} />
                        Create User
                    </button>
                    <div style={{
                        padding: '10px 20px',
                        backgroundColor: '#2196f3',
                        color: 'white',
                        borderRadius: '6px',
                        fontWeight: '600'
                    }}>
                        Total Users: {totalElements}
                    </div>
                </div>
            </div>

            {/* Search and Filter Bar */}
            <div style={{ 
                marginBottom: '20px', 
                display: 'flex', 
                gap: '15px', 
                alignItems: 'center',
                padding: '15px',
                backgroundColor: '#fff',
                border: '1px solid #e0e0e0',
                borderRadius: '8px'
            }}>
                <div style={{ flex: 1, position: 'relative' }}>
                    <Search size={18} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: '#666' }} />
                    <input
                        type="text"
                        placeholder="Search by name, email, or mobile..."
                        value={searchTerm}
                        onChange={(e) => handleSearch(e.target.value)}
                        style={{
                            width: '100%',
                            padding: '10px 10px 10px 40px',
                            border: '1px solid #e0e0e0',
                            borderRadius: '6px',
                            fontSize: '14px'
                        }}
                    />
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <Filter size={18} style={{ color: '#666' }} />
                    <select
                        value={selectedRole}
                        onChange={(e) => handleRoleFilter(e.target.value)}
                        style={{
                            padding: '10px 35px 10px 12px',
                            border: '1px solid #e0e0e0',
                            borderRadius: '6px',
                            fontSize: '14px',
                            cursor: 'pointer',
                            backgroundColor: 'white'
                        }}
                    >
                        <option value="">All Roles</option>
                        {availableRoles.map(role => (
                            <option key={role} value={role}>{role.replace('ROLE_', '')}</option>
                        ))}
                    </select>
                </div>
                {(searchTerm || selectedRole) && (
                    <button
                        onClick={() => {
                            setSearchTerm('');
                            setSelectedRole('');
                            setCurrentPage(0);
                        }}
                        style={{
                            padding: '10px 16px',
                            backgroundColor: '#f44336',
                            color: 'white',
                            border: 'none',
                            borderRadius: '6px',
                            fontSize: '13px',
                            fontWeight: '500',
                            cursor: 'pointer'
                        }}
                    >
                        Clear Filters
                    </button>
                )}
            </div>

            {/* Users Table */}
            <div style={{
                backgroundColor: '#fff',
                border: '1px solid #e0e0e0',
                borderRadius: '8px',
                overflow: 'hidden',
                boxShadow: '0 2px 4px rgba(0,0,0,0.05)'
            }}>
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                    <thead>
                        <tr style={{ backgroundColor: '#f5f5f5', borderBottom: '2px solid #e0e0e0' }}>
                            <th style={{ padding: '15px', textAlign: 'left', fontWeight: '600' }}>ID</th>
                            <th style={{ padding: '15px', textAlign: 'left', fontWeight: '600' }}>Name</th>
                            <th style={{ padding: '15px', textAlign: 'left', fontWeight: '600' }}>Email</th>
                            <th style={{ padding: '15px', textAlign: 'left', fontWeight: '600' }}>Mobile</th>
                            <th style={{ padding: '15px', textAlign: 'left', fontWeight: '600' }}>Roles</th>
                            <th style={{ padding: '15px', textAlign: 'center', fontWeight: '600' }}>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {users.map((user) => (
                            <tr key={user.id} style={{ borderBottom: '1px solid #e0e0e0' }}>
                                <td style={{ padding: '15px' }}>
                                    <span style={{ 
                                        padding: '4px 8px', 
                                        backgroundColor: '#e3f2fd', 
                                        borderRadius: '4px',
                                        fontWeight: '500'
                                    }}>
                                        #{user.id}
                                    </span>
                                </td>
                                <td style={{ padding: '15px', fontWeight: '500', cursor: 'pointer' }} 
                                    onClick={() => handleViewDetails(user)}
                                    onMouseEnter={(e) => e.currentTarget.style.color = '#2196f3'}
                                    onMouseLeave={(e) => e.currentTarget.style.color = '#000'}
                                    title="Click to view details"
                                >
                                    {user.name}
                                </td>
                                <td style={{ padding: '15px' }}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                        <Mail size={16} style={{ color: '#666' }} />
                                        {user.email}
                                    </div>
                                </td>
                                <td style={{ padding: '15px' }}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                        <Phone size={16} style={{ color: '#666' }} />
                                        {user.mobileNumber}
                                    </div>
                                </td>
                                <td style={{ padding: '15px' }}>
                                    <div style={{ display: 'flex', gap: '5px', flexWrap: 'wrap' }}>
                                        {user.roles && user.roles.map((role, idx) => (
                                            <span key={idx} style={{
                                                padding: '4px 10px',
                                                backgroundColor: role.includes('ADMIN') ? '#fff3e0' : '#e8f5e9',
                                                color: role.includes('ADMIN') ? '#f57c00' : '#2e7d32',
                                                borderRadius: '12px',
                                                fontSize: '12px',
                                                fontWeight: '500',
                                                display: 'flex',
                                                alignItems: 'center',
                                                gap: '4px'
                                            }}>
                                                {role.includes('ADMIN') && <Shield size={12} />}
                                                {role.replace('ROLE_', '')}
                                            </span>
                                        ))}
                                    </div>
                                </td>
                                <td style={{ padding: '15px', textAlign: 'center', position: 'relative' }}>
                                    <button
                                        onClick={() => toggleMenu(user.id)}
                                        style={{
                                            padding: '8px',
                                            backgroundColor: 'transparent',
                                            border: '1px solid #e0e0e0',
                                            borderRadius: '4px',
                                            cursor: 'pointer',
                                            display: 'flex',
                                            alignItems: 'center',
                                            justifyContent: 'center',
                                            transition: 'all 0.2s'
                                        }}
                                        onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f5f5f5'}
                                        onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                                    >
                                        <MoreVertical size={18} />
                                    </button>
                                    
                                    {/* Dropdown Menu */}
                                    {activeMenu === user.id && (
                                        <div style={{
                                            position: 'absolute',
                                            top: '100%',
                                            right: '15px',
                                            marginTop: '5px',
                                            backgroundColor: 'white',
                                            border: '1px solid #e0e0e0',
                                            borderRadius: '6px',
                                            boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
                                            zIndex: 1000,
                                            minWidth: '150px',
                                            overflow: 'hidden'
                                        }}>
                                            <button
                                                onClick={() => handleEditUser(user)}
                                                style={{
                                                    width: '100%',
                                                    padding: '10px 15px',
                                                    textAlign: 'left',
                                                    border: 'none',
                                                    backgroundColor: 'white',
                                                    cursor: 'pointer',
                                                    display: 'flex',
                                                    alignItems: 'center',
                                                    gap: '10px',
                                                    fontSize: '14px',
                                                    transition: 'background-color 0.2s'
                                                }}
                                                onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f5f5f5'}
                                                onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'white'}
                                            >
                                                <Edit size={16} style={{ color: '#2196f3' }} />
                                                Edit User
                                            </button>
                                            <button
                                                onClick={() => handleManageRoles(user)}
                                                style={{
                                                    width: '100%',
                                                    padding: '10px 15px',
                                                    textAlign: 'left',
                                                    border: 'none',
                                                    backgroundColor: 'white',
                                                    cursor: 'pointer',
                                                    display: 'flex',
                                                    alignItems: 'center',
                                                    gap: '10px',
                                                    fontSize: '14px',
                                                    transition: 'background-color 0.2s'
                                                }}
                                                onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f5f5f5'}
                                                onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'white'}
                                            >
                                                <Shield size={16} style={{ color: '#ff9800' }} />
                                                Manage Roles
                                            </button>
                                            <button
                                                onClick={() => handleDeleteUser(user)}
                                                style={{
                                                    width: '100%',
                                                    padding: '10px 15px',
                                                    textAlign: 'left',
                                                    border: 'none',
                                                    backgroundColor: 'white',
                                                    cursor: 'pointer',
                                                    display: 'flex',
                                                    alignItems: 'center',
                                                    gap: '10px',
                                                    fontSize: '14px',
                                                    transition: 'background-color 0.2s',
                                                    borderTop: '1px solid #f0f0f0'
                                                }}
                                                onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#fee'}
                                                onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'white'}
                                            >
                                                <Trash2 size={16} style={{ color: '#f44336' }} />
                                                <span style={{ color: '#f44336' }}>Delete User</span>
                                            </button>
                                        </div>
                                    )}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {/* Pagination Controls */}
            {totalPages > 1 && (
                <div style={{
                    marginTop: '20px',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '15px',
                    backgroundColor: '#fff',
                    border: '1px solid #e0e0e0',
                    borderRadius: '8px'
                }}>
                    <div style={{ fontSize: '14px', color: '#666' }}>
                        Showing {currentPage * pageSize + 1} to {Math.min((currentPage + 1) * pageSize, totalElements)} of {totalElements} users
                    </div>
                    <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                        <button
                            onClick={() => handlePageChange(currentPage - 1)}
                            disabled={currentPage === 0}
                            style={{
                                padding: '8px 12px',
                                backgroundColor: currentPage === 0 ? '#f5f5f5' : 'white',
                                color: currentPage === 0 ? '#ccc' : '#333',
                                border: '1px solid #e0e0e0',
                                borderRadius: '6px',
                                cursor: currentPage === 0 ? 'not-allowed' : 'pointer',
                                display: 'flex',
                                alignItems: 'center',
                                gap: '5px',
                                fontSize: '14px'
                            }}
                        >
                            <ChevronLeft size={16} />
                            Previous
                        </button>
                        <div style={{ display: 'flex', gap: '5px' }}>
                            {[...Array(totalPages)].map((_, idx) => (
                                <button
                                    key={idx}
                                    onClick={() => handlePageChange(idx)}
                                    style={{
                                        padding: '8px 12px',
                                        backgroundColor: currentPage === idx ? '#2196f3' : 'white',
                                        color: currentPage === idx ? 'white' : '#333',
                                        border: '1px solid #e0e0e0',
                                        borderRadius: '6px',
                                        cursor: 'pointer',
                                        fontSize: '14px',
                                        minWidth: '40px'
                                    }}
                                >
                                    {idx + 1}
                                </button>
                            ))}
                        </div>
                        <button
                            onClick={() => handlePageChange(currentPage + 1)}
                            disabled={currentPage >= totalPages - 1}
                            style={{
                                padding: '8px 12px',
                                backgroundColor: currentPage >= totalPages - 1 ? '#f5f5f5' : 'white',
                                color: currentPage >= totalPages - 1 ? '#ccc' : '#333',
                                border: '1px solid #e0e0e0',
                                borderRadius: '6px',
                                cursor: currentPage >= totalPages - 1 ? 'not-allowed' : 'pointer',
                                display: 'flex',
                                alignItems: 'center',
                                gap: '5px',
                                fontSize: '14px'
                            }}
                        >
                            Next
                            <ChevronRight size={16} />
                        </button>
                    </div>
                </div>
            )}

            {users.length === 0 && (
                <div style={{
                    textAlign: 'center',
                    padding: '60px 20px',
                    color: '#666'
                }}>
                    <Users size={48} style={{ marginBottom: '20px', opacity: 0.5 }} />
                    <p style={{ fontSize: '16px' }}>No users found</p>
                </div>
            )}

            {/* User Details Modal */}
            {showUserDetails && selectedUser && (
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
                }} onClick={closeDetailsModal}>
                    <div style={{
                        backgroundColor: 'white',
                        borderRadius: '12px',
                        padding: '30px',
                        maxWidth: '600px',
                        width: '90%',
                        maxHeight: '80vh',
                        overflow: 'auto',
                        boxShadow: '0 10px 40px rgba(0,0,0,0.2)'
                    }} onClick={(e) => e.stopPropagation()}>
                        {/* Header */}
                        <div style={{ 
                            display: 'flex', 
                            justifyContent: 'space-between', 
                            alignItems: 'center',
                            marginBottom: '20px',
                            paddingBottom: '15px',
                            borderBottom: '2px solid #e0e0e0'
                        }}>
                            <h2 style={{ fontSize: '24px', margin: 0 }}>User Details</h2>
                            <button 
                                onClick={closeDetailsModal}
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

                        {/* User Info */}
                        <div style={{ marginBottom: '30px' }}>
                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ 
                                    display: 'block', 
                                    fontSize: '12px', 
                                    color: '#666', 
                                    marginBottom: '5px',
                                    textTransform: 'uppercase',
                                    fontWeight: '600'
                                }}>User ID</label>
                                <div style={{ 
                                    padding: '12px', 
                                    backgroundColor: '#f5f5f5', 
                                    borderRadius: '6px',
                                    fontWeight: '500'
                                }}>
                                    #{selectedUser.id}
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
                                }}>Name</label>
                                <div style={{ 
                                    padding: '12px', 
                                    backgroundColor: '#f5f5f5', 
                                    borderRadius: '6px',
                                    fontWeight: '500'
                                }}>
                                    {selectedUser.name}
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
                                }}>Email</label>
                                <div style={{ 
                                    padding: '12px', 
                                    backgroundColor: '#f5f5f5', 
                                    borderRadius: '6px',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '8px'
                                }}>
                                    <Mail size={16} style={{ color: '#666' }} />
                                    {selectedUser.email}
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
                                }}>Mobile Number</label>
                                <div style={{ 
                                    padding: '12px', 
                                    backgroundColor: '#f5f5f5', 
                                    borderRadius: '6px',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '8px'
                                }}>
                                    <Phone size={16} style={{ color: '#666' }} />
                                    {selectedUser.mobileNumber}
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
                                }}>Roles</label>
                                <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                                    {selectedUser.roles && selectedUser.roles.map((role, idx) => (
                                        <span key={idx} style={{
                                            padding: '8px 16px',
                                            backgroundColor: role.includes('ADMIN') ? '#fff3e0' : '#e8f5e9',
                                            color: role.includes('ADMIN') ? '#f57c00' : '#2e7d32',
                                            borderRadius: '16px',
                                            fontSize: '14px',
                                            fontWeight: '600',
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: '6px'
                                        }}>
                                            {role.includes('ADMIN') && <Shield size={14} />}
                                            {role.replace('ROLE_', '')}
                                        </span>
                                    ))}
                                </div>
                            </div>
                        </div>

                        {/* Action Buttons */}
                        <div style={{ 
                            display: 'flex', 
                            gap: '10px',
                            paddingTop: '20px',
                            borderTop: '1px solid #e0e0e0'
                        }}>
                            <button
                                onClick={() => {
                                    closeDetailsModal();
                                    handleEditUser(selectedUser);
                                }}
                                style={{
                                    flex: 1,
                                    padding: '12px 20px',
                                    backgroundColor: '#2196f3',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '6px',
                                    fontSize: '14px',
                                    fontWeight: '600',
                                    cursor: 'pointer',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    gap: '8px',
                                    transition: 'background-color 0.2s'
                                }}
                                onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#1976d2'}
                                onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#2196f3'}
                            >
                                <Edit size={16} />
                                Edit User
                            </button>
                            <button
                                onClick={() => {
                                    closeDetailsModal();
                                    handleDeleteUser(selectedUser);
                                }}
                                style={{
                                    flex: 1,
                                    padding: '12px 20px',
                                    backgroundColor: 'white',
                                    color: '#f44336',
                                    border: '2px solid #f44336',
                                    borderRadius: '6px',
                                    fontSize: '14px',
                                    fontWeight: '600',
                                    cursor: 'pointer',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    gap: '8px',
                                    transition: 'all 0.2s'
                                }}
                                onMouseEnter={(e) => {
                                    e.currentTarget.style.backgroundColor = '#f44336';
                                    e.currentTarget.style.color = 'white';
                                }}
                                onMouseLeave={(e) => {
                                    e.currentTarget.style.backgroundColor = 'white';
                                    e.currentTarget.style.color = '#f44336';
                                }}
                            >
                                <Trash2 size={16} />
                                Delete User
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Edit User Modal */}
            {showEditModal && selectedUser && (
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
                }} onClick={() => setShowEditModal(false)}>
                    <div style={{
                        backgroundColor: 'white',
                        borderRadius: '12px',
                        padding: '30px',
                        maxWidth: '600px',
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
                            <h2 style={{ fontSize: '24px', margin: 0, display: 'flex', alignItems: 'center', gap: '10px' }}>
                                <Edit size={24} style={{ color: '#2196f3' }} />
                                Edit User
                            </h2>
                            <button 
                                onClick={() => setShowEditModal(false)}
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
                                    fontSize: '14px', 
                                    fontWeight: '600', 
                                    marginBottom: '8px'
                                }}>Name *</label>
                                <input
                                    type="text"
                                    value={editFormData.name || ''}
                                    onChange={(e) => setEditFormData({...editFormData, name: e.target.value})}
                                    style={{
                                        width: '100%',
                                        padding: '12px',
                                        border: '1px solid #e0e0e0',
                                        borderRadius: '6px',
                                        fontSize: '14px'
                                    }}
                                    required
                                />
                            </div>

                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ 
                                    display: 'block', 
                                    fontSize: '14px', 
                                    fontWeight: '600', 
                                    marginBottom: '8px'
                                }}>Email *</label>
                                <input
                                    type="email"
                                    value={editFormData.email || ''}
                                    onChange={(e) => setEditFormData({...editFormData, email: e.target.value})}
                                    style={{
                                        width: '100%',
                                        padding: '12px',
                                        border: '1px solid #e0e0e0',
                                        borderRadius: '6px',
                                        fontSize: '14px'
                                    }}
                                    required
                                />
                            </div>

                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ 
                                    display: 'block', 
                                    fontSize: '14px', 
                                    fontWeight: '600', 
                                    marginBottom: '8px'
                                }}>Mobile Number *</label>
                                <input
                                    type="tel"
                                    value={editFormData.mobileNumber || ''}
                                    onChange={(e) => setEditFormData({...editFormData, mobileNumber: e.target.value})}
                                    style={{
                                        width: '100%',
                                        padding: '12px',
                                        border: '1px solid #e0e0e0',
                                        borderRadius: '6px',
                                        fontSize: '14px'
                                    }}
                                    required
                                />
                            </div>

                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ 
                                    display: 'block', 
                                    fontSize: '14px', 
                                    fontWeight: '600', 
                                    marginBottom: '8px'
                                }}>Roles</label>
                                <div style={{ 
                                    padding: '12px', 
                                    backgroundColor: '#f5f5f5', 
                                    borderRadius: '6px'
                                }}>
                                    {editFormData.roles && editFormData.roles.map((role, idx) => (
                                        <span key={idx} style={{
                                            display: 'inline-block',
                                            padding: '6px 12px',
                                            backgroundColor: role.includes('ADMIN') ? '#fff3e0' : '#e8f5e9',
                                            color: role.includes('ADMIN') ? '#f57c00' : '#2e7d32',
                                            borderRadius: '12px',
                                            fontSize: '13px',
                                            fontWeight: '500',
                                            marginRight: '8px',
                                            marginBottom: '8px'
                                        }}>
                                            {role.replace('ROLE_', '')}
                                        </span>
                                    ))}
                                </div>
                                <p style={{ fontSize: '12px', color: '#666', marginTop: '8px' }}>
                                    Note: Role management requires additional permissions
                                </p>
                            </div>
                        </div>

                        <div style={{ 
                            display: 'flex', 
                            gap: '10px',
                            paddingTop: '20px',
                            borderTop: '1px solid #e0e0e0'
                        }}>
                            <button
                                onClick={() => setShowEditModal(false)}
                                style={{
                                    flex: 1,
                                    padding: '12px 20px',
                                    backgroundColor: 'white',
                                    color: '#666',
                                    border: '2px solid #e0e0e0',
                                    borderRadius: '6px',
                                    fontSize: '14px',
                                    fontWeight: '600',
                                    cursor: 'pointer',
                                    transition: 'all 0.2s'
                                }}
                                onMouseEnter={(e) => {
                                    e.currentTarget.style.backgroundColor = '#f5f5f5';
                                }}
                                onMouseLeave={(e) => {
                                    e.currentTarget.style.backgroundColor = 'white';
                                }}
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleSaveEdit}
                                disabled={saving}
                                style={{
                                    flex: 1,
                                    padding: '12px 20px',
                                    backgroundColor: saving ? '#ccc' : '#2196f3',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '6px',
                                    fontSize: '14px',
                                    fontWeight: '600',
                                    cursor: saving ? 'not-allowed' : 'pointer',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    gap: '8px',
                                    transition: 'background-color 0.2s'
                                }}
                                onMouseEnter={(e) => {
                                    if (!saving) e.currentTarget.style.backgroundColor = '#1976d2';
                                }}
                                onMouseLeave={(e) => {
                                    if (!saving) e.currentTarget.style.backgroundColor = '#2196f3';
                                }}
                            >
                                <Save size={16} />
                                {saving ? 'Saving...' : 'Save Changes'}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Create User Modal */}
            {showCreateModal && (
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
                }} onClick={() => setShowCreateModal(false)}>
                    <div style={{
                        backgroundColor: 'white',
                        borderRadius: '12px',
                        padding: '30px',
                        maxWidth: '600px',
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
                            <h2 style={{ fontSize: '24px', margin: 0, display: 'flex', alignItems: 'center', gap: '10px' }}>
                                <UserPlus size={24} style={{ color: '#4caf50' }} />
                                Create New User
                            </h2>
                            <button 
                                onClick={() => setShowCreateModal(false)}
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
                                    fontSize: '14px', 
                                    fontWeight: '600', 
                                    marginBottom: '8px'
                                }}>Name *</label>
                                <input
                                    type="text"
                                    value={createFormData.name}
                                    onChange={(e) => setCreateFormData({...createFormData, name: e.target.value})}
                                    style={{
                                        width: '100%',
                                        padding: '12px',
                                        border: '1px solid #e0e0e0',
                                        borderRadius: '6px',
                                        fontSize: '14px'
                                    }}
                                    required
                                />
                            </div>

                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ 
                                    display: 'block', 
                                    fontSize: '14px', 
                                    fontWeight: '600', 
                                    marginBottom: '8px'
                                }}>Email *</label>
                                <input
                                    type="email"
                                    value={createFormData.email}
                                    onChange={(e) => setCreateFormData({...createFormData, email: e.target.value})}
                                    style={{
                                        width: '100%',
                                        padding: '12px',
                                        border: '1px solid #e0e0e0',
                                        borderRadius: '6px',
                                        fontSize: '14px'
                                    }}
                                    required
                                />
                            </div>

                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ 
                                    display: 'block', 
                                    fontSize: '14px', 
                                    fontWeight: '600', 
                                    marginBottom: '8px'
                                }}>Mobile Number *</label>
                                <input
                                    type="tel"
                                    value={createFormData.mobileNumber}
                                    onChange={(e) => setCreateFormData({...createFormData, mobileNumber: e.target.value})}
                                    style={{
                                        width: '100%',
                                        padding: '12px',
                                        border: '1px solid #e0e0e0',
                                        borderRadius: '6px',
                                        fontSize: '14px'
                                    }}
                                    required
                                />
                            </div>

                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ 
                                    display: 'block', 
                                    fontSize: '14px', 
                                    fontWeight: '600', 
                                    marginBottom: '8px'
                                }}>Password *</label>
                                <input
                                    type="password"
                                    value={createFormData.password}
                                    onChange={(e) => setCreateFormData({...createFormData, password: e.target.value})}
                                    style={{
                                        width: '100%',
                                        padding: '12px',
                                        border: '1px solid #e0e0e0',
                                        borderRadius: '6px',
                                        fontSize: '14px'
                                    }}
                                    required
                                />
                                <p style={{ fontSize: '12px', color: '#666', marginTop: '8px' }}>
                                    Password will be hashed automatically
                                </p>
                            </div>
                        </div>

                        <div style={{ 
                            display: 'flex', 
                            gap: '10px',
                            paddingTop: '20px',
                            borderTop: '1px solid #e0e0e0'
                        }}>
                            <button
                                onClick={() => setShowCreateModal(false)}
                                style={{
                                    flex: 1,
                                    padding: '12px 20px',
                                    backgroundColor: 'white',
                                    color: '#666',
                                    border: '2px solid #e0e0e0',
                                    borderRadius: '6px',
                                    fontSize: '14px',
                                    fontWeight: '600',
                                    cursor: 'pointer',
                                    transition: 'all 0.2s'
                                }}
                                onMouseEnter={(e) => {
                                    e.currentTarget.style.backgroundColor = '#f5f5f5';
                                }}
                                onMouseLeave={(e) => {
                                    e.currentTarget.style.backgroundColor = 'white';
                                }}
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleCreateUser}
                                disabled={creating}
                                style={{
                                    flex: 1,
                                    padding: '12px 20px',
                                    backgroundColor: creating ? '#ccc' : '#4caf50',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '6px',
                                    fontSize: '14px',
                                    fontWeight: '600',
                                    cursor: creating ? 'not-allowed' : 'pointer',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    gap: '8px',
                                    transition: 'background-color 0.2s'
                                }}
                                onMouseEnter={(e) => {
                                    if (!creating) e.currentTarget.style.backgroundColor = '#45a049';
                                }}
                                onMouseLeave={(e) => {
                                    if (!creating) e.currentTarget.style.backgroundColor = '#4caf50';
                                }}
                            >
                                <UserPlus size={16} />
                                {creating ? 'Creating...' : 'Create User'}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Role Management Modal */}
            {showRoleModal && roleUser && (
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
                }} onClick={() => setShowRoleModal(false)}>
                    <div style={{
                        backgroundColor: 'white',
                        borderRadius: '12px',
                        padding: '30px',
                        maxWidth: '600px',
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
                            <h2 style={{ fontSize: '24px', margin: 0, display: 'flex', alignItems: 'center', gap: '10px' }}>
                                <Shield size={24} style={{ color: '#ff9800' }} />
                                Manage Roles - {roleUser.name}
                            </h2>
                            <button 
                                onClick={() => setShowRoleModal(false)}
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
                            <h3 style={{ fontSize: '16px', marginBottom: '15px', color: '#666' }}>Current Roles:</h3>
                            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '10px', marginBottom: '20px' }}>
                                {userRoles.length === 0 ? (
                                    <p style={{ color: '#999', fontStyle: 'italic' }}>No roles assigned</p>
                                ) : (
                                    userRoles.map((role, idx) => (
                                        <div key={idx} style={{
                                            padding: '10px 15px',
                                            backgroundColor: role.includes('ADMIN') ? '#fff3e0' : '#e8f5e9',
                                            borderRadius: '8px',
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: '10px'
                                        }}>
                                            <span style={{
                                                fontSize: '14px',
                                                fontWeight: '600',
                                                color: role.includes('ADMIN') ? '#f57c00' : '#2e7d32'
                                            }}>
                                                {role.replace('ROLE_', '')}
                                            </span>
                                            <button
                                                onClick={() => handleRemoveRole(role)}
                                                style={{
                                                    background: 'none',
                                                    border: 'none',
                                                    cursor: 'pointer',
                                                    padding: '2px',
                                                    display: 'flex',
                                                    alignItems: 'center',
                                                    color: '#666'
                                                }}
                                            >
                                                <X size={16} />
                                            </button>
                                        </div>
                                    ))
                                )}
                            </div>

                            <h3 style={{ fontSize: '16px', marginTop: '30px', marginBottom: '15px', color: '#666' }}>Add Role:</h3>
                            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '10px' }}>
                                {availableRoles.filter(role => !userRoles.includes(role)).map((role, idx) => (
                                    <button
                                        key={idx}
                                        onClick={() => handleAddRole(role)}
                                        disabled={addingRole}
                                        style={{
                                            padding: '10px 15px',
                                            backgroundColor: 'white',
                                            border: '2px solid #e0e0e0',
                                            borderRadius: '8px',
                                            cursor: addingRole ? 'not-allowed' : 'pointer',
                                            fontSize: '14px',
                                            fontWeight: '600',
                                            transition: 'all 0.2s',
                                            color: '#333'
                                        }}
                                        onMouseEnter={(e) => {
                                            if (!addingRole) {
                                                e.currentTarget.style.borderColor = '#2196f3';
                                                e.currentTarget.style.backgroundColor = '#e3f2fd';
                                            }
                                        }}
                                        onMouseLeave={(e) => {
                                            if (!addingRole) {
                                                e.currentTarget.style.borderColor = '#e0e0e0';
                                                e.currentTarget.style.backgroundColor = 'white';
                                            }
                                        }}
                                    >
                                        + {role.replace('ROLE_', '')}
                                    </button>
                                ))}
                            </div>
                        </div>

                        <div style={{
                            paddingTop: '20px',
                            borderTop: '1px solid #e0e0e0'
                        }}>
                            <button
                                onClick={() => setShowRoleModal(false)}
                                style={{
                                    padding: '12px 24px',
                                    backgroundColor: '#2196f3',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '6px',
                                    fontSize: '14px',
                                    fontWeight: '600',
                                    cursor: 'pointer',
                                    width: '100%'
                                }}
                            >
                                Done
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AdminUsers;
