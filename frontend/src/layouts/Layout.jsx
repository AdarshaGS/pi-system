import { NavLink, Outlet } from 'react-router-dom';
import {
    LayoutDashboard,
    Wallet,
    TrendingUp,
    Briefcase,
    CreditCard,
    Lightbulb,
    Settings as SettingsIcon,
    LogOut,
    Shield,
    Users,
    RefreshCw
} from 'lucide-react';
import { useState, useEffect } from 'react';
import { useFeatures } from '../contexts/FeatureContext';

const Layout = () => {
    const [isAdmin, setIsAdmin] = useState(false);
    const { isFeatureEnabled } = useFeatures();

    useEffect(() => {
        // Check if user has admin role
        const user = JSON.parse(localStorage.getItem('user'));
        if (user && user.token) {
            // Decode JWT to check roles (simple check - in production use proper JWT library)
            try {
                const payload = JSON.parse(atob(user.token.split('.')[1]));
                console.log('JWT Payload:', payload);
                console.log('Roles from JWT:', payload.roles);
                
                const roles = payload.roles || [];
                const hasAdmin = roles.some(role => 
                    role === 'ROLE_ADMIN' || 
                    role === 'ROLE_SUPER_ADMIN' ||
                    role.includes('ADMIN')
                );
                
                console.log('Is Admin?', hasAdmin);
                setIsAdmin(hasAdmin);
            } catch (e) {
                console.error('Error checking admin status:', e);
            }
        }
    }, []);

    return (
        <div className="app-layout">
            <aside className="sidebar">
                <div className="sidebar-header">
                    PI SYSTEM
                </div>
                <nav className="sidebar-nav">
                    <NavLink to="/dashboard" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                        <LayoutDashboard />
                        Dashboard
                    </NavLink>
                    {isFeatureEnabled('BUDGET_MODULE') && (
                        <>
                            <NavLink to="/budget" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                                <Wallet />
                                Budget
                            </NavLink>
                            <NavLink to="/cashflow" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                                <TrendingUp />
                                Cash Flow
                            </NavLink>
                        </>
                    )}
                    {isFeatureEnabled('RECURRING_TRANSACTIONS') && (
                        <NavLink to="/recurring" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                            <RefreshCw />
                            Recurring
                        </NavLink>
                    )}
                    {isFeatureEnabled('PORTFOLIO') && (
                        <NavLink to="/portfolio" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                            <Briefcase />
                            Portfolio
                        </NavLink>
                    )}
                    {isFeatureEnabled('LOANS') && (
                        <NavLink to="/loans" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                            <CreditCard />
                            Loans
                        </NavLink>
                    )}
                    <NavLink to="/insights" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                        <Lightbulb />
                        Insights
                    </NavLink>
                    <NavLink to="/settings" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                        <SettingsIcon />
                        Settings
                    </NavLink>
                    
                    {/* Admin Section */}
                    {isAdmin && (
                        <>
                            <div style={{ 
                                margin: '20px 0 10px 0', 
                                padding: '0 20px',
                                fontSize: '12px',
                                fontWeight: '600',
                                color: '#666',
                                textTransform: 'uppercase',
                                letterSpacing: '0.5px'
                            }}>
                                Admin Portal
                            </div>
                            <NavLink to="/admin" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                                <Shield />
                                Admin Dashboard
                            </NavLink>
                            <NavLink to="/admin/users" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                                <Users />
                                User Management
                            </NavLink>
                        </>
                    )}
                </nav>
                <div style={{ padding: '20px', borderTop: '1px solid var(--border-color)' }}>
                    <NavLink to="/login" className="nav-item">
                        <LogOut />
                        Logout
                    </NavLink>
                </div>
            </aside>
            <main className="main-content">
                <Outlet />
            </main>
        </div>
    );
};

export default Layout;
