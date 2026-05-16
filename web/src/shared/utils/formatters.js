export function formatDate(dt) {
  if (!dt) return '—';
  return new Date(dt).toLocaleDateString('en-PH', {
    month: 'short', day: 'numeric', year: 'numeric',
  });
}

export function formatDateTime(dt) {
  if (!dt) return '—';
  const d = new Date(dt);
  return d.toLocaleDateString('en-PH', { month: 'short', day: 'numeric', year: 'numeric' })
    + ' · ' + d.toLocaleTimeString('en-PH', { hour: 'numeric', minute: '2-digit' });
}

export function formatTime(dt) {
  if (!dt) return '';
  return new Date(dt).toLocaleTimeString('en-PH', { hour: 'numeric', minute: '2-digit' });
}

export function formatPeso(n) {
  if (!n && n !== 0) return '₱0.00';
  return '₱' + Number(n).toLocaleString('en-PH', { minimumFractionDigits: 2 });
}
