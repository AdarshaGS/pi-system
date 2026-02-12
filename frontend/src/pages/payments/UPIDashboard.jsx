import React, { useState, useEffect } from 'react';
import './UPIDashboard.css';

const UPIDashboard = () => {
    const [activeTab, setActiveTab] = useState('send');
    const [upiId, setUpiId] = useState('');
    const [transactions, setTransactions] = useState([]);
    const [pendingRequests, setPendingRequests] = useState([]);

    // Send Money State
    const [sendData, setSendData] = useState({
        senderUpiId: '',
        receiverUpiId: '',
        amount: '',
        pin: '',
        remarks: ''
    });

    // Request Money State
    const [requestData, setRequestData] = useState({
        requesterUpiId: '',
        payerUpiId: '',
        amount: '',
        remarks: ''
    });

    // QR Code State
    const [qrData, setQrData] = useState({
        upiId: '',
        amount: '',
        merchantName: '',
        remarks: ''
    });
    const [generatedQR, setGeneratedQR] = useState(null);

    // Load transaction history
    const loadTransactionHistory = async () => {
        if (!upiId) return;
        try {
            const response = await fetch(`/api/upi/transactions/history?upiId=${upiId}`);
            const data = await response.json();
            setTransactions(data);
        } catch (error) {
            console.error('Error loading transactions:', error);
        }
    };

    // Load pending requests
    const loadPendingRequests = async () => {
        if (!upiId) return;
        try {
            const response = await fetch(`/api/upi/transactions/requests/pending?upiId=${upiId}`);
            const data = await response.json();
            setPendingRequests(data);
        } catch (error) {
            console.error('Error loading pending requests:', error);
        }
    };

    useEffect(() => {
        loadTransactionHistory();
        loadPendingRequests();
    }, [upiId]);

    // Send Money Handler
    const handleSendMoney = async (e) => {
        e.preventDefault();
        try {
            const response = await fetch('/api/upi/transactions/send', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(sendData)
            });
            const result = await response.json();
            alert(result.message);
            if (result.status === 'success') {
                setSendData({ senderUpiId: '', receiverUpiId: '', amount: '', pin: '', remarks: '' });
                loadTransactionHistory();
            }
        } catch (error) {
            alert('Error sending money: ' + error.message);
        }
    };

    // Request Money Handler
    const handleRequestMoney = async (e) => {
        e.preventDefault();
        try {
            const response = await fetch('/api/upi/transactions/request', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(requestData)
            });
            const result = await response.json();
            alert(result.message);
            if (result.status === 'pending') {
                setRequestData({ requesterUpiId: '', payerUpiId: '', amount: '', remarks: '' });
            }
        } catch (error) {
            alert('Error requesting money: ' + error.message);
        }
    };

    // Generate QR Code Handler
    const handleGenerateQR = async (e) => {
        e.preventDefault();
        try {
            const response = await fetch('/api/upi/qr/generate', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(qrData)
            });
            const result = await response.json();
            if (result.status === 'success') {
                setGeneratedQR(result.qrData);
                alert('QR Code generated successfully!');
            } else {
                alert(result.message);
            }
        } catch (error) {
            alert('Error generating QR: ' + error.message);
        }
    };

    // Accept Payment Request
    const handleAcceptRequest = async (requestId) => {
        const pin = prompt('Enter your UPI PIN to accept this request:');
        if (!pin) return;

        try {
            const response = await fetch(`/api/upi/transactions/requests/${requestId}/accept`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ pin })
            });
            const result = await response.json();
            alert(result.message);
            if (result.status === 'success') {
                loadPendingRequests();
                loadTransactionHistory();
            }
        } catch (error) {
            alert('Error accepting request: ' + error.message);
        }
    };

    // Reject Payment Request
    const handleRejectRequest = async (requestId) => {
        if (!confirm('Are you sure you want to reject this request?')) return;

        try {
            const response = await fetch(`/api/upi/transactions/requests/${requestId}/reject`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });
            const result = await response.json();
            alert(result.message);
            loadPendingRequests();
        } catch (error) {
            alert('Error rejecting request: ' + error.message);
        }
    };

    return (
        <div className="upi-dashboard">
            <div className="dashboard-header">
                <h1>💳 UPI Payments</h1>
                <div className="upi-id-selector">
                    <label>Your UPI ID:</label>
                    <input
                        type="text"
                        value={upiId}
                        onChange={(e) => setUpiId(e.target.value)}
                        placeholder="yourname@upi"
                        className="upi-id-input"
                    />
                </div>
            </div>

            <div className="tabs">
                <button
                    className={activeTab === 'send' ? 'tab active' : 'tab'}
                    onClick={() => setActiveTab('send')}
                >
                    Send Money
                </button>
                <button
                    className={activeTab === 'request' ? 'tab active' : 'tab'}
                    onClick={() => setActiveTab('request')}
                >
                    Request Money
                </button>
                <button
                    className={activeTab === 'qr' ? 'tab active' : 'tab'}
                    onClick={() => setActiveTab('qr')}
                >
                    QR Code
                </button>
                <button
                    className={activeTab === 'pending' ? 'tab active' : 'tab'}
                    onClick={() => setActiveTab('pending')}
                >
                    Pending Requests ({pendingRequests.length})
                </button>
                <button
                    className={activeTab === 'history' ? 'tab active' : 'tab'}
                    onClick={() => setActiveTab('history')}
                >
                    History
                </button>
            </div>

            <div className="tab-content">
                {activeTab === 'send' && (
                    <div className="send-money-form">
                        <h2>Send Money</h2>
                        <form onSubmit={handleSendMoney}>
                            <div className="form-group">
                                <label>From UPI ID</label>
                                <input
                                    type="text"
                                    value={sendData.senderUpiId}
                                    onChange={(e) => setSendData({ ...sendData, senderUpiId: e.target.value })}
                                    placeholder="yourname@upi"
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label>To UPI ID</label>
                                <input
                                    type="text"
                                    value={sendData.receiverUpiId}
                                    onChange={(e) => setSendData({ ...sendData, receiverUpiId: e.target.value })}
                                    placeholder="receiver@upi"
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label>Amount (₹)</label>
                                <input
                                    type="number"
                                    value={sendData.amount}
                                    onChange={(e) => setSendData({ ...sendData, amount: e.target.value })}
                                    placeholder="0.00"
                                    min="1"
                                    step="0.01"
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label>UPI PIN</label>
                                <input
                                    type="password"
                                    value={sendData.pin}
                                    onChange={(e) => setSendData({ ...sendData, pin: e.target.value })}
                                    placeholder="Enter 4-6 digit PIN"
                                    maxLength="6"
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label>Remarks (Optional)</label>
                                <input
                                    type="text"
                                    value={sendData.remarks}
                                    onChange={(e) => setSendData({ ...sendData, remarks: e.target.value })}
                                    placeholder="Payment for..."
                                />
                            </div>
                            <button type="submit" className="btn-primary">Send Money</button>
                        </form>
                    </div>
                )}

                {activeTab === 'request' && (
                    <div className="request-money-form">
                        <h2>Request Money</h2>
                        <form onSubmit={handleRequestMoney}>
                            <div className="form-group">
                                <label>Your UPI ID</label>
                                <input
                                    type="text"
                                    value={requestData.requesterUpiId}
                                    onChange={(e) => setRequestData({ ...requestData, requesterUpiId: e.target.value })}
                                    placeholder="yourname@upi"
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label>Request From (UPI ID)</label>
                                <input
                                    type="text"
                                    value={requestData.payerUpiId}
                                    onChange={(e) => setRequestData({ ...requestData, payerUpiId: e.target.value })}
                                    placeholder="payer@upi"
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label>Amount (₹)</label>
                                <input
                                    type="number"
                                    value={requestData.amount}
                                    onChange={(e) => setRequestData({ ...requestData, amount: e.target.value })}
                                    placeholder="0.00"
                                    min="1"
                                    step="0.01"
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label>Remarks (Optional)</label>
                                <input
                                    type="text"
                                    value={requestData.remarks}
                                    onChange={(e) => setRequestData({ ...requestData, remarks: e.target.value })}
                                    placeholder="Request for..."
                                />
                            </div>
                            <button type="submit" className="btn-primary">Request Money</button>
                        </form>
                    </div>
                )}

                {activeTab === 'qr' && (
                    <div className="qr-code-section">
                        <h2>Generate QR Code</h2>
                        <form onSubmit={handleGenerateQR}>
                            <div className="form-group">
                                <label>Your UPI ID</label>
                                <input
                                    type="text"
                                    value={qrData.upiId}
                                    onChange={(e) => setQrData({ ...qrData, upiId: e.target.value })}
                                    placeholder="yourname@upi"
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label>Amount (₹)</label>
                                <input
                                    type="number"
                                    value={qrData.amount}
                                    onChange={(e) => setQrData({ ...qrData, amount: e.target.value })}
                                    placeholder="0.00"
                                    min="1"
                                    step="0.01"
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label>Merchant Name (Optional)</label>
                                <input
                                    type="text"
                                    value={qrData.merchantName}
                                    onChange={(e) => setQrData({ ...qrData, merchantName: e.target.value })}
                                    placeholder="Your Business Name"
                                />
                            </div>
                            <div className="form-group">
                                <label>Remarks (Optional)</label>
                                <input
                                    type="text"
                                    value={qrData.remarks}
                                    onChange={(e) => setQrData({ ...qrData, remarks: e.target.value })}
                                    placeholder="Payment for..."
                                />
                            </div>
                            <button type="submit" className="btn-primary">Generate QR Code</button>
                        </form>

                        {generatedQR && (
                            <div className="qr-result">
                                <h3>QR Code Generated!</h3>
                                <div className="qr-data">
                                    <code>{generatedQR}</code>
                                </div>
                                <p className="qr-note">
                                    Use this UPI string to generate a QR code using any QR code library
                                </p>
                            </div>
                        )}
                    </div>
                )}

                {activeTab === 'pending' && (
                    <div className="pending-requests">
                        <h2>Pending Payment Requests</h2>
                        {pendingRequests.length === 0 ? (
                            <p className="no-data">No pending requests</p>
                        ) : (
                            <div className="requests-list">
                                {pendingRequests.map((request) => (
                                    <div key={request.requestId} className="request-card">
                                        <div className="request-info">
                                            <p><strong>From:</strong> {request.requesterUpiId}</p>
                                            <p><strong>Amount:</strong> ₹{request.amount}</p>
                                            <p><strong>Remarks:</strong> {request.remarks || 'N/A'}</p>
                                            <p><strong>Date:</strong> {new Date(request.createdAt).toLocaleString()}</p>
                                        </div>
                                        <div className="request-actions">
                                            <button
                                                onClick={() => handleAcceptRequest(request.requestId)}
                                                className="btn-success"
                                            >
                                                Accept
                                            </button>
                                            <button
                                                onClick={() => handleRejectRequest(request.requestId)}
                                                className="btn-danger"
                                            >
                                                Reject
                                            </button>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                )}

                {activeTab === 'history' && (
                    <div className="transaction-history">
                        <h2>Transaction History</h2>
                        {transactions.length === 0 ? (
                            <p className="no-data">No transactions yet</p>
                        ) : (
                            <div className="transactions-list">
                                {transactions.map((tx) => (
                                    <div key={tx.transactionId} className={`transaction-card ${tx.type}`}>
                                        <div className="tx-icon">
                                            {tx.type === 'credit' ? '↓' : '↑'}
                                        </div>
                                        <div className="tx-info">
                                            <p className="tx-type">{tx.type === 'credit' ? 'Received' : 'Sent'}</p>
                                            <p className="tx-remarks">{tx.remarks || 'No remarks'}</p>
                                            <p className="tx-date">{new Date(tx.date).toLocaleString()}</p>
                                        </div>
                                        <div className="tx-amount">
                                            <p className={tx.type === 'credit' ? 'amount-credit' : 'amount-debit'}>
                                                {tx.type === 'credit' ? '+' : '-'}₹{tx.amount}
                                            </p>
                                            <p className={`tx-status status-${tx.status}`}>{tx.status}</p>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default UPIDashboard;
