import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import "../styles/register.css";
import { GoogleLogin } from "@react-oauth/google";
import { useAuth } from "../context/AuthContext";
import { authAPI } from "../services/api";

function SuccessToast({ message }) {
  return (
    <div className="success-toast">
      <span className="success-toast-icon">✓</span>
      {message}
    </div>
  );
}

function Register() {
  const navigate = useNavigate();
  const { setUser } = useAuth();

  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    confirmPassword: "",
  });
  const [isLoading, setIsLoading]   = useState(false);
  const [error, setError]           = useState("");
  const [successMsg, setSuccessMsg] = useState("");

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  // ── Email / Password registration ───────────────────────────────────────────
  const handleRegister = async (e) => {
    e.preventDefault();
    setError("");
    setSuccessMsg("");

    if (formData.password !== formData.confirmPassword) {
      setError("Passwords do not match.");
      return;
    }
    if (formData.password.length < 6) {
      setError("Password must be at least 6 characters.");
      return;
    }

    setIsLoading(true);
    try {
      const response = await authAPI.register({
        firstName: formData.firstName,
        lastName:  formData.lastName,
        email:     formData.email,
        password:  formData.password,
      });

      const { accessToken, user } = response.data.data;
      localStorage.setItem("token", accessToken);
      setUser(user);
      navigate(
        user.role === "ADMIN" ? "/admin" : "/dashboard",
        { state: { justLoggedIn: true, firstName: user.firstName, role: user.role } }
      );
    } catch (err) {
      if (err.response?.status === 409) {
        setError("This email is already registered. Please log in instead.");
      } else if (err.response?.data?.error?.message) {
        setError(err.response.data.error.message);
      } else {
        setError("Registration failed. Please try again.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  // ── Google Sign-Up/In ───────────────────────────────────────────────────────
  // The backend auto-creates a PATIENT account for new Google users,
  // or logs in an existing one — same endpoint as Login.
  const handleGoogleSuccess = async (credentialResponse) => {
    setError("");
    setSuccessMsg("");
    try {
      const idToken  = credentialResponse.credential;
      const response = await authAPI.googleLogin({ idToken });

      if (response.data && response.data.data) {
        const { accessToken, user } = response.data.data;
        localStorage.setItem("token", accessToken);
        setUser(user);
        navigate(
          user.role === "ADMIN" ? "/admin" : "/dashboard",
          { state: { justLoggedIn: true, firstName: user.firstName, role: user.role } }
        );
      }
    } catch (_) {
      setError("Google sign-up failed. Please try again.");
    }
  };

  return (
    <div className="register-container">
      <div className="register-card">
        <div className="register-header">
          <Link to="/" className="logo-text">🦷 DentaLink</Link>
          <h2>Create your account</h2>
          <p>Join us for better dental care</p>
        </div>

        {successMsg && <SuccessToast message={successMsg} />}
        {error && (
          <div className="error-banner">
            <span>⚠</span> {error}
          </div>
        )}

        <form onSubmit={handleRegister} className="register-form">
          <div className="form-row">
            <div className="form-group">
              <label>First Name</label>
              <input
                type="text"
                name="firstName"
                placeholder="John"
                value={formData.firstName}
                onChange={handleChange}
                required
                disabled={isLoading}
              />
            </div>

            <div className="form-group">
              <label>Last Name</label>
              <input
                type="text"
                name="lastName"
                placeholder="Smith"
                value={formData.lastName}
                onChange={handleChange}
                required
                disabled={isLoading}
              />
            </div>
          </div>

          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              name="email"
              placeholder="your.email@example.com"
              value={formData.email}
              onChange={handleChange}
              required
              disabled={isLoading}
            />
          </div>

          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              name="password"
              placeholder="*********"
              value={formData.password}
              onChange={handleChange}
              required
              disabled={isLoading}
            />
          </div>

          <div className="form-group">
            <label>Confirm Password</label>
            <input
              type="password"
              name="confirmPassword"
              placeholder="*********"
              value={formData.confirmPassword}
              onChange={handleChange}
              required
              disabled={isLoading}
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary btn-block"
            disabled={isLoading}
          >
            {isLoading ? "Creating account…" : "Register"}
          </button>
        </form>

        <div className="divider">
          <span>Or sign up with</span>
        </div>

        {/* Real Google Sign-Up button */}
        <div className="google-btn-wrapper">
          <GoogleLogin
            onSuccess={handleGoogleSuccess}
            onError={() => setError("Google sign-up failed. Please try again.")}
            width="100%"
            text="signup_with"
            shape="rectangular"
            theme="outline"
            size="large"
          />
        </div>

        <div className="register-footer">
          <p>
            Already have an account? <Link to="/login">Login here</Link>
          </p>
          <Link to="/" className="back-link">Back to home</Link>
        </div>
      </div>
    </div>
  );
}

export default Register;