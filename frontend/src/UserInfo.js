import React, { useEffect, useState } from "react";
import axios from "axios";

const UserInfo = () => {
  const [user, setUser] = useState(null);
  const [userAccountId, setUserAccountId] = useState(null);
  const [activeForm, setActiveForm] = useState(null);
  const [formData, setFormData] = useState({});
  const [message, setMessage] = useState("");
  const [isError, setIsError] = useState(false);
  const token = localStorage.getItem("token");

  const isValidEmail = (email) => {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  };

  useEffect(() => {
    if (!token) {
      console.error("No token found");
      return;
    }

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

    axios
      .get(`http://localhost:8080/bank/accounts/${userAccountId}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
      .then((res) => {
        setUser(res.data);
      })
      .catch((err) => {
        console.error("Error fetching user info:", err);
      });
  }, [token, userAccountId]);

  const handleSubmit = async () => {
    let url = "";
    let payload = {};

    switch (activeForm) {
      case "address":
        url = "http://localhost:8080/bank/change-address";
        payload = { address: formData.address };
        break;
      case "password":
        url = "http://localhost:8080/bank/change-password";
        payload = {
          currentPassword: formData.currentPassword,
          newPassword: formData.newPassword,
        };
        break;
      case "email":
        if (!formData.email || !isValidEmail(formData.email)) {
          setIsError(true);
          setMessage("Please enter a valid email address.");
          return;
        }
        url = "http://localhost:8080/bank/change-email";
        payload = { email: formData.email };
        break;
      default:
        return;
    }

    try {
      const response = await axios.post(url, payload, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      setIsError(false);
      if (response.data && response.data.message) {
        setMessage(response.data.message);
      } else {
        setMessage("Update successful!");
      }

      if (activeForm === "email") {
        localStorage.removeItem("token");

        setTimeout(() => {
          alert("Your email has been updated. Please log in again.");
          window.location.href = "/login"; 
        }, 1000);
      }
    } catch (error) {
      console.error("Update failed:", error);
      setIsError(true);
      if (error.response && error.response.data && error.response.data.detail) {
        setMessage(error.response.data.detail);
      } else {
        setMessage("Something went wrong.");
      }
    }
  };

  return (
    <div style={{ padding: "20px", display: "flex", gap: "40px" }}>
      <div style={{ flex: 1 }}>
        {user ? (
          <>
            <h2 style={{ marginBottom: "20px" }}>
              Welcome {user.accountHolderName}!
            </h2>
            <p>
              <strong>Account Id:</strong> {user.accountId}
            </p>
            <p>
              <strong>Balance:</strong> ₹{user.balance}/-
            </p>
            <p>
              <strong>Residence:</strong> {user.accountHolderAddress}
            </p>
            <p>
              <strong>Email Address:</strong> {user.accountHolderEmailAddress}
            </p>

            <div
              style={{
                marginTop: "60px",
                display: "flex",
                flexDirection: "column",
                gap: "5px",
              }}
            >
              <button
                onClick={() => {
                  setActiveForm("address");
                  setMessage("");
                }}
              >
                Update Residence
              </button>
              <button
                onClick={() => {
                  setActiveForm("password");
                  setMessage("");
                }}
              >
                Change Password
              </button>
              <button
                onClick={() => {
                  setActiveForm("email");
                  setMessage("");
                }}
              >
                Update Email Address
              </button>
            </div>
          </>
        ) : (
          <p>Loading...</p>
        )}
      </div>

      {/* Form Section */}
      <div style={{ flex: 1 }}>
        {activeForm === "address" && (
          <>
            <h3>Update Address</h3>
            <input
              type="text"
              placeholder="New address"
              value={formData.address || ""}
              onChange={(e) =>
                setFormData({ ...formData, address: e.target.value })
              }
            />
            <br />
            <button onClick={handleSubmit}>Submit</button>
          </>
        )}

        {activeForm === "password" && (
          <>
            <h3>Change Password</h3>
            <input
              type="password"
              placeholder="Current Password"
              value={formData.currentPassword || ""}
              onChange={(e) =>
                setFormData({ ...formData, currentPassword: e.target.value })
              }
            />
            <br />
            <input
              type="password"
              placeholder="New Password"
              value={formData.newPassword || ""}
              onChange={(e) =>
                setFormData({ ...formData, newPassword: e.target.value })
              }
            />
            <br />
            <button onClick={handleSubmit}>Submit</button>
          </>
        )}

        {activeForm === "email" && (
          <>
            <h3>Update Email</h3>
            <input
              type="email"
              placeholder="New Email Address"
              value={formData.email || ""}
              onChange={(e) =>
                setFormData({ ...formData, email: e.target.value })
              }
            />
            <br />
            <button onClick={handleSubmit}>Submit</button>
          </>
        )}

        {message && (
          <p
            style={{
              color: isError ? "red" : "green",
              marginTop: "10px",
              fontSize: "14px",
            }}
          >
            {message}
          </p>
        )}
      </div>
    </div>
  );
};

export default UserInfo;
