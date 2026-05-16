import { useState, useEffect } from 'react';
import Navbar from '@/shared/components/Navbar';
import { formatDate, formatTime } from '@/shared/utils/formatters';
import { appointmentsAPI, paymentsAPI } from '@/shared/api/api';
import { AlertCircle, CalendarDays } from 'lucide-react';
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
  const [error, setError] = useState('');

  useEffect(() => {
    appointmentsAPI
      .getAll()
      .then(res => setAppointments(res.data.data || []))
      .catch(() => setError('Failed to load appointments. Please refresh the page.'))
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
      setError(err.response?.data?.error?.message || 'Failed to initiate payment. Please try again.');
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
        <div style={{ display: 'flex', gap: '10px', marginBottom: '20px', flexWrap: 'wrap' }}>
          {FILTERS.map(f => (
            <button
              key={f.key}
              onClick={() => setFilter(f.key)}
              style={{
                background: filter === f.key ? 'var(--primary)' : 'white',
                color: filter === f.key ? 'white' : 'var(--gray-700)',
                border: '1px solid var(--gray-300)',
                borderRadius: '20px',
                padding: '6px 14px',
                cursor: 'pointer'
              }}
            >
              {f.label}
            </button>
          ))}
        </div>

        {error && (
          <div style={{
            background: '#fef2f2',
            border: '1px solid #fecaca',
            borderRadius: '8px',
            padding: '16px 20px',
            color: '#dc2626',
            fontSize: '14px',
            marginBottom: '16px'
          }}>
            <AlertCircle size={16} /> {error}
          </div>
        )}

        {/* CONTENT */}
        {loading ? (
          <p>Loading...</p>
        ) : filtered.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon"><CalendarDays size={36} /></div>
            <div className="empty-title">No appointments yet</div>
            <div className="empty-text">Book your first appointment.</div>
          </div>
        ) : (
          <div className="table-container" style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ textAlign: 'left', borderBottom: '1px solid #ddd' }}>
                  <th>Service</th>
                  <th>Dentist</th>
                  <th>Date</th>
                  <th>Time</th>
                  <th>Status</th>
                  <th>Payment</th>
                  <th>Action</th>
                </tr>
              </thead>

              <tbody>
                {filtered.map(a => (
                  <tr key={a.id} style={{ borderBottom: '1px solid #eee' }}>
                    <td>{a.serviceName}</td>
                    <td>Dr. {a.dentistName}</td>
                    <td>{formatDate(a.appointmentDatetime)}</td>
                    <td>{formatTime(a.appointmentDatetime)}</td>

                    {/* STATUS */}
                    <td style={{
                      color:
                        a.status === 'CONFIRMED' ? 'green' :
                        a.status === 'PENDING_PAYMENT' ? 'orange' :
                        a.status === 'CANCELLED' ? 'red' :
                        'gray'
                    }}>
                      {a.status.replace('_', ' ')}
                    </td>

                    {/* PAYMENT */}
                    <td style={{
                      color: a.paymentStatus === 'PAID' ? 'green' : 'red'
                    }}>
                      {a.paymentStatus}
                    </td>

                    {/* ACTION */}
                    <td>
                      {a.paymentStatus === 'UNPAID' && (
                        <button
                          onClick={() => handlePay(a.id)}
                          style={{
                            background: 'var(--primary)',
                            color: 'white',
                            border: 'none',
                            padding: '6px 12px',
                            borderRadius: '6px',
                            cursor: 'pointer'
                          }}
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
        )}
      </main>
    </div>
  );
}