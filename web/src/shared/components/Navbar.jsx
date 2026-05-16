import { useState, useRef, useEffect } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '@/shared/context/AuthContext';
import { Stethoscope, Menu, X } from 'lucide-react';

export default function Navbar({ links = [] }) {
  const { user, logout } = useAuth();
  const [open, setOpen] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const ref = useRef(null);

  useEffect(() => {
    const handler = (e) => {
      if (ref.current && !ref.current.contains(e.target)) {
        setOpen(false);
        setMobileOpen(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const initials = user ? `${user.firstName?.[0] ?? ''}${user.lastName?.[0] ?? ''}`.toUpperCase() : '?';

  return (
    <nav className="navbar" ref={ref}>
      <Link to={user?.role === 'ADMIN' ? '/admin' : '/dashboard'} className="navbar-brand">
        <Stethoscope size={18} /> DentaLink
      </Link>

      <div className="navbar-nav">
        {links.map(({ to, label, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
          >
            {label}
          </NavLink>
        ))}
      </div>

      <div className="navbar-right">
        <button
          className="navbar-hamburger"
          onClick={() => setMobileOpen(p => !p)}
          aria-label="Toggle navigation"
        >
          {mobileOpen ? <X size={20} /> : <Menu size={20} />}
        </button>

        <div className="user-menu">
          <button className="user-menu-btn" onClick={() => setOpen(p => !p)}>
            <div className="user-avatar">{initials}</div>
            <span className="user-name">{user?.firstName}</span>
            <span style={{ fontSize: '10px', color: 'var(--gray-400)', marginLeft: '2px' }}>▼</span>
          </button>

          {open && (
            <div className="user-dropdown">
              <div className="user-dropdown-header">
                <div className="user-dropdown-name">{user?.firstName} {user?.lastName}</div>
                <div className="user-dropdown-email">{user?.email}</div>
              </div>
              <button className="user-dropdown-item danger" onClick={() => { setOpen(false); logout(); }}>
                <span>↩</span> Log Out
              </button>
            </div>
          )}
        </div>
      </div>

      {mobileOpen && (
        <div className="navbar-mobile-nav">
          {links.map(({ to, label, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
              onClick={() => setMobileOpen(false)}
            >
              {label}
            </NavLink>
          ))}
        </div>
      )}
    </nav>
  );
}
