import { useEffect, useRef } from 'react';
import { MapPin, Phone, Mail, Clock, Globe, ExternalLink } from 'lucide-react';
import Navbar from '@/shared/components/Navbar';
import '@/features/dashboard/styles/dashboard.css';

const CLINIC_LAT  = 10.24738412405074;
const CLINIC_LNG  = 123.8000086426953;
const CLINIC_NAME = "DentaLink Dental Clinic";

const NAV_LINKS = [
  { to: '/dashboard',       label: 'Dashboard' },
  { to: '/services',        label: 'Services' },
  { to: '/my-appointments', label: 'My Appointments' },
  { to: '/contact',         label: 'Contact' },
];

const CLINIC_HOURS = [
  { label: 'Monday – Friday', time: '8:00 AM – 6:00 PM', days: [1, 2, 3, 4, 5] },
  { label: 'Saturday',        time: '9:00 AM – 5:00 PM', days: [6] },
  { label: 'Sunday',          time: 'Closed',             days: [0], closed: true },
];

// ── Map component ────────────────────────────────────────────────────────────

function ClinicMap() {
  const mapRef         = useRef(null);
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

    if (window.google && window.google.maps) { initMap(); return; }

    if (!document.getElementById('google-maps-script')) {
      const script    = document.createElement('script');
      script.id       = 'google-maps-script';
      script.src      = `https://maps.googleapis.com/maps/api/js?key=${apiKey}`;
      script.async    = true;
      script.defer    = true;
      script.onload   = initMap;
      document.head.appendChild(script);
    } else {
      document.getElementById('google-maps-script').addEventListener('load', initMap);
    }
  }, []);

  return (
    <div
      ref={mapRef}
      style={{ width: '100%', height: '380px', borderRadius: 'var(--radius-xl)', background: 'var(--gray-100)' }}
    />
  );
}

// ── Info row component ────────────────────────────────────────────────────────

function InfoRow({ icon, label, children, isLast }) {
  return (
    <div style={{
      display: 'flex',
      gap: 'var(--space-3)',
      paddingBottom: isLast ? 0 : 'var(--space-4)',
      marginBottom: isLast ? 0 : 'var(--space-4)',
      borderBottom: isLast ? 'none' : '1px solid var(--gray-100)',
    }}>
      <div style={{
        width: 34, height: 34,
        borderRadius: 'var(--radius-lg)',
        background: 'rgba(110,193,195,0.12)',
        color: 'var(--primary)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        flexShrink: 0,
        marginTop: 2,
      }}>
        {icon}
      </div>
      <div>
        <div style={{
          fontSize: '0.6875rem',
          fontWeight: 'var(--font-semibold)',
          color: 'var(--gray-400)',
          textTransform: 'uppercase',
          letterSpacing: '0.06em',
          marginBottom: 5,
        }}>
          {label}
        </div>
        <div style={{ fontSize: 'var(--text-sm)', color: 'var(--gray-700)', lineHeight: 1.65 }}>
          {children}
        </div>
      </div>
    </div>
  );
}

// ── Page ─────────────────────────────────────────────────────────────────────

