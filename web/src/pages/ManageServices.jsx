import { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';
import { servicesAPI } from '../services/api';
import '../styles/dashboard.css';

const NAV_LINKS = [
    { to: '/admin', label: 'Dashboard' },
    { to: '/admin/services', label: 'Manage Services' },
    { to: '/admin/dentists', label: 'Manage Dentists' },
    { to: '/admin/appointments', label: 'Manage Appointments' },
];

function formatPeso(n) {
    if (!n) return '₱0.00';
    return `₱${Number(n).toLocaleString('en-PH', { minimumFractionDigits: 2 })}`;
}

// ✅ CHANGED
const EMPTY_FORM = { name: '', description: '', price: '', imageFile: null };

export default function ManageServices() {
    const [services, setServices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [modal, setModal] = useState(null);
    const [editing, setEditing] = useState(null);
    const [form, setForm] = useState(EMPTY_FORM);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState('');
    const [deleteId, setDeleteId] = useState(null);

    const load = () => {
        setLoading(true);
        servicesAPI.getAll()
            .then(r => setServices(r.data.data || []))
            .catch(() => { })
            .finally(() => setLoading(false));
    };

    useEffect(load, []);

    const openCreate = () => {
        setForm(EMPTY_FORM);
        setEditing(null);
        setError('');
        setModal('create');
    };

    const openEdit = (s) => {
        setForm({
            name: s.name,
            description: s.description || '',
            price: s.price,
            imageFile: null,
        });
        setEditing(s);
        setError('');
        setModal('edit');
    };

    const closeModal = () => {
        setModal(null);
        setEditing(null);
        setError('');
    };

    const handleSave = async (e) => {
        e.preventDefault();

        if (!form.name.trim() || !form.price) {
            setError('Name and price are required.');
            return;
        }

        setSaving(true);
        setError('');

        try {
            const payload = {
                name: form.name.trim(),
                description: form.description.trim(),
                price: parseFloat(form.price),
            };

            let savedService;
            if (modal === 'create') {
                const res = await servicesAPI.create(payload);
                savedService = res.data.data;
            } else {
                const res = await servicesAPI.update(editing.id, payload);
                savedService = res.data.data;
            }

            // Upload image separately if a file was selected
            if (form.imageFile && savedService?.id) {
                const imageForm = new FormData();
                imageForm.append('file', form.imageFile);
                const imgRes = await servicesAPI.uploadImage(savedService.id, imageForm);
                // Update the saved service with the returned imageUrl
                savedService.imageUrl = imgRes.data.data?.imageUrl || savedService.imageUrl;
            }

            closeModal();
            load();
        } catch (err) {
            setError(err.response?.data?.error?.message || 'Failed to save service.');
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async (id) => {
        try {
            await servicesAPI.delete(id);
            setDeleteId(null);
            load();
        } catch (err) {
            alert(err.response?.data?.error?.message || 'Failed to delete service.');
        }
    };

    return (
        <div className="app-layout">
            <Navbar links={NAV_LINKS} />
            <main className="page-container">

                <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
                    <div>
                        <h1 className="page-title">Manage Services</h1>
                        <p className="page-subtitle">Create and manage dental service offerings</p>
                    </div>
                    <button
                        onClick={openCreate}
                        style={{
                            height: '44px', padding: '0 var(--space-5)',
                            background: 'var(--gradient-primary)', color: 'white',
                            border: 'none', borderRadius: 'var(--radius-lg)',
                            fontWeight: 'var(--font-semibold)', fontSize: 'var(--text-sm)',
                            cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 'var(--space-2)',
                            transition: 'all 0.2s ease',
                        }}
                    >
                        + Add New Service
                    </button>
                </div>

                {loading ? (
                    <div className="loading-container"><div className="spinner" /></div>
                ) : services.length === 0 ? (
                    <div className="empty-state">
                        <span className="empty-icon">🦷</span>
                        <div className="empty-title">No services yet</div>
                        <div className="empty-text">Create your first dental service to get started.</div>
                        <button onClick={openCreate} className="btn-sm btn-primary-sm" style={{ marginTop: 'var(--space-4)', height: 44, padding: '0 24px' }}>+ Add Service</button>
                    </div>
                ) : (
                    <div className="services-grid">
                        {services.map(s => (
                            <div key={s.id} className="service-card">
                                <div className="service-img">
                                    {s.imageUrl ? (
                                        <img
                                            src={s.imageUrl}
                                            alt={s.name}
                                            onError={(e) => { e.target.src = '/tooth-icon.png'; }}
                                        />
                                    ) : <span>🦷</span>}
                                </div>
                                <div className="service-card-body">
                                    <div className="service-name">{s.name}</div>
                                    <div className="service-desc">{s.description || 'No description provided.'}</div>
                                    <div className="service-price">{formatPeso(s.price)}</div>
                                    <div style={{ display: 'flex', gap: 'var(--space-3)', marginTop: 'auto' }}>
                                        <button className="btn-sm btn-outline-sm" style={{ flex: 1, justifyContent: 'center' }} onClick={() => openEdit(s)}>
                                            ✏ Edit
                                        </button>
                                        <button className="btn-sm btn-danger-sm" style={{ flex: 1, justifyContent: 'center' }} onClick={() => setDeleteId(s.id)}>
                                            🗑 Delete
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </main>

            {/* MODAL */}
            {modal && (
                <div className="modal-overlay" onClick={e => e.target === e.currentTarget && closeModal()}>
                    <div className="modal">
                        <div className="modal-header">
                            <div>
                                <div className="modal-title">{modal === 'create' ? 'Add New Service' : 'Edit Service'}</div>
                            </div>
                            <button className="modal-close" onClick={closeModal}>×</button>
                        </div>

                        <form onSubmit={handleSave}>
                            <div className="modal-body">
                                {error && <div className="error-banner"><span>⚠</span> {error}</div>}

                                <div className="form-group">
                                    <label>Service Name</label>
                                    <input type="text" value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} required />
                                </div>

                                <div className="form-group">
                                    <label>Description</label>
                                    <textarea value={form.description} onChange={e => setForm(f => ({ ...f, description: e.target.value }))} />
                                </div>

                                <div className="form-group">
                                    <label>Price (₱)</label>
                                    <input type="number" value={form.price} onChange={e => setForm(f => ({ ...f, price: e.target.value }))} required />
                                </div>

                                {/* ✅ FIXED IMAGE INPUT */}
                                <div className="form-group">
                                    <label>Service Image</label>
                                    <input
                                        type="file"
                                        accept="image/*"
                                        onChange={e => setForm(f => ({ ...f, imageFile: e.target.files[0] }))}
                                    />
                                </div>

                            </div>

                            <div className="modal-footer" style={{ padding: '0 var(--space-6) var(--space-6)' }}>
                                <button type="button" className="btn-sm btn-outline-sm" onClick={closeModal} disabled={saving}>Cancel</button>
                                <button
                                    type="submit"
                                    disabled={saving}
                                    style={{
                                        height: '44px', padding: '0 var(--space-6)',
                                        background: 'var(--gradient-primary)', color: 'white',
                                        border: 'none', borderRadius: 'var(--radius-lg)',
                                        fontWeight: 'var(--font-semibold)', fontSize: 'var(--text-sm)',
                                        cursor: saving ? 'not-allowed' : 'pointer', opacity: saving ? 0.7 : 1,
                                    }}
                                >
                                    {saving ? 'Saving…' : 'Save Service'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* DELETE MODAL */}
            {deleteId && (
                <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setDeleteId(null)}>
                    <div className="modal" style={{ maxWidth: 400 }}>
                        <div className="modal-header">
                            <div className="modal-title">Delete Service</div>
                            <button className="modal-close" onClick={() => setDeleteId(null)}>×</button>
                        </div>
                        <div className="modal-body">
                            <p style={{ color: 'var(--gray-600)' }}>
                                Are you sure you want to delete this service? This action cannot be undone.
                            </p>
                        </div>
                        <div className="modal-footer" style={{ padding: '0 var(--space-6) var(--space-6)' }}>
                            <button className="btn-sm btn-outline-sm" onClick={() => setDeleteId(null)}>Cancel</button>
                            <button
                                className="btn-sm"
                                style={{ height: 44, padding: '0 24px', background: 'var(--danger)', color: 'white', border: 'none', borderRadius: 'var(--radius-lg)', fontWeight: 600 }}
                                onClick={() => handleDelete(deleteId)}
                            >
                                Delete
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}