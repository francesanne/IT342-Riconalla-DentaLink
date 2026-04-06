import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { adminAPI } from '../services/api';
import '../styles/dashboard.css';

const NAV_LINKS = [
  { to: '/admin', label: 'Dashboard' },
  { to: '/admin/services', label: 'Manage Services' },
  { to: '/admin/dentists', label: 'Manage Dentists' },
  { to: '/admin/appointments', label: 'Manage Appointments' },
];

function StatusBadge({ status }) {
  const map = {
    PENDING_PAYMENT: ['badge-pending', 'Pending Payment'],
    CONFIRMED: ['badge-confirmed', 'Confirmed'],
    COMPLETED: ['badge-completed', 'Completed'],
    CANCELLED: ['badge-cancelled', 'Cancelled'],
  };
  const [cls, label] = map[status] || ['badge-pending', status];
  return <span className={`badge ${cls}`}>{label}</span>;
}

function formatDate(dt) {
  if (!dt) return '—';
  return new Date(dt).toLocaleDateString('en-PH', { month: 'short', day: 'numeric', year: 'numeric', hour: 'numeric', minute: '2-digit' });
}
function formatPeso(n) {
  if (!n && n !== 0) return '₱0.00';
  return `₱${Number(n).toLocaleString('en-PH', { minimumFractionDigits: 2 })}`;
}

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    adminAPI.dashboard()
      .then(res => setStats(res.data.data))
      .catch(() => {})
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

        {loading ? (
          <div className="loading-container"><div className="spinner" /></div>
        ) : (
          <>
            {/* Stats */}
            <div className="stats-grid">
              <div className="stat-card">
                <div className="stat-icon blue">📋</div>
                <div className="stat-info">
                  <div className="stat-label">Total Appointments</div>
                  <div className="stat-value">{stats?.totalAppointments ?? 0}</div>
                </div>
              </div>
              <div className="stat-card">
                <div className="stat-icon orange">⏳</div>
                <div className="stat-info">
                  <div className="stat-label">Pending Payments</div>
                  <div className="stat-value">{stats?.pendingPayments ?? 0}</div>
                </div>
              </div>
              <div className="stat-card">
                <div className="stat-icon teal">✓</div>
                <div className="stat-info">
                  <div className="stat-label">Confirmed Today</div>
                  <div className="stat-value">{stats?.confirmedAppointments ?? 0}</div>
                </div>
              </div>
              <div className="stat-card">
                <div className="stat-icon green">₱</div>
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
                      <th>Payment</th>
                    </tr>
                  </thead>
                  <tbody>
                    {(stats?.recentAppointments ?? []).length === 0 ? (
                      <tr><td colSpan={6} style={{ textAlign: 'center', padding: 'var(--space-8)', color: 'var(--gray-400)' }}>No appointments yet</td></tr>
                    ) : (stats?.recentAppointments ?? []).map(a => (
                      <tr key={a.appointmentId}>
                        <td>
                          <div style={{ fontWeight: 'var(--font-medium)', color: 'var(--gray-900)' }}>
                            {a.patient?.firstName} {a.patient?.lastName}
                          </div>
                        </td>
                        <td style={{ color: 'var(--gray-700)' }}>{a.serviceName || `Service #${a.serviceId}`}</td>
                        <td style={{ color: 'var(--gray-700)' }}>{a.dentistName || `Dentist #${a.dentistId}`}</td>
                        <td style={{ color: 'var(--gray-600)', fontSize: 'var(--text-sm)' }}>{formatDate(a.appointmentDatetime)}</td>
                        <td><StatusBadge status={a.appointmentStatus} /></td>
                        <td>
                          <span className={`badge ${a.paymentStatus === 'PAID' ? 'badge-paid' : 'badge-unpaid'}`}>
                            {a.paymentStatus === 'PAID' ? 'Paid' : 'Unpaid'}
                          </span>
                        </td>
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
