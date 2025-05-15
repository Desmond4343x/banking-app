import React, { useState } from 'react';
import axios from 'axios';

const CreateAccount = () => {
  const [formData, setFormData] = useState({
    accountHolderName: '',
    balance: '',
    password: '',
    accountHolderAddress: '',
    accountHolderEmailAddress: '',
    accountHolderRole: 'user',
  });

  const [message, setMessage] = useState('');
  const [isError, setIsError] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');

    // Validate all fields are filled
    const isAnyEmpty = Object.values(formData).some(value => value.trim() === '');
    if (isAnyEmpty) {
      setIsError(true);
      setMessage('All fields are required.');
      return;
    }

    try {
      const res = await axios.post('http://localhost:8080/bank', formData);
      const createdId = res.data.accountId;
      setIsError(false);
      setMessage(`Account created successfully! Your Account ID is ${createdId}.`);
    } catch (err) {
      console.error(err);
      setIsError(true);
      setMessage('Error creating account. Please check your input or try again later.');
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h2 style={{ marginBottom: '0' }}>Create Account</h2>
      <p style={{ marginTop: '0', fontSize: '16px', color: '#555' }}>
        Please enter your details carefully.
      </p>
      <form onSubmit={handleSubmit}>
        <input
          name="accountHolderName"
          placeholder="Name"
          onChange={handleChange}
        /><br />
        <input
          name="accountHolderEmailAddress"
          placeholder="Email"
          onChange={handleChange}
        /><br />
        <input
          name="password"
          type="password"
          placeholder="Password"
          onChange={handleChange}
        /><br />
        <input
          name="accountHolderAddress"
          placeholder="Address"
          onChange={handleChange}
        /><br />
        <input
          name="balance"
          type="number"
          placeholder="Balance"
          onChange={handleChange}
        /><br />
        <button type="submit">Create Account</button>
        {message && (
          <p style={{
            color: isError ? 'red' : 'green',
            marginTop: '10px',
            fontSize: '14px'
          }}>
            {message}
          </p>
        )}
      </form>
    </div>
  );
};

export default CreateAccount;
