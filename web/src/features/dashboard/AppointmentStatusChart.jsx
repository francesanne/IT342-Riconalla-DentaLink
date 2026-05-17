const STATUS_CONFIG = [
  { key: 'pendingPaymentAppointments', label: 'Pending Payment', color: '#e9a84c' },
  { key: 'confirmedAppointments',      label: 'Confirmed',       color: '#6EC1C3' },
  { key: 'completedAppointments',      label: 'Completed',       color: '#52b788' },
  { key: 'cancelledAppointments',      label: 'Cancelled',       color: '#94a3b8' },
];

const SIZE      = 160;
const THICKNESS = 28;
const RADIUS    = (SIZE - THICKNESS) / 2;
const CX        = SIZE / 2;
const CY        = SIZE / 2;
const CIRC      = 2 * Math.PI * RADIUS;

function DonutRing({ segments, total }) {
  let accum = 0;
  const arcs = segments.map(s => {
    const dash = total > 0 ? (s.value / total) * CIRC : 0;
    const arc  = { color: s.color, dash, offset: accum };
    accum += dash;
    return arc;
  });

  return (
    <svg
      width={SIZE}
      height={SIZE}
      viewBox={`0 0 ${SIZE} ${SIZE}`}
      style={{ transform: 'rotate(-90deg)', display: 'block' }}
      aria-hidden="true"
    >
      <circle
        cx={CX} cy={CY} r={RADIUS}
        fill="none"
        stroke="#f3f4f6"
        strokeWidth={THICKNESS}
      />
      {arcs.filter(a => a.dash > 0).map((a, i) => (
        <circle
          key={i}
          cx={CX} cy={CY} r={RADIUS}
          fill="none"
          stroke={a.color}
          strokeWidth={THICKNESS}
          strokeDasharray={`${a.dash} ${CIRC}`}
          strokeDashoffset={-a.offset}
          strokeLinecap="butt"
        />
      ))}
    </svg>
  );
}

export default function AppointmentStatusChart({ stats }) {
  const total    = stats?.totalAppointments ?? 0;
  const segments = STATUS_CONFIG.map(s => ({ ...s, value: stats?.[s.key] ?? 0 }));

  return (
    <div className="card" style={{ display: 'flex', flexDirection: 'column' }}>
      <div className="card-header">
        <span className="card-title">Appointment Status</span>
      </div>

      <div style={{ flex: 1, padding: 'var(--space-6)', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 'var(--space-5)' }}>

        {/* Donut with centre label */}
        <div style={{ position: 'relative', width: SIZE, height: SIZE, flexShrink: 0 }}>
          <DonutRing segments={segments} total={total} />
          <div style={{
            position: 'absolute', inset: 0,
            display: 'flex', flexDirection: 'column',
            alignItems: 'center', justifyContent: 'center',
          }}>
            <span style={{
              fontSize: 'var(--text-2xl)',
              fontWeight: 'var(--font-bold)',
              color: 'var(--gray-900)',
              fontFamily: 'var(--font-display)',
              lineHeight: 1,
            }}>
              {total}
            </span>
            <span style={{
              fontSize: 'var(--text-xs)',
              color: 'var(--gray-400)',
              fontWeight: 'var(--font-medium)',
              marginTop: 3,
            }}>
              Total
            </span>
          </div>
        </div>

        {/* Legend */}
        <div style={{ width: '100%', display: 'flex', flexDirection: 'column' }}>
          {segments.map((s, i) => (
            <div
              key={s.key}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '8px 0',
                borderBottom: i < segments.length - 1 ? '1px solid var(--gray-100)' : 'none',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                <div style={{
                  width: 10, height: 10,
                  borderRadius: 2,
                  background: s.color,
                  flexShrink: 0,
                }} />
                <span style={{ fontSize: 'var(--text-sm)', color: 'var(--gray-600)' }}>
                  {s.label}
                </span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
                <span style={{
                  fontSize: 'var(--text-sm)',
                  fontWeight: 'var(--font-semibold)',
                  color: 'var(--gray-900)',
                  minWidth: 20,
                  textAlign: 'right',
                }}>
                  {s.value}
                </span>
                <span style={{
                  fontSize: 'var(--text-xs)',
                  color: 'var(--gray-400)',
                  minWidth: 32,
                  textAlign: 'right',
                }}>
                  {total > 0 ? `${Math.round((s.value / total) * 100)}%` : '—'}
                </span>
              </div>
            </div>
          ))}
        </div>

      </div>
    </div>
  );
}
