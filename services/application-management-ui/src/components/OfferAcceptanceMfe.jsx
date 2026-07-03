import React, { useEffect, useState } from 'react';
import { getDeclarations, getSelectedOffer, submitESign } from '../api/client.js';

const DECLARATIONS_RETRY_DELAY_MS = 2000;
const DECLARATIONS_MAX_ATTEMPTS = 5;

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
  const [offer, setOffer] = useState(null);

  useEffect(() => {
    let cancelled = false;

    // The offer-acceptance session is created asynchronously from a Kafka event fired when
    // the application reaches APPROVED, so it may not exist yet the instant this page loads.
    // Retry a few times before surfacing an error to the applicant.
    async function loadDeclarations(attempt) {
      try {
        const data = await getDeclarations(applicationId);
        if (cancelled) return;
        const list = Array.isArray(data) ? data : (data.declarations ?? []);
        setDeclarations(list);
        const initial = {};
        list.forEach(d => { initial[d.declarationId ?? d.id] = false; });
        setChecked(initial);
        setLoading(false);
      } catch (e) {
        if (cancelled) return;
        if (e.status === 404 && attempt < DECLARATIONS_MAX_ATTEMPTS) {
          setTimeout(() => loadDeclarations(attempt + 1), DECLARATIONS_RETRY_DELAY_MS);
          return;
        }
        setError(e.message);
        setLoading(false);
      }
    }

    loadDeclarations(1);
    getSelectedOffer(applicationId).then(data => { if (!cancelled) setOffer(data); }).catch(() => {});

    return () => { cancelled = true; };
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

  // Loan details from the applicant's confirmed pricing offer (pricing-orchestration-service
  // owns offer data; application-management-service's status response has no loan fields).
  const amount = offer?.approvedAmount ?? null;
  const term = offer?.termMonths ?? null;
  const purpose = application?.loanPurpose ?? null;
  const apr = offer?.apr ?? null;
  const monthlyPayment = offer?.monthlyRepayment ?? null;

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
          <p style={{ color: '#6b7280', fontSize: '0.93rem', marginBottom: 20 }}>
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
