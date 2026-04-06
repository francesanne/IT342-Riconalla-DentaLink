import { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';
import { appointmentsAPI } from '../services/api';
import '../styles/dashboard.css';

const NAV_LINKS = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/services', label: 'Services' },
  { to: '/my-appointments', label: 'My Appointments' },
];

const FILTERS = [
  { key: 'ALL', label: 'All' },
  { key: 'CONFIRMED', label: 'Confirmed' },
  { key: 'PENDING_PAYMENT', label: 'Pending Payment' },
  { key: 'COMPLETED', label: 'Completed' },
  { key: 'CANCELLED', label: 'Cancelled' },
];

function formatDate(dt) {
  if (!dt) return '—';
  return new Date(dt).toLocaleDateString('en-PH', {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });
}

function formatTime(dt) {
  if (!dt) return '';
  return new Date(dt).toLocaleTimeString('en-PH', {
    hour: 'numeric',
    minute: '2-digit',
  });
}

export default function MyAppointments() {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');

  useEffect(() => {
    appointmentsAPI
      .getAll()
      .then(res => setAppointments(res.data.data || []))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const filtered =
    filter === 'ALL'
      ? appointments
      : appointments.filter(a => a.status === filter);

  const handlePay = (appointmentId) => {
    alert(`Payment feature coming soon for appointment #${appointmentId}`);
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

        {/* CONTENT */}
        {loading ? (
          <p>Loading...</p>
        ) : filtered.length === 0 ? (
          <div className="empty-state">
            <span className="empty-icon">📅</span>
            <div className="empty-title">No appointments yet</div>
            <div className="empty-text">Book your first appointment.</div>
          </div>
        ) : (
          <div className="table-container">
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