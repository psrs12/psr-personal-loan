import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import Header from '../components/Header.jsx';
import { validateInvitation, createApplication } from '../api/client.js';

const LOAN_PURPOSES = [
  'DEBT_CONSOLIDATION',
  'HOME_IMPROVEMENT',
  'MAJOR_PURCHASE',
  'MEDICAL',
  'VEHICLE',
  'BUSINESS',
  'OTHER',
];

const EMPLOYMENT_TYPES = [
  'FULL_TIME',
  'PART_TIME',
  'SELF_EMPLOYED',
  'CONTRACT',
  'RETIRED',
  'UNEMPLOYED',
];

export default function InvitePage() {
  const [params] = useSearchParams();
  const navigate = useNavigate();

  const [prefill, setPrefill] = useState(null);
  const [loadError, setLoadError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState(null);

  const [form, setForm] = useState({
    employmentType: 'FULL_TIME',
    employerName: '',
    annualIncome: '',
    monthlyObligations: '',
    loanPurpose: 'DEBT_CONSOLIDATION',
  });

  useEffect(() => {
    const token = params.get('token');
    if (!token) {
      setLoadError('Missing invitation token. Please use the link provided in your invitation.');
      return;
    }
    validateInvitation(token)
      .then(data => setPrefill({ ...data, token }))
      .catch(e => setLoadError(e.message));
  }, []);

  function set(field, value) {
    setForm(f => ({ ...f, [field]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    setSubmitError(null);
    try {
      const payload = {
        invitationToken: prefill.token,
        firstName: prefill.firstName,
        lastName: prefill.lastName,
        address: prefill.address,
        offerId: prefill.offerId,
        campaignOfferId: prefill.campaignOfferId,
        requestedAmount: prefill.requestedAmount,
        requestedTermMonths: prefill.requestedTermMonths,
        employmentType: form.employmentType,
        employerName: form.employerName,
        annualIncome: Number(form.annualIncome),
        monthlyObligations: Number(form.monthlyObligations),
        loanPurpose: form.loanPurpose,
      };
      await createApplication(payload);
      navigate('/portal/login');
    } catch (e) {
      setSubmitError(e.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page-shell">
      <Header />
      <div className="card">
        <h2>Start Your Loan Application</h2>
        <p className="card-subtitle">Complete your details below to submit your application. Your name and address have been pre-filled from your invitation.</p>

        {loadError && <div className="alert-error">{loadError}</div>}

        {!prefill && !loadError && (
          <div className="alert-info">Validating your invitation...</div>
        )}

        {prefill && (
          <form onSubmit={handleSubmit}>
            <div className="prefill-notice">
              Your name and address are pre-filled from your invitation and cannot be edited.
            </div>

            <div className="section-label">Personal Details</div>

            <div className="form-row">
              <div className="form-group">
                <label>First Name</label>
                <input type="text" value={prefill.firstName ?? ''} readOnly />
              </div>
              <div className="form-group">
                <label>Last Name</label>
                <input type="text" value={prefill.lastName ?? ''} readOnly />
              </div>
            </div>

            <div className="form-group">
              <label>Address</label>
              <input
                type="text"
                value={prefill.address ? `${prefill.address.line1 ?? ''} ${prefill.address.city ?? ''} ${prefill.address.state ?? ''} ${prefill.address.postcode ?? ''}`.trim() : ''}
                readOnly
              />
            </div>

            <hr className="divider" />
            <div className="section-label">Employment &amp; Income</div>

            <div className="form-row">
              <div className="form-group">
                <label>Employment Type</label>
                <select value={form.employmentType} onChange={e => set('employmentType', e.target.value)} required>
                  {EMPLOYMENT_TYPES.map(t => (
                    <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label>Employer Name</label>
                <input
                  type="text"
                  value={form.employerName}
                  onChange={e => set('employerName', e.target.value)}
                  placeholder="e.g. Acme Corp"
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Annual Income ($)</label>
                <input
                  type="number"
                  min="0"
                  step="1"
                  value={form.annualIncome}
                  onChange={e => set('annualIncome', e.target.value)}
                  required
                  placeholder="e.g. 65000"
                />
              </div>
              <div className="form-group">
                <label>Monthly Obligations ($)</label>
                <input
                  type="number"
                  min="0"
                  step="1"
                  value={form.monthlyObligations}
                  onChange={e => set('monthlyObligations', e.target.value)}
                  required
                  placeholder="e.g. 800"
                />
              </div>
            </div>

            <hr className="divider" />
            <div className="section-label">Loan Details</div>

            <div className="form-row">
              <div className="form-group">
                <label>Requested Amount ($)</label>
                <input type="number" value={prefill.requestedAmount ?? ''} readOnly />
              </div>
              <div className="form-group">
                <label>Term (months)</label>
                <input type="number" value={prefill.requestedTermMonths ?? ''} readOnly />
              </div>
            </div>

            <div className="form-group">
              <label>Loan Purpose</label>
              <select value={form.loanPurpose} onChange={e => set('loanPurpose', e.target.value)} required>
                {LOAN_PURPOSES.map(p => (
                  <option key={p} value={p}>{p.replace(/_/g, ' ')}</option>
                ))}
              </select>
            </div>

            {submitError && <div className="alert-error">{submitError}</div>}

            <button type="submit" className="btn-primary" disabled={submitting}>
              {submitting ? 'Submitting...' : 'Submit Application'}
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
