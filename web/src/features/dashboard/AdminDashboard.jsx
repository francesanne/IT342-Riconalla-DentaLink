import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { AlertCircle, CalendarDays, Clock, CheckCircle2, Banknote } from 'lucide-react';
import Navbar from '@/shared/components/Navbar';
import StatusBadge from '@/shared/components/StatusBadge';
import { formatDateTime as formatDate, formatPeso } from '@/shared/utils/formatters';
import { adminAPI } from '@/shared/api/api';
import WelcomePopup from '@/shared/components/WelcomePopup';
import './styles/dashboard.css';

const NAV_LINKS = [
  { to: '/admin',                  label: 'Dashboard', end: true },
  { to: '/admin/services',         label: 'Manage Services' },
  { to: '/admin/dentists',         label: 'Manage Dentists' },
  { to: '/admin/appointments',     label: 'Manage Appointments' },
  { to: '/admin/payments',         label: 'Payments' },
];


export default function AdminDashboard() {
  const [stats, setStats]   = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    adminAPI.dashboard()
      .then(res => setStats(res.data.data))
      .catch(() => setError('Unable to load dashboard data. Please refresh the page.'))
      .finally(() => setLoading(false));
  }, []);

  const today = new Date().toLocaleDateString('en-PH', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' });

  return (
    <div className="app-layout">
      {/* ── Welcome popup — only shows once right after login ── */}
      <WelcomePopup />

      <Navbar links={NAV_LINKS} />
      <main className="page-container">

        {/* Header */}
        <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
          <div>
            <h1 className="page-title">Dashboard</h1>
            <p className="page-subtitle">{today}</p>
          </div>
        </div>

        {error && (
          <div className="error-banner">
            <AlertCircle size={16} /> {error}
          </div>
        )}

        {loading ? (
          <div className="loading-container"><div className="spinner" /></div>
        ) : (
          <>
            {/* Stats */}
            <div className="stats-grid">
              <div className="stat-card">
                <div className="stat-icon blue"><CalendarDays size={20} /></div>
                <div className="stat-info">
                  <div className="stat-label">Total Appointments</div>
                  <div className="stat-value">{stats?.totalAppointments ?? 0}</div>
                </div>
              </div>
              <div className="stat-card">
                <div className="stat-icon orange"><Clock size={20} /></div>
                <div className="stat-info">
                  <div className="stat-label">Pending Payments</div>
                  <div className="stat-value">{stats?.pendingPayments ?? 0}</div>
                </div>
              </div>
              <div className="stat-card">
                <div className="stat-icon teal"><CheckCircle2 size={20} /></div>
                <div className="stat-info">
                  <div className="stat-label">Confirmed (Total)</div>
                  <div className="stat-value">{stats?.confirmedAppointments ?? 0}</div>
                </div>
              </div>
              <div className="stat-card">
                <div className="stat-icon green"><Banknote size={20} /></div>
                <div className="stat-info">
                  <div className="stat-label">Total Revenue</div>
                  <div className="stat-value" style={{ fontSize: 'var(--text-xl)' }}>{formatPeso(stats?.totalRevenue)}</div>
                </div>
              </div>
            </div>

            {/* Recent Appointments */}
            <div className="card">
              <div className="card-header">
                <span className="card-title">Recent Appointments</span>
                <Link to="/admin/appointments" style={{ fontSize: 'var(--text-sm)', color: 'var(--primary)', fontWeight: 'var(--font-medium)' }}>
                  View All →
                </Link>
              </div>
              <div className="table-wrapper">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Patient</th>
                      <th>Service</th>
                      <th>Dentist</th>
                      <th>Date & Time</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {(stats?.recentAppointments ?? []).length === 0 ? (
                      <tr><td colSpan={5} style={{ textAlign: 'center', padding: 'var(--space-8)', color: 'var(--gray-400)' }}>No appointments yet</td></tr>
                    ) : (stats?.recentAppointments ?? []).map(a => (
                      <tr key={a.id}>
                        <td>
                          <div style={{ fontWeight: 'var(--font-medium)', color: 'var(--gray-900)' }}>
                            {a.patientName}
                          </div>
                        </td>
                        <td style={{ color: 'var(--gray-700)' }}>{a.serviceName ?? '—'}</td>
                        <td style={{ color: 'var(--gray-700)' }}>{a.dentistName}</td>
                        <td style={{ color: 'var(--gray-600)', fontSize: 'var(--text-sm)' }}>{formatDate(a.appointmentDatetime)}</td>
                        <td><StatusBadge status={a.appointmentStatus} /></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </>
        )}
      </main>
    </div>
  );
}