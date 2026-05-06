import { Link } from "react-router-dom";
import { useEffect, useRef } from "react";
import "../styles/landing.css";

// Clinic coordinates — fixed per SDD AC-7
const CLINIC_LAT = 10.24738412405074; 
const CLINIC_LNG = 123.8000086426953;
const CLINIC_NAME = "DentaLink Dental Clinic";

function ClinicMap() {
  const mapRef = useRef(null);
  const mapInstanceRef = useRef(null);

  useEffect(() => {
    const apiKey = import.meta.env.VITE_GOOGLE_MAPS_API_KEY;

    const initMap = () => {
      if (!mapRef.current || mapInstanceRef.current) return;

      const map = new window.google.maps.Map(mapRef.current, {
        center: { lat: CLINIC_LAT, lng: CLINIC_LNG },
        zoom: 15,
        mapTypeControl: false,
        streetViewControl: false,
        fullscreenControl: false,
      });

      new window.google.maps.Marker({
        position: { lat: CLINIC_LAT, lng: CLINIC_LNG },
        map,
        title: CLINIC_NAME,
      });

      mapInstanceRef.current = map;
    };

    // If Maps API already loaded (e.g. hot reload)
    if (window.google && window.google.maps) {
      initMap();
      return;
    }

    // Load Maps script dynamically — avoids hardcoding key in index.html
    if (!document.getElementById("google-maps-script")) {
      const script = document.createElement("script");
      script.id = "google-maps-script";
      script.src = `https://maps.googleapis.com/maps/api/js?key=${apiKey}`;
      script.async = true;
      script.defer = true;
      script.onload = initMap;
      document.head.appendChild(script);
    } else {
      // Script tag exists but may still be loading
      document.getElementById("google-maps-script").addEventListener("load", initMap);
    }
  }, []);

  return (
    <div
      ref={mapRef}
      style={{ width: "100%", height: "220px", borderRadius: "12px", background: "#e5e7eb" }}
    />
  );
}

function Landing() {
  const mapsUrl = `https://www.google.com/maps/dir/?api=1&destination=${CLINIC_LAT},${CLINIC_LNG}`;

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

              {/* Google Maps — SDD AC-7 */}
              <ClinicMap />

              <a href={mapsUrl} target="_blank" rel="noopener noreferrer">
                <button className="btn btn-outline" style={{ marginTop: "12px", width: "100%" }}>
                  📍 Get Directions
                </button>
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Landing;