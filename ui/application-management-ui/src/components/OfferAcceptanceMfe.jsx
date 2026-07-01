import React, { useEffect, useState } from 'react';
import { getDeclarations, submitESign } from '../api/client.js';

function fmt(value, prefix = '') {
  if (value === null || value === undefined) return '—';
  return prefix + Number(value).toLocaleString();
}

export default function OfferAcceptanceMfe({ applicationId, application }) {
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
        list.forEach(d => { initial[d.declarationId ?? d.id] = false; });
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
    .every(d => checked[d.declarationId ?? d.id]);

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

  // Loan details from application aggregate
  const loan = application?.loanRequest ?? {};
  const amount = loan.requestedAmount ?? application?.requestedAmount;
  const term = loan.requestedTermMonths ?? loan.term ?? application?.requestedTermMonths;
  const purpose = loan.loanPurpose ?? application?.loanPurpose;
  const apr = application?.offeredApr ?? application?.apr ?? null;
  const monthlyPayment = application?.monthlyPayment ?? null;

  return (
    <div>
      {/* Offer summary */}
      <div className="section-label">Your Approved Offer</div>
      <div className="offer-summary">
        <div className="offer-summary-item">
          <span className="offer-summary-label">Loan Amount</span>
          <span className="offer-summary-value">{fmt(amount, '$')}</span>
        </div>
        <div className="offer-summary-item">
          <span className="offer-summary-label">Term</span>
          <span className="offer-summary-value">{term ? `${term} months` : '—'}</span>
        </div>
        {apr !== null && (
          <div className="offer-summary-item">
            <span className="offer-summary-label">APR</span>
            <span className="offer-summary-value">{apr}%</span>
          </div>
        )}
        {monthlyPayment !== null && (
          <div className="offer-summary-item">
            <span className="offer-summary-label">Monthly Payment</span>
            <span className="offer-summary-value">{fmt(monthlyPayment, '$')}</span>
          </div>
        )}
        {purpose && (
          <div className="offer-summary-item">
            <span className="offer-summary-label">Purpose</span>
            <span className="offer-summary-value">{String(purpose).replace(/_/g, ' ')}</span>
          </div>
        )}
      </div>

      <hr className="divider" />

      {/* E-sign section */}
      <div className="section-label">Declarations &amp; E-Signature</div>

      {submitted ? (
        <div className="esign-success">
          <div className="esign-success-icon">✓</div>
          <h3>E-Signature Submitted</h3>
          <p>Your offer has been accepted. Your application is now being finalised.</p>
        </div>
      ) : (
        <>
          <p style={{ color: '#5a6a7e', fontSize: '0.93rem', marginBottom: 20 }}>
            Please read and accept all mandatory declarations below to complete your loan offer acceptance.
            Items marked <strong style={{ color: '#b71c1c' }}>*</strong> are required.
          </p>

          {loading && <div className="alert-info">Loading declarations...</div>}
          {error && <div className="alert-error">{error}</div>}

          {!loading && !error && (
            <form onSubmit={handleSubmit}>
              <div className="declarations-list">
                {declarations.map(d => {
                  const id = d.declarationId ?? d.id;
                  return (
                    <label key={id} className={`declaration-item${checked[id] ? ' declaration-item--checked' : ''}`}>
                      <input
                        type="checkbox"
                        checked={!!checked[id]}
                        onChange={() => toggle(id)}
                        className="declaration-checkbox"
                      />
                      <div className="declaration-body">
                        {d.title && (
                          <div className="declaration-title">
                            {d.mandatory && <span className="declaration-required">*</span>}
                            {d.title}
                          </div>
                        )}
                        <div className="declaration-text">
                          {d.text ?? d.content ?? d.description}
                        </div>
                      </div>
                    </label>
                  );
                })}
              </div>

              {submitError && <div className="alert-error" style={{ marginTop: 16 }}>{submitError}</div>}

              <button
                type="submit"
                className="btn-primary"
                disabled={!mandatoryAll || submitting}
                style={{ marginTop: 24 }}
              >
                {submitting ? 'Submitting…' : 'Accept and Sign'}
              </button>
            </form>
          )}
        </>
      )}
    </div>
  );
}
