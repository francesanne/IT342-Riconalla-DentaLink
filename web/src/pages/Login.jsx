import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import axios from "axios";
import "../styles/login.css";

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
      
      // Your backend returns: { "token": "eyJhbGci..." }
      if (response.data && response.data.token) {
        // Store the token
        localStorage.setItem('token', response.data.token);
        
        // Also store user email for display purposes (optional)
        localStorage.setItem('userEmail', trimmedEmail);
        
        console.log("Token stored successfully");
        alert("Login successful!");
        
        // Redirect to dashboard or home
        navigate("/dashboard"); // or "/" if you don't have dashboard yet
        
      } else {
        console.error("Unexpected response format:", response.data);
        alert("Login successful but unexpected response format");
      }

    } catch (error) {
      console.log("Login error:", error);
      
      if (error.response) {
        // Your backend returns error message in the response
        console.log("Error status:", error.response.status);
        console.log("Error data:", error.response.data);
        
        // Show the error message from backend
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

  const handleGoogleLogin = () => {
    console.log("Google login clicked");
    alert("Google login will be implemented soon");
  };

  // For testing - fill with your registered credentials
  const fillTestCredentials = () => {
    // CHANGE THESE TO YOUR ACTUAL REGISTERED CREDENTIALS
    setEmail("test@example.com"); // Replace with your registered email
    setPassword("password123");    // Replace with your registered password
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

        {/* Temporary test button - remove after debugging */}
        <button 
          type="button"
          onClick={fillTestCredentials}
          style={{
            marginTop: '10px',
            padding: '5px',
            fontSize: '12px',
            background: '#f0f0f0',
            border: '1px dashed #999',
            borderRadius: '4px',
            cursor: 'pointer',
            width: '100%'
          }}
        >
          📋 Fill Test Credentials (DEBUG ONLY)
        </button>

        <div className="divider">
          <span>Or continue with</span>
        </div>

        <button 
          onClick={handleGoogleLogin}
          className="btn btn-google"
          disabled={isLoading}
        >
          <span>G</span>
          Login with Google
        </button>

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