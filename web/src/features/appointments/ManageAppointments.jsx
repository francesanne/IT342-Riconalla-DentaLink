import { useState, useEffect } from 'react';
import Navbar from '@/shared/components/Navbar';
import StatusBadge from '@/shared/components/StatusBadge';
import { formatDateTime } from '@/shared/utils/formatters';
import { appointmentsAPI } from '@/shared/api/api';
import { toast } from 'sonner';
import { Check, X, CalendarDays, AlertTriangle, HelpCircle } from 'lucide-react';
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
  const [cancelModal, setCancelModal] = useState({ open: false, id: null, isPaid: false });

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

  const handleCancelClick = (id) => {
    const appt = appointments.find(a => a.id === id);
    setCancelModal({ open: true, id, isPaid: appt?.paymentStatus === 'PAID' });
    setOpenMenuId(null);
  };

  const confirmCancel = () => {
    handleStatusUpdate(cancelModal.id, 'CANCELLED');
    setCancelModal({ open: false, id: null, isPaid: false });
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
            {(() => {
              const appt = appointments.find(a => a.id === openMenuId);
              return appt?.status !== 'COMPLETED' && appt?.paymentStatus === 'PAID' ? (
                <button
                  className="dropdown-action dropdown-action-success"
                  onClick={() => handleStatusUpdate(openMenuId, 'COMPLETED')}
                >
                  <Check size={13} /> Mark Completed
                </button>
              ) : appt?.status !== 'COMPLETED' && appt?.paymentStatus !== 'PAID' ? (
                <div className="dropdown-action dropdown-action-success" style={{
                  opacity: 0.38,
                  cursor: 'not-allowed',
                  pointerEvents: 'none',
                }}>
                  <Check size={13} /> Mark Completed
                </div>
              ) : null;
            })()}
            <button
              className="dropdown-action dropdown-action-danger"
              onClick={() => handleCancelClick(openMenuId)}
            >
              <X size={13} /> Mark Cancelled
            </button>
          </div>
        </>
      )}
      {/* Cancel confirmation modal */}
      {cancelModal.open && (
        <div style={{
          position: 'fixed', inset: 0,
          background: 'rgba(17,24,39,0.5)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          zIndex: 200,
        }}>
          <div style={{
            background: 'white',
            borderRadius: '20px',
            padding: '40px 36px 32px',
            maxWidth: '420px',
            width: '90%',
            boxShadow: '0 8px 40px rgba(0,0,0,0.18)',
            textAlign: 'center',
          }}>

            {/* Icon */}
            <div style={{
              width: 68, height: 68, borderRadius: '50%',
              background: cancelModal.isPaid
                ? 'linear-gradient(135deg, rgba(239,68,68,0.12), rgba(239,68,68,0.06))'
                : 'linear-gradient(135deg, rgba(251,191,36,0.15), rgba(251,191,36,0.06))',
              border: cancelModal.isPaid
                ? '2px solid rgba(239,68,68,0.2)'
                : '2px solid rgba(251,191,36,0.25)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              margin: '0 auto 20px',
            }}>
              {cancelModal.isPaid
                ? <AlertTriangle size={28} color="#ef4444" strokeWidth={2} />
                : <HelpCircle size={28} color="#d97706" strokeWidth={2} />}
            </div>

            {/* Title */}
            <h3 style={{
              margin: '0 0 10px',
              fontSize: '20px',
              fontWeight: 700,
              color: 'var(--gray-900)',
              fontFamily: 'var(--font-display)',
            }}>
              Cancel Appointment
            </h3>

            {/* Message */}
            {cancelModal.isPaid ? (
              <>
                <p style={{ fontSize: '14px', color: 'var(--gray-600)', lineHeight: 1.65, margin: '0 0 10px' }}>
                  This appointment has already been{' '}
                  <strong style={{ color: '#ef4444' }}>paid</strong>.
                  Cancelling will <strong>not</strong> issue an automatic refund.
                </p>
                <p style={{ fontSize: '13px', color: 'var(--gray-500)', lineHeight: 1.6, margin: 0 }}>
                  Please coordinate with the patient directly to arrange a manual refund.
                </p>
              </>
            ) : (
              <p style={{ fontSize: '14px', color: 'var(--gray-600)', lineHeight: 1.65, margin: 0 }}>
                Are you sure you want to cancel this appointment?
                This action <strong>cannot be undone</strong>.
              </p>
            )}

            {/* Divider */}
            <div style={{ height: 1, background: 'var(--gray-100)', margin: '24px 0 20px' }} />

            {/* Buttons */}
            <div style={{ display: 'flex', gap: '10px', justifyContent: 'center' }}>
              <button
                onClick={() => setCancelModal({ open: false, id: null, isPaid: false })}
                style={{
                  height: 42, padding: '0 22px',
                  background: 'white', color: 'var(--gray-600)',
                  border: '1.5px solid var(--gray-200)',
                  borderRadius: '10px', fontWeight: 600,
                  fontSize: '13px', cursor: 'pointer',
                  transition: 'all 0.15s',
                }}
              >
                Go Back
              </button>
              <button
                onClick={confirmCancel}
                style={{
                  height: 42, padding: '0 22px',
                  background: 'linear-gradient(135deg, #ef4444, #dc2626)',
                  color: 'white', border: 'none',
                  borderRadius: '10px', fontWeight: 600,
                  fontSize: '13px', cursor: 'pointer',
                  boxShadow: '0 2px 8px rgba(239,68,68,0.3)',
                  transition: 'all 0.15s',
                }}
              >
                Yes, Cancel Appointment
              </button>
            </div>

          </div>
        </div>
      )}
    </div>
  );
}