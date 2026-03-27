const NetWorth = () => {
    const assets = [
        { name: 'Stocks', value: '₹ 6,40,000', change: '+8.4%' },
        { name: 'Mutual Funds', value: '₹ 2,10,000', change: '+12.8%' },
        { name: 'Cash / Bank', value: '₹ 4,50,000', change: '0%' },
        { name: 'Fixed Deposits', value: '₹ 5,00,000', change: '+7.1%' },
        { name: 'Others', value: '₹ 4,50,000', change: 'N/A' },
    ];

    const liabilities = [
        { name: 'Home Loan', value: '₹ 3,50,000' },
        { name: 'Credit Cards', value: '₹ 55,000' },
    ];

    return (
        <div>
            <div className="page-header">
                <h1 className="page-title">Net Worth</h1>
            </div>

            <div style={{
                display: 'flex',
                gap: '24px',
                padding: '16px',
                background: 'white',
                borderRadius: '8px',
                marginBottom: '32px',
                fontSize: '15px',
                fontWeight: '600'
            }}>
                <span>Net Worth: <span style={{ color: 'var(--brand-color)' }}>₹ 18.45L</span></span>
                <span style={{ color: 'var(--border-color)' }}>|</span>
                <span>Assets: <span style={{ color: 'var(--success-color)' }}>₹ 22.5L</span></span>
                <span style={{ color: 'var(--border-color)' }}>|</span>
                <span>Liabilities: <span style={{ color: 'var(--error-color)' }}>₹ 4.05L</span></span>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1.5fr 1fr', gap: '32px' }}>
                <section>
                    <h2 style={{ fontSize: '18px', marginBottom: '16px' }}>Assets Breakdown</h2>
                    <div style={{ display: 'grid', gap: '12px' }}>
                        {assets.map((asset, i) => (
                            <div key={i} className="stat-card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '16px 24px' }}>
                                <div>
                                    <div style={{ fontWeight: '600' }}>{asset.name}</div>
                                    <div style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>Current Value</div>
                                </div>
                                <div style={{ textAlign: 'right' }}>
                                    <div style={{ fontWeight: '700', fontSize: '18px' }}>{asset.value}</div>
                                    <div style={{ fontSize: '14px', color: 'var(--success-color)' }}>{asset.change}</div>
                                </div>
                            </div>
                        ))}
                    </div>
                </section>

                <section>
                    <h2 style={{ fontSize: '18px', marginBottom: '16px' }}>Liabilities Breakdown</h2>
                    <div style={{ display: 'grid', gap: '12px' }}>
                        {liabilities.map((lib, i) => (
                            <div key={i} className="stat-card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '16px 24px' }}>
                                <div>
                                    <div style={{ fontWeight: '600' }}>{lib.name}</div>
                                    <div style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>Outstanding</div>
                                </div>
                                <div style={{ textAlign: 'right' }}>
                                    <div style={{ fontWeight: '700', fontSize: '18px', color: 'var(--error-color)' }}>{lib.value}</div>
                                </div>
                            </div>
                        ))}
                    </div>
                </section>
            </div>
        </div>
    );
};

export default NetWorth;
