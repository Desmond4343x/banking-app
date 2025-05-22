import React, { useState, useEffect } from 'react';
import axios from 'axios';
const api = process.env.REACT_APP_BACKEND_URL;

const RequestMoney = () => {
  const [senderId, setSenderId] = useState('');
  const [amount, setAmount] = useState('');
  const [message, setMessage] = useState('');
  const [isError, setIsError] = useState(false);
  const [userAccountId, setUserAccountId] = useState(null);

  const token = localStorage.getItem('token');

  useEffect(() => {
    const fetchAccountId = async () => {
      try {
        const res = await axios.get(`${api}/bank/account`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        setUserAccountId(res.data.accountId);
      } catch (error) {
        console.error('Failed to fetch account info:', error);
      }
    };

    fetchAccountId();
  }, [token]);

  const handleRequest = async (e) => {
    e.preventDefault();
    setMessage('');

    if (!senderId.trim() || !amount.trim()) {
      setIsError(true);
      setMessage('Sender ID and amount are required.');
      return;
    }

    if (parseInt(senderId) === userAccountId) {
      setIsError(true);
      setMessage('You cannot request money from your own account.');
      return;
    }

    if (parseFloat(amount) < 0) {
      setIsError(true);
      setMessage('Amount cannot be negative.');
      return;
    }

    try {
      await axios.put(
        `${api}/bank/accounts/requestFrom`,
        {
          senderId: parseInt(senderId),
          amount: parseFloat(amount),
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      setIsError(false);
      setMessage('Money request sent successfully!');
      setSenderId('');
      setAmount('');
    } catch (error) {
      console.error('Error requesting money:', error);

      let errorMsg = 'Failed to request money. Please try again.';
      if (error.response && error.response.data) {
        errorMsg = error.response.data;
      }

      setIsError(true);
      setMessage(errorMsg);
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h2 style={{ marginBottom: '10px' }}>Request Money</h2>
      <form onSubmit={handleRequest}>
        <input
          type="text"
          placeholder="Sender ID"
          value={senderId}
          onChange={(e) => setSenderId(e.target.value)}
        /><br />
        <input
          type="number"
          placeholder="Amount"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
        /><br />
        <button type="submit">Request</button>
      </form>
      {message && (
        <p
          style={{
            marginTop: '10px',
            fontSize: '14px',
            color: isError ? 'red' : 'green',
          }}
        >
          {message}
        </p>
      )}
    </div>
  );
};

export default RequestMoney;
