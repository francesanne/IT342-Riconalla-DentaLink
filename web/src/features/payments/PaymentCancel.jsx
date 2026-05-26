import { useSearchParams, useNavigate } from 'react-router-dom';
import Navbar from '@/shared/components/Navbar';
import { Clock } from 'lucide-react';
import '@/features/dashboard/styles/dashboard.css';

const NAV_LINKS = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/services', label: 'Services' },
  { to: '/my-appointments', label: 'My Appointments' },
  { to: '/contact',         label: 'Contact' },
];

export default function PaymentCancel() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  return (
    <div className="app-layout">
      <Navbar links={NAV_LINKS} />

      <main className="page-container" style={{ display: 'flex', justifyContent: 'center', alignItems: 'flex-start', paddingTop: '60px' }}>
        <div style={{
          background: 'white',
          borderRadius: '16px',
          boxShadow: '0 4px 24px rgba(0,0,0,0.08)',
          padding: '48px 40px',
          maxWidth: '480px',
          width: '100%',
          textAlign: 'center',
        }}>
          <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 16 }}>
            <Clock size={64} color="var(--warning)" />
          </div>

          <h2 style={{ color: 'var(--gray-700)', marginBottom: 8 }}>Payment Pending</h2>
          <p style={{ color: 'var(--gray-500)', marginBottom: 8, fontSize: '14px' }}>
            You exited the payment page. Your appointment has been saved with a
            <strong> Pending Payment</strong> status.
          </p>
          <p style={{ color: 'var(--gray-400)', marginBottom: 28, fontSize: '13px' }}>
            You can complete payment anytime from <strong>My Appointments</strong> using the
            <strong> Pay Now</strong> button.
          </p>

          <button
            onClick={() => navigate('/my-appointments')}
            style={{
              width: '100%', height: 44,
              background: 'var(--gradient-primary)',
              color: 'white', border: 'none',
              borderRadius: 10, fontWeight: 600,
              fontSize: 14, cursor: 'pointer',
            }}
          >
            Go to My Appointments
          </button>
        </div>
      </main>
    </div>
  );
}