export default function Contact() {
  const mapsUrl  = `https://www.google.com/maps/dir/?api=1&destination=${CLINIC_LAT},${CLINIC_LNG}`;
  const todayIdx = new Date().getDay();

  return (
    <div className="app-layout">
      <Navbar links={NAV_LINKS} />

      <main className="page-container">
        <div className="page-header">
          <h1 className="page-title">Contact Us</h1>
          <p className="page-subtitle">Find us, get directions, or reach out to our team</p>
        </div>

        <div className="contact-layout">

          {/* ── Left column: contact info ── */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>

            {/* Get in Touch card */}
            <div className="card">
              <div className="card-header">
                <span className="card-title">Get in Touch</span>
              </div>
              <div style={{ padding: 'var(--space-5)' }}>

                <InfoRow icon={<MapPin size={15} />} label="Address">
                  3rd Floor, One Cybersquare<br />
                  Cebu IT Park, Archbishop Reyes Ave<br />
                  Apas, Cebu City, 6000
                </InfoRow>

                <InfoRow icon={<Phone size={15} />} label="Phone">
                  (032) 234-5678<br />
                  +63 917 234 5678 <span style={{ color: 'var(--gray-400)', fontSize: 'var(--text-xs)' }}>Globe</span>
                </InfoRow>

                <InfoRow icon={<Mail size={15} />} label="Email">
                  <a
                    href="mailto:hello@dentalink.ph"
                    style={{ color: 'var(--primary)', textDecoration: 'none', fontWeight: 'var(--font-medium)' }}
                  >
                    hello@dentalink.ph
                  </a>
                </InfoRow>

                <InfoRow icon={<Globe size={15} />} label="Social Media" isLast>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                    <a
                      href="https://www.facebook.com/DentaLinkClinic"
                      target="_blank"
                      rel="noopener noreferrer"
                      style={{ display: 'inline-flex', alignItems: 'center', gap: 6, color: '#1877f2', textDecoration: 'none', fontWeight: 'var(--font-medium)' }}
                    >
                      <ExternalLink size={13} /> DentaLink Dental Clinic
                    </a>
                    <a
                      href="https://www.instagram.com/dentalink_clinic"
                      target="_blank"
                      rel="noopener noreferrer"
                      style={{ display: 'inline-flex', alignItems: 'center', gap: 6, color: '#e1306c', textDecoration: 'none', fontWeight: 'var(--font-medium)' }}
                    >
                      <ExternalLink size={13} /> @dentalink_clinic
                    </a>
                  </div>
                </InfoRow>

              </div>
            </div>

            {/* Clinic hours card */}
            <div className="card">
              <div className="card-header">
                <span className="card-title" style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                  <Clock size={16} style={{ color: 'var(--primary)' }} />
                  Clinic Hours
                </span>
              </div>
              <div style={{ padding: 'var(--space-5)' }}>
                {CLINIC_HOURS.map((h, i) => {
                  const isToday = h.days.includes(todayIdx);
                  return (
                    <div
                      key={i}
                      style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        padding: '9px 12px',
                        borderRadius: 'var(--radius-lg)',
                        background: isToday ? 'rgba(110,193,195,0.10)' : 'transparent',
                        marginBottom: i < CLINIC_HOURS.length - 1 ? 'var(--space-1)' : 0,
                      }}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                        {isToday && (
                          <span style={{
                            width: 6, height: 6, borderRadius: '50%',
                            background: 'var(--primary)',
                            display: 'inline-block', flexShrink: 0,
                          }} />
                        )}
                        <span style={{
                          fontSize: 'var(--text-sm)',
                          color: isToday ? 'var(--primary)' : 'var(--gray-600)',
                          fontWeight: isToday ? 'var(--font-semibold)' : 'var(--font-normal)',
                          paddingLeft: isToday ? 0 : 14,
                        }}>
                          {h.label}
                        </span>
                      </div>
                      <span style={{
                        fontSize: 'var(--text-sm)',
                        fontWeight: 'var(--font-medium)',
                        color: h.closed ? 'var(--danger)' : (isToday ? 'var(--primary)' : 'var(--gray-700)'),
                      }}>
                        {h.time}
                      </span>
                    </div>
                  );
                })}
              </div>
            </div>

          </div>

          {/* ── Right column: map ── */}
          <div className="card" style={{ padding: 'var(--space-6)' }}>
            <ClinicMap />

            <div style={{ marginTop: 'var(--space-5)', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 'var(--space-3)' }}>
              <div>
                <div style={{ fontWeight: 'var(--font-semibold)', color: 'var(--gray-800)', fontSize: 'var(--text-base)' }}>
                  {CLINIC_NAME}
                </div>
                <div style={{ fontSize: 'var(--text-sm)', color: 'var(--gray-500)', marginTop: 2 }}>
                  Cebu IT Park, Cebu City, Philippines
                </div>
              </div>
              <a href={mapsUrl} target="_blank" rel="noopener noreferrer" style={{ textDecoration: 'none' }}>
                <button className="btn-sm btn-primary-sm" style={{ paddingLeft: 'var(--space-5)', paddingRight: 'var(--space-5)' }}>
                  Get Directions
                </button>
              </a>
            </div>
          </div>

        </div>
      </main>
    </div>
  );
}
