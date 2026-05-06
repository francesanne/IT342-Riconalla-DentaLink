import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import '../styles/popup.css';

export default function WelcomePopup() {
  const location = useLocation();
  const navigate  = useNavigate();

  const { justLoggedIn, firstName, role } = location.state || {};

  const [visible, setVisible] = useState(!!justLoggedIn);

  useEffect(() => {
    if (!justLoggedIn) return;

 
    navigate(location.pathname, { replace: true, state: {} });

    const timer = setTimeout(() => setVisible(false), 3000);
    return () => clearTimeout(timer);
  }, []); 

  if (!visible) return null;

  return (
    <div className="wp-overlay" onClick={() => setVisible(false)}>
      <div className="wp-card" onClick={(e) => e.stopPropagation()}>
        {/* Animated checkmark circle */}
        <div className="wp-icon-wrap">
          <svg className="wp-check" viewBox="0 0 52 52">
            <circle className="wp-circle" cx="26" cy="26" r="25" fill="none" />
            <path  className="wp-tick"   fill="none" d="M14 27l8 8 16-16" />
          </svg>
        </div>

        <h3 className="wp-title">Welcome back!</h3>

        {firstName && (
          <p className="wp-name">Hello, {firstName} 👋</p>
        )}

        <p className="wp-sub">You have successfully signed in to DentaLink.</p>

        <span className="wp-badge">
          {role === 'ADMIN' ? '🛡️ Administrator' : '🦷 Patient'}
        </span>

        {/* Progress bar that drains over 3 s */}
        <div className="wp-progress">
          <div className="wp-progress-bar" />
        </div>

        <p className="wp-hint">Click anywhere to dismiss</p>
      </div>
    </div>
  );
}