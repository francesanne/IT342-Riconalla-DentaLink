import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/v1';

const api = axios.create({ baseURL: BASE_URL });

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

// Auth
export const authAPI = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
  googleLogin: (data) => api.post('/auth/google', data),
  me: () => api.get('/auth/me'),
  logout: () => api.post('/auth/logout'),
};

// Services
export const servicesAPI = {
  getAll: () => api.get('/services'),
  create: (data) => api.post('/services', data, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  update: (id, data) => api.put(`/services/${id}`, data, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  delete: (id) => api.delete(`/services/${id}`),
};

// Dentists
export const dentistsAPI = {
  getAll: () => api.get('/dentists'),
  getById: (id) => api.get(`/dentists/${id}`),
  create: (data) => api.post('/dentists', data),
  update: (id, data) => api.put(`/dentists/${id}`, data),
  delete: (id) => api.delete(`/dentists/${id}`),
};

// Appointments
export const appointmentsAPI = {
  create: (data) => api.post('/appointments', data),
  getAll: (status) => api.get('/appointments', { params: status ? { status } : {} }),
  getById: (id) => api.get(`/appointments/${id}`),
  updateStatus: (id, status) => api.put(`/appointments/${id}/status`, { status }),
};

// Admin
export const adminAPI = {
  dashboard: () => api.get('/admin/dashboard'),
};

export default api;