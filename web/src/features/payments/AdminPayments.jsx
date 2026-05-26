import { useState, useEffect } from 'react';
import Navbar from '@/shared/components/Navbar';
import StatusBadge from '@/shared/components/StatusBadge';
import { formatDate, formatPeso } from '@/shared/utils/formatters';
import { paymentsAPI, appointmentsAPI } from '@/shared/api/api';
import { AlertCircle, CreditCard, TrendingUp, Banknote } from 'lucide-react';
import '@/features/dashboard/styles/dashboard.css';

const NAV_LINKS = [
  { to: '/admin',                  label: 'Dashboard', end: true },
  { to: '/admin/services',         label: 'Manage Services' },
  { to: '/admin/dentists',         label: 'Manage Dentists' },
  { to: '/admin/appointments',     label: 'Manage Appointments' },
  { to: '/admin/payments',         label: 'Payments' },
];


export default function AdminPayments() {
  const [payments, setPayments] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [loading,  setLoading]  = useState(true);
  const [error,    setError]    = useState('');
  const [patientFilter, setPatientFilter] = useState('');

  useEffect(() => {
    Promise.all([
      paymentsAPI.getAll(),
      appointmentsAPI.getAll(),
    ])
      .then(([payRes, apptRes]) => {
        setPayments(payRes.data.data ?? []);
        setAppointments(apptRes.data.data ?? []);
      })
      .catch(() => setError('Failed to load payment records.'))
      .finally(() => setLoading(false));
  }, []);

  const patientNames = [...new Set(
    payments
      .filter(p => p.patient)
      .map(p => `${p.patient.firstName} ${p.patient.lastName}`.trim())
  )].sort();

  const filtered = patientFilter
    ? payments.filter(p => `${p.patient?.firstName} ${p.patient?.lastName}`.trim() === patientFilter)
    : payments;

  const totalRevenue = filtered
    .filter(p => p.paymentStatus === 'PAID')
    .reduce((sum, p) => sum + Number(p.paymentAmount ?? 0), 0);

  const unpaidCount = appointments.filter(a => {
    const isPending = a.status === 'PENDING_PAYMENT' && a.paymentStatus === 'UNPAID';
    if (!isPending) return false;
    if (!patientFilter) return true;
    const name = `${a.patient?.firstName ?? ''} ${a.patient?.lastName ?? ''}`.trim();
    return name === patientFilter;
  }).length;

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
        <div className="stats-grid" style={{ marginBottom: 'var(--space-6)' }}>
          <div className="stat-card">
            <div className="stat-icon blue"><CreditCard size={20} /></div>
            <div className="stat-info">
              <div className="stat-label">{patientFilter ? `${patientFilter}'s Payments` : 'Total Payments'}</div>
              <div className="stat-value">{filtered.length}</div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon orange"><TrendingUp size={20} /></div>
            <div className="stat-info">
              <div className="stat-label">{patientFilter ? `${patientFilter}'s Unpaid` : 'Unpaid / Pending'}</div>
              <div className="stat-value">{unpaidCount}</div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon teal"><Banknote size={20} /></div>
            <div className="stat-info">
              <div className="stat-label">{patientFilter ? `${patientFilter}'s Revenue` : 'Total Revenue'}</div>
              <div className="stat-value" style={{ fontSize: 'var(--text-xl)' }}>{formatPeso(totalRevenue)}</div>
            </div>
          </div>
        </div>

        {/* Patient filter */}
        <div style={{ marginBottom: 'var(--space-4)' }}>
          <div className="form-group" style={{ maxWidth: 260 }}>
            <select
              value={patientFilter}
              onChange={e => setPatientFilter(e.target.value)}
            >
              <option value="">All Patients</option>
              {patientNames.map(name => (
                <option key={name} value={name}>{name}</option>
              ))}
            </select>
          </div>
        </div>

        {/* Table */}
        <div className="card">
          {error && (
            <div style={{ padding: 'var(--space-6)' }}>
              <div className="error-banner"><AlertCircle size={16} /> {error}</div>
            </div>
          )}

          <div className="table-wrapper">
            <table className="data-table">
              <thead>
                <tr>
                  <th className="col-hide-tablet">Payment ID</th>
                  <th>Appointment</th>
                  <th>Patient</th>
                  <th>Amount</th>
                  <th className="col-hide-tablet">PayMongo Ref</th>
                  <th>Status</th>
                  <th>Date</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  [...Array(5)].map((_, i) => (
                    <tr key={i}>
                      <td className="col-hide-tablet"><div className="skeleton skeleton-text-sm" style={{ width: 50 }} /></td>
                      <td>
                        <div className="skeleton skeleton-text" style={{ width: '65%', marginBottom: 5 }} />
                        <div className="skeleton skeleton-text-sm" style={{ width: '42%' }} />
                      </td>
                      <td><div className="skeleton skeleton-text" style={{ width: '60%' }} /></td>
                      <td><div className="skeleton skeleton-text" style={{ width: 70 }} /></td>
                      <td className="col-hide-tablet"><div className="skeleton skeleton-text-sm" style={{ width: '75%' }} /></td>
                      <td><div className="skeleton skeleton-badge" /></td>
                      <td><div className="skeleton skeleton-text" style={{ width: 80 }} /></td>
                    </tr>
                  ))
                ) : payments.length === 0 ? (
                  <tr>
                    <td colSpan={7}>
                      <div className="empty-state" style={{ padding: 'var(--space-10) var(--space-8)' }}>
                        <div className="empty-icon"><CreditCard size={28} /></div>
                        <div className="empty-title" style={{ fontSize: 'var(--text-base)' }}>No payment records yet</div>
                        <div className="empty-text">Payments will appear here once patients complete checkout.</div>
                      </div>
                    </td>
                  </tr>
                ) : filtered.length === 0 ? (
                  <tr>
                    <td colSpan={7}>
                      <div className="empty-state" style={{ padding: 'var(--space-10) var(--space-8)' }}>
                        <div className="empty-icon"><CreditCard size={28} /></div>
                        <div className="empty-title" style={{ fontSize: 'var(--text-base)' }}>No payments found</div>
                        <div className="empty-text">No payment records match the selected patient.</div>
                      </div>
                    </td>
                  </tr>
                ) : (
                    filtered.map(p => (
                      <tr key={p.id}>
                        <td className="col-hide-tablet" style={{ color: 'var(--gray-400)', fontSize: 'var(--text-sm)', fontFamily: 'monospace' }}>
                          #{p.id}
                        </td>
                        <td style={{ color: 'var(--gray-700)' }}>
                          <div style={{ fontWeight: 'var(--font-medium)' }}>
                            {p.serviceName || 'Dental Service'}
                          </div>
                          <div style={{ fontSize: 'var(--text-xs)', color: 'var(--gray-400)' }}>
                            Appt #{p.appointmentId}
                          </div>
                        </td>
                        <td>
                          <div style={{ fontWeight: 'var(--font-medium)' }}>
                            {p.patient ? `${p.patient.firstName} ${p.patient.lastName}` : '—'}
                          </div>
                        </td>
                        <td style={{ fontWeight: 'var(--font-semibold)', color: 'var(--gray-800)' }}>
                          {formatPeso(p.paymentAmount)}
                        </td>
                        <td className="col-hide-tablet" style={{ fontSize: 'var(--text-xs)', fontFamily: 'monospace', color: 'var(--gray-500)', maxWidth: 180, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
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
        </div>
      </main>
    </div>
  );
}