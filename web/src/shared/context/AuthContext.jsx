import { createContext, useContext, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authAPI } from '@/shared/api/api';
import { toast } from 'sonner';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');

    if (!token) {
      setLoading(false);
      return;
    }

    authAPI.me()
      .then(res => setUser(res.data.data))
      .catch(() => {
        localStorage.removeItem('token');
      })
      .finally(() => setLoading(false));
  }, []);

  const logout = async () => {
    try {
      await authAPI.logout();
    } catch (_) {}

    localStorage.removeItem('token');
    localStorage.removeItem('user');
    // Toast BEFORE setUser(null) — Sonner's global store holds it across the navigation.
    // If we toast after setUser, the competing ProtectedRoute Navigate fires first and
    // the logout state we'd pass via location.state is wiped.
    toast.success('Logged out successfully.');
    setUser(null);
    navigate('/login', { replace: true });
  };

  const refreshUser = async () => {
    try {
      const res = await authAPI.me();
      setUser(res.data.data);
    } catch (_) {}
  };

  return (
    <AuthContext.Provider value={{ user, setUser, loading, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
