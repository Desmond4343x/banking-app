import React, { useEffect, useState } from "react";
import axios from "axios";

const UserInfo = () => {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem("token");

    if (!token) {
      console.error("No token found");
      return;
    }
    const accountId = 1;

    axios
      .get(`http://localhost:8080/bank/accounts/${accountId}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
      .then((res) => {
        console.log("User Data:", res.data);
        setUser(res.data);
      })
      .catch((err) => {
        console.error("Error fetching user info:", err);
      });
  }, []);

  return (
    <div style={{ padding: '20px' }}>
      {user ? (
        <>
          <h2 style={{ marginBottom: '20px' }}>Welcome {user.accountHolderName}!</h2>
          <div>
            <p style={{ marginTop: '0', marginBottom: '0' }}><strong>Account Id:</strong> {user.accountId}</p>
            <p style={{ marginTop: '0', marginBottom: '0' }}><strong>Balance:</strong> ₹{user.balance}/-</p>
            <p style={{ marginTop: '0', marginBottom: '0' }}><strong>Residence:</strong> {user.accountHolderAddress}</p>
            <p style={{ marginTop: '0', marginBottom: '0' }}><strong>Email Address:</strong> {user.accountHolderEmailAddress}</p>
          </div>
        </>
      ) : (
        <p>Loading...</p>
      )}
    </div>
  );
  
  
};

export default UserInfo;
