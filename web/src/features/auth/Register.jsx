import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Eye, EyeOff } from "lucide-react";
import "./styles/register.css";
import { GoogleLogin } from "@react-oauth/google";
import { useAuth } from "@/shared/context/AuthContext";
import { authAPI } from "@/shared/api/api";
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
  const [isLoading, setIsLoading]               = useState(false);
  const [showPassword, setShowPassword]         = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

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
    if (formData.password.length < 8) {
      toast.error("Password must be at least 8 characters.");
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
      toast.success(`Welcome to DentaLink, ${user.firstName?.trim()}!`);
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
        toast.success(`Welcome to DentaLink, ${user.firstName?.trim()}!`);
        setUser(user);
        navigate(user.role === "ADMIN" ? "/admin" : "/dashboard");
      }
    } catch (_) {
      toast.error("Google sign-up failed. Please try again.");
    }
  };

  // Reusable inline styles for the eye toggle button
  const eyeButtonStyle = {
    position: 'absolute', right: 12, top: '50%',
    transform: 'translateY(-50%)',
    background: 'none', border: 'none',
    cursor: 'pointer', color: 'var(--gray-400)',
    display: 'flex', alignItems: 'center', padding: 0,
  };

  return (
    <div className="register-container">
      <div className="register-card">
        <div className="register-header">
          <Link to="/" className="logo-text" style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}><img src="/Logo.png" alt="DentaLink" style={{ height: '36px', objectFit: 'contain', display: 'block' }} /><span>DentaLink</span></Link>
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
            <div style={{ position: 'relative' }}>
              <input
                type={showPassword ? 'text' : 'password'}
                name="password"
                placeholder="*********"
                value={formData.password}
                onChange={handleChange}
                required
                disabled={isLoading}
                style={{ paddingRight: '44px' }}
              />
              <button
                type="button"
                onClick={() => setShowPassword(p => !p)}
                style={eyeButtonStyle}
                tabIndex={-1}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? <Eye size={16} /> : <EyeOff size={16} />}
              </button>
            </div>
          </div>

          <div className="form-group">
            <label>Confirm Password</label>
            <div style={{ position: 'relative' }}>
              <input
                type={showConfirmPassword ? 'text' : 'password'}
                name="confirmPassword"
                placeholder="*********"
                value={formData.confirmPassword}
                onChange={handleChange}
                required
                disabled={isLoading}
                style={{ paddingRight: '44px' }}
              />
              <button
                type="button"
                onClick={() => setShowConfirmPassword(p => !p)}
                style={eyeButtonStyle}
                tabIndex={-1}
                aria-label={showConfirmPassword ? 'Hide confirm password' : 'Show confirm password'}
              >
                {showConfirmPassword ? <Eye size={16} /> : <EyeOff size={16} />}
              </button>
            </div>
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