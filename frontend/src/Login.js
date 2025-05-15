import React, { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

const Login = ({ setIsLoggedIn }) => {
  const [id, setId] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [isError, setIsError] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setMessage("");

    if (id.trim() === "" || password.trim() === "") {
      setIsError(true);
      setMessage("Both ID and password are required.");
      return;
    }

    try {
      const response = await axios.post("http://localhost:8080/bank/login", {
        id,
        password,
      });

      const token = response.data.token;
      localStorage.setItem("token", token);

      setIsLoggedIn(true);
      setIsError(false);
      setMessage("Login successful!");
      navigate("/userinfo");
    } catch (error) {
      console.error("Login failed:", error);
      setIsError(true);
      setMessage("Invalid credentials. Please try again.");
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h2 style={{ marginBottom: '0' }}>Login</h2>
      <p style={{ marginTop: '0', fontSize: '16px', color: '#555' }}>
        Please enter your Account ID and password.
      </p>
      <form onSubmit={handleLogin}>
        <input
          type="text"
          placeholder="ID"
          value={id}
          onChange={(e) => setId(e.target.value)}
        /><br />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        /><br />
        <button type="submit">Login</button>
        {message && (
          <p
            style={{
              color: isError ? 'red' : 'green',
              marginTop: '10px',
              fontSize: '14px'
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
