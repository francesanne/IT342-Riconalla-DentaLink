import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import "../styles/login.css";
import { GoogleLogin } from '@react-oauth/google';
import { useAuth } from "../context/AuthContext";
import { authAPI } from "../services/api";

function Login() {
  const navigate = useNavigate();
  const { setUser } = useAuth();

  const [email, setEmail]         = useState("");
  const [password, setPassword]   = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError]         = useState("");

  // Set user then navigate WITH a state flag so the destination page
  // can show the success popup. We do NOT try to show the popup here
  // because App.jsx immediately unmounts <Login> the moment setUser()
  // runs (the route condition "user ? <Navigate> : <Login>" triggers).
  const finishLogin = (token, userData) => {
    localStorage.setItem("token", token);
    setUser(userData);
    navigate(
      userData.role === "ADMIN" ? "/admin" : "/dashboard",
      { state: { justLoggedIn: true, firstName: userData.firstName, role: userData.role } }
    );
  };

  // ── Email / Password ────────────────────────────────────────────────────────
  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");

    const trimmedEmail    = email.trim();
    const trimmedPassword = password.trim();

    if (!trimmedEmail || !trimmedPassword) {
      setError("Please enter both email and password");
      return;
    }

    setIsLoading(true);
    try {
      const response = await authAPI.login(
        { email: trimmedEmail, password: trimmedPassword }
      );

      if (response.data && response.data.data) {
        const { accessToken, user } = response.data.data;
        finishLogin(accessToken, user);
      }
    } catch (err) {
      if (err.response) {
        setError(
          err.response.data?.error?.message ||
          err.response.data?.message ||
          "Invalid email or password"
        );
      } else if (err.request) {
        setError("Cannot connect to server. Please check if the backend is running.");
      } else {
        setError("An error occurred. Please try again.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  // ── Google ──────────────────────────────────────────────────────────────────
  const handleGoogleSuccess = async (credentialResponse) => {
    setError("");
    try {
      const idToken  = credentialResponse.credential;
      const response = await authAPI.googleLogin({ idToken });

      if (response.data && response.data.data) {
        const { accessToken, user } = response.data.data;
        finishLogin(accessToken, user);
      }
    } catch (_) {
      setError("Google login failed. Please try again.");
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

        {error && (
          <div className="error-banner">
            <span>⚠</span> {error}
          </div>
        )}

        <form onSubmit={handleLogin} className="login-form">
          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              placeholder="your.email@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              disabled={isLoading}
            />
          </div>

          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              disabled={isLoading}
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary btn-block"
            disabled={isLoading}
          >
            {isLoading ? "Signing in…" : "Login"}
          </button>
        </form>

        <div className="divider">
          <span>Or continue with</span>
        </div>

        <div className="google-btn-wrapper">
          <GoogleLogin
            onSuccess={handleGoogleSuccess}
            onError={() => setError("Google login failed. Please try again.")}
            width="100%"
            text="signin_with"
            shape="rectangular"
            theme="outline"
            size="large"
          />
        </div>

        <div className="login-footer">
          <p>
            Don't have an account? <Link to="/register">Register here</Link>
          </p>
          <Link to="/" className="back-link">Back to home</Link>
        </div>
      </div>
    </div>
  );
}

export default Login;