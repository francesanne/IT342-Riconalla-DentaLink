import { Link } from "react-router-dom";
import "../styles/landing.css";

function Landing() {
  return (
    <div className="landing-container">
      <div className="landing-card">
        <div className="landing-content">
          {/* Left Column */}
          <div className="left-column">
            <div className="logo-section">
              <span className="logo-icon">🦷</span>
              <span className="logo-text">DentaLink</span>
            </div>

            <div className="hero-section">
              <h1>Book Your Dental Appointment with Confidence</h1>
              <p>
                Experience world-class dental care with our team of expert professionals. 
                Your smile is our priority, and your comfort is our commitment.
              </p>

              <div className="button-group">
                <Link to="/register">
                  <button className="btn btn-primary">Get Started</button>
                </Link>
                <Link to="/login">
                  <button className="btn btn-secondary">Login</button>
                </Link>
                <button className="btn btn-outline">Book Now</button>
              </div>

              <div className="hero-stats">
                <div>
                  <span className="stats-number">4,000+</span>
                  <span>Patients</span>
                </div>
                <div className="stars">
                  <span>★★★★★</span>
                </div>
                <span className="verified">Verified Reviews</span>
              </div>
            </div>
          </div>

          {/* Right Column */}
          <div className="right-column">
            <div className="clinic-section">
              <h2>Visit Our Clinic</h2>
              <p>Conveniently located in the heart of the city</p>
              
              <div className="map-placeholder">
                <span>📍</span>
                <p>Map will be displayed here</p>
                <small>Google Maps Integration</small>
              </div>

              <button className="btn btn-outline">Get Directions</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Landing;