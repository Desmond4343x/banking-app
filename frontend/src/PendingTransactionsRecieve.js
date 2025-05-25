import React, { useEffect, useState } from 'react';
import axios from 'axios';
const api = process.env.REACT_APP_BACKEND_URL;

const PendingTransactionsRecieve = () => {
  const [transactions, setTransactions] = useState([]);
  const [userId, setUserId] = useState(null);
  const [errorMessage, setErrorMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const token = localStorage.getItem('token');

  useEffect(() => {
    const fetchUserId = async () => {
      try {
        const res = await axios.get(`${api}/bank/account`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        setUserId(res.data.accountId);
      } catch (err) {
        console.error('Error fetching user ID:', err);
        setErrorMessage('Failed to fetch user information.');
        setLoading(false);
      }
    };
    fetchUserId();
  }, [token]);

  useEffect(() => {
    if (!userId) return;

    const fetchPendingTransactions = async () => {
      try {
        const res = await axios.get(
          `${api}/bank/transactions/pending/${userId}/received`,
          {
            headers: { Authorization: `Bearer ${token}` },
          }
        );
        setTransactions(res.data);
      } catch (err) {
        console.error('Error fetching pending transaction requests:', err);
        setErrorMessage('Failed to fetch pending transaction requests.');
      } finally {
        setLoading(false);
      }
    };

    fetchPendingTransactions();
  }, [userId, token]);

  const handleDelete = async (transId) => {
    try {
      await axios.delete(`${api}/bank/transaction/${transId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setTransactions(transactions.filter(txn => txn.transId !== transId));
    } catch (err) {
      console.error('Error deleting transaction:', err);
      setErrorMessage('Failed to delete the request.');
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h2>Pending Transactions Requests</h2>

      {errorMessage && (
        <div style={{ color: 'red', marginBottom: '10px' }}>{errorMessage}</div>
      )}

      <div style={{ maxHeight: '500px', overflowY: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
          <thead style={{ backgroundColor: '#f0f0f0', textAlign: 'left' }}>
            <tr>
              <th style={{ padding: '8px', border: '1px solid #ddd' }}>Transaction ID</th>
              <th style={{ padding: '8px', border: '1px solid #ddd' }}>Sender ID</th>
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
                  Loading requests...
                </td>
              </tr>
            ) : transactions.length === 0 ? (
              <tr>
                <td colSpan="6" style={{ padding: '10px', textAlign: 'center' }}>
                  No pending Requests.
                </td>
              </tr>
            ) : (
              transactions.map((txn) => (
                <tr key={txn.transId}>
                  <td style={{ padding: '6px 8px', border: '1px solid #ddd' }}>{txn.transId}</td>
                  <td style={{ padding: '6px 8px', border: '1px solid #ddd' }}>{txn.senderId}</td>
                  <td style={{ padding: '6px 8px', border: '1px solid #ddd' }}>₹{txn.amount}</td>
                  <td style={{ padding: '6px 8px', border: '1px solid #ddd' }}>{txn.status}</td>
                  <td style={{ padding: '6px 8px', border: '1px solid #ddd' }}>{txn.timestamp}</td>
                  <td style={{ padding: '6px 8px', border: '1px solid #ddd' }}>
                    <button onClick={() => handleDelete(txn.transId)}>Delete</button>
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

export default PendingTransactionsRecieve;
