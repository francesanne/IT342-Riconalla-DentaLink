import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth }  from '@/shared/context/AuthContext';
import Landing from '@/pages/Landing';
import Login from '@/features/auth/Login'
import Register from '@/features/auth/Register'
import PatientDashboard from '@/features/dashboard/PatientDashboard';
import Services from '@/features/services/Services';
import MyAppointments from '@/pages/MyAppointments';
import AdminDashboard from '@/features/dashboard/AdminDashboard';
 import AdminPayments from '@/pages/AdminPayments';
 import ManageServices from '@/features/services/ManageServices';
import ManageDentists from '@/features/dentists/ManageDentists'
import ManageAppointments from '@/pages/ManageAppointments';
import PaymentSuccess from '@/pages/PaymentSuccess';
import PaymentCancel from '@/pages/PaymentCancel';
import Profile from '@/pages/Profile';

function ProtectedRoute({ children, adminOnly = false }) {
  const { user, loading } = useAuth();
  if (loading) return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <div className="spinner" />
    </div>
  );
  if (!user) return <Navigate to="/login" replace />;
  if (adminOnly && user.role !== 'ADMIN') return <Navigate to="/dashboard" replace />;
  return children;
}

function AppRoutes() {
  const { user } = useAuth();
  return (
    <Routes>
      <Route path="/" element={<Landing />} />
      <Route path="/login" element={user ? <Navigate to={user.role === 'ADMIN' ? '/admin' : '/dashboard'} replace /> : <Login />} />
      <Route path="/register" element={user ? <Navigate to="/dashboard" replace /> : <Register />} />

      <Route path="/dashboard" element={<ProtectedRoute><PatientDashboard /></ProtectedRoute>} />
      <Route path="/services" element={<ProtectedRoute><Services /></ProtectedRoute>} />
      <Route path="/my-appointments" element={<ProtectedRoute><MyAppointments /></ProtectedRoute>} />
      <Route path="/profile"         element={<ProtectedRoute><Profile /></ProtectedRoute>} />
      <Route path="/payment/success" element={<ProtectedRoute><PaymentSuccess /></ProtectedRoute>} />
      <Route path="/payment/cancel"  element={<ProtectedRoute><PaymentCancel /></ProtectedRoute>} />

      <Route path="/admin" element={<ProtectedRoute adminOnly><AdminDashboard /></ProtectedRoute>} />
      <Route path="/admin/services" element={<ProtectedRoute adminOnly><ManageServices /></ProtectedRoute>} />
      <Route path="/admin/dentists" element={<ProtectedRoute adminOnly><ManageDentists /></ProtectedRoute>} />
      <Route path="/admin/appointments" element={<ProtectedRoute adminOnly><ManageAppointments /></ProtectedRoute>} />
      <Route path="/admin/payments"     element={<ProtectedRoute adminOnly><AdminPayments /></ProtectedRoute>} />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  );
}

export default App;