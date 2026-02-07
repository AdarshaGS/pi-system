import { NavLink, Outlet } from 'react-router-dom';
import {
    LayoutDashboard,
    Wallet,
    TrendingUp,
    Briefcase,
    CreditCard,
    Shield,
    Lightbulb,
    Settings as SettingsIcon,
    LogOut,
    Users,
    RefreshCw,
    Calculator,
    DollarSign,
    Landmark,
    Menu,
    X
} from 'lucide-react';
import { useState, useEffect } from 'react';
import { useFeatures } from '../contexts/FeatureContext';

const Layout = () => {
    const [isAdmin, setIsAdmin] = useState(false);
    const [isSidebarOpen, setIsSidebarOpen] = useState(true);
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
            {/* Hamburger Menu Button */}
            <button
                onClick={() => setIsSidebarOpen(!isSidebarOpen)}
                style={{
                    position: 'fixed',
                    top: '20px',
                    left: isSidebarOpen ? '270px' : '20px',
                    zIndex: 1001,
                    backgroundColor: '#4f46e5',
                    color: 'white',
                    border: 'none',
                    borderRadius: '8px',
                    padding: '10px',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
                    transition: 'left 0.3s ease'
                }}
            >
                {isSidebarOpen ? <X size={20} /> : <Menu size={20} />}
            </button>

            <aside className="sidebar" style={{
                transform: isSidebarOpen ? 'translateX(0)' : 'translateX(-100%)',
                transition: 'transform 0.3s ease',
                display: 'flex',
                flexDirection: 'column',
                height: '100vh',
                overflow: 'hidden'
            }}>
                <div className="sidebar-header">
                    PI SYSTEM
                </div>
                <nav className="sidebar-nav" style={{
                    flex: 1,
                    overflowY: 'auto',
                    overflowX: 'hidden'
                }}>
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
                    {isFeatureEnabled('BUDGET_MODULE') && (
                        <NavLink to="/recurring" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                            <RefreshCw />
                            Recurring
                        </NavLink>
                    )}
                    {isFeatureEnabled('INVESTMENTS_MODULE') && (
                        <NavLink to="/portfolio" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                            <Briefcase />
                            Portfolio
                        </NavLink>
                    )}
                    {isFeatureEnabled('BANKING_MODULE') && (
                        <>
                            <NavLink to="/banking" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                                <Landmark />
                                Banking (FD/RD)
                            </NavLink>
                            <NavLink to="/loans" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                                <CreditCard />
                                Loans
                            </NavLink>
                        </>
                    )}
                    {isFeatureEnabled('INSURANCE_MODULE') && (
                        <NavLink to="/insurance" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                            <Shield />
                            Insurance
                        </NavLink>
                    )}
                    {/* {isFeatureEnabled('TAX_MANAGEMENT') && ( */}
                    <NavLink to="/tax" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                        <Calculator />
                        Tax Management
                    </NavLink>
                    {/* )} */}
                    <NavLink to="/lending" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                        <DollarSign />
                        Lending
                    </NavLink>
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
                                Admin
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
            <main className="main-content" style={{
                marginLeft: isSidebarOpen ? '280px' : '0',
                transition: 'margin-left 0.3s ease',
                width: isSidebarOpen ? 'calc(100% - 280px)' : '100%'
            }}>
                <Outlet />
            </main>
        </div>
    );
};

export default Layout;
