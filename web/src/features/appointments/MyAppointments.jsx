import { useState, useEffect } from 'react';
import Navbar from '@/shared/components/Navbar';
import { formatDate, formatTime } from '@/shared/utils/formatters';
import { appointmentsAPI, paymentsAPI } from '@/shared/api/api';
import { CalendarDays } from 'lucide-react';
import { toast } from 'sonner';
import StatusBadge from '@/shared/components/StatusBadge';
import '@/features/dashboard/styles/dashboard.css';

const NAV_LINKS = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/services', label: 'Services' },
  { to: '/my-appointments', label: 'My Appointments' },
  { to: '/profile',         label: 'Profile' },
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

  useEffect(() => {
    appointmentsAPI
      .getAll()
      .then(res => setAppointments(res.data.data || []))
      .catch(() => toast.error('Failed to load appointments. Please refresh the page.'))
      .finally(() => setLoading(false));
  }, []);

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
                        {a.paymentStatus === 'UNPAID' && a.status !== 'CANCELLED' && (
                          <button
                            className="btn-sm btn-primary-sm"
                            onClick={() => handlePay(a.id)}
                          >
                            Pay Now
                          </button>
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
    </div>
  );
}