import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import axios from "axios";
import "../styles/register.css";

function Register() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    confirmPassword: ""
  });

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    console.log("Register clicked");

    // Basic validation
    if (formData.password !== formData.confirmPassword) {
      alert("Passwords do not match");
      return;
    }

    try {
      const response = await axios.post(
        "http://localhost:8080/auth/register",
        {
          firstName: formData.firstName,
          lastName: formData.lastName,
          email: formData.email,
          password: formData.password
        }
      );

      console.log(response.data);
      alert("Registration successful! Please login.");
      navigate("/login");

    } catch (error) {
      console.log(error);
      alert("Registration failed. Email might already be registered.");
    }
  };

  const handleGoogleSignUp = () => {
    console.log("Google sign up clicked");
    alert("Google sign up will be implemented");
  };

  return (
    <div className="register-container">
      <div className="register-card">
        <div className="register-header">
          <Link to="/" className="logo-text">🦷 DentaLink</Link>
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
            />
          </div>

          <button type="submit" className="btn btn-primary btn-block">
            Register
          </button>
        </form>

        <div className="divider">
          <span>Or sign up with</span>
        </div>

        <button 
          onClick={handleGoogleSignUp}
          className="btn btn-google"
        >
          <span>G</span>
          Sign up with Google
        </button>

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