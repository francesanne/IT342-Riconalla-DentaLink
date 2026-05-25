import { useState, useEffect } from 'react';
import Navbar from '@/shared/components/Navbar';
import StatusBadge from '@/shared/components/StatusBadge';
import { formatDateTime } from '@/shared/utils/formatters';
import { appointmentsAPI } from '@/shared/api/api';
import { toast } from 'sonner';
import { Check, X, CalendarDays } from 'lucide-react';
import '@/features/dashboard/styles/dashboard.css';

const NAV_LINKS = [
  { to: '/admin', label: 'Dashboard', end: true },
  { to: '/admin/services', label: 'Manage Services' },
  { to: '/admin/dentists', label: 'Manage Dentists' },
  { to: '/admin/appointments', label: 'Manage Appointments' },
  { to: '/admin/payments', label: 'Payments' },
];

const STATUS_FILTERS = [
  { key: '', label: 'All Status' },
  { key: 'PENDING_PAYMENT', label: 'Pending Payment' },
  { key: 'CONFIRMED', label: 'Confirmed' },
  { key: 'COMPLETED', label: 'Completed' },
  { key: 'CANCELLED', label: 'Cancelled' },
];


export default function ManageAppointments() {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');
  const [updating, setUpdating] = useState(null);
  const [openMenuId, setOpenMenuId] = useState(null);
  const [menuPos, setMenuPos] = useState({ top: 0, right: 0 });

  const load = () => {
    setLoading(true);
    appointmentsAPI.getAll(statusFilter || undefined)
      .then(r => setAppointments(r.data.data || []))
      .catch(() => toast.error('Failed to load appointments. Please refresh.'))
      .finally(() => setLoading(false));
  };

  useEffect(load, [statusFilter]);

  const handleStatusUpdate = async (id, newStatus) => {
    setUpdating(id); setOpenMenuId(null);
    try {
      const res = await appointmentsAPI.updateStatus(id, newStatus);
      const updated = res.data.data;
      setAppointments(prev => prev.map(a => a.id === id ? updated : a));
      toast.success(`Appointment marked as ${newStatus.toLowerCase()}.`);
    } catch (err) {
      toast.error(err.response?.data?.error?.message || 'Failed to update appointment status.');
    } finally { setUpdating(null); }
  };

  const openMenu = (e, id) => {
    if (openMenuId === id) { setOpenMenuId(null); return; }
    const rect = e.currentTarget.getBoundingClientRect();
    setMenuPos({ top: rect.bottom + 4, right: window.innerWidth - rect.right });
    setOpenMenuId(id);
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

        <div className="card">
          <div className="table-wrapper">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Patient</th>
                  <th>Service</th>
                  <th className="col-hide-tablet">Dentist</th>
                  <th>Date & Time</th>
                  <th>Status</th>
                  <th>Payment</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  [...Array(6)].map((_, i) => (
                    <tr key={i}>
                      <td>
                        <div className="skeleton skeleton-text" style={{ width: '70%', marginBottom: 5 }} />
                        <div className="skeleton skeleton-text-sm" style={{ width: '55%' }} />
                      </td>
                      <td><div className="skeleton skeleton-text" style={{ width: '65%' }} /></td>
                      <td className="col-hide-tablet"><div className="skeleton skeleton-text" style={{ width: '58%' }} /></td>
                      <td><div className="skeleton skeleton-text" style={{ width: '62%' }} /></td>
                      <td><div className="skeleton skeleton-badge" /></td>
                      <td><div className="skeleton skeleton-badge" /></td>
                      <td><div className="skeleton skeleton-text" style={{ width: 100 }} /></td>
                    </tr>
                  ))
                ) : appointments.length === 0 ? (
                  <tr>
                    <td colSpan={7}>
                      <div className="empty-state" style={{ padding: 'var(--space-10) var(--space-8)' }}>
                        <div className="empty-icon"><CalendarDays size={28} /></div>
                        <div className="empty-title" style={{ fontSize: 'var(--text-base)' }}>No appointments found</div>
                        <div className="empty-text">Try a different status filter or check back later.</div>
                      </div>
                    </td>
                  </tr>
                ) : appointments.map(a => (
                    <tr key={a.id}>
                      <td>
                        <div style={{ fontWeight: 'var(--font-medium)', color: 'var(--primary)' }}>
                          {[a.patient?.firstName, a.patient?.lastName].filter(Boolean).join(' ') || '—'}
                        </div>
                        <div style={{ fontSize: 'var(--text-xs)', color: 'var(--gray-400)' }}>
                          {a.patient?.email || ''}
                        </div>
                      </td>
                      <td style={{ color: 'var(--gray-700)' }}>{a.serviceName || `Service #${a.serviceId}`}</td>
                      <td className="col-hide-tablet" style={{ color: 'var(--gray-700)' }}>{a.dentistName || `Dentist #${a.dentistId}`}</td>
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
                          <button
                            className="btn-sm btn-outline-sm"
                            onClick={(e) => openMenu(e, a.id)}
                            style={{ gap: 4 }}
                          >
                            Update Status <span style={{ fontSize: 10 }}>▼</span>
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
      </main>

      {/* Dropdown rendered in a fixed portal — escapes overflow:hidden on .card and .table-wrapper */}
      {openMenuId !== null && (
        <>
          <div
            style={{ position: 'fixed', inset: 0, zIndex: 40 }}
            onClick={() => setOpenMenuId(null)}
          />
          <div style={{
            position: 'fixed',
            top: menuPos.top,
            right: menuPos.right,
            background: 'white',
            border: '1px solid var(--gray-200)',
            borderRadius: 'var(--radius-lg)',
            boxShadow: 'var(--shadow-xl)',
            zIndex: 50,
            minWidth: 160,
            overflow: 'hidden',
          }}>
            {appointments.find(a => a.id === openMenuId)?.status !== 'COMPLETED' && (
              <button
                className="dropdown-action dropdown-action-success"
                onClick={() => handleStatusUpdate(openMenuId, 'COMPLETED')}
              >
                <Check size={13} /> Mark Completed
              </button>
            )}
            <button
              className="dropdown-action dropdown-action-danger"
              onClick={() => handleStatusUpdate(openMenuId, 'CANCELLED')}
            >
              <X size={13} /> Mark Cancelled
            </button>
          </div>
        </>
      )}
    </div>
  );
}