import React, { useEffect, useState } from 'react';
import axios from 'axios';

const TransactionHistory = () => {
  const [transactions, setTransactions] = useState([]);
  const [filteredTransactions, setFilteredTransactions] = useState([]);
  const [userAccountId, setUserAccountId] = useState(null);
  const [filters, setFilters] = useState({
    transId: '',
    senderId: '',
    receiverId: '',
    status: {
      success: false,
      failed: false,
      pending: false,
    },
    month: '',
    sentOnly: false,
    receivedOnly: false,
  });

  const token = localStorage.getItem('token');

  // Fetch user's account ID
  useEffect(() => {
    const fetchAccountId = async () => {
      try {
        const res = await axios.get('http://localhost:8080/bank/accounts/1', {
          headers: { Authorization: `Bearer ${token}` },
        });
        setUserAccountId(res.data.accountId);
      } catch (error) {
        console.error('Failed to fetch account info:', error);
      }
    };

    fetchAccountId();
  }, [token]);

  // Fetch all transactions for the user (replace with dynamic ID if needed)
  useEffect(() => {
    const fetchTransactions = async () => {
      try {
        const res = await axios.get('http://localhost:8080/bank/transactions/1', {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        setTransactions(res.data);
        setFilteredTransactions(res.data);
      } catch (error) {
        console.error('Failed to fetch transactions:', error);
      }
    };

    fetchTransactions();
  }, [token]);

  const applyFilters = () => {
    const {
      transId,
      senderId,
      receiverId,
      status,
      month,
      sentOnly,
      receivedOnly,
    } = filters;

    const filtered = transactions.filter((txn) => {
      const matchTransId = transId ? txn.transId.toString().includes(transId) : true;
      const matchSenderId = senderId ? txn.senderId.toString().includes(senderId) : true;
      const matchReceiverId = receiverId ? txn.receiverId.toString().includes(receiverId) : true;
      const matchStatus = Object.values(status).some(Boolean)
        ? status[txn.status.toLowerCase()]
        : true;
      const matchMonth = month
        ? txn.timestamp.toLowerCase().includes(month.toLowerCase())
        : true;

      const matchSentOnly = sentOnly && userAccountId !== null
        ? txn.senderId === userAccountId
        : true;

      const matchReceivedOnly = receivedOnly && userAccountId !== null
        ? txn.receiverId === userAccountId
        : true;

      const sentOrReceivedLogic = sentOnly || receivedOnly
        ? matchSentOnly && matchReceivedOnly
        : true;

      return (
        matchTransId &&
        matchSenderId &&
        matchReceiverId &&
        matchStatus &&
        matchMonth &&
        sentOrReceivedLogic
      );
    });

    setFilteredTransactions(filtered);
  };

  const handleStatusChange = (statusKey) => {
    setFilters((prev) => ({
      ...prev,
      status: {
        ...prev.status,
        [statusKey]: !prev.status[statusKey],
      },
    }));
  };

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    if (type === 'checkbox') {
      setFilters((prev) => ({
        ...prev,
        [name]: checked,
      }));
    } else {
      setFilters((prev) => ({
        ...prev,
        [name]: value,
      }));
    }
  };

  const clearFilters = () => {
    setFilters({
      transId: '',
      senderId: '',
      receiverId: '',
      status: {
        success: false,
        failed: false,
        pending: false,
      },
      month: '',
      sentOnly: false,
      receivedOnly: false,
    });
    setFilteredTransactions(transactions);
  };

  return (
    <div style={{ padding: '20px' }}>
      <h2>Transaction History</h2>

      <div style={{ marginBottom: '20px', border: '1px solid #ccc', padding: '10px' }}>
        <h4>Filters</h4>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '10px' }}>
          <input
            type="text"
            placeholder="Transaction ID"
            name="transId"
            value={filters.transId}
            onChange={handleInputChange}
          />
          <input
            type="text"
            placeholder="Sender ID"
            name="senderId"
            value={filters.senderId}
            onChange={handleInputChange}
          />
          <input
            type="text"
            placeholder="Receiver ID"
            name="receiverId"
            value={filters.receiverId}
            onChange={handleInputChange}
          />
          <select name="month" value={filters.month} onChange={handleInputChange}>
            <option value="">All Months</option>
            <option value="January">January</option>
            <option value="February">February</option>
            <option value="March">March</option>
            <option value="April">April</option>
            <option value="May">May</option>
            <option value="June">June</option>
          </select>
          <label>
            <input
              type="checkbox"
              checked={filters.status.success}
              onChange={() => handleStatusChange('success')}
            />
            Success
          </label>
          <label>
            <input
              type="checkbox"
              checked={filters.status.failed}
              onChange={() => handleStatusChange('failed')}
            />
            Failed
          </label>
          <label>
            <input
              type="checkbox"
              checked={filters.status.pending}
              onChange={() => handleStatusChange('pending')}
            />
            Pending
          </label>
          <label>
            <input
              type="checkbox"
              name="sentOnly"
              checked={filters.sentOnly}
              onChange={handleInputChange}
            />
            Show Sent Only
          </label>
          <label>
            <input
              type="checkbox"
              name="receivedOnly"
              checked={filters.receivedOnly}
              onChange={handleInputChange}
            />
            Show Received Only
          </label>
          <button onClick={applyFilters}>Apply</button>
          <button onClick={clearFilters}>Clear</button>
        </div>
      </div>

      <div style={{ maxHeight: '400px', overflowY: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
          <thead style={{ backgroundColor: '#f0f0f0', textAlign: 'left' }}>
            <tr>
              <th style={{ padding: '8px', border: '1px solid #ddd' }}>Transaction ID</th>
              <th style={{ padding: '8px', border: '1px solid #ddd' }}>Sender ID</th>
              <th style={{ padding: '8px', border: '1px solid #ddd' }}>Receiver ID</th>
              <th style={{ padding: '8px', border: '1px solid #ddd' }}>Amount (₹)</th>
              <th style={{ padding: '8px', border: '1px solid #ddd' }}>Status</th>
              <th style={{ padding: '8px', border: '1px solid #ddd' }}>Timestamp</th>
            </tr>
          </thead>
          <tbody>
            {filteredTransactions.map((txn) => (
              <tr key={txn.transId}>
                <td style={{ padding: '6px 8px', border: '1px solid #ddd' }}>{txn.transId}</td>
                <td style={{ padding: '6px 8px', border: '1px solid #ddd' }}>{txn.senderId}</td>
                <td style={{ padding: '6px 8px', border: '1px solid #ddd' }}>{txn.receiverId}</td>
                <td style={{ padding: '6px 8px', border: '1px solid #ddd' }}>{txn.amount}</td>
                <td style={{ padding: '6px 8px', border: '1px solid #ddd' }}>{txn.status}</td>
                <td style={{ padding: '6px 8px', border: '1px solid #ddd' }}>{txn.timestamp}</td>
              </tr>
            ))}
            {filteredTransactions.length === 0 && (
              <tr>
                <td colSpan="6" style={{ padding: '10px', textAlign: 'center' }}>
                  No transactions found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default TransactionHistory;
