import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
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

const US_STATES = [
  'AL','AK','AZ','AR','CA','CO','CT','DE','FL','GA',
  'HI','ID','IL','IN','IA','KS','KY','LA','ME','MD',
  'MA','MI','MN','MS','MO','MT','NE','NV','NH','NJ',
  'NM','NY','NC','ND','OH','OK','OR','PA','RI','SC',
  'SD','TN','TX','UT','VT','VA','WA','WV','WI','WY',
];

const EMPTY_FORM = {
  invitationToken: '',
  firstName: '',
  lastName: '',
  dateOfBirth: '',
  ssn: '',
  addressLine1: '',
  city: '',
  state: 'AL',
  postcode: '',
  employmentType: 'FULL_TIME',
  employerName: '',
  annualIncome: '',
  monthlyObligations: '',
  requestedAmount: '',
  requestedTermMonths: '',
  loanPurpose: 'DEBT_CONSOLIDATION',
};

export default function ApplyPage() {
  const navigate = useNavigate();

  const [form, setForm] = useState(EMPTY_FORM);
  const [prefill, setPrefill] = useState(null);      // set when invitation validated
  const [validating, setValidating] = useState(false);
  const [validateError, setValidateError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState(null);

  function set(field, value) {
    setForm(f => ({ ...f, [field]: value }));
  }

  async function handleValidate() {
    if (!form.invitationToken.trim()) return;
    setValidating(true);
    setValidateError(null);
    setPrefill(null);
    try {
      const data = await validateInvitation(form.invitationToken.trim());
      setPrefill(data);
      setForm(f => ({
        ...f,
        firstName: data.firstName ?? '',
        lastName: data.lastName ?? '',
        addressLine1: data.address?.line1 ?? '',
        city: data.address?.city ?? '',
        state: data.address?.state ?? 'AL',
        postcode: data.address?.postcode ?? '',
        requestedAmount: data.requestedAmount ?? '',
        requestedTermMonths: data.requestedTermMonths ?? '',
      }));
    } catch (e) {
      setValidateError(e.message);
    } finally {
      setValidating(false);
    }
  }

  function clearInvitation() {
    setPrefill(null);
    setValidateError(null);
    setForm(f => ({
      ...f,
      invitationToken: '',
      firstName: '',
      lastName: '',
      addressLine1: '',
      city: '',
      state: 'AL',
      postcode: '',
      requestedAmount: '',
      requestedTermMonths: '',
    }));
  }

  const isITA = !!prefill;

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    setSubmitError(null);
    try {
      let payload;
      if (isITA) {
        payload = {
          applicationSource: 'INVITATION',
          invitationToken: form.invitationToken.trim(),
          firstName: form.firstName,
          lastName: form.lastName,
          address: {
            line1: form.addressLine1,
            city: form.city,
            state: form.state,
            postcode: form.postcode,
          },
          offerId: prefill.offerId,
          campaignOfferId: prefill.campaignOfferId,
          requestedAmount: Number(form.requestedAmount),
          requestedTermMonths: Number(form.requestedTermMonths),
          employmentType: form.employmentType,
          employerName: form.employerName,
          annualIncome: Number(form.annualIncome),
          monthlyObligations: Number(form.monthlyObligations),
          loanPurpose: form.loanPurpose,
        };
      } else {
        payload = {
          applicationSource: 'DIRECT',
          firstName: form.firstName,
          lastName: form.lastName,
          dateOfBirth: form.dateOfBirth,
          ssn: form.ssn,
          address: {
            line1: form.addressLine1,
            city: form.city,
            state: form.state,
            postcode: form.postcode,
          },
          employmentType: form.employmentType,
          employerName: form.employerName,
          annualIncome: Number(form.annualIncome),
          monthlyObligations: Number(form.monthlyObligations),
          requestedAmount: Number(form.requestedAmount),
          requestedTermMonths: Number(form.requestedTermMonths),
          loanPurpose: form.loanPurpose,
        };
      }
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
        <h2>Apply for a Personal Loan</h2>
        <p className="card-subtitle">
          If you received an invitation, enter your invitation code below to pre-fill your details.
          Otherwise, leave it blank and complete the form manually.
        </p>

        {/* Invitation token section */}
        <div className="invitation-section">
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label>Invitation Code (optional)</label>
            <div className="invitation-row">
              <input
                type="text"
                value={form.invitationToken}
                onChange={e => { set('invitationToken', e.target.value); if (prefill) clearInvitation(); }}
                placeholder="Enter your invitation code"
                disabled={validating}
              />
              <button
                type="button"
                className="btn-secondary"
                onClick={handleValidate}
                disabled={validating || !form.invitationToken.trim()}
              >
                {validating ? 'Validating…' : 'Validate'}
              </button>
            </div>
          </div>
          {validateError && <div className="alert-error" style={{ marginTop: 10 }}>{validateError}</div>}
          {isITA && (
            <div className="prefill-notice" style={{ marginTop: 10 }}>
              Invitation validated. Your name, address, and loan details have been pre-filled and are read-only.
            </div>
          )}
        </div>

        <hr className="divider" />

        <form onSubmit={handleSubmit}>

          {/* Personal Details */}
          <div className="section-label">Personal Details</div>

          <div className="form-row">
            <div className="form-group">
              <label>First Name</label>
              <input
                type="text"
                value={form.firstName}
                onChange={e => set('firstName', e.target.value)}
                readOnly={isITA}
                required
                placeholder="Jane"
              />
            </div>
            <div className="form-group">
              <label>Last Name</label>
              <input
                type="text"
                value={form.lastName}
                onChange={e => set('lastName', e.target.value)}
                readOnly={isITA}
                required
                placeholder="Smith"
              />
            </div>
          </div>

          {!isITA && (
            <div className="form-row">
              <div className="form-group">
                <label>Date of Birth</label>
                <input
                  type="date"
                  value={form.dateOfBirth}
                  onChange={e => set('dateOfBirth', e.target.value)}
                  required
                />
              </div>
              <div className="form-group">
                <label>Social Security Number</label>
                <input
                  type="password"
                  value={form.ssn}
                  onChange={e => set('ssn', e.target.value)}
                  required
                  placeholder="XXX-XX-XXXX"
                  autoComplete="off"
                />
              </div>
            </div>
          )}

          <div className="form-group">
            <label>Street Address</label>
            <input
              type="text"
              value={form.addressLine1}
              onChange={e => set('addressLine1', e.target.value)}
              readOnly={isITA}
              required
              placeholder="123 Main St"
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>City</label>
              <input
                type="text"
                value={form.city}
                onChange={e => set('city', e.target.value)}
                readOnly={isITA}
                required
                placeholder="Springfield"
              />
            </div>
            <div className="form-group">
              <label>State</label>
              <select
                value={form.state}
                onChange={e => set('state', e.target.value)}
                disabled={isITA}
                required
              >
                {US_STATES.map(s => <option key={s} value={s}>{s}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label>ZIP Code</label>
              <input
                type="text"
                value={form.postcode}
                onChange={e => set('postcode', e.target.value)}
                readOnly={isITA}
                required
                placeholder="62701"
                maxLength={10}
              />
            </div>
          </div>

          <hr className="divider" />

          {/* Employment & Income */}
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

          {/* Loan Details */}
          <div className="section-label">Loan Details</div>

          <div className="form-row">
            <div className="form-group">
              <label>Requested Amount ($)</label>
              <input
                type="number"
                min="1000"
                step="100"
                value={form.requestedAmount}
                onChange={e => set('requestedAmount', e.target.value)}
                readOnly={isITA}
                required
                placeholder="e.g. 15000"
              />
            </div>
            <div className="form-group">
              <label>Term (months)</label>
              <input
                type="number"
                min="12"
                max="84"
                step="12"
                value={form.requestedTermMonths}
                onChange={e => set('requestedTermMonths', e.target.value)}
                readOnly={isITA}
                required
                placeholder="e.g. 36"
              />
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
            {submitting ? 'Submitting…' : 'Submit Application'}
          </button>
        </form>
      </div>
    </div>
  );
}
