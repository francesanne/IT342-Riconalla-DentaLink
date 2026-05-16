import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '@/shared/components/Navbar';
import BookingModal from '@/features/appointments/BookingModal';
import { servicesAPI } from '@/shared/api/api';
import ToothIcon from '@/shared/components/ToothIcon';
import '@/features/dashboard/styles/dashboard.css';

const NAV_LINKS = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/services', label: 'Services' },
  { to: '/my-appointments', label: 'My Appointments' },
  { to: '/profile',         label: 'Profile' },
  { to: '/contact',         label: 'Contact' },
];

function formatPeso(n) {
  if (!n) return '₱0.00';
  return `₱${Number(n).toLocaleString('en-PH', { minimumFractionDigits: 2 })}`;
}

export default function Services() {
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedService, setSelectedService] = useState(null);
  const [successMsg, setSuccessMsg] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    servicesAPI.getAll()
      .then(res => setServices(res.data.data || []))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const handleBookingSuccess = () => {
    setSelectedService(null);
    setSuccessMsg('Appointment booked successfully! You can view it in My Appointments.');
    setTimeout(() => setSuccessMsg(''), 5000);
  };

  return (
    <div className="app-layout">
      <Navbar links={NAV_LINKS} />
      <main className="page-container">
        <div className="page-header">
          <h1 className="page-title">Our Services</h1>
          <p className="page-subtitle">Choose from our comprehensive range of dental services</p>
        </div>

        {successMsg && (
          <div className="success-banner">
            <span>✓</span> {successMsg}
          </div>
        )}

        {loading ? (
          <div className="services-grid">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="service-card">
                <div className="skeleton skeleton-service-img" />
                <div className="service-card-body">
                  <div className="skeleton skeleton-text-lg" style={{ width: '72%', marginBottom: 10 }} />
                  <div className="skeleton skeleton-text-sm" style={{ width: '100%', marginBottom: 5 }} />
                  <div className="skeleton skeleton-text-sm" style={{ width: '80%', marginBottom: 16 }} />
                  <div className="skeleton skeleton-text-lg" style={{ width: 80, marginBottom: 16 }} />
                  <div className="skeleton" style={{ height: 44, borderRadius: 'var(--radius-lg)' }} />
                </div>
              </div>
            ))}
          </div>
        ) : services.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon"><ToothIcon size={36} /></div>
            <div className="empty-title">No services available</div>
            <div className="empty-text">Check back soon for available dental services.</div>
          </div>
        ) : (
          <div className="services-grid">
            {services.map(service => (
              <div key={service.id} className="service-card">
                <div className="service-img">
                  {service.imageUrl ? (
                    <img src={service.imageUrl} alt={service.name} />
                  ) : (
                    <ToothIcon size={36} color="var(--gray-300)" />
                  )}
                </div>
                <div className="service-card-body">
                  <div className="service-name">{service.name}</div>
                  <div className="service-desc">{service.description || 'Professional dental service by our expert team.'}</div>
                  <div className="service-price">{formatPeso(service.price)}</div>
                  <button
                    className="btn-sm btn-primary-sm"
                    style={{ width: '100%', height: '44px', borderRadius: 'var(--radius-lg)', justifyContent: 'center', fontSize: 'var(--text-sm)' }}
                    onClick={() => setSelectedService(service)}
                  >
                    Book Appointment
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>

      {selectedService && (
        <BookingModal
          service={selectedService}
          onClose={() => setSelectedService(null)}
          onSuccess={handleBookingSuccess}
        />
      )}
    </div>
  );
}