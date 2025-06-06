import React, { useState } from 'react';
import axios from 'axios';

const api = process.env.REACT_APP_BACKEND_URL;

const AdminDashboard = () => {
  const [tableData, setTableData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [accountId, setAccountId] = useState('');
  const [accountData, setAccountData] = useState(null);
  const [showConfirm, setShowConfirm] = useState(false);
  const [confirmId, setConfirmId] = useState(null);

  const token = localStorage.getItem('token');

  const fetchAllAccounts = async () => {
    try {
      setLoading(true);
      setErrorMsg('');
      setAccountData(null);
      const res = await axios.get(`${api}/bank/accounts`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setTableData(res.data);
    } catch (err) {
      setErrorMsg('Failed to fetch accounts.');
    } finally {
      setLoading(false);
    }
  };

  const fetchAllTransactions = async () => {
    try {
      setLoading(true);
      setErrorMsg('');
      setAccountData(null);
      const res = await axios.get(`${api}/bank/transactions`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setTableData(res.data);
    } catch (err) {
      setErrorMsg('Failed to fetch transactions.');
    } finally {
      setLoading(false);
    }
  };

  const fetchPendingTransactions = async () => {
    try {
      setLoading(true);
      setErrorMsg('');
      setAccountData(null);
      const res = await axios.get(`${api}/bank/transactions/pending`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setTableData(res.data);
    } catch (err) {
      setErrorMsg('Failed to fetch pending transactions.');
    } finally {
      setLoading(false);
    }
  };

  const fetchAccountById = async () => {
      if (!accountId) {
        setErrorMsg('Please enter a valid Account ID.');
        return;
      }
    try {
      setLoading(true);
      setErrorMsg('');
      const res = await axios.get(`${api}/bank/accounts/${accountId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setAccountData(res.data);
      setTableData([]);
    } catch (err) {
      setErrorMsg('Failed to fetch account by ID.');
      setAccountData(null);
    } finally {
      setLoading(false);
    }
  };

  const fetchTransactionUser = async () => {
      if (!accountId) {
        setErrorMsg('Please enter a valid Account ID.');
        return;
      }
    try {
      setLoading(true);
      setErrorMsg('');
      const res = await axios.get(`${api}/bank/transactions/${accountId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setTableData(res.data);
      setAccountData(null);
    } catch (err) {
      setErrorMsg('Failed to fetch transactions.');
    } finally {
      setLoading(false);
    }
  };

  const fetchPendingTransactionsById = async () => {
      if (!accountId) {
        setErrorMsg('Please enter a valid Account ID.');
        return;
      }
    try {
      setLoading(true);
      setErrorMsg('');
      const res = await axios.get(`${api}/bank/transactions/pending/${accountId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setTableData(res.data);
      setAccountData(null);
    } catch (err) {
      setErrorMsg('Failed to fetch pending transactions by ID.');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteClick = (id) => {
    setConfirmId(id);
    setShowConfirm(true);
  };

  const confirmDelete = async () => {
    try {
      setLoading(true);
      await axios.delete(`${api}/bank/accounts/${confirmId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setTableData((prev) => prev.filter(acc => acc.accountId !== confirmId));
      if (accountData?.accountId === confirmId) {
        setAccountData(null);
      }
      setErrorMsg('');
    } catch (err) {
      setErrorMsg('Failed to delete account.');
    } finally {
      setShowConfirm(false);
      setConfirmId(null);
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h2 style={{ marginBottom: '00px' }}>Admin Dashboard</h2>

      <p style={{ marginBottom: '30px' }}>
        This dashboard can only be accessed by verified admins.
      </p>

      <div style={{ marginBottom: '20px' }}>
        <button onClick={fetchAllAccounts} style={{ marginRight: '10px' }}>Get All Accounts</button>
        <button onClick={fetchAllTransactions} style={{ marginRight: '10px' }}>Get All Transactions</button>
        <button onClick={fetchPendingTransactions} style={{ marginRight: '10px' }}>Get All Pending Transactions</button>
      </div>

      <div style={{ marginBottom: '20px' }}>
        <input
          type="text"
          value={accountId}
          onChange={(e) => setAccountId(e.target.value)}
          placeholder="Enter Account ID"
          style={{ marginRight: '10px' }}
        />
        <button onClick={fetchAccountById} style={{ marginRight: '10px' }}>Get Account By ID</button>
        <button onClick={fetchTransactionUser} style={{ marginRight: '10px' }}>Get Transactions By User</button>
        <button onClick={fetchPendingTransactionsById}>Get Pending Transactions By User</button>
      </div>

      {errorMsg && <div style={{ fontSize: '14px', color: 'red', marginBottom: '10px' }}>{errorMsg}</div>}

      {loading ? (
        <p style={{ fontSize: '14px' }}>Fetching data...</p>
      ) : accountData ? (
        <div style={{
          border: '1px solid #ccc',
          padding: '15px',
          borderRadius: '8px',
          boxShadow: '0 0 10px rgba(0,0,0,0.1)',
          maxWidth: '500px',
          backgroundColor: '#f9f9f9',
          marginBottom: '20px'
        }}>
          <h2>Account Details</h2>
          <p><strong>Account ID:</strong> {accountData.accountId}</p>
          <p><strong>Name:</strong> {accountData.accountHolderName}</p>
          <p><strong>Balance:</strong> ₹{accountData.balance}</p>
          <p><strong>Address:</strong> {accountData.accountHolderAddress}</p>
          <p><strong>Email:</strong> {accountData.accountHolderEmailAddress}</p>
          <button onClick={() => handleDeleteClick(accountData.accountId)} style={{ marginTop: '10px' }}>Delete Account</button>
        </div>
      ) : tableData.length > 0 ? (
        <div style={{ maxHeight: '500px', overflowY: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
            <thead style={{ backgroundColor: '#f0f0f0', textAlign: 'left' }}>
              <tr>
                {Object.keys(tableData[0]).map((key) => (
                  <th key={key} style={{ padding: '8px', border: '1px solid #ddd' }}>{key}</th>
                ))}
                {tableData[0].accountId && <th style={{ padding: '8px', border: '1px solid #ddd' }}>Action</th>}
              </tr>
            </thead>
            <tbody>
              {tableData.map((item, index) => (
                <tr key={index}>
                  {Object.entries(item).map(([key, val]) => (
                    <td key={key} style={{ padding: '6px 8px', border: '1px solid #ddd' }}>{val}</td>
                  ))}
                  {item.accountId && (
                    <td style={{ padding: '6px 8px', border: '1px solid #ddd' }}>
                      <button onClick={() => handleDeleteClick(item.accountId)}>Delete</button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p style={{ fontSize: '13px' }}>No data to display.</p>
      )}

      {showConfirm && (
        <div style={{
          marginTop: '20px',
          padding: '15px',
          border: '1px solid #aaa',
          backgroundColor: '#fff3f3',
          maxWidth: '400px',
          borderRadius: '6px'
        }}>
          <p style={{ marginBottom: '10px' }}>Are you sure you want to delete this account?</p>
          <button onClick={confirmDelete} style={{ marginRight: '10px' }}>Yes, Delete</button>
          <button onClick={() => setShowConfirm(false)}>Cancel</button>
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;
