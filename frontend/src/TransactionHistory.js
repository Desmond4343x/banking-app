import React, { useEffect, useState } from 'react';
import axios from 'axios';
import jsPDF from 'jspdf';
import 'jspdf-autotable';
const api = process.env.REACT_APP_BACKEND_URL;

const lightenColor = (color, percent) => {
  let num = parseInt(color.replace('#', ''), 16),
    amt = Math.round(2.55 * percent * 100),
    R = (num >> 16) + amt,
    G = ((num >> 8) & 0x00ff) + amt,
    B = (num & 0x0000ff) + amt;
  return (
    '#' +
    (
      0x1000000 +
      (R < 255 ? (R < 1 ? 0 : R) : 255) * 0x10000 +
      (G < 255 ? (G < 1 ? 0 : G) : 255) * 0x100 +
      (B < 255 ? (B < 1 ? 0 : B) : 255)
    )
      .toString(16)
      .slice(1)
  );
};

const TransactionHistory = () => {
  const [transactions, setTransactions] = useState([]);
  const [filteredTransactions, setFilteredTransactions] = useState([]);
  const [userAccountId, setUserAccountId] = useState(null);
  const [userName, setUserName] = useState('');
  const [filters, setFilters] = useState({
    transId: '',
    senderId: '',
    receiverId: '',
    status: {
      success: false,
      declined: false,
      pending: false,
      deposit: false,
      withdraw: false,
    },
    month: '',
    sentOnly: false,
    receivedOnly: false,
  });
  const [filtersOpen, setFiltersOpen] = useState(true);
  const [loading, setLoading] = useState(true);

  const token = localStorage.getItem('token');

  useEffect(() => {
    const fetchAccountId = async () => {
      try {
        const res = await axios.get(`${api}/bank/account`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        setUserAccountId(res.data.accountId);
        setUserName(res.data.accountHolderName);
      } catch (error) {
        console.error('Failed to fetch account info:', error);
      }
    };

    fetchAccountId();
  }, [token]);

  const downloadPDF = () => {
    const {
    transId,
    senderId,
    receiverId,
    status,
    month: selectedMonth,
    sentOnly,
    receivedOnly,
  } = filters;

    const doc = new jsPDF();

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(20);
    doc.setTextColor(40, 40, 40);
    doc.text('Transaction History', 14, 20);

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(12);
    doc.text('Account ID:', 14,  30 );
    doc.setFont('helvetica', 'normal');
    doc.text(`${userAccountId}`, 39, 30); 

    doc.setFont('helvetica', 'bold');
    doc.text('Name:', 14, 36);
    doc.setFont('helvetica', 'normal');
    doc.text(`${userName}`, 28, 36); 

    //formatting date
    const dateObj = new Date();
    const day = dateObj.getDate();
    const mnth = dateObj.toLocaleString('default', { month: 'long' });
    const year = dateObj.getFullYear();
    const formattedDate = `${day} ${mnth}, ${year}`;

    doc.setFont('helvetica', 'bold');
    doc.text(`Date:`, 14, 42);
    doc.setFont('helvetica', 'normal');
    doc.text(`${formattedDate}`, 25, 42);


    const tableColumn = ["Transaction ID", "Sender ID", "Receiver ID", "Amount", "Status", "Timestamp"];
    const tableRows = filteredTransactions.map(txn => [
      txn.transId,
      txn.senderId,
      txn.receiverId,
      txn.amount,
      txn.status,
      txn.timestamp,
    ]);

    const getRowColor = (txn) => {
      const status = txn.status.toLowerCase();

      if (status === 'declined') return [244, 204, 204];        // pink
      if (status === 'pending') return [255, 249, 196];         // yellow
      if (status === 'deposit') return [200, 230, 201];         // green
      if (status === 'withdraw') return [255, 209, 179];        // red tint
      if (txn.senderId === userAccountId) return [255, 209, 179];  // red tint
      if (txn.receiverId === userAccountId) return [200, 230, 201]; // green tint

      return [255, 255, 255]; // default white
    };

    const activeFilters = [];

    if (transId) activeFilters.push(`Transaction ID = ${transId}`);
    if (senderId) activeFilters.push(`Sender ID = ${senderId}`);
    if (receiverId) activeFilters.push(`Receiver ID = ${receiverId}`);

    if (status) {
      const selectedStatuses = Object.entries(status)
        .filter(([key, value]) => value)
        .map(([key]) => key.charAt(0).toUpperCase() + key.slice(1));
      if (selectedStatuses.length > 0) {
        activeFilters.push(`Status = ${selectedStatuses.join(', ')}`);
      }
    }

    if (selectedMonth) activeFilters.push(`Month = ${selectedMonth}`);
    if (sentOnly) activeFilters.push(`Sent Only`);
    if (receivedOnly) activeFilters.push(`Received Only`);

    const filtersLine = activeFilters.length > 0
      ? `Filters Applied: ${activeFilters.join(' | ')}`
      : 'Filters Applied: None';

    // Draw on PDF
    doc.setFontSize(10);
    doc.setFont('helvetica', 'italic');
    doc.setTextColor(80, 80, 80);
    doc.text(doc.splitTextToSize(filtersLine, 180), 14, 52);

    doc.autoTable({
      startY: 58,
      head: [tableColumn],
      body: tableRows,
      styles: {
        fontSize: 10,
      },
      didParseCell: function (data) {
        if (data.section === 'body') {
          const txn = filteredTransactions[data.row.index];
          const bgColor = getRowColor(txn);
          data.cell.styles.fillColor = bgColor;
        }
      }
    });

    doc.save('transaction_history.pdf');
  };

  useEffect(() => {
    if (!userAccountId) return;

    const fetchTransactions = async () => {
      setLoading(true);
      try {
        const res = await axios.get(`${api}/bank/transactions/${userAccountId}`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        setTransactions(res.data);
        setFilteredTransactions(res.data);
      } catch (error) {
        console.error('Failed to fetch transactions:', error);
      } finally {
        setLoading(false); // End loading
      }
    };

    fetchTransactions();
  }, [token, userAccountId]);

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
      const matchTransId = transId ? txn.transId.toString().trim() === transId.trim() : true;
      const matchSenderId = senderId ? txn.senderId.toString().trim() === senderId.trim() : true;
      const matchReceiverId = receiverId ? txn.receiverId.toString().trim() === receiverId.trim() : true;
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
        declined: false,
        pending: false,
        deposit: false,
        withdraw: false,
      },
      month: '',
      sentOnly: false,
      receivedOnly: false,
    });
    setFilteredTransactions(transactions);
  };

  const getRowStyle = (txn) => {
    if (txn.status.toLowerCase() === 'declined') {
      return { backgroundColor: '#f4cccc' }; //pink
    }
    if (txn.status.toLowerCase() === 'pending') {
      return { backgroundColor: '#fff9c4' }; // yellow
    }
    if (txn.status.toLowerCase() === 'deposit') {
      return { backgroundColor: '#c8e6c9' }; // green
    }
    if (txn.status.toLowerCase() === 'withdraw') {
      return { backgroundColor: '#ffd1b3' }; // red tint
    }
    if (txn.senderId === userAccountId) {
      return { backgroundColor: '#ffd1b3' }; // red tint
    }
    if (txn.receiverId === userAccountId) {
      return { backgroundColor: '#c8e6c9' }; // green tint
    }
    return {};
  };

  return (
    <div style={{ padding: '20px' }}>
      <h2>Transaction History</h2>

      {loading ? (
        <div className="text-center my-4 text-gray-600">Loading transactions...</div>
      ) : (
        <>
          <div
            style={{
              marginBottom: '20px',
              border: '1px solid #ccc',
              borderRadius: '8px',
              overflow: 'hidden',
            }}
          >
            <div
              onClick={() => setFiltersOpen((open) => !open)}
              style={{
                padding: '10px',
                backgroundColor: 'white',
                cursor: 'pointer',
                userSelect: 'none',
                fontWeight: 'bold',
                fontSize: '15px',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
              }}
              aria-expanded={filtersOpen}
              aria-controls="filters-section"
            >
              Filters
              <span
                style={{
                  transform: filtersOpen ? 'rotate(90deg)' : 'rotate(0deg)',
                  transition: 'transform 0.3s',
                }}
              >
                ▶
              </span>
            </div>

            {filtersOpen && (
              <div
                id="filters-section"
                style={{
                  padding: '10px',
                  display: 'flex',
                  flexWrap: 'wrap',
                  gap: '20px',
                  justifyContent: 'space-between',
                }}
              >
                {/* Text Filters */}
                <div
                  style={{ display: 'flex', flexDirection: 'column', gap: '8px', minWidth: '200px' }}
                >
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
                </div>

                {/* Status Filters */}
                <div
                  style={{ display: 'flex', flexDirection: 'column', gap: '5px', minWidth: '150px' }}
                >
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
                      checked={filters.status.declined}
                      onChange={() => handleStatusChange('declined')}
                    />
                    Declined
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
                      checked={filters.status.deposit}
                      onChange={() => handleStatusChange('deposit')}
                    />
                    Deposit
                  </label>
                  <label>
                    <input
                      type="checkbox"
                      checked={filters.status.withdraw}
                      onChange={() => handleStatusChange('withdraw')}
                    />
                    Withdraw
                  </label>
                </div>

                {/* Month Selector */}
                <div style={{ display: 'flex', flexDirection: 'column', minWidth: '160px' }}>
                  <label htmlFor="month">Month</label>
                  <select name="month" value={filters.month} onChange={handleInputChange}>
                    <option value="">All Months</option>
                    <option value="January">January</option>
                    <option value="February">February</option>
                    <option value="March">March</option>
                    <option value="April">April</option>
                    <option value="May">May</option>
                    <option value="June">June</option>
                    <option value="July">July</option>
                    <option value="August">August</option>
                    <option value="September">September</option>
                    <option value="October">October</option>
                    <option value="November">November</option>
                    <option value="December">December</option>
                  </select>
                </div>

                {/* Sent/Received Filters */}
                <div
                  style={{ display: 'flex', flexDirection: 'column', gap: '5px', minWidth: '180px' }}
                >
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
                </div>

                {/* Action Buttons */}
                <div style={{ marginTop: '15px', display: 'flex', gap: '10px', width: '100%' }}>
                  <button onClick={applyFilters}>Apply</button>
                  <button onClick={clearFilters}>Clear</button>
                </div>
              </div>
            )}
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-start', marginBottom: '10px' }}>
            <button onClick={downloadPDF}>Download as PDF</button>
          </div>

          <div style={{ maxHeight: '500px', overflowY: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
              <thead style={{ backgroundColor: '#f0f0f0', textAlign: 'left' }}>
                <tr>
                  <th style={{ padding: '8px', border: '1px solid #ddd', fontWeight: 'bold' }}>
                    Transaction ID
                  </th>
                  <th style={{ padding: '8px', border: '1px solid #ddd', fontWeight: 'bold' }}>
                    Sender ID
                  </th>
                  <th style={{ padding: '8px', border: '1px solid #ddd', fontWeight: 'bold' }}>
                    Receiver ID
                  </th>
                  <th style={{ padding: '8px', border: '1px solid #ddd', fontWeight: 'bold' }}>Amount</th>
                  <th style={{ padding: '8px', border: '1px solid #ddd', fontWeight: 'bold' }}>Status</th>
                  <th style={{ padding: '8px', border: '1px solid #ddd', fontWeight: 'bold' }}>
                    Timestamp
                  </th>
                </tr>
              </thead>
              <tbody>
                {filteredTransactions.length > 0 ? (
                  filteredTransactions.map((txn) => (
                    <tr
                      key={txn.transId}
                      style={{
                        ...getRowStyle(txn),
                        transition: 'background-color 0.2s ease',
                        cursor: 'pointer',
                      }}
                      onMouseEnter={(e) => {
                        const original = getRowStyle(txn).backgroundColor || '#fff';
                        e.currentTarget.style.backgroundColor = lightenColor(original, 0.1);
                      }}
                      onMouseLeave={(e) => {
                        const style = getRowStyle(txn);
                        e.currentTarget.style.backgroundColor = style.backgroundColor || '';
                      }}
                    >
                      <td style={{ padding: '8px', border: '1px solid #ddd' }}>{txn.transId}</td>
                      <td style={{ padding: '8px', border: '1px solid #ddd' }}>{txn.senderId}</td>
                      <td style={{ padding: '8px', border: '1px solid #ddd' }}>{txn.receiverId}</td>
                      <td style={{ padding: '8px', border: '1px solid #ddd' }}>{txn.amount}</td>
                      <td style={{ padding: '8px', border: '1px solid #ddd' }}>{txn.status}</td>
                      <td style={{ padding: '8px', border: '1px solid #ddd' }}>{txn.timestamp}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="6" style={{ textAlign: 'center', padding: '20px' }}>
                      No transactions found.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );

};

export default TransactionHistory;
