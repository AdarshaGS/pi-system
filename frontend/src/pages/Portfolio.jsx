import { useState, useEffect } from 'react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts';
import TransactionModal from '../components/TransactionModal';
import api from '../api';
import stockPriceWebSocket from '../services/stockPriceWebSocket';

const Portfolio = () => {
    const [portfolioData, setPortfolioData] = useState(null);
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showTransactionModal, setShowTransactionModal] = useState(false);
    const [selectedStock, setSelectedStock] = useState(null);
    const [transactionType, setTransactionType] = useState('BUY');
    const [transactionStats, setTransactionStats] = useState(null);
    const [livePrices, setLivePrices] = useState({}); // Store live prices by symbol
    const [wsConnected, setWsConnected] = useState(false);

    const COLORS = ['#0066ff', '#00c49f', '#ffbb28', '#ff8042'];

    useEffect(() => {
        fetchPortfolioData();
        fetchTransactions();
        fetchTransactionStats();
        
        // Connect to WebSocket for live price updates
        connectWebSocket();

        // Cleanup on unmount
        return () => {
            stockPriceWebSocket.disconnect();
        };
    }, []);

    const connectWebSocket = () => {
        stockPriceWebSocket.connect(
            'http://localhost:8080/ws-stock-prices',
            () => {
                console.log('WebSocket connected successfully');
                setWsConnected(true);
                
                // Subscribe to all stock price updates
                stockPriceWebSocket.subscribeToAllStocks((priceUpdates) => {
                    console.log('Received price updates:', priceUpdates);
                    
                    // Update live prices state
                    const newPrices = {};
                    priceUpdates.forEach(update => {
                        newPrices[update.symbol] = {
                            currentPrice: update.currentPrice,
                            change: update.change,
                            changePercent: update.changePercent,
                            dayHigh: update.dayHigh,
                            dayLow: update.dayLow,
                            timestamp: update.timestamp
                        };
                    });
                    
                    setLivePrices(prevPrices => ({
                        ...prevPrices,
                        ...newPrices
                    }));
                });
            },
            (error) => {
                console.error('WebSocket connection error:', error);
                setWsConnected(false);
            }
        );
    };

    const fetchPortfolioData = async () => {
        try {
            const token = localStorage.getItem('token');
            const userIdResponse = await api.get('/auth/me', {
                headers: { Authorization: `Bearer ${token}` }
            });
            const userId = userIdResponse.data.id;

            const response = await api.get(`/portfolio/${userId}`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            setPortfolioData(response.data);
        } catch (error) {
            console.error('Error fetching portfolio:', error);
        } finally {
            setLoading(false);
        }
    };

    const fetchTransactions = async () => {
        try {
            const token = localStorage.getItem('token');
            const userIdResponse = await api.get('/auth/me', {
                headers: { Authorization: `Bearer ${token}` }
            });
            const userId = userIdResponse.data.id;

            const response = await api.get(`/portfolio/transactions/${userId}`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            setTransactions(response.data);
        } catch (error) {
            console.error('Error fetching transactions:', error);
        }
    };

    const fetchTransactionStats = async () => {
        try {
            const token = localStorage.getItem('token');
            const userIdResponse = await api.get('/auth/me', {
                headers: { Authorization: `Bearer ${token}` }
            });
            const userId = userIdResponse.data.id;

            const response = await api.get(`/portfolio/transactions/${userId}/stats`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            setTransactionStats(response.data);
        } catch (error) {
            console.error('Error fetching transaction stats:', error);
        }
    };

    const handleAddTransaction = (type = 'BUY', symbol = null) => {
        setTransactionType(type);
        setSelectedStock(symbol);
        setShowTransactionModal(true);
    };

    const handleTransactionSaved = () => {
        setShowTransactionModal(false);
        fetchPortfolioData();
        fetchTransactions();
        fetchTransactionStats();
    };

    const handleDeleteTransaction = async (transactionId) => {
        if (!window.confirm('Are you sure you want to delete this transaction?')) return;

        try {
            const token = localStorage.getItem('token');
            await api.delete(`/portfolio/transactions/${transactionId}`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            fetchTransactions();
            fetchPortfolioData();
            fetchTransactionStats();
        } catch (error) {
            console.error('Error deleting transaction:', error);
            alert('Failed to delete transaction');
        }
    };

    const formatCurrency = (value) => {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            maximumFractionDigits: 0
        }).format(value);
    };

    // Get live price for a symbol, fallback to portfolio data price
    const getLivePrice = (symbol) => {
        return livePrices[symbol]?.currentPrice || null;
    };

    // Calculate updated values with live prices
    const getUpdatedHolding = (holding) => {
        const livePrice = getLivePrice(holding.stockSymbol);
        if (livePrice) {
            const currentValue = holding.quantity * livePrice;
            const profitLoss = currentValue - holding.investedAmount;
            const returnPercentage = (profitLoss / holding.investedAmount) * 100;
            
            return {
                ...holding,
                currentPrice: livePrice,
                currentValue,
                profitLoss,
                returnPercentage
            };
        }
        return holding;
    };

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

    const assetData = portfolioData?.assetAllocation || [
        { name: 'Equity', value: 640000 },
        { name: 'Debt', value: 500000 },
        { name: 'Cash', value: 450000 },
    ];

    const sectorData = portfolioData?.sectorAllocation || [
        { name: 'IT', value: 40 },
        { name: 'Finance', value: 30 },
        { name: 'FMCG', value: 20 },
        { name: 'Others', value: 10 },
    ];

    return (
        <div>
            <style>{`
                @keyframes pulse {
                    0%, 100% { opacity: 1; }
                    50% { opacity: 0.5; }
                }
            `}</style>
            
            <div className="page-header">
                <h1 className="page-title">Portfolio</h1>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{ 
                        display: 'flex', 
                        alignItems: 'center', 
                        gap: '8px',
                        padding: '6px 12px',
                        borderRadius: '20px',
                        backgroundColor: wsConnected ? '#e8f5e9' : '#ffebee',
                        fontSize: '12px'
                    }}>
                        <span style={{ 
                            width: '8px', 
                            height: '8px', 
                            borderRadius: '50%', 
                            backgroundColor: wsConnected ? '#4caf50' : '#f44336',
                            animation: wsConnected ? 'pulse 2s infinite' : 'none'
                        }}></span>
                        <span style={{ color: wsConnected ? '#2e7d32' : '#c62828' }}>
                            {wsConnected ? 'Live Updates' : 'Offline'}
                        </span>
                    </div>
                    <button 
                        className="btn-primary" 
                        onClick={() => handleAddTransaction('BUY')}
                        style={{ padding: '10px 20px', fontSize: '14px' }}
                    >
                        + Add Transaction
                    </button>
                </div>
            </div>

            <section className="hero-card" style={{ marginBottom: '32px' }}>
                <div className="hero-label">Total Portfolio Value</div>
                <div className="hero-value">
                    {portfolioData?.totalCurrentValue 
                        ? formatCurrency(portfolioData.totalCurrentValue) 
                        : '₹ 0'}
                </div>
                <div className={`hero-delta ${portfolioData?.overallReturn >= 0 ? 'delta-positive' : 'delta-negative'}`}>
                    Overall Return: {portfolioData?.overallReturn ? `${portfolioData.overallReturn.toFixed(2)}%` : '0%'}
                </div>
            </section>

            {transactionStats && (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '16px', marginBottom: '32px' }}>
                    <div className="stat-card">
                        <div style={{ fontSize: '12px', color: '#666', marginBottom: '8px' }}>Total Invested</div>
                        <div style={{ fontSize: '24px', fontWeight: '600' }}>
                            {formatCurrency(transactionStats.totalInvested || 0)}
                        </div>
                    </div>
                    <div className="stat-card">
                        <div style={{ fontSize: '12px', color: '#666', marginBottom: '8px' }}>Realized Gains</div>
                        <div style={{ fontSize: '24px', fontWeight: '600', color: transactionStats.totalRealizedGains >= 0 ? 'var(--success-color)' : 'var(--danger-color)' }}>
                            {formatCurrency(transactionStats.totalRealizedGains || 0)}
                        </div>
                    </div>
                    <div className="stat-card">
                        <div style={{ fontSize: '12px', color: '#666', marginBottom: '8px' }}>Total Transactions</div>
                        <div style={{ fontSize: '24px', fontWeight: '600' }}>{transactionStats.totalTransactions || 0}</div>
                    </div>
                    <div className="stat-card">
                        <div style={{ fontSize: '12px', color: '#666', marginBottom: '8px' }}>Buy/Sell Ratio</div>
                        <div style={{ fontSize: '24px', fontWeight: '600' }}>
                            {transactionStats.buyCount || 0} / {transactionStats.sellCount || 0}
                        </div>
                    </div>
                </div>
            )}

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px', marginBottom: '32px' }}>
                <ChartBox title="Asset Allocation" data={assetData} />
                <ChartBox title="Sector Allocation" data={sectorData} />
            </div>

            <div className="data-table-container">
                <h2 className="section-title">Holdings</h2>
                {portfolioData?.holdings && portfolioData.holdings.length > 0 ? (
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>Symbol</th>
                                <th>Quantity</th>
                                <th>Avg Price</th>
                                <th>Current Price</th>
                                <th>Invested Amount</th>
                                <th>Current Value</th>
                                <th>P&L</th>
                                <th>Return %</th>
                            </tr>
                        </thead>
                        <tbody>
                            {portfolioData.holdings.map((holding, i) => {
                                const updatedHolding = getUpdatedHolding(holding);
                                const priceChange = livePrices[holding.stockSymbol];
                                
                                return (
                                    <tr key={i}>
                                        <td style={{ fontWeight: '600' }}>{updatedHolding.stockSymbol}</td>
                                        <td>{updatedHolding.quantity}</td>
                                        <td>{formatCurrency(updatedHolding.avgPrice)}</td>
                                        <td>
                                            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-start' }}>
                                                <span>{formatCurrency(updatedHolding.currentPrice)}</span>
                                                {priceChange && priceChange.changePercent && (
                                                    <span style={{ 
                                                        fontSize: '11px',
                                                        color: priceChange.changePercent >= 0 ? '#4caf50' : '#f44336'
                                                    }}>
                                                        {priceChange.changePercent >= 0 ? '▲' : '▼'} {Math.abs(priceChange.changePercent).toFixed(2)}%
                                                    </span>
                                                )}
                                            </div>
                                        </td>
                                        <td>{formatCurrency(updatedHolding.investedAmount)}</td>
                                        <td>{formatCurrency(updatedHolding.currentValue)}</td>
                                        <td style={{ color: updatedHolding.profitLoss >= 0 ? 'var(--success-color)' : 'var(--danger-color)', fontWeight: '600' }}>
                                            {formatCurrency(updatedHolding.profitLoss)}
                                        </td>
                                        <td style={{ color: updatedHolding.returnPercentage >= 0 ? 'var(--success-color)' : 'var(--danger-color)', fontWeight: '600' }}>
                                            {updatedHolding.returnPercentage.toFixed(2)}%
                                        </td>
                                        <td>
                                            <button 
                                                onClick={() => handleAddTransaction('BUY', updatedHolding.stockSymbol)}
                                                style={{ marginRight: '8px', padding: '4px 8px', fontSize: '12px' }}
                                            >
                                                Buy
                                            </button>
                                            <button 
                                                onClick={() => handleAddTransaction('SELL', updatedHolding.stockSymbol)}
                                                style={{ padding: '4px 8px', fontSize: '12px' }}
                                            >
                                                Sell
                                            </button>
                                        </td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                ) : (
                    <p>No holdings data available.</p>
                )}
            </div>

            <h2 style={{ fontSize: '18px', marginTop: '48px', marginBottom: '16px' }}>Transaction History</h2>
            <div className="data-table-container">
                <table>
                    <thead>
                        <tr>
                            <th>Date</th>
                            <th>Type</th>
                            <th>Symbol</th>
                            <th>Quantity</th>
                            <th>Price</th>
                            <th>Fees</th>
                            <th>Total Amount</th>
                            <th>Realized Gain</th>
                            <th>Notes</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {transactions.length > 0 ? (
                            transactions.map((tx) => (
                                <tr key={tx.id}>
                                    <td>{new Date(tx.transactionDate).toLocaleDateString()}</td>
                                    <td>
                                        <span style={{ 
                                            padding: '4px 8px', 
                                            borderRadius: '4px', 
                                            fontSize: '12px',
                                            backgroundColor: tx.transactionType === 'BUY' ? '#e3f2fd' : '#ffebee',
                                            color: tx.transactionType === 'BUY' ? '#1976d2' : '#c62828'
                                        }}>
                                            {tx.transactionType}
                                        </span>
                                    </td>
                                    <td style={{ fontWeight: '600' }}>{tx.symbol}</td>
                                    <td>{tx.quantity}</td>
                                    <td>{formatCurrency(tx.price)}</td>
                                    <td>{formatCurrency(tx.fees)}</td>
                                    <td>{formatCurrency(tx.totalAmount)}</td>
                                    <td style={{ color: tx.realizedGain >= 0 ? 'var(--success-color)' : 'var(--danger-color)' }}>
                                        {tx.realizedGain ? formatCurrency(tx.realizedGain) : '-'}
                                    </td>
                                    <td>{tx.notes || '-'}</td>
                                    <td>
                                        <button 
                                            onClick={() => handleDeleteTransaction(tx.id)}
                                            style={{ padding: '4px 8px', fontSize: '12px', backgroundColor: '#f44336', color: 'white' }}
                                        >
                                            Delete
                                        </button>
                                    </td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan="10" style={{ textAlign: 'center', padding: '20px', color: '#666' }}>
                                    No transactions recorded yet.
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>

            {showTransactionModal && (
                <TransactionModal
                    type={transactionType}
                    symbol={selectedStock}
                    onSave={handleTransactionSaved}
                    onClose={() => setShowTransactionModal(false)}
                />
            )}
        </div>
    );
};

export default Portfolio;
