import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Eye, EyeOff } from "lucide-react";
import "./styles/login.css";
import { GoogleLogin } from '@react-oauth/google';
import { useAuth } from "@/shared/context/AuthContext";
import { authAPI } from "@/shared/api/api";
import { toast } from 'sonner';

function Login() {
  const navigate = useNavigate();
  const { setUser } = useAuth();

  const [email, setEmail]             = useState("");
  const [password, setPassword]       = useState("");
  const [isLoading, setIsLoading]     = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const finishLogin = (token, userData) => {
    localStorage.setItem("token", token);
    toast.success(`Welcome back, ${userData.firstName?.trim()}!`);
    setUser(userData);
    navigate(userData.role === "ADMIN" ? "/admin" : "/dashboard");
  };

  // ── Email / Password ────────────────────────────────────────────────────────
  const handleLogin = async (e) => {
    e.preventDefault();

    const trimmedEmail    = email.trim();
    const trimmedPassword = password.trim();

    if (!trimmedEmail || !trimmedPassword) {
      toast.error("Please enter both email and password.");
      return;
    }

    setIsLoading(true);
    try {
      const response = await authAPI.login({ email: trimmedEmail, password: trimmedPassword });
      if (response.data && response.data.data) {
        const { accessToken, user } = response.data.data;
        finishLogin(accessToken, user);
      }
    } catch (err) {
      if (err.response) {
        toast.error(
          err.response.data?.error?.message ||
          err.response.data?.message ||
          "Invalid email or password."
        );
      } else if (err.request) {
        toast.error("Cannot connect to server. Please check if the backend is running.");
      } else {
        toast.error("An error occurred. Please try again.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  // ── Google ──────────────────────────────────────────────────────────────────
  const handleGoogleSuccess = async (credentialResponse) => {
    try {
      const idToken  = credentialResponse.credential;
      const response = await authAPI.googleLogin({ idToken });
      if (response.data && response.data.data) {
        const { accessToken, user } = response.data.data;
        finishLogin(accessToken, user);
      }
    } catch (_) {
      toast.error("Google login failed. Please try again.");
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <Link to="/" className="logo-text" style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}><img src="/Logo.png" alt="DentaLink" style={{ height: '36px', objectFit: 'contain', display: 'block' }} /><span>DentaLink</span></Link>
          <h2>Sign in to your account</h2>
          <p>Welcome back! Please enter your details.</p>
        </div>

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
            <div style={{ position: 'relative' }}>
              <input
                type={showPassword ? 'text' : 'password'}
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                disabled={isLoading}
                style={{ paddingRight: '44px' }}
              />
              <button
                type="button"
                onClick={() => setShowPassword(p => !p)}
                style={{
                  position: 'absolute', right: 12, top: '50%',
                  transform: 'translateY(-50%)',
                  background: 'none', border: 'none',
                  cursor: 'pointer', color: 'var(--gray-400)',
                  display: 'flex', alignItems: 'center', padding: 0,
                }}
                tabIndex={-1}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? <Eye size={16} /> : <EyeOff size={16} />}
              </button>
            </div>
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
            onError={() => toast.error("Google login failed. Please try again.")}
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