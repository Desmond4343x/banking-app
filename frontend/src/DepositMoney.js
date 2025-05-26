import React, { useState, useEffect } from 'react';
import axios from 'axios';
const api = process.env.REACT_APP_BACKEND_URL;

const DepositMoney = () => {
  const [amount, setAmount] = useState('');
  const [message, setMessage] = useState('');
  const [isError, setIsError] = useState(false);
  const [balance, setBalance] = useState(null);

  const token = localStorage.getItem('token');

  useEffect(() => {
    const fetchBalance = async () => {
      try {
        const res = await axios.get(`${api}/bank/account`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        setBalance(res.data.balance);
      } catch (err) {
        console.error('Failed to fetch balance:', err);
      }
    };

    fetchBalance();
  }, [token]);

  const handleDeposit = async (e) => {
    e.preventDefault();
    setMessage('');

    const numericAmount = parseFloat(amount);

    if (!amount.trim()) {
      setIsError(true);
      setMessage('Amount is required.');
      return;
    }

    if (isNaN(numericAmount) || numericAmount <= 0) {
      setIsError(true);
      setMessage('Amount must be a positive number.');
      return;
    }

    if (balance === null) {
      setIsError(true);
      setMessage('Account info not loaded yet. Please try again in a few seconds.');
      return;
    }

    try {
      const response = await axios.put(
        `${api}/bank/accounts/deposit`,
        {
          amount: numericAmount,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      setIsError(false);
      setMessage('Deposit successful!');
      setAmount('');
      setBalance(response.data.balance); 
    } catch (error) {
      console.error('Error depositing money:', error);

      let errorMsg = 'Failed to deposit money. Please try again.';
      if (error.response && error.response.data) {
        errorMsg = error.response.data;
      }

      setIsError(true);
      setMessage(errorMsg);
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h2 style={{ marginBottom: '10px' }}>Deposit Money</h2>
      {balance !== null && (
        <p style={{ marginTop: '0', marginBottom: '0px' }}>
          <strong>Current Balance:</strong> ₹{balance}/-
        </p>
      )}
      <form onSubmit={handleDeposit}>
        <input
          type="number"
          placeholder="Amount"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
        /><br />
        <button type="submit">Deposit</button>
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

export default DepositMoney;
