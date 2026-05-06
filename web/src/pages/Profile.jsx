import { useState, useRef } from 'react';
import Navbar from '../components/Navbar';
import { usersAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import '../styles/dashboard.css';

const NAV_LINKS = [
  { to: '/dashboard',        label: 'Dashboard' },
  { to: '/services',         label: 'Services' },
  { to: '/my-appointments',  label: 'My Appointments' },
];

export default function Profile() {
  const { user, setUser } = useAuth();
  const fileInputRef = useRef(null);

  // Profile form state
  const [form, setForm] = useState({
    firstName:       user?.firstName || '',
    lastName:        user?.lastName  || '',
    currentPassword: '',
    newPassword:     '',
  });
  const [saving,        setSaving]        = useState(false);
  const [profileMsg,    setProfileMsg]    = useState('');
  const [profileError,  setProfileError]  = useState('');

  // Picture upload state
  const [uploading,     setUploading]     = useState(false);
  const [pictureMsg,    setPictureMsg]    = useState('');
  const [pictureError,  setPictureError]  = useState('');
  const [previewUrl,    setPreviewUrl]    = useState(user?.profileImageUrl || null);

  const initials = `${user?.firstName?.[0] || ''}${user?.lastName?.[0] || ''}`.toUpperCase();

  // ── Handle profile text update ───────────────────────────────────────────
  const handleProfileSave = async (e) => {
    e.preventDefault();
    setProfileMsg('');
    setProfileError('');

    if (!form.currentPassword) {
      setProfileError('Current password is required to save changes.');
      return;
    }

    setSaving(true);
    try {
      const payload = {
        firstName:       form.firstName.trim(),
        lastName:        form.lastName.trim(),
        currentPassword: form.currentPassword,
      };
      if (form.newPassword.trim()) {
        payload.newPassword = form.newPassword.trim();
      }

      const res = await usersAPI.updateProfile(payload);
      const updated = res.data.data;

      // Update auth context so Navbar reflects new name immediately
      setUser(prev => ({ ...prev, firstName: updated.firstName, lastName: updated.lastName }));

      setForm(f => ({ ...f, currentPassword: '', newPassword: '' }));
      setProfileMsg('Profile updated successfully.');
    } catch (err) {
      setProfileError(
        err.response?.data?.error?.message || 'Failed to update profile. Please try again.'
      );
    } finally {
      setSaving(false);
    }
  };

  // ── Handle profile picture upload ────────────────────────────────────────
  const handleFileChange = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Client-side pre-validation
    if (!['image/jpeg', 'image/png'].includes(file.type)) {
      setPictureError('Only JPEG and PNG files are allowed.');
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      setPictureError('File size must be 5 MB or less.');
      return;
    }

    setPictureMsg('');
    setPictureError('');
    setUploading(true);

    // Show local preview immediately
    const localUrl = URL.createObjectURL(file);
    setPreviewUrl(localUrl);

    try {
      const formData = new FormData();
      formData.append('file', file);

      const res = await usersAPI.uploadProfilePicture(formData);
      const remoteUrl = res.data.data?.profileImageUrl;

      // Replace local blob URL with the real Supabase URL
      setPreviewUrl(remoteUrl);
      setUser(prev => ({ ...prev, profileImageUrl: remoteUrl }));
      setPictureMsg('Profile picture updated.');
    } catch (err) {
      setPreviewUrl(user?.profileImageUrl || null);
      setPictureError(
        err.response?.data?.error?.message || 'Upload failed. Please try again.'
      );
    } finally {
      setUploading(false);
      // Reset input so same file can be re-selected if needed
      e.target.value = '';
    }
  };

  return (
    <div className="app-layout">
      <Navbar links={NAV_LINKS} />

      <main className="page-container" style={{ maxWidth: 640, margin: '0 auto' }}>
        <div className="page-header">
          <h1 className="page-title">My Profile</h1>
          <p className="page-subtitle">Manage your account information</p>
        </div>

        {/* ── Profile Picture ── */}
        <div className="card" style={{ marginBottom: 24, padding: '28px 32px' }}>
          <h2 style={{ fontSize: 'var(--text-lg)', fontWeight: 'var(--font-semibold)', marginBottom: 20 }}>
            Profile Picture
          </h2>

          <div style={{ display: 'flex', alignItems: 'center', gap: 24 }}>
            {/* Avatar */}
            <div style={{
              width: 80, height: 80, borderRadius: '50%',
              background: previewUrl ? 'transparent' : 'var(--gradient-primary)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontSize: 28, fontWeight: 700, color: 'white',
              overflow: 'hidden', flexShrink: 0,
              border: '2px solid var(--gray-200)',
            }}>
              {previewUrl
                ? <img src={previewUrl} alt="Profile" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                : initials}
            </div>

            <div style={{ flex: 1 }}>
              <button
                onClick={() => fileInputRef.current?.click()}
                disabled={uploading}
                style={{
                  padding: '8px 20px',
                  background: 'white',
                  border: '1px solid var(--gray-300)',
                  borderRadius: 8, cursor: 'pointer',
                  fontSize: 'var(--text-sm)', fontWeight: 'var(--font-medium)',
                }}
              >
                {uploading ? 'Uploading…' : 'Change Photo'}
              </button>
              <p style={{ fontSize: 12, color: 'var(--gray-400)', marginTop: 6 }}>
                JPEG or PNG · max 5 MB
              </p>
              <input
                ref={fileInputRef}
                type="file"
                accept="image/jpeg,image/png"
                style={{ display: 'none' }}
                onChange={handleFileChange}
              />
              {pictureMsg   && <p style={{ fontSize: 13, color: 'green',          marginTop: 6 }}>{pictureMsg}</p>}
              {pictureError && <p style={{ fontSize: 13, color: 'var(--error)',    marginTop: 6 }}>{pictureError}</p>}
            </div>
          </div>
        </div>

        {/* ── Profile Info ── */}
        <div className="card" style={{ padding: '28px 32px' }}>
          <h2 style={{ fontSize: 'var(--text-lg)', fontWeight: 'var(--font-semibold)', marginBottom: 20 }}>
            Personal Information
          </h2>

          <form onSubmit={handleProfileSave}>

            {profileMsg   && <div style={{ padding: '10px 14px', background: '#f0fdf4', border: '1px solid #86efac', borderRadius: 8, color: '#166534', fontSize: 13, marginBottom: 16 }}>{profileMsg}</div>}
            {profileError && <div className="error-banner"><span>⚠</span> {profileError}</div>}

            {/* Read-only email */}
            <div className="form-group">
              <label>Email</label>
              <input type="email" value={user?.email || ''} disabled
                style={{ background: 'var(--gray-50)', color: 'var(--gray-400)', cursor: 'not-allowed' }} />
              <p style={{ fontSize: 12, color: 'var(--gray-400)', marginTop: 4 }}>
                Email cannot be changed.
              </p>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
              <div className="form-group">
                <label>First Name</label>
                <input
                  type="text"
                  value={form.firstName}
                  onChange={e => setForm(f => ({ ...f, firstName: e.target.value }))}
                  required
                />
              </div>
              <div className="form-group">
                <label>Last Name</label>
                <input
                  type="text"
                  value={form.lastName}
                  onChange={e => setForm(f => ({ ...f, lastName: e.target.value }))}
                  required
                />
              </div>
            </div>

            <hr style={{ border: 'none', borderTop: '1px solid var(--gray-200)', margin: '20px 0' }} />
            <h3 style={{ fontSize: 'var(--text-sm)', fontWeight: 'var(--font-semibold)', color: 'var(--gray-700)', marginBottom: 16 }}>
              Change Password <span style={{ fontWeight: 400, color: 'var(--gray-400)' }}>(optional)</span>
            </h3>

            <div className="form-group">
              <label>Current Password <span style={{ color: 'var(--error)' }}>*</span></label>
              <input
                type="password"
                value={form.currentPassword}
                onChange={e => setForm(f => ({ ...f, currentPassword: e.target.value }))}
                placeholder="Required to save any changes"
                required
              />
            </div>

            <div className="form-group">
              <label>New Password <span style={{ color: 'var(--gray-400)', fontWeight: 400 }}>(leave blank to keep current)</span></label>
              <input
                type="password"
                value={form.newPassword}
                onChange={e => setForm(f => ({ ...f, newPassword: e.target.value }))}
                placeholder="Minimum 8 characters"
                minLength={form.newPassword ? 8 : undefined}
              />
            </div>

            <button
              type="submit"
              disabled={saving}
              style={{
                marginTop: 8,
                height: 44, padding: '0 28px',
                background: 'var(--gradient-primary)',
                color: 'white', border: 'none',
                borderRadius: 10, fontWeight: 600,
                fontSize: 'var(--text-sm)', cursor: saving ? 'not-allowed' : 'pointer',
                opacity: saving ? 0.7 : 1,
              }}
            >
              {saving ? 'Saving…' : 'Save Changes'}
            </button>
          </form>
        </div>
      </main>
    </div>
  );
}