import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header.jsx';
import { login } from '../api/client.js';

export default function LoginPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ applicationId: '', last4SSN: '', dateOfBirth: '' });
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  function set(field, value) {
    setForm(f => ({ ...f, [field]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const data = await login(form.applicationId.trim(), form.last4SSN.trim(), form.dateOfBirth);
      sessionStorage.setItem('sessionToken', data.sessionToken);
      sessionStorage.setItem('applicationId', data.applicationId);
      navigate('/portal/status');
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page-shell">
      <Header />
      <div className="card">
        <h2>Access Your Application</h2>
        <p className="card-subtitle">Enter your application details to check your status and complete next steps.</p>

        {error && <div className="alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Application ID</label>
            <input
              type="text"
              value={form.applicationId}
              onChange={e => set('applicationId', e.target.value)}
              required
              placeholder="e.g. APP-12345"
              autoComplete="off"
            />
          </div>

          <div className="form-group">
            <label>Last 4 Digits of SSN</label>
            <input
              type="password"
              maxLength={4}
              value={form.last4SSN}
              onChange={e => set('last4SSN', e.target.value)}
              required
              placeholder="XXXX"
              autoComplete="off"
              inputMode="numeric"
            />
          </div>

          <div className="form-group">
            <label>Date of Birth</label>
            <input
              type="date"
              value={form.dateOfBirth}
              onChange={e => set('dateOfBirth', e.target.value)}
              required
            />
          </div>

          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Verifying...' : 'Access My Application'}
          </button>
        </form>

        <p style={{ marginTop: 20, fontSize: '0.85rem', color: '#7a8ea8', textAlign: 'center' }}>
          Your session is encrypted and expires after 30 minutes.
        </p>
        <p style={{ marginTop: 12, fontSize: '0.88rem', textAlign: 'center' }}>
          New applicant? <a href="/apply">Apply now</a>
        </p>
      </div>
    </div>
  );
}
