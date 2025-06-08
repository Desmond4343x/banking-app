import React, { useEffect, useState } from 'react';
import axios from 'axios';
const api = process.env.REACT_APP_BACKEND_URL;

const PendingTransactions = () => {
  const [transactions, setTransactions] = useState([]);
  const [userId, setUserId] = useState(null);
  const [balance, setBalance] = useState(0);
  const [errorMessage, setErrorMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [globalProcessing, setGlobalProcessing] = useState(false);

  const token = localStorage.getItem('token');

  useEffect(() => {
    const fetchUserDetails = async () => {
      try {
        const res = await axios.get(`${api}/bank/account`, {
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

  useEffect(() => {
    if (!userId) return;

    const fetchPendingTransactions = async () => {
      setLoading(true);
      try {
        const res = await axios.get(
          `${api}/bank/transactions/pending/${userId}/sent`,
          { headers: { Authorization: `Bearer ${token}` } }
        );
        setTransactions(res.data);
      } catch (err) {
        console.error('Failed to fetch pending transactions:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchPendingTransactions();
  }, [userId, token]);

  const handleAccept = async (transId, amount) => {
    if (globalProcessing) return;
    if (balance < amount) {
      setErrorMessage('Insufficient balance');
      return;
    }

    setGlobalProcessing(true);

    try {
      await axios.put(
        `${api}/bank/transactions/pending/execute`,
        { transId },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setTransactions(transactions.filter(t => t.transId !== transId));
      setBalance(prev => prev - amount);
      setErrorMessage('');
    } catch (err) {
      console.error('Failed to accept transaction:', err);
    } finally {
      setGlobalProcessing(false);
    }
  };

  const handleDecline = async (transId) => {
    if (globalProcessing) return;

    setGlobalProcessing(true);

    try {
      await axios.put(
        `${api}/bank/transactions/pending/decline`,
        { transId },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setTransactions(transactions.filter(t => t.transId !== transId));
    } catch (err) {
      console.error('Failed to decline transaction:', err);
    } finally {
      setGlobalProcessing(false);
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
            {loading ? (
              <tr>
                <td colSpan="6" style={{ padding: '10px', textAlign: 'center' }}>
                  Loading transactions...
                </td>
              </tr>
            ) : transactions.length === 0 ? (
              <tr>
                <td colSpan="6" style={{ padding: '10px', textAlign: 'center' }}>
                  No pending approvals.
                </td>
              </tr>
            ) : (
              transactions.map((txn) => (
                <tr key={txn.transId}>
                  <td style={{ padding: '6px 8px', border: '1px solid #ddd' }}>{txn.transId}</td>
                  <td style={{ padding: '6px 8px', border: '1px solid #ddd' }}>{txn.receiverId}</td>
                  <td style={{ padding: '6px 8px', border: '1px solid #ddd' }}>₹{txn.amount}</td>
                  <td style={{ padding: '6px 8px', border: '1px solid #ddd' }}>{txn.status}</td>
                  <td style={{ padding: '6px 8px', border: '1px solid #ddd' }}>{txn.timestamp}</td>
                  <td style={{ padding: '6px 8px', border: '1px solid #ddd' }}>
                    <button
                      onClick={() => handleAccept(txn.transId, txn.amount)}
                      disabled={globalProcessing}
                      style={{ marginRight: '8px' }}
                    >
                      Accept
                    </button>
                    <button
                      onClick={() => handleDecline(txn.transId)}
                      disabled={globalProcessing}
                    >
                      Decline
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default PendingTransactions;
