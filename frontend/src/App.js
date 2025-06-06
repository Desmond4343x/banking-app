import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Link, useNavigate } from 'react-router-dom';
import CreateAccount from './CreateAccount';
import Login from './Login';
import UserInfo from './UserInfo';
import Home from './Home';
import SendMoney from './SendMoney';
import RequestMoney from './RequestMoney';
import TransactionHistory from './TransactionHistory';
import PendingTransactionsSend from './PendingTransactionsSend';
import PendingTransactionsRecieve from './PendingTransactionsRecieve';
import WithdrawMoney from './WithdrawMoney';
import DepositMoney from './DepositMoney';
import Help from './Help';
import AdminDashboard from "./AdminDashboard";
import NotAdmin from "./NotAdmin";
import axios from 'axios';

const api = process.env.REACT_APP_BACKEND_URL;

const AdminLink = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  const handleAdminAccess = async (e) => {
    e.preventDefault(); 
    setLoading(true);

    try {
      const token = localStorage.getItem("token");

      const res = await axios.get(`${api}/bank/is-admin`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      console.log("Admin check response:", res.data);

      if (res.data === true || res.data === "true") {
        navigate("/admin-dashboard");
      } else {
        navigate("/not-admin");
      }
    } catch (err) {
      console.error("Admin check failed:", err.response?.data || err.message);
      navigate("/not-admin");
    } finally {
      setLoading(false);
    }
  };

  return (
    <li style={{ marginTop: '5px' }}>
      <Link
        to="#"
        onClick={handleAdminAccess}
        style={{
          textDecoration: 'none',
          pointerEvents: loading ? 'none' : 'auto',
          opacity: loading ? 0.6 : 1,
        }}
      >
        {loading ? "Checking access..." : "Admin Dashboard"}
      </Link>
    </li>
  );
};

const App = () => {
  const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem("token"));

  const handleLogout = () => {
    localStorage.removeItem("token");
    setIsLoggedIn(false);
  };

  useEffect(() => {
    localStorage.removeItem("token");
    setIsLoggedIn(false);
  }, []);

  useEffect(() => {
    const checkToken = () => {
      setIsLoggedIn(!!localStorage.getItem("token"));
    };
    window.addEventListener("storage", checkToken);
    return () => window.removeEventListener("storage", checkToken);
  }, []);

  return (
    <Router>
      <div style={{ display: 'flex' }}>
        {/* Sidebar */}
        <nav style={{ width: '210px', padding: '20px', backgroundColor: '#f0f0f0', minHeight: '100vh' }}>
          <ul style={{ listStyleType: 'none', padding: 0 }}>
            <li style={{ marginTop: '50px' }}><Link to="/" style={{ textDecoration: 'none' }} onClick={handleLogout}>Home</Link></li>

            {!isLoggedIn ? (
              <>
                <li style={{ marginTop: '50px' }}>
                  <Link to="/createAccount" style={{ textDecoration: 'none' }}>Create Account</Link>
                </li>
                <li style={{ marginTop: '5px' }}>
                  <Link to="/login" style={{ textDecoration: 'none' }}>Login</Link>
                </li>
              </>
            ) : (
              <>
                <li style={{ marginTop: '50px' }}><Link to="/userinfo" style={{ textDecoration: 'none' }}>My Account</Link></li>
                <li style={{ marginTop: '35px' }}><Link to="/sendmoney" style={{ textDecoration: 'none' }}>Send Money</Link></li>
                <li style={{ marginTop: '5px' }}><Link to="/requestmoney" style={{ textDecoration: 'none' }}>Request Money</Link></li>
                <li style={{ marginTop: '5px' }}><Link to="/depositmoney" style={{ textDecoration: 'none' }}>Deposit Money</Link></li>
                <li style={{ marginTop: '5px' }}><Link to="/withdrawmoney" style={{ textDecoration: 'none' }}>Withdraw Money</Link></li>
                <li style={{ marginTop: '35px' }}><Link to="/pendingapprovals" style={{ textDecoration: 'none' }}>Pending Approvals</Link></li>
                <li style={{ marginTop: '5px' }}><Link to="/pendingrequests" style={{ textDecoration: 'none' }}>Pending Requests</Link></li>
                <li style={{ marginTop: '35px' }}><Link to="/transactionhistory" style={{ textDecoration: 'none' }}>Transaction History</Link></li>

                {/* Admin Dashboard - protected button */}
                <AdminLink />

                <li style={{ marginTop: '60px' }}><Link to="/help" style={{ textDecoration: 'none' }}>Help</Link></li>
                <li style={{ marginTop: '5px' }}>
                  <Link to="/" onClick={handleLogout} style={{ textDecoration: 'none' }}>Logout</Link>
                </li>
              </>
            )}
          </ul>
        </nav>

        {/* Main content */}
        <div style={{ flexGrow: 1, padding: '20px' }}>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/createAccount" element={<CreateAccount />} />
            <Route path="/login" element={<Login setIsLoggedIn={setIsLoggedIn} />} />
            <Route path="/userinfo" element={<UserInfo />} />
            <Route path="/sendmoney" element={<SendMoney />} />
            <Route path="/requestmoney" element={<RequestMoney />} />
            <Route path="/transactionhistory" element={<TransactionHistory />} />
            <Route path="/pendingapprovals" element={<PendingTransactionsSend />} />
            <Route path="/pendingrequests" element={<PendingTransactionsRecieve />} />
            <Route path="/depositmoney" element={<DepositMoney />} />
            <Route path="/withdrawmoney" element={<WithdrawMoney />} />
            <Route path="/help" element={<Help />} />
            <Route path="/admin-dashboard" element={<AdminDashboard />} />
            <Route path="/not-admin" element={<NotAdmin />} />
          </Routes>
        </div>
      </div>
    </Router>
  );
};

export default App;
