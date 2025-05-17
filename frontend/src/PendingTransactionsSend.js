import React, { useEffect, useState } from 'react';
import axios from 'axios';

const PendingTransactions = () => {
  const [transactions, setTransactions] = useState([]);
  const [userId, setUserId] = useState(null);
  const [balance, setBalance] = useState(0);
  const [errorMessage, setErrorMessage] = useState('');
  const token = localStorage.getItem('token');

  // Fetch ID balance
  useEffect(() => {
    const fetchUserDetails = async () => {
      try {
        const res = await axios.get('http://localhost:8080/bank/account', {
          headers: { Authorization: `Bearer ${token}` },
        });
        setUserId(res.data.accountId);
        setBalance(res.data.balance);
      } catch (err) {
        console.error('Failed to fetch user ID and balance:', err);
      }
    };

    fetchUserDetails();
  }, [token]);

  // Fetch where I'm sender
  useEffect(() => {
    if (!userId) return;

    const fetchPendingTransactions = async () => {
      try {
        const res = await axios.get(
          `http://localhost:8080/bank/transactions/pending/${userId}/sent`,
          { headers: { Authorization: `Bearer ${token}` } }
        );
        setTransactions(res.data);
      } catch (err) {
        console.error('Failed to fetch pending transactions:', err);
      }
    };

    fetchPendingTransactions();
  }, [userId, token]);

  // Accept transaction (only if balance is sufficient)
  const handleAccept = async (transId, amount) => {
    if (balance < amount) {
      setErrorMessage('Insufficient balance');
      return;
    }

    try {
      await axios.put(
        'http://localhost:8080/bank/transactions/pending/execute',
        { transId },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setTransactions(transactions.filter(t => t.transId !== transId));
      setBalance(prev => prev - amount);
      setErrorMessage('');
    } catch (err) {
      console.error('Failed to accept transaction:', err);
    }
  };

  // Decline transaction
  const handleDecline = async (transId) => {
    try {
      await axios.put(
        'http://localhost:8080/bank/transactions/pending/decline',
        { transId },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setTransactions(transactions.filter(t => t.transId !== transId));
    } catch (err) {
      console.error('Failed to decline transaction:', err);
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h2>Pending Transaction Approvals</h2>

      {errorMessage && (
        <div style={{ color: 'red', marginBottom: '10px' }}>{errorMessage}</div>
      )}

      <div style={{ maxHeight: '500px', overflowY: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
          <thead style={{ backgroundColor: '#f0f0f0', textAlign: 'left' }}>
            <tr>
              <th style={{ padding: '8px', border: '1px solid #ddd' }}>Transaction ID</th>
              <th style={{ padding: '8px', border: '1px solid #ddd' }}>Receiver ID</th>
              <th style={{ padding: '8px', border: '1px solid #ddd' }}>Amount (₹)</th>
              <th style={{ padding: '8px', border: '1px solid #ddd' }}>Status</th>
              <th style={{ padding: '8px', border: '1px solid #ddd' }}>Timestamp</th>
              <th style={{ padding: '8px', border: '1px solid #ddd' }}>Action</th>
            </tr>
          </thead>
          <tbody>
            {transactions.map((txn) => (
              <tr key={txn.transId}>
                <td style={{ padding: '6px 8px', border: '1px solid #ddd' }}>{txn.transId}</td>
                <td style={{ padding: '6px 8px', border: '1px solid #ddd' }}>{txn.receiverId}</td>
                <td style={{ padding: '6px 8px', border: '1px solid #ddd' }}>₹{txn.amount}</td>
                <td style={{ padding: '6px 8px', border: '1px solid #ddd' }}>{txn.status}</td>
                <td style={{ padding: '6px 8px', border: '1px solid #ddd' }}>{txn.timestamp}</td>
                <td style={{ padding: '6px 8px', border: '1px solid #ddd' }}>
                  <button
                    onClick={() => handleAccept(txn.transId, txn.amount)}
                    style={{ marginRight: '8px' }}
                  >
                    Accept
                  </button>
                  <button onClick={() => handleDecline(txn.transId)}>Decline</button>
                </td>
              </tr>
            ))}
            {transactions.length === 0 && (
              <tr>
                <td colSpan="6" style={{ padding: '10px', textAlign: 'center' }}>
                  No pending Approvals.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default PendingTransactions;
