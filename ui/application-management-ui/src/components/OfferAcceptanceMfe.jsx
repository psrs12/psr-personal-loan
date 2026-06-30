import React, { useEffect, useState } from 'react';
import { getDeclarations, submitESign } from '../api/client.js';

export default function OfferAcceptanceMfe({ applicationId }) {
  const [declarations, setDeclarations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [checked, setChecked] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [submitError, setSubmitError] = useState(null);

  useEffect(() => {
    getDeclarations(applicationId)
      .then(data => {
        const list = Array.isArray(data) ? data : (data.declarations ?? []);
        setDeclarations(list);
        const initial = {};
        list.forEach(d => { initial[d.id] = false; });
        setChecked(initial);
      })
      .catch(e => setError(e.message))
      .finally(() => setLoading(false));
  }, [applicationId]);

  function toggle(id) {
    setChecked(c => ({ ...c, [id]: !c[id] }));
  }

  const mandatoryAll = declarations
    .filter(d => d.mandatory)
    .every(d => checked[d.id]);

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    setSubmitError(null);
    try {
      const accepted = Object.entries(checked)
        .filter(([, v]) => v)
        .map(([id]) => id);
      await submitESign(applicationId, accepted);
      setSubmitted(true);
    } catch (e) {
      setSubmitError(e.message);
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) return <div className="alert-info">Loading declarations...</div>;
  if (error) return <div className="alert-error">{error}</div>;

  if (submitted) {
    return (
      <div style={{ textAlign: 'center', padding: '40px 0' }}>
        <div style={{ fontSize: '3rem', color: '#1b5e20', marginBottom: 12 }}>✓</div>
        <h3 style={{ color: '#1b5e20' }}>E-Signature Submitted</h3>
        <p style={{ color: '#5a6a7e' }}>Your offer has been accepted. Your application is now being finalised.</p>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit}>
      <p style={{ color: '#5a6a7e', marginBottom: 24 }}>
        Please read and accept the declarations below to complete your loan offer acceptance.
        All mandatory declarations must be accepted to proceed.
      </p>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 14, marginBottom: 28 }}>
        {declarations.map(d => (
          <label
            key={d.id}
            style={{
              display: 'flex',
              gap: 14,
              alignItems: 'flex-start',
              background: '#f5f8ff',
              border: '1px solid #c5d5ea',
              borderRadius: 6,
              padding: '14px 16px',
              cursor: 'pointer',
            }}
          >
            <input
              type="checkbox"
              checked={!!checked[d.id]}
              onChange={() => toggle(d.id)}
              style={{ marginTop: 3, flexShrink: 0, width: 18, height: 18, accentColor: '#1565c0' }}
            />
            <span style={{ fontSize: '0.93rem', lineHeight: 1.6, color: '#2d3f55' }}>
              {d.mandatory && (
                <span style={{ color: '#b71c1c', fontWeight: 700, marginRight: 6 }}>*</span>
              )}
              {d.text ?? d.description ?? d.content}
            </span>
          </label>
        ))}
      </div>

      <p style={{ fontSize: '0.82rem', color: '#7a8ea8', marginBottom: 16 }}>
        * Mandatory declarations must be accepted.
      </p>

      {submitError && <div className="alert-error">{submitError}</div>}

      <button
        type="submit"
        className="btn-primary"
        disabled={!mandatoryAll || submitting}
      >
        {submitting ? 'Submitting...' : 'Accept and Sign'}
      </button>
    </form>
  );
}
