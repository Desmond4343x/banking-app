import React, { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

const Login = ({ setIsLoggedIn }) => {
  const [idOrEmail, setIdOrEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [isError, setIsError] = useState(false);
  const [useEmailLogin, setUseEmailLogin] = useState(false);
  const navigate = useNavigate();

  const isValidEmail = (email) => {
    return /^\S+@\S+\.\S+$/.test(email);
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setMessage("");

    if (idOrEmail.trim() === "" || password.trim() === "") {
      setIsError(true);
      setMessage("Both fields are required.");
      return;
    }

    if (useEmailLogin && !isValidEmail(idOrEmail.trim())) {
      setIsError(true);
      setMessage("Please enter a valid email address.");
      return;
    }

    try {
      const payload = useEmailLogin
        ? { email: idOrEmail.toLowerCase().trim(), password }
        : { id: idOrEmail.trim(), password };

      const response = await axios.post("http://localhost:8080/bank/login", payload);
      const token = response.data.token;
      localStorage.setItem("token", token);

      setIsLoggedIn(true);
      setIsError(false);
      setMessage("Login successful!");
      navigate("/userinfo");
    } catch (error) {
      console.error("Login failed:", error);

      setIsError(true);

      if (error.response && error.response.data && error.response.data.detail) {
        setMessage(error.response.data.detail);
      } else {
        setMessage("Invalid credentials. Please try again.");
      }
    }
  };

  return (
  <div style={{ padding: "20px" }}>
    <h2 style={{ marginBottom: "0" }}>Login</h2>
    <p style={{ marginTop: "0", fontSize: "16px", color: "#555" }}>
      Please enter your {useEmailLogin ? "Email" : "Account ID"} and password.
    </p>
    <form onSubmit={handleLogin}>
      <input
        type={useEmailLogin ? "email" : "text"}
        placeholder={useEmailLogin ? "Email" : "Account ID"}
        value={idOrEmail}
        onChange={(e) => {
          if (useEmailLogin) {
            setIdOrEmail(e.target.value);
          } else {
            // allow digits only for Account ID
            const onlyNums = e.target.value.replace(/\D/g, "");
            setIdOrEmail(onlyNums);
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
      <button type="submit">Login</button>
      <br />
      <button
        type="button"
        style={{
          marginTop: "40px",
          cursor: "pointer",
        }}
        onClick={() => {
          setUseEmailLogin((prev) => !prev);
          setIdOrEmail("");
          setMessage("");
        }}
      >
        {useEmailLogin ? "Use Account ID instead" : "Use Email instead"}
      </button>
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
    </form>
  </div>
);
};

export default Login;
