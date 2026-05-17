import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import "./styles/register.css";
import { GoogleLogin } from "@react-oauth/google";
import { useAuth } from "@/shared/context/AuthContext";
import { authAPI } from "@/shared/api/api";
import ToothIcon from '@/shared/components/ToothIcon';
import { toast } from 'sonner';

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
  const [isLoading, setIsLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  // ── Email / Password registration ───────────────────────────────────────────
  const handleRegister = async (e) => {
    e.preventDefault();

    if (formData.password !== formData.confirmPassword) {
      toast.error("Passwords do not match.");
      return;
    }
    if (formData.password.length < 6) {
      toast.error("Password must be at least 6 characters.");
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
      toast.success(`Welcome to DentaLink, ${user.firstName}!`);
      setUser(user);
      navigate(user.role === "ADMIN" ? "/admin" : "/dashboard");
    } catch (err) {
      if (err.response?.status === 409) {
        toast.error("This email is already registered. Please log in instead.");
      } else if (err.response?.data?.error?.message) {
        toast.error(err.response.data.error.message);
      } else {
        toast.error("Registration failed. Please try again.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  // ── Google Sign-Up/In ───────────────────────────────────────────────────────
  const handleGoogleSuccess = async (credentialResponse) => {
    try {
      const idToken  = credentialResponse.credential;
      const response = await authAPI.googleLogin({ idToken });
      if (response.data && response.data.data) {
        const { accessToken, user } = response.data.data;
        localStorage.setItem("token", accessToken);
        toast.success(`Welcome to DentaLink, ${user.firstName}!`);
        setUser(user);
        navigate(user.role === "ADMIN" ? "/admin" : "/dashboard");
      }
    } catch (_) {
      toast.error("Google sign-up failed. Please try again.");
    }
  };

  return (
    <div className="register-container">
      <div className="register-card">
        <div className="register-header">
          <Link to="/" className="logo-text" style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}><ToothIcon size={18} /> DentaLink</Link>
          <h2>Create your account</h2>
          <p>Join us for better dental care</p>
        </div>

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

        <div className="google-btn-wrapper">
          <GoogleLogin
            onSuccess={handleGoogleSuccess}
            onError={() => toast.error("Google sign-up failed. Please try again.")}
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
