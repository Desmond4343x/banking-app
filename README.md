# 💰 Banking App

Secure, modular, and scalable banking system with core features like user authentication, role-based access control, encrypted storage, and inter-user transactions.

### 🌐 Hosted On

- **Frontend**: [Vercel](https://silverstone-dun.vercel.app/)
- **Backend**: Render
- **Database**: Aiven (MySQL)

---

## 🔐 Features

1. **RSA/AES Hybrid Encryption**
   - AES key encrypts user data.
   - RSA (public key) encrypts AES key.
   - RSA (private key) decrypts AES, which then decrypts the data.

2. **Secure Password Management**
   - BCrypt hashing for user passwords.
   - Passwords and RSA private keys stored in a **vault-like**, separate database.

3. **JWT-Based Authentication**
   - JWT tokens used to protect API routes.
   - Verification and RBAC implemented at the API entrypoint.

4. **Role-Based Access Control (RBAC)**
   - Admin-only routes and dashboard with restricted functionality.

5. **Spring MVC Architecture**
   - Modular and clean code structure following Java Spring MVC patterns.

6. **Google SMTP Integration**
   - Automated email services and verification via Gmail SMTP.

7. **Cross-User Operations**
   - Includes `SendMoney`, `RequestMoney` functionality beyond standard CRUD.

---

## ⚠️ Drawbacks

- Very basic frontend with limited user interactivity.
- No proper async/delay/loading feedback handling.
- No session refresh or multiple session detection.
- JWT tokens aren't invalidated (no delisting or blacklisting).
- Unverified accounts are not auto-deleted.
- Password reset lacks OTP verification (if email is compromised, so is the account).

---

## 🔧 Planned Improvements

- ✨ **Modern, responsive frontend UI** with better UX and animations.
- 🚪 **API Gateway layer** for:
  - Rate limiting
  - Load balancing
  - API flooding protection
- 💳 **Transaction framework** with:
  - ACID compliance
  - Rollback support
- 📊 **ML/AI-Based Analytics**
  - Monthly spending analysis
  - Category-based insights
- 📈 **Data Visualizations**
  - Graphs and charts for financial metrics
- 📈 **Investment Simulation**
  - SIPs, FDs, interest logic, stock trading modules

---

## 📎 Live Link

🔗 [silverstone-dun.vercel.app](https://silverstone-dun.vercel.app/)

---

## 📂 Technologies Used

- **Frontend**: React.js, Vercel
- **Backend**: Spring Boot, Render
- **Database**: MySQL (Aiven)
- **Security**: RSA, AES, BCrypt, JWT
- **Email**: Gmail SMTP
