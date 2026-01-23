import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts';

const Portfolio = () => {
    const assetData = [
        { name: 'Equity', value: 640000 },
        { name: 'Debt', value: 500000 },
        { name: 'Cash', value: 450000 },
    ];

    const sectorData = [
        { name: 'IT', value: 40 },
        { name: 'Finance', value: 30 },
        { name: 'FMCG', value: 20 },
        { name: 'Others', value: 10 },
    ];

    const marketCapData = [
        { name: 'Large Cap', value: 60 },
        { name: 'Mid Cap', value: 25 },
        { name: 'Small Cap', value: 15 },
    ];

    const COLORS = ['#0066ff', '#00c49f', '#ffbb28', '#ff8042'];

    const holdings = [
        { name: 'Reliance Industries', invested: '₹ 1,20,000', current: '₹ 1,45,000', allocation: '12%', xirr: '18.2%' },
        { name: 'HDFC Bank', invested: '₹ 95,000', current: '₹ 1,12,000', allocation: '10%', xirr: '14.5%' },
        { name: 'TCS', invested: '₹ 80,000', current: '₹ 88,000', allocation: '8%', xirr: '10.1%' },
        { name: 'Infosys', invested: '₹ 75,000', current: '₹ 82,000', allocation: '7.5%', xirr: '9.4%' },
    ];

    const ChartBox = ({ title, data }) => (
        <div className="stat-card" style={{ height: '300px', display: 'flex', flexDirection: 'column' }}>
            <h3 style={{ fontSize: '14px', marginBottom: '16px', textAlign: 'center' }}>{title}</h3>
            <div style={{ flex: 1 }}>
                <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                        <Pie
                            data={data}
                            innerRadius={60}
                            outerRadius={80}
                            paddingAngle={5}
                            dataKey="value"
                        >
                            {data.map((entry, index) => (
                                <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                            ))}
                        </Pie>
                        <Tooltip />
                    </PieChart>
                </ResponsiveContainer>
            </div>
        </div>
    );

    return (
        <div>
            <div className="page-header">
                <h1 className="page-title">Portfolio</h1>
            </div>

            <section className="hero-card" style={{ marginBottom: '32px' }}>
                <div className="hero-label">Total Portfolio Value</div>
                <div className="hero-value">₹ 9,80,000</div>
                <div className="hero-delta delta-positive">Overall XIRR: 13.8%</div>
            </section>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '24px', marginBottom: '32px' }}>
                <ChartBox title="Asset Allocation" data={assetData} />
                <ChartBox title="Sector Allocation" data={sectorData} />
                <ChartBox title="Market Cap Split" data={marketCapData} />
            </div>

            <h2 style={{ fontSize: '18px', marginBottom: '16px' }}>Holdings</h2>
            <div className="data-table-container">
                <table>
                    <thead>
                        <tr>
                            <th>Instrument Name</th>
                            <th>Invested Value</th>
                            <th>Current Value</th>
                            <th>% Allocation</th>
                            <th>XIRR</th>
                        </tr>
                    </thead>
                    <tbody>
                        {holdings.map((item, i) => (
                            <tr key={i}>
                                <td style={{ fontWeight: '600' }}>{item.name}</td>
                                <td>{item.invested}</td>
                                <td>{item.current}</td>
                                <td>{item.allocation}</td>
                                <td style={{ color: 'var(--success-color)', fontWeight: '600' }}>{item.xirr}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default Portfolio;
