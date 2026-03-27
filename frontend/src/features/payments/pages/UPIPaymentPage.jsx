// UPI frontend integration stub
// Add this to your React app (e.g., src/pages/payments/UPIPayment.jsx)

import { useState } from 'react';

const UPIPayment = () => {
  const [payerVPA, setPayerVPA] = useState('');
  const [payeeVPA, setPayeeVPA] = useState('');
  const [amount, setAmount] = useState('');
  const [remarks, setRemarks] = useState('');
  const [txnId, setTxnId] = useState(null);
  const [status, setStatus] = useState(null);

  const initiatePayment = async () => {
    const res = await fetch('http://localhost:8082/api/v1/upi/initiate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ payerVPA, payeeVPA, amount, remarks })
    });
    const text = await res.text();
    setTxnId(text.match(/Transaction ID: (TXN\d+)/)?.[1] || null);
  };

  const checkStatus = async () => {
    if (!txnId) return;
    const res = await fetch(`http://localhost:8082/api/v1/upi/status/${txnId}`);
    const text = await res.text();
    setStatus(text);
  };

  return (
    <div>
      <h2>UPI Payment</h2>
      <input placeholder="Payer VPA" value={payerVPA} onChange={e => setPayerVPA(e.target.value)} />
      <input placeholder="Payee VPA" value={payeeVPA} onChange={e => setPayeeVPA(e.target.value)} />
      <input placeholder="Amount" value={amount} onChange={e => setAmount(e.target.value)} />
      <input placeholder="Remarks" value={remarks} onChange={e => setRemarks(e.target.value)} />
      <button onClick={initiatePayment}>Initiate Payment</button>
      {txnId && <div>Transaction ID: {txnId} <button onClick={checkStatus}>Check Status</button></div>}
      {status && <div>Status: {status}</div>}
    </div>
  );
};

export default UPIPayment;
