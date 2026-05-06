import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { appointmentsAPI } from '../services/api';
import Navbar from '../components/Navbar';
import '../styles/dashboard.css';

const NAV_LINKS = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/services', label: 'Services' },
  { to: '/my-appointments', label: 'My Appointments' },
];

function formatDate(dt) {
  if (!dt) return '—';
  return new Date(dt).toLocaleDateString('en-PH', {
    weekday: 'long', month: 'long', day: 'numeric', year: 'numeric',
  });
}

function formatTime(dt) {
  if (!dt) return '';
  return new Date(dt).toLocaleTimeString('en-PH', {
    hour: 'numeric', minute: '2-digit',
  });
}

export default function PaymentSuccess() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const appointmentId = searchParams.get('appointmentId');

  const [appointment, setAppointment] = useState(null);
  const [loading, setLoading] = useState(true);
  const [status, setStatus] = useState('checking'); // 'checking' | 'confirmed' | 'pending' | 'error'

  useEffect(() => {
    if (!appointmentId) {
      setStatus('error');
      setLoading(false);
      return;
    }

    // Poll up to 5 times (5 seconds) — webhook may take a moment to process
    let attempts = 0;
    const MAX_ATTEMPTS = 5;

    const check = async () => {
      try {
        const res = await appointmentsAPI.getById(appointmentId);
        const appt = res.data.data;
        setAppointment(appt);

        if (appt.appointmentStatus === 'CONFIRMED' || appt.status === 'CONFIRMED') {
          setStatus('confirmed');
          setLoading(false);
        } else if (attempts < MAX_ATTEMPTS) {
          attempts++;
          setTimeout(check, 1000);
        } else {
          // Webhook not yet processed — show pending state
          setStatus('pending');
          setLoading(false);
        }
      } catch {
        setStatus('error');
        setLoading(false);
      }
    };

    check();
  }, [appointmentId]);

  return (
    <div className="app-layout">
      <Navbar links={NAV_LINKS} />

      <main className="page-container" style={{ display: 'flex', justifyContent: 'center', alignItems: 'flex-start', paddingTop: '60px' }}>
        <div style={{
          background: 'white',
          borderRadius: '16px',
          boxShadow: '0 4px 24px rgba(0,0,0,0.08)',
          padding: '48px 40px',
          maxWidth: '480px',
          width: '100%',
          textAlign: 'center',
        }}>

          {loading && (
            <>
              <div className="spinner" style={{ width: 48, height: 48, margin: '0 auto 20px' }} />
              <h2 style={{ color: 'var(--gray-700)', marginBottom: 8 }}>Confirming your payment…</h2>
              <p style={{ color: 'var(--gray-500)', fontSize: '14px' }}>Please wait while we verify your payment.</p>
            </>
          )}

          {!loading && status === 'confirmed' && (
            <>
              <div style={{ fontSize: 64, marginBottom: 16 }}>✅</div>
              <h2 style={{ color: 'var(--primary)', marginBottom: 8 }}>Payment Confirmed!</h2>
              <p style={{ color: 'var(--gray-500)', marginBottom: 24, fontSize: '14px' }}>
                Your appointment has been confirmed. See you soon!
              </p>

              {appointment && (
                <div style={{
                  background: 'linear-gradient(135deg, rgba(110,193,195,0.08), rgba(154,208,166,0.08))',
                  border: '1px solid rgba(110,193,195,0.25)',
                  borderRadius: 12,
                  padding: '20px',
                  marginBottom: 24,
                  textAlign: 'left',
                }}>
                  <div style={{ fontSize: 13, color: 'var(--gray-500)', marginBottom: 12, fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.06em' }}>
                    Appointment Details
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                    <Row label="Service"  value={appointment.serviceName || '—'} />
                    <Row label="Dentist"  value={appointment.dentistName ? `Dr. ${appointment.dentistName}` : '—'} />
                    <Row label="Date"     value={formatDate(appointment.appointmentDatetime)} />
                    <Row label="Time"     value={formatTime(appointment.appointmentDatetime)} />
                    <Row label="Status"   value="Confirmed" valueColor="green" />
                  </div>
                </div>
              )}

              <button
                onClick={() => navigate('/my-appointments')}
                style={{
                  width: '100%', height: 44,
                  background: 'var(--gradient-primary)',
                  color: 'white', border: 'none',
                  borderRadius: 10, fontWeight: 600,
                  fontSize: 14, cursor: 'pointer',
                }}
              >
                View My Appointments
              </button>
            </>
          )}

          {!loading && status === 'pending' && (
            <>
              <div style={{ fontSize: 64, marginBottom: 16 }}>⏳</div>
              <h2 style={{ color: 'var(--gray-700)', marginBottom: 8 }}>Payment Received</h2>
              <p style={{ color: 'var(--gray-500)', marginBottom: 24, fontSize: '14px' }}>
                Your payment was received. Your appointment confirmation may take a moment to process.
                Check <strong>My Appointments</strong> shortly.
              </p>
              <button
                onClick={() => navigate('/my-appointments')}
                style={{
                  width: '100%', height: 44,
                  background: 'var(--gradient-primary)',
                  color: 'white', border: 'none',
                  borderRadius: 10, fontWeight: 600,
                  fontSize: 14, cursor: 'pointer',
                }}
              >
                View My Appointments
              </button>
            </>
          )}

          {!loading && status === 'error' && (
            <>
              <div style={{ fontSize: 64, marginBottom: 16 }}>⚠️</div>
              <h2 style={{ color: 'var(--gray-700)', marginBottom: 8 }}>Something went wrong</h2>
              <p style={{ color: 'var(--gray-500)', marginBottom: 24, fontSize: '14px' }}>
                We couldn't retrieve your appointment details. Please check My Appointments.
              </p>
              <button
                onClick={() => navigate('/my-appointments')}
                style={{
                  width: '100%', height: 44,
                  background: 'var(--gradient-primary)',
                  color: 'white', border: 'none',
                  borderRadius: 10, fontWeight: 600,
                  fontSize: 14, cursor: 'pointer',
                }}
              >
                View My Appointments
              </button>
            </>
          )}

        </div>
      </main>
    </div>
  );
}

function Row({ label, value, valueColor }) {
  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 14 }}>
      <span style={{ color: 'var(--gray-500)' }}>{label}</span>
      <span style={{ fontWeight: 600, color: valueColor || 'var(--gray-800)' }}>{value}</span>
    </div>
  );
}