import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import axios from "axios";
import "../styles/login.css";
import { GoogleLogin } from '@react-oauth/google';
import { useAuth } from "../context/AuthContext";
import { authAPI } from "../services/api";

function Login() {
  const navigate = useNavigate();
  const { setUser } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");
    const trimmedEmail = email.trim();
    const trimmedPassword = password.trim();
    if (!trimmedEmail || !trimmedPassword) { setError("Please enter both email and password"); return; }
    setIsLoading(true);
    try {
      const response = await axios.post(
        "http://localhost:8080/auth/login",
        { email: trimmedEmail, password: trimmedPassword },
        { headers: { 'Content-Type': 'application/json' } }
      );
      if (response.data && response.data.token) {
        localStorage.setItem('token', response.data.token);
        const me = await authAPI.me();
        const userData = me.data.data;
        setUser(userData);
        navigate(userData.role === 'ADMIN' ? '/admin' : '/dashboard');
      }
    } catch (err) {
      if (err.response) {
        setError(err.response.data?.error?.message || err.response.data?.message || "Invalid email or password");
      } else if (err.request) {
        setError("Cannot connect to server. Please check if the backend is running.");
      } else {
        setError("An error occurred. Please try again.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <Link to="/" className="logo-text">🦷 DentaLink</Link>
          <h2>Sign in to your account</h2>
          <p>Welcome back! Please enter your details.</p>
        </div>

        {error && <div className="error-banner"><span>⚠</span> {error}</div>}

        <form onSubmit={handleLogin} className="login-form">
          <div className="form-group">
            <label>Email</label>
            <input type="email" placeholder="your.email@example.com" value={email} onChange={(e) => setEmail(e.target.value)} required disabled={isLoading} />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input type="password" placeholder="••••••••" value={password} onChange={(e) => setPassword(e.target.value)} required disabled={isLoading} />
          </div>
          <button type="submit" className="btn btn-primary btn-block" disabled={isLoading}>
            {isLoading ? "Signing in…" : "Login"}
          </button>
        </form>

        <div className="divider"><span>Or continue with</span></div>

        <div>
          <GoogleLogin
            onSuccess={async (credentialResponse) => {
              try {
                const idToken = credentialResponse.credential;
                const response = await axios.post("http://localhost:8080/auth/google", { idToken });
                if (response.data && response.data.token) {
                  localStorage.setItem('token', response.data.token);
                  const me = await authAPI.me();
                  const userData = me.data.data;
                  setUser(userData);
                  navigate(userData.role === 'ADMIN' ? '/admin' : '/dashboard');
                }
              } catch (_) {
                setError("Google login failed. Please try again.");
              }
            }}
            onError={() => setError("Google login failed.")}
          />
        </div>

        <div className="login-footer">
          <p>Don't have an account? <Link to="/register">Register here</Link></p>
          <Link to="/" className="back-link">Back to home</Link>
        </div>
      </div>
    </div>
  );
}

export default Login;
