import React, { useState, useEffect } from 'react';
import axios from 'axios';
const api = process.env.REACT_APP_BACKEND_URL;

const SendMoney = () => {
  const [receiverId, setReceiverId] = useState('');
  const [amount, setAmount] = useState('');
  const [message, setMessage] = useState('');
  const [isError, setIsError] = useState(false);
  const [balance, setBalance] = useState(null);
  const [userAccountId, setUserAccountId] = useState(null);

  const token = localStorage.getItem('token');

  useEffect(() => {
    const fetchAccountData = async () => {
      try {
        const res = await axios.get(`${api}/bank/account`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        setBalance(res.data.balance);
        setUserAccountId(res.data.accountId);
      } catch (err) {
        console.error('Failed to fetch account data:', err);
      }
    };

    fetchAccountData();
  }, [token]);

  const handleSend = async (e) => {
    e.preventDefault();
    setMessage('');

    if (!receiverId.trim() || !amount.trim()) {
      setIsError(true);
      setMessage('Receiver ID and amount are required.');
      return;
    }

    if (parseInt(receiverId) === userAccountId) {
      setIsError(true);
      setMessage('You cannot send money to your own account.');
      return;
    }

    const numericAmount = parseFloat(amount);

    if (isNaN(numericAmount) || numericAmount <= 0) {
      setIsError(true);
      setMessage('Amount must be a positive number.');
      return;
    }

    if (balance !== null && numericAmount > balance) {
      setIsError(true);
      setMessage('Insufficient balance.');
      return;
    }

    try {
      const response = await axios.put(
        `${api}/bank/accounts/sendTo`,
        {
          receiverId: parseInt(receiverId),
          amount: numericAmount,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      setIsError(false);
      setMessage('Money sent successfully!');
      setReceiverId('');
      setAmount('');
      setBalance(response.data.balance); 
    } catch (error) {
      console.error('Error sending money:', error);

      let errorMsg = 'Failed to send money. Please try again.';
      if (error.response && error.response.data) {
        errorMsg = error.response.data;
      }

      setIsError(true);
      setMessage(errorMsg);
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h2 style={{ marginBottom: '10px' }}>Send Money</h2>
      {balance !== null && (
        <p style={{ marginTop: '0', marginBottom: '0px' }}>
          <strong>Current Balance:</strong> ₹{balance}/-
        </p>
      )}
      <form onSubmit={handleSend}>
        <input
          type="text"
          placeholder="Receiver ID"
          value={receiverId}
          onChange={(e) => setReceiverId(e.target.value)}
        /><br />
        <input
          type="number"
          placeholder="Amount"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
        /><br />
        <button type="submit">Send</button>
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

export default SendMoney;
