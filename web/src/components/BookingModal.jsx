import { useState, useEffect } from 'react';
import { dentistsAPI, appointmentsAPI } from '../services/api';

const TIME_SLOTS = [
  '08:00', '08:30', '09:00', '09:30', '10:00', '10:30',
  '11:00', '11:30', '13:00', '13:30', '14:00', '14:30',
  '15:00', '15:30', '16:00', '16:30', '17:00',
];

function formatPeso(amount) {
  if (!amount) return '₱0.00';
  return `₱${Number(amount).toLocaleString('en-PH', { minimumFractionDigits: 2 })}`;
}

export default function BookingModal({ service, onClose, onSuccess }) {
  const [dentists, setDentists] = useState([]);
  const [dentistId, setDentistId] = useState('');
  const [date, setDate] = useState('');
  const [time, setTime] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Min date = today
  const today = new Date().toISOString().split('T')[0];

  useEffect(() => {
    dentistsAPI.getAll().then(res => {
      const active = (res.data.data || []).filter(d => d.dentistStatus === 'ACTIVE');
      setDentists(active);
    }).catch(() => {});
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!dentistId || !date || !time) { setError('Please fill in all fields.'); return; }
    setLoading(true);
    try {
      const appointmentDatetime = `${date}T${time}:00`;
      await appointmentsAPI.create({
        serviceId: service.id,
        dentistId: Number(dentistId),
        appointmentDatetime,
      });
      onSuccess?.();
    } catch (err) {
      const msg = err.response?.data?.error?.message || err.response?.data?.message || 'Booking failed. Please try again.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal">
        <div className="modal-header">
          <div>
            <div className="modal-title">Book Appointment</div>
            <div className="modal-subtitle">{service.name}</div>
          </div>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        {/* Service summary */}
        <div style={{ padding: '0 var(--space-6)', marginBottom: 'var(--space-4)' }}>
          <div style={{
            background: 'linear-gradient(135deg, rgba(110,193,195,0.08) 0%, rgba(154,208,166,0.08) 100%)',
            border: '1px solid rgba(110,193,195,0.25)',
            borderRadius: 'var(--radius-xl)',
            padding: 'var(--space-4)',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          }}>
            <div>
              <div style={{ fontSize: 'var(--text-xs)', color: 'var(--gray-500)', fontWeight: 'var(--font-medium)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>Total Amount</div>
              <div style={{ fontSize: 'var(--text-2xl)', fontWeight: 'var(--font-bold)', color: 'var(--primary)', fontFamily: 'var(--font-display)' }}>
                {formatPeso(service.price)}
              </div>
            </div>
          </div>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="modal-body" style={{ paddingTop: 0 }}>
            {error && <div className="error-banner"><span>⚠</span> {error}</div>}

            {/* Dentist select */}
            <div className="form-group">
              <label>Select Dentist</label>
              <select value={dentistId} onChange={e => setDentistId(e.target.value)} required>
                <option value="">Choose a dentist…</option>
                {dentists.map(d => (
                  <option key={d.dentistId} value={d.dentistId}>
                    {d.dentistName} — {d.dentistSpecialization}
                  </option>
                ))}
              </select>
            </div>

            {/* Date */}
            <div className="form-group">
              <label>Preferred Date</label>
              <input type="date" value={date} min={today} onChange={e => setDate(e.target.value)} required />
            </div>

            {/* Time */}
            <div className="form-group">
              <label>Preferred Time</label>
              <select value={time} onChange={e => setTime(e.target.value)} required>
                <option value="">Choose a time…</option>
                {TIME_SLOTS.map(t => (
                  <option key={t} value={t}>{formatTime(t)}</option>
                ))}
              </select>
            </div>

            {/* Note */}
            <p style={{ fontSize: 'var(--text-xs)', color: 'var(--gray-400)', margin: 0, lineHeight: 1.5 }}>
              Your appointment will be created with a <strong>Pending Payment</strong> status. After booking, you can proceed with payment.
            </p>
          </div>

          <div className="modal-footer" style={{ padding: '0 var(--space-6) var(--space-6)' }}>
            <button type="button" onClick={onClose} className="btn-sm btn-outline-sm" disabled={loading}>
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              style={{
                height: '44px',
                padding: '0 var(--space-6)',
                background: 'var(--gradient-primary)',
                color: 'white',
                border: 'none',
                borderRadius: 'var(--radius-lg)',
                fontWeight: 'var(--font-semibold)',
                fontSize: 'var(--text-sm)',
                cursor: loading ? 'not-allowed' : 'pointer',
                opacity: loading ? 0.7 : 1,
                transition: 'all 0.2s ease',
                display: 'flex',
                alignItems: 'center',
                gap: 'var(--space-2)',
              }}
            >
              {loading ? (
                <><span className="spinner" style={{ width: 16, height: 16, borderWidth: 2 }} /> Booking…</>
              ) : 'Confirm Booking'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function formatTime(t) {
  const [h, m] = t.split(':').map(Number);
  const ampm = h >= 12 ? 'PM' : 'AM';
  const hour = h % 12 || 12;
  return `${hour}:${m.toString().padStart(2, '0')} ${ampm}`;
}