import { useNavigate } from 'react-router-dom';

const Settings = () => {
    const navigate = useNavigate();

    return (
        <div>
            <div className="page-header">
                <h1 className="page-title">Settings</h1>
            </div>

            <div style={{ maxWidth: '600px' }}>
                <section className="stat-card" style={{ marginBottom: '24px' }}>
                    <h2 style={{ fontSize: '18px', marginBottom: '20px' }}>Profile Information</h2>
                    <div style={{ display: 'grid', gap: '16px' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border-color)', paddingBottom: '12px' }}>
                            <span style={{ color: 'var(--text-secondary)' }}>Full Name</span>
                            <span style={{ fontWeight: '600' }}>Adarsha GS</span>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border-color)', paddingBottom: '12px' }}>
                            <span style={{ color: 'var(--text-secondary)' }}>Email Address</span>
                            <span style={{ fontWeight: '600' }}>adarsha@example.com</span>
                        </div>
                    </div>
                </section>

                <section className="stat-card" style={{ marginBottom: '24px' }}>
                    <h2 style={{ fontSize: '18px', marginBottom: '20px' }}>Data & Sync</h2>
                    <div style={{ display: 'grid', gap: '16px' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <div>
                                <div style={{ fontWeight: '600' }}>Account Aggregator</div>
                                <div style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>Connected via Setu AA</div>
                            </div>
                            <span style={{ padding: '4px 12px', background: '#e6fffa', color: '#2c7a7b', borderRadius: '12px', fontSize: '12px', fontWeight: '700' }}>ACTIVE</span>
                        </div>
                        <button className="auth-button" style={{ background: 'white', color: 'var(--brand-color)', border: '1px solid var(--brand-color)', height: '40px', padding: '0 16px', fontSize: '14px', width: 'auto' }}>
                            Refresh Data
                        </button>
                    </div>
                </section>

                <section>
                    <button
                        onClick={() => navigate('/login')}
                        style={{ color: 'var(--error-color)', fontWeight: '600', background: 'none', padding: '0', fontSize: '14px' }}
                    >
                        Logout from all devices
                    </button>
                </section>
            </div>
        </div>
    );
};

export default Settings;
