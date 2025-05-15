import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Link, useNavigate, useLocation } from 'react-router-dom';
import CreateAccount from './CreateAccount';
import Login from './Login';
import UserInfo from './UserInfo';
import Home from './Home';
import SendMoney from './SendMoney';
import RequestMoney from './RequestMoney';
import TransactionHistory from './TransactionHistory';

const App = () => {
  const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem("token"));

  const handleLogout = () => {
    localStorage.removeItem("token");
    setIsLoggedIn(false);
  };

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
        <nav style={{ width: '200px', padding: '20px', backgroundColor: '#f0f0f0', minHeight: '100vh' }}>
          <ul style={{ listStyleType: 'none', padding: 0 }}>
            <li style={{ marginTop: '50px' }}><Link to="/" style={{ textDecoration: 'none' }} onClick={handleLogout}>Home</Link></li>

            {!isLoggedIn ? (
              <>
                <li style={{ marginTop: '50px' }}>
                  <Link to="/createAccount" style={{ textDecoration: 'none' }}>Create Account</Link>
                </li>
                <li>
                  <Link to="/login" style={{ textDecoration: 'none' }}>Login</Link>
                </li>
              </>
            ) : (
              <>
                <li style={{ marginTop: '50px' }}><Link to="/userinfo" style={{ textDecoration: 'none' }}>My Account</Link></li>
                <li><Link to="/sendmoney" style={{ textDecoration: 'none' }}>Send Money</Link></li>
                <li><Link to="/requestmoney" style={{ textDecoration: 'none' }}>Request Money</Link></li>
                <li><Link to="#" style={{ textDecoration: 'none' }}>Deposit Money</Link></li>
                <li><Link to="#" style={{ textDecoration: 'none' }}>Withdraw Money</Link></li>
                <li><Link to="/transactionhistory" style={{ textDecoration: 'none' }}>Transaction History</Link></li>
                <li><Link to="#" style={{ textDecoration: 'none' }}>Pending Transactions</Link></li>
                <li style={{ marginTop: '60px' }}><Link to="#" style={{ textDecoration: 'none' }}>Help</Link></li>
                <li>
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
          </Routes>
        </div>
      </div>
    </Router>
  );
};

export default App;
