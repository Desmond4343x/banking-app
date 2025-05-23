import React, { useState } from 'react';
import axios from 'axios';
const api = process.env.REACT_APP_BACKEND_URL;

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


    const isAnyEmpty = Object.values(formData).some(value => value.trim() === '');
    if (isAnyEmpty) {
      setIsError(true);
      setMessage('All fields are required.');
      return;
    }

    try {
      const res = await axios.post(`${api}/bank`, formData);
      const createdId = res.data.accountId;
      setIsError(false);
      setMessage(`Verification Mail has been sent to your Email Address. Your Account ID is ${createdId}.`);
    } catch (err) {
      console.error(err);
      setIsError(true);
      if (err.response && err.response.data) {
        setMessage(err.response.data);
      } else {
        setMessage('Error creating account. Please check your input or try again later.');
      }
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
