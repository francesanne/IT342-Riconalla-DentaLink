import { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';
import { appointmentsAPI } from '../services/api';
import '../styles/dashboard.css';

const NAV_LINKS = [
  { to: '/admin', label: 'Dashboard' },
  { to: '/admin/services', label: 'Manage Services' },
  { to: '/admin/dentists', label: 'Manage Dentists' },
  { to: '/admin/appointments', label: 'Manage Appointments' },
];

const STATUS_FILTERS = [
  { key: '', label: 'All Status' },
  { key: 'PENDING_PAYMENT', label: 'Pending Payment' },
  { key: 'CONFIRMED', label: 'Confirmed' },
  { key: 'COMPLETED', label: 'Completed' },
  { key: 'CANCELLED', label: 'Cancelled' },
];

function formatDateTime(dt) {
  if (!dt) return '—';
  const d = new Date(dt);
  return d.toLocaleDateString('en-PH', { month: 'short', day: 'numeric', year: 'numeric' }) +
    ' · ' + d.toLocaleTimeString('en-PH', { hour: 'numeric', minute: '2-digit' });
}

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

export default function ManageAppointments() {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');
  const [updating, setUpdating] = useState(null);
  const [openMenuId, setOpenMenuId] = useState(null);

  const load = () => {
    setLoading(true);
    appointmentsAPI.getAll(statusFilter || undefined)
      .then(r => setAppointments(r.data.data || []))
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(load, [statusFilter]);

  const handleStatusUpdate = async (id, newStatus) => {
    setUpdating(id); setOpenMenuId(null);
    try {
      await appointmentsAPI.updateStatus(id, newStatus);
      load();
    } catch (err) {
      alert(err.response?.data?.error?.message || 'Failed to update status.');
    } finally { setUpdating(null); }
  };

  return (
    <div className="app-layout">
      <Navbar links={NAV_LINKS} />
      <main className="page-container">
        <div className="page-header">
          <h1 className="page-title">Manage Appointments</h1>
          <p className="page-subtitle">View and update all patient appointments</p>
        </div>

        {/* Filters */}
        <div style={{ display: 'flex', gap: 'var(--space-3)', marginBottom: 'var(--space-6)', flexWrap: 'wrap', alignItems: 'center' }}>
          <div style={{ display: 'flex', gap: 'var(--space-2)' }}>
            {STATUS_FILTERS.map(f => (
              <button
                key={f.key}
                className={`filter-chip${statusFilter === f.key ? ' active' : ''}`}
                onClick={() => setStatusFilter(f.key)}
              >
                {f.label}
              </button>
            ))}
          </div>
        </div>

        {loading ? (
          <div className="loading-container"><div className="spinner" /></div>
        ) : (
          <div className="card">
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
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {appointments.length === 0 ? (
                    <tr><td colSpan={7} style={{ textAlign: 'center', padding: 'var(--space-12)', color: 'var(--gray-400)' }}>
                      No appointments found.
                    </td></tr>
                  ) : appointments.map(a => (
                    <tr key={a.id}>
                      <td>
                        <div style={{ fontWeight: 'var(--font-medium)', color: 'var(--primary)' }}>
                          {a.patient?.firstName} {a.patient?.lastName}
                        </div>
                        <div style={{ fontSize: 'var(--text-xs)', color: 'var(--gray-400)' }}>{a.patient?.email}</div>
                      </td>
                      <td style={{ color: 'var(--gray-700)' }}>{a.serviceName || `Service #${a.serviceId}`}</td>
                      <td style={{ color: 'var(--gray-700)' }}>{a.dentistName || `Dentist #${a.dentistId}`}</td>
                      <td style={{ fontSize: 'var(--text-sm)', color: 'var(--gray-600)' }}>{formatDateTime(a.appointmentDatetime)}</td>
                      <td><StatusBadge status={a.status} /></td>
                      <td>
                        <span className={`badge ${a.paymentStatus === 'PAID' ? 'badge-paid' : 'badge-unpaid'}`}>
                          {a.paymentStatus === 'PAID' ? 'Paid' : 'Unpaid'}
                        </span>
                      </td>
                      <td>
                        {updating === a.id ? (
                          <div className="spinner" style={{ width: 18, height: 18, borderWidth: 2 }} />
                        ) : (a.status === 'COMPLETED' || a.status === 'CANCELLED') ? (
                          <span style={{ fontSize: 'var(--text-xs)', color: 'var(--gray-400)' }}>—</span>
                        ) : (
                          <div style={{ position: 'relative' }}>
                            <button
                              className="btn-sm btn-outline-sm"
                              onClick={() => setOpenMenuId(openMenuId === a.id ? null : a.id)}
                              style={{ gap: 4 }}
                            >
                              Update Status <span style={{ fontSize: 10 }}>▼</span>
                            </button>
                            {openMenuId === a.id && (
                              <div style={{
                                position: 'absolute', top: 'calc(100% + 4px)', right: 0,
                                background: 'white', border: '1px solid var(--gray-200)',
                                borderRadius: 'var(--radius-lg)', boxShadow: 'var(--shadow-xl)',
                                zIndex: 50, minWidth: 160, overflow: 'hidden',
                              }}>
                                {a.status !== 'COMPLETED' && (
                                  <button
                                    style={{ display: 'block', width: '100%', padding: 'var(--space-3) var(--space-4)', textAlign: 'left', border: 'none', background: 'none', cursor: 'pointer', fontSize: 'var(--text-sm)', color: 'var(--gray-700)', transition: 'background 0.15s' }}
                                    onMouseEnter={e => e.currentTarget.style.background = 'var(--success-light)'}
                                    onMouseLeave={e => e.currentTarget.style.background = 'none'}
                                    onClick={() => handleStatusUpdate(a.id, 'COMPLETED')}
                                  >
                                    ✓ Mark Completed
                                  </button>
                                )}
                                <button
                                  style={{ display: 'block', width: '100%', padding: 'var(--space-3) var(--space-4)', textAlign: 'left', border: 'none', background: 'none', cursor: 'pointer', fontSize: 'var(--text-sm)', color: 'var(--danger)', transition: 'background 0.15s' }}
                                  onMouseEnter={e => e.currentTarget.style.background = 'var(--danger-light)'}
                                  onMouseLeave={e => e.currentTarget.style.background = 'none'}
                                  onClick={() => handleStatusUpdate(a.id, 'CANCELLED')}
                                >
                                  ✕ Mark Cancelled
                                </button>
                              </div>
                            )}
                          </div>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </main>

      {/* Close dropdown on outside click */}
      {openMenuId !== null && (
        <div
          style={{ position: 'fixed', inset: 0, zIndex: 40 }}
          onClick={() => setOpenMenuId(null)}
        />
      )}
    </div>
  );
}