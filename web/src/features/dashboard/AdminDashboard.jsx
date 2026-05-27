import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { AlertCircle, CalendarDays, Clock, CheckCircle2, Banknote } from 'lucide-react';
import Navbar from '@/shared/components/Navbar';
import StatusBadge from '@/shared/components/StatusBadge';
import { formatDateTime as formatDate, formatPeso } from '@/shared/utils/formatters';
import { adminAPI } from '@/shared/api/api';
import AppointmentStatusChart from './AppointmentStatusChart';
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
          <>
            {/* Skeleton stats */}
            <div className="stats-grid">
              {[...Array(4)].map((_, i) => (
                <div key={i} className="stat-card">
                  <div className="skeleton skeleton-icon" />
                  <div className="stat-info">
                    <div className="skeleton skeleton-text-sm" style={{ width: '62%', marginBottom: 8 }} />
                    <div className="skeleton skeleton-text-lg" style={{ width: '42%' }} />
                  </div>
                </div>
              ))}
            </div>

            {/* Skeleton chart + table row */}
            <div className="dashboard-chart-row">
              <div className="card">
                <div className="card-header">
                  <div className="skeleton skeleton-text" style={{ width: 160 }} />
                </div>
                <div style={{ padding: 'var(--space-6)', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-5)' }}>
                  <div className="skeleton" style={{ width: 160, height: 160, borderRadius: '50%' }} />
                  <div style={{ width: '100%', display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
                    {[...Array(4)].map((_, i) => (
                      <div key={i} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '6px 0' }}>
                        <div className="skeleton skeleton-text" style={{ width: '55%' }} />
                        <div className="skeleton skeleton-text-sm" style={{ width: 40 }} />
                      </div>
                    ))}
                  </div>
                </div>
              </div>
              <div className="card">
                <div className="card-header">
                  <div className="skeleton skeleton-text" style={{ width: 180 }} />
                </div>
                <div className="table-wrapper">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Patient</th><th>Service</th>
                        <th className="col-hide-tablet">Dentist</th>
                        <th>Date & Time</th><th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {[...Array(5)].map((_, i) => (
                        <tr key={i}>
                          <td><div className="skeleton skeleton-text" style={{ width: '75%' }} /></td>
                          <td><div className="skeleton skeleton-text" style={{ width: '65%' }} /></td>
                          <td className="col-hide-tablet"><div className="skeleton skeleton-text" style={{ width: '55%' }} /></td>
                          <td><div className="skeleton skeleton-text" style={{ width: '60%' }} /></td>
                          <td><div className="skeleton skeleton-badge" /></td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </>
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

            {/* Chart + Recent Appointments */}
            <div className="dashboard-chart-row">
              <AppointmentStatusChart stats={stats} />

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
                        <th className="col-hide-tablet">Dentist</th>
                        <th>Date & Time</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {(stats?.recentAppointments ?? []).length === 0 ? (
                        <tr>
                          <td colSpan={5}>
                            <div className="empty-state" style={{ padding: 'var(--space-10) var(--space-8)' }}>
                              <div className="empty-icon"><CalendarDays size={28} /></div>
                              <div className="empty-title" style={{ fontSize: 'var(--text-base)' }}>No appointments yet</div>
                              <div className="empty-text">Appointments will appear here once patients start booking.</div>
                            </div>
                          </td>
                        </tr>
                      ) : (stats?.recentAppointments ?? []).map(a => (
                        <tr key={a.id}>
                          <td>
                            <div style={{ fontWeight: 'var(--font-medium)', color: 'var(--gray-900)' }}>
                              {a.patientName}
                            </div>
                          </td>
                          <td style={{ color: 'var(--gray-700)' }}>{a.serviceName ?? '—'}</td>
                          <td className="col-hide-tablet" style={{ color: 'var(--gray-700)' }}>{a.dentistName ? `Dr. ${a.dentistName}` : '—'}</td>
                          <td style={{ color: 'var(--gray-600)', fontSize: 'var(--text-sm)' }}>{formatDate(a.appointmentDatetime)}</td>
                          <td><StatusBadge status={a.appointmentStatus} /></td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </>
        )}
      </main>
    </div>
  );
}