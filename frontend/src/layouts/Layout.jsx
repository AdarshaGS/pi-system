import { NavLink, Outlet } from 'react-router-dom';
import {
    LayoutDashboard,
    Wallet,
    Briefcase,
    Lightbulb,
    Settings as SettingsIcon,
    LogOut
} from 'lucide-react';

const Layout = () => {
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
                    <NavLink to="/budget" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                        <Wallet />
                        Budget
                    </NavLink>
                    <NavLink to="/portfolio" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                        <Briefcase />
                        Portfolio
                    </NavLink>
                    <NavLink to="/insights" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                        <Lightbulb />
                        Insights
                    </NavLink>
                    <NavLink to="/settings" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                        <SettingsIcon />
                        Settings
                    </NavLink>
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
