import { useState, useEffect } from 'react';
import Navbar from '@/shared/components/Navbar';
import { formatDate, formatTime } from '@/shared/utils/formatters';
import { appointmentsAPI, paymentsAPI } from '@/shared/api/api';
import { CalendarDays, HelpCircle } from 'lucide-react';
import { toast } from 'sonner';
import StatusBadge from '@/shared/components/StatusBadge';
import '@/features/dashboard/styles/dashboard.css';

const NAV_LINKS = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/services', label: 'Services' },
  { to: '/my-appointments', label: 'My Appointments' },
  { to: '/contact',         label: 'Contact' },
];

const FILTERS = [
  { key: 'ALL', label: 'All' },
  { key: 'CONFIRMED', label: 'Confirmed' },
  { key: 'PENDING_PAYMENT', label: 'Pending Payment' },
  { key: 'COMPLETED', label: 'Completed' },
  { key: 'CANCELLED', label: 'Cancelled' },
];


export default function MyAppointments() {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');
  const [cancelConfirm, setCancelConfirm] = useState(null); // stores appointment id pending confirmation

  // Extracted so handleCancel can re-fetch without toggling the loading spinner
  const loadAppointments = async () => {
    const res = await appointmentsAPI.getAll();
    setAppointments(res.data.data || []);
  };

  useEffect(() => {
    setLoading(true);
    loadAppointments()
      .catch(() => toast.error('Failed to load appointments. Please refresh the page.'))
      .finally(() => setLoading(false));
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const filtered =
    filter === 'ALL'
      ? appointments
      : appointments.filter(a => a.status === filter);

  const handlePay = async (appointmentId) => {
    try {
      const res = await paymentsAPI.createIntent({ appointmentId });
      const checkoutUrl = res.data.data?.checkoutUrl;
      if (checkoutUrl) {
        window.location.href = checkoutUrl;
      }
    } catch (err) {
      toast.error(err.response?.data?.error?.message || 'Failed to initiate payment. Please try again.');
    }
  };

  const handleCancel = async (appointmentId) => {
    try {
      await appointmentsAPI.cancel(appointmentId);
      toast.success('Appointment cancelled.');
      // Re-fetch the full list so filtered views update correctly
      // (optimistic update would leave the row visible in the PENDING_PAYMENT filter)
      await loadAppointments();
    } catch (err) {
      toast.error(err.response?.data?.error?.message || 'Failed to cancel appointment.');
    } finally {
      setCancelConfirm(null);
    }
  };

  return (
    <div className="app-layout">
      <Navbar links={NAV_LINKS} />

      <main className="page-container">
        <div className="page-header">
          <h1 className="page-title">My Appointments</h1>
          <p className="page-subtitle">View and track your dental appointments</p>
        </div>

        {/* FILTERS */}
        <div className="filters-bar">
          {FILTERS.map(f => (
            <button
              key={f.key}
              className={`filter-chip${filter === f.key ? ' active' : ''}`}
              onClick={() => setFilter(f.key)}
            >
              {f.label}
            </button>
          ))}
        </div>

        {/* CONTENT */}
        {loading ? (
          <div className="loading-container"><div className="spinner" /></div>
        ) : filtered.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon"><CalendarDays size={36} /></div>
            <div className="empty-title">No appointments found</div>
            <div className="empty-text">
              {filter === 'ALL' ? 'Book your first appointment to get started.' : 'No appointments match this filter.'}
            </div>
          </div>
        ) : (
          <div className="card">
            <div className="table-wrapper">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Service</th>
                    <th className="col-hide-md">Dentist</th>
                    <th>Date</th>
                    <th className="col-hide-md">Time</th>
                    <th>Status</th>
                    <th>Payment</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {filtered.map(a => (
                    <tr key={a.id}>
                      <td>
                        <div style={{ fontWeight: 'var(--font-medium)', color: 'var(--gray-900)' }}>{a.serviceName}</div>
                      </td>
                      <td className="col-hide-md" style={{ color: 'var(--gray-700)' }}>Dr. {a.dentistName}</td>
                      <td style={{ color: 'var(--gray-600)' }}>{formatDate(a.appointmentDatetime)}</td>
                      <td className="col-hide-md" style={{ color: 'var(--gray-600)' }}>{formatTime(a.appointmentDatetime)}</td>
                      <td><StatusBadge status={a.status} /></td>
                      <td><StatusBadge status={a.paymentStatus} /></td>
                      <td>
                        {a.paymentStatus === 'UNPAID' && a.status === 'PENDING_PAYMENT' && (
                          <div style={{ display: 'flex', gap: 'var(--space-2)' }}>
                            <button className="btn-sm btn-primary-sm" onClick={() => handlePay(a.id)}>
                              Pay Now
                            </button>
                            <button
                              className="btn-sm btn-outline-sm"
                              style={{ color: 'var(--danger)', borderColor: 'var(--danger)' }}
                              onClick={() => setCancelConfirm(a.id)}
                            >
                              Cancel
                            </button>
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

      {/* Cancel confirmation modal */}
      {cancelConfirm !== null && (
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
              background: 'linear-gradient(135deg, rgba(251,191,36,0.15), rgba(251,191,36,0.06))',
              border: '2px solid rgba(251,191,36,0.25)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              margin: '0 auto 20px',
            }}>
              <HelpCircle size={28} color="#d97706" strokeWidth={2} />
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
            <p style={{ fontSize: '14px', color: 'var(--gray-600)', lineHeight: 1.65, margin: 0 }}>
              Are you sure you want to cancel this appointment?
              This action <strong>cannot be undone</strong>.
            </p>

            {/* Divider */}
            <div style={{ height: 1, background: 'var(--gray-100)', margin: '24px 0 20px' }} />

            {/* Buttons */}
            <div style={{ display: 'flex', gap: '10px', justifyContent: 'center' }}>
              <button
                onClick={() => setCancelConfirm(null)}
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
                onClick={() => handleCancel(cancelConfirm)}
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