import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '@/shared/components/Navbar'
import StatusBadge from '@/shared/components/StatusBadge';
import { formatDate, formatTime } from '@/shared/utils/formatters';
import { appointmentsAPI } from '@/shared/api/api';
import { useAuth } from '@/shared/context/AuthContext';
import WelcomePopup from '@/shared/components/WelcomePopup';
import './styles/dashboard.css'

const NAV_LINKS = [
  { to: '/dashboard',       label: 'Dashboard' },
  { to: '/services',        label: 'Services' },
  { to: '/my-appointments', label: 'My Appointments' },
  { to: '/profile',         label: 'Profile' },
  { to: '/contact',         label: 'Contact' },
];


export default function PatientDashboard() {
  const { user } = useAuth();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    appointmentsAPI.getAll()
      .then(res => setAppointments(res.data.data || []))
      .catch(() => setError('Unable to load your appointments. Please refresh the page.'))
      .finally(() => setLoading(false));
  }, []);

  const upcoming = appointments.filter(a =>
    (a.status === 'CONFIRMED' || a.status === 'PENDING_PAYMENT') &&
    new Date(a.appointmentDatetime) >= new Date()
  ).sort((a, b) => new Date(a.appointmentDatetime) - new Date(b.appointmentDatetime));

  const past = appointments.filter(a =>
    a.status === 'COMPLETED' || a.status === 'CANCELLED' ||
    (a.status !== 'CANCELLED' && new Date(a.appointmentDatetime) < new Date())
  ).sort((a, b) => new Date(b.appointmentDatetime) - new Date(a.appointmentDatetime)).slice(0, 5);

  const pendingCount   = appointments.filter(a => a.paymentStatus === 'UNPAID' && a.status !== 'CANCELLED').length;
  const completedCount = appointments.filter(a => a.status === 'COMPLETED').length;

  const greeting = () => {
    const h = new Date().getHours();
    if (h < 12) return 'Good morning';
    if (h < 18) return 'Good afternoon';
    return 'Good evening';
  };

  if (error) return (
    <div className="app-layout">
      <Navbar links={NAV_LINKS} />
      <main className="page-container">
        <div style={{
          background: '#fef2f2',
          border: '1px solid #fecaca',
          borderRadius: '8px',
          padding: '16px 20px',
          color: '#dc2626',
          fontSize: '14px',
          marginTop: '24px'
        }}>
          ⚠️ {error}
        </div>
      </main>
    </div>
  );

  return (
    <div className="app-layout">
      {/* ── Welcome popup — only shows once right after login/Google sign-in ── */}
      <WelcomePopup />

      <Navbar links={NAV_LINKS} />
      <main className="page-container">

        {/* Hero banner */}
        <div className="dashboard-hero">
          <div className="hero-greeting">{greeting()}, 🌟</div>
          <div className="hero-name">{user?.firstName} {user?.lastName}</div>
          <div className="hero-subtitle">Ready for your dental visit?</div>
          <Link to="/services" className="hero-action">
            📅 Book Appointment
          </Link>
        </div>

        {/* Stats */}
        <div className="stats-grid" style={{ marginBottom: 'var(--space-8)' }}>
          <div className="stat-card">
            <div className="stat-icon blue">📋</div>
            <div className="stat-info">
              <div className="stat-label">Upcoming Appointments</div>
              <div className="stat-value">{upcoming.length}</div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon green">✓</div>
            <div className="stat-info">
              <div className="stat-label">Completed Visits</div>
              <div className="stat-value">{completedCount}</div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon orange">⏳</div>
            <div className="stat-info">
              <div className="stat-label">Pending Payments</div>
              <div className="stat-value">{pendingCount}</div>
            </div>
          </div>
        </div>

        {/* Quick actions */}
        <div className="quick-actions" style={{ marginBottom: 'var(--space-8)' }}>
          <Link to="/services" className="quick-action-btn">
            <div className="quick-action-icon">🦷</div>
            <span className="quick-action-label">Services</span>
          </Link>
          <Link to="/my-appointments" className="quick-action-btn">
            <div className="quick-action-icon">📅</div>
            <span className="quick-action-label">Appointments</span>
          </Link>
          <Link to="/contact" className="quick-action-btn">
            <div className="quick-action-icon">💬</div>
            <span className="quick-action-label">Contact</span>
          </Link>
        </div>

        {/* Two-column layout */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-6)' }} className="dashboard-cols">

          {/* Upcoming */}
          <div className="card">
            <div className="card-header">
              <span className="card-title">Upcoming Appointments</span>
              <Link to="/my-appointments" style={{ fontSize: 'var(--text-sm)', color: 'var(--primary)', fontWeight: 'var(--font-medium)' }}>
                View All →
              </Link>
            </div>
            <div className="card-body" style={{ padding: upcoming.length ? 'var(--space-4)' : 'var(--space-6)', display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
              {loading ? (
                <div className="loading-container"><div className="spinner" /></div>
              ) : upcoming.length === 0 ? (
                <div className="empty-state">
                  <span className="empty-icon">📅</span>
                  <div className="empty-title">No upcoming appointments</div>
                  <div className="empty-text">Book a service to get started</div>
                </div>
              ) : upcoming.slice(0, 3).map(a => (
                <div key={a.id} className="upcoming-card">
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 'var(--space-2)' }}>
                    <div style={{ fontWeight: 'var(--font-semibold)', color: 'var(--gray-900)', fontSize: 'var(--text-sm)' }}>{a.serviceName}</div>
                    <StatusBadge status={a.status} />
                  </div>
                  <div style={{ fontSize: 'var(--text-sm)', color: 'var(--gray-500)' }}>👨‍⚕️ {a.dentistName}</div>
                  <div style={{ fontSize: 'var(--text-xs)', color: 'var(--gray-400)', marginTop: 'var(--space-2)', display: 'flex', gap: 'var(--space-3)' }}>
                    <span>📅 {formatDate(a.appointmentDatetime)}</span>
                    <span>⏰ {formatTime(a.appointmentDatetime)}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Recent history */}
          <div className="card">
            <div className="card-header">
              <span className="card-title">Recent History</span>
              <Link to="/my-appointments" style={{ fontSize: 'var(--text-sm)', color: 'var(--primary)', fontWeight: 'var(--font-medium)' }}>
                View All →
              </Link>
            </div>
            <div className="card-body" style={{ padding: past.length ? 'var(--space-4)' : 'var(--space-6)', display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
              {loading ? (
                <div className="loading-container"><div className="spinner" /></div>
              ) : past.length === 0 ? (
                <div className="empty-state">
                  <span className="empty-icon">📋</span>
                  <div className="empty-title">No history yet</div>
                  <div className="empty-text">Your completed visits will appear here</div>
                </div>
              ) : past.map(a => (
                <div key={a.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingBottom: 'var(--space-3)', borderBottom: '1px solid var(--gray-100)' }}>
                  <div>
                    <div style={{ fontSize: 'var(--text-sm)', fontWeight: 'var(--font-medium)', color: 'var(--gray-800)' }}>{a.serviceName}</div>
                    <div style={{ fontSize: 'var(--text-xs)', color: 'var(--gray-500)', marginTop: '2px' }}>
                      👨‍⚕️ {a.dentistName} · {formatDate(a.appointmentDatetime)}
                    </div>
                  </div>
                  <StatusBadge status={a.status} />
                </div>
              ))}
            </div>
          </div>
        </div>

      </main>

      <style>{`
        @media (max-width: 768px) {
          .dashboard-cols { grid-template-columns: 1fr !important; }
        }
      `}</style>
    </div>
  );
}