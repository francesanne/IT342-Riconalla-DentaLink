import { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';
import { paymentsAPI } from '../services/api';
import '../styles/dashboard.css';

const NAV_LINKS = [
  { to: '/admin',                  label: 'Dashboard' },
  { to: '/admin/services',         label: 'Manage Services' },
  { to: '/admin/dentists',         label: 'Manage Dentists' },
  { to: '/admin/appointments',     label: 'Manage Appointments' },
  { to: '/admin/payments',         label: 'Payments' },
];

function formatPeso(amount) {
  if (amount == null) return '—';
  return `₱${Number(amount).toLocaleString('en-PH', { minimumFractionDigits: 2 })}`;
}

function formatDate(dt) {
  if (!dt) return '—';
  return new Date(dt).toLocaleDateString('en-PH', {
    month: 'short', day: 'numeric', year: 'numeric',
  });
}

function StatusBadge({ status }) {
  const map = {
    PAID:   ['badge-confirmed', 'Paid'],
    FAILED: ['badge-cancelled', 'Failed'],
    UNPAID: ['badge-pending',   'Unpaid'],
  };
  const [cls, label] = map[status] ?? ['badge-pending', status];
  return <span className={`badge ${cls}`}>{label}</span>;
}

export default function AdminPayments() {
  const [payments, setPayments] = useState([]);
  const [loading,  setLoading]  = useState(true);
  const [error,    setError]    = useState('');

  useEffect(() => {
    paymentsAPI.getAll()
      .then(res => setPayments(res.data.data ?? []))
      .catch(() => setError('Failed to load payment records.'))
      .finally(() => setLoading(false));
  }, []);

  const totalRevenue = payments
    .filter(p => p.paymentStatus === 'PAID')
    .reduce((sum, p) => sum + Number(p.paymentAmount ?? 0), 0);

  return (
    <div className="app-layout">
      <Navbar links={NAV_LINKS} />

      <main className="page-container">
        {/* Header */}
        <div className="page-header">
          <h1 className="page-title">Payment Records</h1>
          <p className="page-subtitle">
            Records are updated automatically via PayMongo webhook. No manual edits permitted.
          </p>
        </div>

        {/* Summary stats */}
        <div className="stats-grid" style={{ marginBottom: 24 }}>
          <div className="stat-card">
            <div className="stat-label">Total Payments</div>
            <div className="stat-value">{payments.length}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Paid</div>
            <div className="stat-value" style={{ color: 'var(--primary)' }}>
              {payments.filter(p => p.paymentStatus === 'PAID').length}
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Total Revenue</div>
            <div className="stat-value" style={{ color: 'var(--primary)' }}>
              {formatPeso(totalRevenue)}
            </div>
          </div>
        </div>

        {/* Table */}
        <div className="card">
          {loading && (
            <div style={{ padding: 40, textAlign: 'center' }}>
              <div className="spinner" style={{ margin: '0 auto 12px' }} />
              <p style={{ color: 'var(--gray-400)' }}>Loading payment records…</p>
            </div>
          )}

          {!loading && error && (
            <div className="error-banner"><span>⚠</span> {error}</div>
          )}

          {!loading && !error && (
            <div className="table-wrapper">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Payment ID</th>
                    <th>Appointment</th>
                    <th>Patient</th>
                    <th>Amount</th>
                    <th>PayMongo Ref</th>
                    <th>Status</th>
                    <th>Date</th>
                  </tr>
                </thead>
                <tbody>
                  {payments.length === 0 ? (
                    <tr>
                      <td colSpan={7} style={{ textAlign: 'center', padding: 'var(--space-8)', color: 'var(--gray-400)' }}>
                        No payment records yet.
                      </td>
                    </tr>
                  ) : (
                    payments.map(p => (
                      <tr key={p.id}>
                        <td style={{ color: 'var(--gray-400)', fontSize: 'var(--text-sm)', fontFamily: 'monospace' }}>
                          #{p.id}
                        </td>
                        <td style={{ color: 'var(--gray-600)' }}>
                          Appt #{p.appointmentId}
                        </td>
                        <td>
                          <div style={{ fontWeight: 'var(--font-medium)' }}>
                            {p.patient ? `${p.patient.firstName} ${p.patient.lastName}` : '—'}
                          </div>
                        </td>
                        <td style={{ fontWeight: 'var(--font-semibold)', color: 'var(--gray-800)' }}>
                          {formatPeso(p.paymentAmount)}
                        </td>
                        <td style={{ fontSize: 'var(--text-xs)', fontFamily: 'monospace', color: 'var(--gray-500)', maxWidth: 180, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                          {p.paymongoPaymentId || '—'}
                        </td>
                        <td>
                          <StatusBadge status={p.paymentStatus} />
                        </td>
                        <td style={{ color: 'var(--gray-600)', fontSize: 'var(--text-sm)' }}>
                          {formatDate(p.paymentCreatedAt)}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}