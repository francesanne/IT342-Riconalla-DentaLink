import { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';
import { dentistsAPI } from '../services/api';
import '../styles/dashboard.css';

const NAV_LINKS = [
  { to: '/admin', label: 'Dashboard' },
  { to: '/admin/services', label: 'Manage Services' },
  { to: '/admin/dentists', label: 'Manage Dentists' },
  { to: '/admin/appointments', label: 'Manage Appointments' },
];

const EMPTY_FORM = { name: '', specialization: '', status: 'ACTIVE' };

function Initials({ name }) {
  const parts = (name || '').trim().split(' ');
  const init = parts.length >= 2 ? parts[0][0] + parts[parts.length - 1][0] : (parts[0]?.[0] ?? '?');
  return (
    <div style={{
      width: 40, height: 40, borderRadius: '50%',
      background: 'var(--gradient-primary)', color: 'white',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      fontWeight: 'var(--font-semibold)', fontSize: 'var(--text-sm)',
      flexShrink: 0,
    }}>
      {init.toUpperCase()}
    </div>
  );
}

export default function ManageDentists() {
  const [dentists, setDentists] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(null);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [deleteId, setDeleteId] = useState(null);

  const load = () => {
    setLoading(true);
    dentistsAPI.getAll().then(r => setDentists(r.data.data || [])).catch(() => {}).finally(() => setLoading(false));
  };

  useEffect(load, []);

  const openCreate = () => { setForm(EMPTY_FORM); setEditing(null); setError(''); setModal('form'); };
  const openEdit = (d) => {
    setForm({ name: d.name, specialization: d.specialization, status: d.status });
    setEditing(d); setError(''); setModal('form');
  };
  const closeModal = () => { setModal(null); setEditing(null); setError(''); };

  const handleSave = async (e) => {
    e.preventDefault();
    if (!form.name.trim() || !form.specialization.trim()) { setError('Name and specialization are required.'); return; }
    setSaving(true); setError('');
    try {
      if (!editing) await dentistsAPI.create({ name: form.name.trim(), specialization: form.specialization.trim(), status: form.status });
      else await dentistsAPI.update(editing.id, { name: form.name.trim(), specialization: form.specialization.trim(), status: form.status });
      closeModal(); load();
    } catch (err) {
      setError(err.response?.data?.error?.message || 'Failed to save dentist.');
    } finally { setSaving(false); }
  };

  const handleDelete = async (id) => {
    try { await dentistsAPI.delete(id); setDeleteId(null); load(); }
    catch (err) { alert(err.response?.data?.error?.message || 'Failed to delete.'); }
  };

  return (
    <div className="app-layout">
      <Navbar links={NAV_LINKS} />
      <main className="page-container">
        <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
          <div>
            <h1 className="page-title">Manage Dentists</h1>
            <p className="page-subtitle">Manage dentist records and availability</p>
          </div>
          <button
            onClick={openCreate}
            style={{
              height: '44px', padding: '0 var(--space-5)',
              background: 'var(--gradient-primary)', color: 'white',
              border: 'none', borderRadius: 'var(--radius-lg)',
              fontWeight: 'var(--font-semibold)', fontSize: 'var(--text-sm)',
              cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 'var(--space-2)',
            }}
          >
            + Add Dentist
          </button>
        </div>

        {loading ? (
          <div className="loading-container"><div className="spinner" /></div>
        ) : (
          <div className="card">
            <div className="table-wrapper">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Name</th>
                    <th>Specialization</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {dentists.length === 0 ? (
                    <tr><td colSpan={5} style={{ textAlign: 'center', padding: 'var(--space-12)', color: 'var(--gray-400)' }}>
                      No dentists yet. Add your first dentist.
                    </td></tr>
                  ) : dentists.map((d, i) => (
                    <tr key={d.id}>
                      <td style={{ color: 'var(--gray-400)', fontSize: 'var(--text-sm)', width: 40 }}>{i + 1}</td>
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
                          <Initials name={d.name} />
                          <span style={{ fontWeight: 'var(--font-medium)', color: 'var(--primary)' }}>{d.name}</span>
                        </div>
                      </td>
                      <td style={{ color: 'var(--gray-600)' }}>{d.specialization}</td>
                      <td>
                        <span className={`badge ${d.status === 'ACTIVE' ? 'badge-active' : 'badge-inactive'}`}>
                          {d.status}
                        </span>
                      </td>
                      <td>
                        <div style={{ display: 'flex', gap: 'var(--space-2)' }}>
                          <button className="btn-sm btn-outline-sm" onClick={() => openEdit(d)}>✏</button>
                    
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </main>

      {/* Form Modal */}
      {modal === 'form' && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && closeModal()}>
          <div className="modal">
            <div className="modal-header">
              <div className="modal-title">{editing ? 'Edit Dentist' : 'Add Dentist'}</div>
              <button className="modal-close" onClick={closeModal}>×</button>
            </div>
            <form onSubmit={handleSave}>
              <div className="modal-body">
                {error && <div className="error-banner"><span>⚠</span> {error}</div>}
                <div className="form-group">
                  <label>Full Name</label>
                  <input type="text" placeholder="e.g., Dr. Maria Cruz" value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} required />
                </div>
                <div className="form-group">
                  <label>Specialization</label>
                  <input type="text" placeholder="e.g., General Dentistry" value={form.specialization} onChange={e => setForm(f => ({ ...f, specialization: e.target.value }))} required />
                </div>
                <div className="form-group">
                  <label>Status</label>
                  <div style={{ display: 'flex', gap: 'var(--space-3)', marginTop: 'var(--space-1)' }}>
                    {['ACTIVE', 'INACTIVE'].map(s => (
                      <button
                        key={s}
                        type="button"
                        onClick={() => setForm(f => ({ ...f, status: s }))}
                        style={{
                          flex: 1, height: '44px', borderRadius: 'var(--radius-lg)',
                          border: '2px solid',
                          borderColor: form.status === s ? 'var(--primary)' : 'var(--gray-200)',
                          background: form.status === s ? 'var(--primary)' : 'white',
                          color: form.status === s ? 'white' : 'var(--gray-600)',
                          fontWeight: 'var(--font-medium)', fontSize: 'var(--text-sm)',
                          cursor: 'pointer', transition: 'all 0.15s ease',
                        }}
                      >
                        {s === 'ACTIVE' ? 'Active' : 'Inactive'}
                      </button>
                    ))}
                  </div>
                </div>
              </div>
              <div className="modal-footer" style={{ padding: '0 var(--space-6) var(--space-6)' }}>
                <button type="button" className="btn-sm btn-outline-sm" onClick={closeModal} disabled={saving}>Cancel</button>
                <button
                  type="submit" disabled={saving}
                  style={{
                    height: '44px', padding: '0 var(--space-6)',
                    background: 'var(--gradient-primary)', color: 'white',
                    border: 'none', borderRadius: 'var(--radius-lg)',
                    fontWeight: 'var(--font-semibold)', fontSize: 'var(--text-sm)',
                    cursor: saving ? 'not-allowed' : 'pointer', opacity: saving ? 0.7 : 1,
                  }}
                >
                  {saving ? 'Saving…' : 'Save'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete confirm */}
      {deleteId && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setDeleteId(null)}>
          <div className="modal" style={{ maxWidth: 400 }}>
            <div className="modal-header">
              <div className="modal-title">Delete Dentist</div>
              <button className="modal-close" onClick={() => setDeleteId(null)}>×</button>
            </div>
            <div className="modal-body">
              <p style={{ color: 'var(--gray-600)' }}>Are you sure you want to remove this dentist record? This cannot be undone.</p>
            </div>
            <div className="modal-footer" style={{ padding: '0 var(--space-6) var(--space-6)' }}>
              <button className="btn-sm btn-outline-sm" onClick={() => setDeleteId(null)}>Cancel</button>
              <button
                className="btn-sm"
                style={{ height: 44, padding: '0 24px', background: 'var(--danger)', color: 'white', border: 'none', borderRadius: 'var(--radius-lg)', fontWeight: 600, cursor: 'pointer' }}
                onClick={() => handleDelete(deleteId)}
              >Delete</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}