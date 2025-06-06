import React, { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
const api = process.env.REACT_APP_BACKEND_URL;

const Login = ({ setIsLoggedIn }) => {
  const [idOrEmail, setIdOrEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [isError, setIsError] = useState(false);
  const [useEmailLogin, setUseEmailLogin] = useState(false);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const isValidEmail = (email) => /^\S+@\S+\.\S+$/.test(email);

  const handleLogin = async (e) => {
    e.preventDefault();
    setMessage("");
    setIsError(false);
    setLoading(true);

    if (idOrEmail.trim() === "" || password.trim() === "") {
      setIsError(true);
      setMessage("Both fields are required.");
      setLoading(false);
      return;
    }

    if (useEmailLogin && !isValidEmail(idOrEmail.trim())) {
      setIsError(true);
      setMessage("Please enter a valid email address.");
      setLoading(false);
      return;
    }

    try {
      const payload = useEmailLogin
        ? { email: idOrEmail.toLowerCase().trim(), password }
        : { id: idOrEmail.trim(), password };

      const response = await axios.post(`${api}/bank/login`, payload);
      const token = response.data.token;
      localStorage.setItem("token", token);

      setIsLoggedIn(true);
      setIsError(false);
      setMessage("Login successful!");
      navigate("/userinfo");
    } catch (error) {
      console.error("Login failed:", error);
      setIsError(true);
      const detailMessage =
        error?.response?.data?.detail || "Invalid credentials. Please try again.";
      setMessage(detailMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleForgotPassword = async () => {
    setMessage("");
    setIsError(false);
    setLoading(true);

    if (idOrEmail.trim() === "") {
      setIsError(true);
      setMessage("Please enter your Account ID or Email first.");
      setLoading(false);
      return;
    }

    if (useEmailLogin && !isValidEmail(idOrEmail.trim())) {
      setIsError(true);
      setMessage("Please enter a valid email address.");
      setLoading(false);
      return;
    }

    const payload = useEmailLogin
      ? { email: idOrEmail.toLowerCase().trim() }
      : { id: idOrEmail.trim() };

    try {
      await axios.post(`${api}/bank/forgot-password`, payload);
      setIsError(false);
      setMessage("Temporary password sent to your registered email.");
    } catch (error) {
      console.error("Forgot password failed:", error);
      setIsError(true);
      const detailMessage =
        error?.response?.data?.detail || "Failed to send temporary password.";
      setMessage(detailMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: "20px" }}>
      <h2 style={{ marginBottom: "0" }}>Login</h2>
      <p style={{ marginTop: "0", fontSize: "16px", color: "#555" }}>
        Please enter your {useEmailLogin ? "Email Address" : "Account ID"} and password.
      </p>
      <form onSubmit={handleLogin}>
        <input
          type={useEmailLogin ? "email" : "text"}
          placeholder={useEmailLogin ? "Email Address" : "Account ID"}
          value={idOrEmail}
          onChange={(e) => {
            const value = e.target.value;
            if (useEmailLogin) {
              setIdOrEmail(value);
            } else {
              setIdOrEmail(value.replace(/\D/g, ""));
            }
          }}
        />
        <br />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <br />
        <button type="submit" disabled={loading}>
          {loading ? "Loading..." : "Login"}
        </button>
        <button
          type="button"
          style={{
            marginTop: "10px",
            marginLeft: "50px",
            color: "#007bff",
            background: "none",
            border: "none",
            cursor: "pointer",
          }}
          onClick={handleForgotPassword}
          disabled={loading}
        >
          Forgot Password?
        </button>
        <br />
        <button
          type="button"
          style={{ marginTop: "40px", cursor: "pointer" }}
          onClick={() => {
            setUseEmailLogin((prev) => !prev);
            setIdOrEmail("");
            setMessage("");
          }}
          disabled={loading}
        >
          {useEmailLogin ? "Use Account ID instead" : "Use Email instead"}
        </button>
        {message && (
          <p
            style={{
              color: isError ? "red" : "green",
              fontSize:"14px",
              marginTop: "10px",
            }}
          >
            {message}
          </p>
        )}
      </form>
    </div>
  );
};

export default Login;
