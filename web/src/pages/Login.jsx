import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import axios from "axios";
import "../styles/login.css";
import { GoogleLogin } from '@react-oauth/google';

function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const handleLogin = async (e) => {
    e.preventDefault();

    const trimmedEmail = email.trim();
    const trimmedPassword = password.trim();

    console.log("Attempting login with:", { email: trimmedEmail });

    if (!trimmedEmail || !trimmedPassword) {
      alert("Please enter both email and password");
      return;
    }

    setIsLoading(true);

    try {
      const response = await axios.post(
        "http://localhost:8080/auth/login",
        {
          email: trimmedEmail,
          password: trimmedPassword
        },
        {
          headers: {
            'Content-Type': 'application/json'
          }
        }
      );

      console.log("Login success - Full response:", response);
      console.log("Login success - Data:", response.data);

      if (response.data && response.data.token) {
        localStorage.setItem('token', response.data.token);
        localStorage.setItem('userEmail', trimmedEmail);

        console.log("Token stored successfully");
        alert("Login successful!");
        navigate("/dashboard");

      } else {
        console.error("Unexpected response format:", response.data);
        alert("Login successful but unexpected response format");
      }

    } catch (error) {
      console.log("Login error:", error);

      if (error.response) {
        console.log("Error status:", error.response.status);
        console.log("Error data:", error.response.data);

        const errorMessage = error.response.data?.message ||
          error.response.data ||
          "Invalid email or password";
        alert(`Login failed: ${errorMessage}`);

      } else if (error.request) {
        console.log("No response from server");
        alert("Cannot connect to server. Please check if backend is running on port 8080");
      } else {
        console.log("Error:", error.message);
        alert("An error occurred. Please try again.");
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
              placeholder="**********"
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
            {isLoading ? "Logging in..." : "Login"}
          </button>
        </form>

        <div className="divider">
          <span>Or continue with</span>
        </div>

        <div>
          <GoogleLogin
            onSuccess={async (credentialResponse) => {
              try {
                const idToken = credentialResponse.credential;

                const response = await axios.post(
                  "http://localhost:8080/auth/google",
                  { idToken }
                );

                if (response.data && response.data.token) {
                  localStorage.setItem('token', response.data.token);
                  alert("Google login successful!");
                  navigate("/dashboard");
                }

              } catch (error) {
                console.error(error);
                alert("Google login failed");
              }
            }}
            onError={() => {
              alert("Google login failed");
            }}
          />
        </div>

        <div className="login-footer">
          <p>
            Don't have an account? <Link to="/register">Register here</Link>
          </p>
          <Link to="/" className="back-link">← Back to home</Link>
        </div>

      </div>
    </div>
  );
}

export default Login;

//google signin complete