import React from 'react';

const Help = () => {
  return (
    <div style={{ padding: '20px' }}>
      <h2 style={{ marginBottom: '10px' }}>Help & Support</h2>

      <p style={{ marginBottom: '10px' }}>
        Welcome to the Help Center. Here's how to use the Secure Digital Banking System.
      </p>

      <h3 style={{ marginBottom: '5px' }}>1. Getting Started</h3>
      <ul style={{ marginTop: 0, marginBottom: '10px', paddingLeft: '20px' }}>
        <li>Create an account with your email and password.</li>
        <li>Login using your credentials to access services.</li>
        <li>Your session will be authenticated via JWT.</li>
      </ul>

      <h3 style={{ marginBottom: '5px' }}>2. Account Management</h3>
      <ul style={{ marginTop: 0, marginBottom: '10px', paddingLeft: '20px' }}>
        <li>View your current account balance.</li>
        <li>Deposit or withdraw funds securely.</li>
      </ul>

      <h3 style={{ marginBottom: '5px' }}>3. Transactions</h3>
      <ul style={{ marginTop: 0, marginBottom: '10px', paddingLeft: '20px' }}>
        <li>Use "Send Money" to transfer funds to another user.</li>
        <li>Use "Request Money" to ask another user for a payment.</li>
        <li>Track past and pending transactions from the transaction page.</li>
      </ul>

      <h3 style={{ marginBottom: '5px' }}>4. Security</h3>
      <ul style={{ marginTop: 0, marginBottom: '10px', paddingLeft: '20px' }}>
        <li>All sensitive data is encrypted using RSA and AES.</li>
        <li>Passwords are stored securely with bcrypt hashing.</li>
        <li>Private keys are stored in a secure vault, not exposed to clients.</li>
      </ul>

      <h3 style={{ marginBottom: '5px' }}>5. Troubleshooting</h3>
      <ul style={{ marginTop: 0, marginBottom: '10px', paddingLeft: '20px' }}>
        <li>If a request fails, check your internet and token validity.</li>
        <li>For balance issues, verify you have sufficient funds.</li>
        <li>If pages don’t load, ensure backend server is running.</li>
      </ul>

      <h3 style={{ marginBottom: '5px' }}>6. Contact</h3>
      <p style={{ marginTop: 0 }}>
        For additional support, contact us at <span style={{ color: 'blue' }}>support@silverstone.com</span>
      </p>
    </div>
  );
};

export default Help;
