export default function StatusBadge({ status }) {
  const map = {
    PENDING_PAYMENT: ['badge-pending',   'Pending Payment'],
    CONFIRMED:       ['badge-confirmed', 'Confirmed'],
    COMPLETED:       ['badge-completed', 'Completed'],
    CANCELLED:       ['badge-cancelled', 'Cancelled'],
    PAID:            ['badge-confirmed', 'Paid'],
    FAILED:          ['badge-cancelled', 'Failed'],
    UNPAID:          ['badge-pending',   'Unpaid'],
  };
  const [cls, label] = map[status] ?? ['badge-pending', status ?? '—'];
  return <span className={`badge ${cls}`}>{label}</span>;
}
