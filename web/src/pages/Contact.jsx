import { useEffect, useRef } from 'react';
import Navbar from '@/shared/components/Navbar';
import '@/features/dashboard/styles/dashboard.css';

const CLINIC_LAT = 10.24738412405074;
const CLINIC_LNG = 123.8000086426953;
const CLINIC_NAME = "DentaLink Dental Clinic";

const NAV_LINKS = [
  { to: '/dashboard',       label: 'Dashboard' },
  { to: '/services',        label: 'Services' },
  { to: '/my-appointments', label: 'My Appointments' },
  { to: '/profile',         label: 'Profile' },
  { to: '/contact',         label: 'Contact' },
];

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

    if (window.google && window.google.maps) {
      initMap();
      return;
    }

    if (!document.getElementById("google-maps-script")) {
      const script = document.createElement("script");
      script.id = "google-maps-script";
      script.src = `https://maps.googleapis.com/maps/api/js?key=${apiKey}`;
      script.async = true;
      script.defer = true;
      script.onload = initMap;
      document.head.appendChild(script);
    } else {
      document.getElementById("google-maps-script").addEventListener("load", initMap);
    }
  }, []);

  return (
    <div
      ref={mapRef}
      style={{ width: '100%', height: '400px', borderRadius: '12px', background: '#e5e7eb' }}
    />
  );
}

export default function Contact() {
  const mapsUrl = `https://www.google.com/maps/dir/?api=1&destination=${CLINIC_LAT},${CLINIC_LNG}`;

  return (
    <div className="app-layout">
      <Navbar links={NAV_LINKS} />

      <main className="page-container">
        <div className="page-header">
          <h1 className="page-title">Clinic Location</h1>
          <p className="page-subtitle">Find us and get directions</p>
        </div>

        <div className="card" style={{ padding: 'var(--space-6)' }}>
          <ClinicMap />

          <div style={{ marginTop: 'var(--space-4)', textAlign: 'center' }}>
            <p style={{ fontWeight: 'var(--font-semibold)', color: 'var(--gray-800)', marginBottom: 'var(--space-3)' }}>
              DentaLink Dental Clinic — Cebu, Philippines
            </p>
            <a href={mapsUrl} target="_blank" rel="noopener noreferrer">
              <button
                style={{
                  background: 'var(--primary)',
                  color: 'white',
                  border: 'none',
                  borderRadius: '8px',
                  padding: '10px 24px',
                  fontSize: 'var(--text-sm)',
                  fontWeight: 'var(--font-medium)',
                  cursor: 'pointer',
                }}
              >
                📍 Get Directions
              </button>
            </a>
          </div>
        </div>
      </main>
    </div>
  );
}
