import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '@/shared/components/Navbar'
import StatusBadge from '@/shared/components/StatusBadge';
import { formatDate, formatTime } from '@/shared/utils/formatters';
import { appointmentsAPI } from '@/shared/api/api';
import { useAuth } from '@/shared/context/AuthContext';
import {
  AlertCircle, CalendarDays, CheckCircle2, Clock,
  UserRound, ClipboardList,
} from 'lucide-react';
import './styles/dashboard.css'

const NAV_LINKS = [
  { to: '/dashboard',       label: 'Dashboard' },
  { to: '/services',        label: 'Services' },
  { to: '/my-appointments', label: 'My Appointments' },
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
    a.status === 'CONFIRMED' &&
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
        <div className="error-banner" style={{ marginTop: 'var(--space-6)' }}>
          <AlertCircle size={16} /> {error}
        </div>
      </main>
    </div>
  );

  return (
    <div className="app-layout">
      <Navbar links={NAV_LINKS} />
      <main className="page-container">

        {/* Hero banner */}
        <div className="dashboard-hero">
          <div className="hero-greeting">{greeting()}</div>
          <div className="hero-name">{user?.firstName} {user?.lastName}</div>
          <div className="hero-subtitle">Ready for your dental visit?</div>
          <Link to="/services" className="hero-action">
            <CalendarDays size={16} /> Book Appointment
          </Link>
        </div>

        {/* Stats */}
        <div className="stats-grid" style={{ marginBottom: 'var(--space-8)' }}>
          <div className="stat-card">
            <div className="stat-icon teal"><CalendarDays size={20} /></div>
            <div className="stat-info">
              <div className="stat-label">Upcoming Appointments</div>
              <div className="stat-value">{upcoming.length}</div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon green"><CheckCircle2 size={20} /></div>
            <div className="stat-info">
              <div className="stat-label">Completed Visits</div>
              <div className="stat-value">{completedCount}</div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon orange"><Clock size={20} /></div>
            <div className="stat-info">
              <div className="stat-label">Pending Payments</div>
              <div className="stat-value">{pendingCount}</div>
            </div>
          </div>
        </div>

        {/* Two-column layout */}
        <div className="dashboard-cols">

          {/* Upcoming */}
          <div className="card">
            <div className="card-header">
              <span className="card-title">Upcoming Appointments</span>
              <Link to="/my-appointments" style={{ fontSize: 'var(--text-sm)', color: 'var(--primary)', fontWeight: 'var(--font-medium)' }}>
                View All
              </Link>
            </div>
            <div className="card-body" style={{ padding: 'var(--space-4)', display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
              {loading ? (
                [...Array(3)].map((_, i) => (
                  <div key={i} className="upcoming-card">
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 'var(--space-2)' }}>
                      <div className="skeleton skeleton-text" style={{ width: '55%' }} />
                      <div className="skeleton skeleton-badge" />
                    </div>
                    <div className="skeleton skeleton-text-sm" style={{ width: '42%', marginTop: 'var(--space-2)' }} />
                    <div style={{ display: 'flex', gap: 'var(--space-3)', marginTop: 'var(--space-2)' }}>
                      <div className="skeleton skeleton-text-sm" style={{ width: 90 }} />
                      <div className="skeleton skeleton-text-sm" style={{ width: 70 }} />
                    </div>
                  </div>
                ))
              ) : upcoming.length === 0 ? (
                <div className="empty-state">
                  <div className="empty-icon"><CalendarDays size={36} /></div>
                  <div className="empty-title">No upcoming appointments</div>
                  <div className="empty-text">Book a service to get started</div>
                </div>
              ) : upcoming.slice(0, 3).map(a => (
                <div key={a.id} className="upcoming-card">
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 'var(--space-2)' }}>
                    <div style={{ fontWeight: 'var(--font-semibold)', color: 'var(--gray-900)', fontSize: 'var(--text-sm)' }}>{a.serviceName}</div>
                    <StatusBadge status={a.status} />
                  </div>
                  <div style={{ fontSize: 'var(--text-sm)', color: 'var(--gray-500)', display: 'flex', alignItems: 'center', gap: 4 }}><UserRound size={13} /> Dr. {a.dentistName}</div>
                  <div style={{ fontSize: 'var(--text-xs)', color: 'var(--gray-400)', marginTop: 'var(--space-2)', display: 'flex', gap: 'var(--space-3)' }}>
                    <span style={{ display: 'flex', alignItems: 'center', gap: 3 }}><CalendarDays size={13} /> {formatDate(a.appointmentDatetime)}</span>
                    <span style={{ display: 'flex', alignItems: 'center', gap: 3 }}><Clock size={13} /> {formatTime(a.appointmentDatetime)}</span>
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
                View All
              </Link>
            </div>
            <div className="card-body" style={{ padding: 'var(--space-4)', display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
              {loading ? (
                [...Array(4)].map((_, i) => (
                  <div key={i} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingBottom: 'var(--space-3)', borderBottom: '1px solid var(--gray-100)' }}>
                    <div>
                      <div className="skeleton skeleton-text" style={{ width: 140, marginBottom: 6 }} />
                      <div className="skeleton skeleton-text-sm" style={{ width: 100 }} />
                    </div>
                    <div className="skeleton skeleton-badge" />
                  </div>
                ))
              ) : past.length === 0 ? (
                <div className="empty-state">
                  <div className="empty-icon"><ClipboardList size={36} /></div>
                  <div className="empty-title">No history yet</div>
                  <div className="empty-text">Your completed visits will appear here</div>
                </div>
              ) : past.map(a => (
                <div key={a.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingBottom: 'var(--space-3)', borderBottom: '1px solid var(--gray-100)' }}>
                  <div>
                    <div style={{ fontSize: 'var(--text-sm)', fontWeight: 'var(--font-medium)', color: 'var(--gray-800)' }}>{a.serviceName}</div>
                    <div style={{ fontSize: 'var(--text-xs)', color: 'var(--gray-500)', marginTop: '2px' }}>
                      <span style={{ display: 'inline-flex', alignItems: 'center', gap: 3 }}><UserRound size={13} /> Dr. {a.dentistName}</span> · {formatDate(a.appointmentDatetime)}
                    </div>
                  </div>
                  <StatusBadge status={a.status} />
                </div>
              ))}
            </div>
          </div>
        </div>

      </main>

    </div>
  );
}