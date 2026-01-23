const Insights = () => {
    const insights = [
        { title: 'Concentration Risk', message: 'Top 3 stocks = 42% of portfolio', type: 'warning' },
        { title: 'Asset Allocation', message: 'Equity exposure higher than moderate risk level', type: 'info' },
        { title: 'Sector Overweight', message: 'IT sector overweight compared to benchmark', type: 'info' },
    ];

    return (
        <div>
            <div className="page-header">
                <h1 className="page-title">Insights</h1>
            </div>

            <div style={{ display: 'grid', gap: '20px' }}>
                {insights.map((insight, i) => (
                    <div key={i} className="stat-card" style={{ borderLeft: `4px solid ${insight.type === 'warning' ? '#ffc107' : 'var(--brand-color)'}` }}>
                        <h3 style={{ fontSize: '16px', marginBottom: '8px' }}>{insight.title}</h3>
                        <p style={{ color: 'var(--text-secondary)' }}>{insight.message}</p>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default Insights;
