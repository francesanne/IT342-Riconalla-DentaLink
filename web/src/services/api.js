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
  getById: (id) => api.get(`/services/${id}`),
  create: (data) => api.post('/services', data),
  update: (id, data) => api.put(`/services/${id}`, data),
  uploadImage: (id, formData) => api.post(`/services/${id}/upload-image`, formData, {
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

// Users / Profile
export const usersAPI = {
  updateProfile: (data) => api.put('/users/me/profile', data),
  uploadProfilePicture: (formData) => api.post('/users/me/upload-profile-picture', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
};

// Payments
export const paymentsAPI = {
  createIntent: (data) => api.post('/payments/create-intent', data),
  getAll: () => api.get('/payments'),
};

// Admin
export const adminAPI = {
  dashboard: () => api.get('/admin/dashboard'),
};

export default api;