import React, { useEffect, useState } from "react";
import axios from "axios";

const UserInfo = () => {
  const [user, setUser] = useState(null);
  const [userAccountId, setUserAccountId] = useState(null);
  const token = localStorage.getItem("token");

  useEffect(() => {
    if (!token) {
      console.error("No token found");
      return;
    }

    // First, fetch account info to get accountId
    const fetchAccountId = async () => {
      try {
        const res = await axios.get("http://localhost:8080/bank/account", {
          headers: { Authorization: `Bearer ${token}` },
        });
        setUserAccountId(res.data.accountId);
      } catch (error) {
        console.error("Failed to fetch account info:", error);
      }
    };

    fetchAccountId();
  }, [token]);

  useEffect(() => {
    if (!token || !userAccountId) return;

    // Now fetch full account details using accountId
    axios
      .get(`http://localhost:8080/bank/accounts/${userAccountId}`, {
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
  }, [token, userAccountId]);

  return (
    <div style={{ padding: "20px" }}>
      {user ? (
        <>
          <h2 style={{ marginBottom: "20px" }}>
            Welcome {user.accountHolderName}!
          </h2>
          <div>
            <p style={{ marginTop: "0", marginBottom: "0" }}>
              <strong>Account Id:</strong> {user.accountId}
            </p>
            <p style={{ marginTop: "0", marginBottom: "0" }}>
              <strong>Balance:</strong> ₹{user.balance}/-
            </p>
            <p style={{ marginTop: "0", marginBottom: "0" }}>
              <strong>Residence:</strong> {user.accountHolderAddress}
            </p>
            <p style={{ marginTop: "0", marginBottom: "0" }}>
              <strong>Email Address:</strong> {user.accountHolderEmailAddress}
            </p>
          </div>
        </>
      ) : (
        <p>Loading...</p>
      )}
    </div>
  );
};

export default UserInfo;
