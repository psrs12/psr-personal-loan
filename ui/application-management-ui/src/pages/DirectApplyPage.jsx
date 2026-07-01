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

const CITIZENSHIP_STATUSES = [
  { value: 'US_CITIZEN', label: 'US Citizen' },
  { value: 'PERMANENT_RESIDENT', label: 'Permanent Resident (Green Card)' },
  { value: 'VISA_HOLDER', label: 'Visa Holder' },
  { value: 'OTHER', label: 'Other' },
];

const TABS = [
  { id: 0, label: 'Personal Details' },
  { id: 1, label: 'Employment & Loan' },
  { id: 2, label: 'Identity & Citizenship' },
];

export default function DirectApplyPage() {
  const navigate = useNavigate();
  const [tab, setTab] = useState(0);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const [invitationToken, setInvitationToken] = useState('');
  const [validatingToken, setValidatingToken] = useState(false);
  const [tokenError, setTokenError] = useState(null);
  const [prefilled, setPrefilled] = useState(false);

  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    dateOfBirth: '',
    addressLine1: '',
    addressLine2: '',
    city: '',
    state: 'CA',
    postcode: '',
    employmentType: 'FULL_TIME',
    employerName: '',
    annualIncome: '',
    monthlyObligations: '',
    requestedAmount: '',
    requestedTermMonths: '36',
    loanPurpose: 'DEBT_CONSOLIDATION',
    ssn: '',
    citizenshipStatus: 'US_CITIZEN',
    offerId: '',
    campaignOfferId: '',
  });

  function set(field, value) {
    setForm(f => ({ ...f, [field]: value }));
  }

  async function applyInvitationToken() {
    if (!invitationToken.trim()) return;
    setValidatingToken(true);
    setTokenError(null);
    try {
      const data = await validateInvitation(invitationToken.trim());
      setForm(f => ({
        ...f,
        firstName: data.firstName ?? f.firstName,
        lastName: data.lastName ?? f.lastName,
        addressLine1: data.address?.line1 ?? f.addressLine1,
        addressLine2: data.address?.line2 ?? f.addressLine2,
        city: data.address?.city ?? f.city,
        state: data.address?.state ?? f.state,
        postcode: data.address?.postcode ?? f.postcode,
        requestedAmount: data.requestedAmount ?? f.requestedAmount,
        requestedTermMonths: data.requestedTermMonths ?? f.requestedTermMonths,
        offerId: data.offerId ?? f.offerId,
        campaignOfferId: data.campaignOfferId ?? f.campaignOfferId,
      }));
      setPrefilled(true);
    } catch (e) {
      setTokenError(e.message);
    } finally {
      setValidatingToken(false);
    }
  }

  function nextTab() {
    setError(null);
    setTab(t => t + 1);
  }

  function prevTab() {
    setError(null);
    setTab(t => t - 1);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const payload = {
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        dateOfBirth: form.dateOfBirth,
        ssn: form.ssn.trim(),
        email: form.email.trim(),
        phone: form.phone.trim() || undefined,
        citizenshipStatus: form.citizenshipStatus,
        address: {
          line1: form.addressLine1.trim(),
          line2: form.addressLine2.trim() || undefined,
          city: form.city.trim(),
          state: form.state,
          postcode: form.postcode.trim(),
        },
        employmentType: form.employmentType,
        employerName: form.employerName.trim() || undefined,
        annualIncome: Number(form.annualIncome),
        monthlyObligations: Number(form.monthlyObligations),
        requestedAmount: Number(form.requestedAmount),
        requestedTermMonths: Number(form.requestedTermMonths),
        loanPurpose: form.loanPurpose,
        invitationToken: invitationToken.trim() || undefined,
        offerId: form.offerId || undefined,
        campaignOfferId: form.campaignOfferId || undefined,
      };
      await createApplication(payload);
      navigate('/portal/login');
    } catch (e) {
      setError(e.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page-shell">
      <Header />
      <div className="card" style={{ maxWidth: 700 }}>
        <h2>Apply for a Personal Loan</h2>
        <p className="card-subtitle">Complete all three steps to submit your application.</p>

        <div className="tab-bar">
          {TABS.map(t => (
            <button
              key={t.id}
              type="button"
              className={`tab-btn${tab === t.id ? ' active' : ''}${t.id > tab ? ' disabled' : ''}`}
              onClick={() => t.id < tab && setTab(t.id)}
            >
              <span className="tab-number">{t.id + 1}</span>
              {t.label}
            </button>
          ))}
        </div>

        <form onSubmit={handleSubmit}>
          {tab === 0 && (
            <div>
              <div className="section-label" style={{ marginTop: 0 }}>Invitation Token (Optional)</div>
              <p style={{ fontSize: '0.88rem', color: '#5a6a7e', marginBottom: 12 }}>
                If you received an invitation, enter your token below to pre-fill your details.
              </p>
              <div style={{ display: 'flex', gap: 10, marginBottom: 8 }}>
                <input
                  type="text"
                  value={invitationToken}
                  onChange={e => setInvitationToken(e.target.value)}
                  placeholder="Enter invitation token"
                  style={{ flex: 1, padding: '10px 14px', border: '1px solid #b0bec5', borderRadius: 5, fontSize: '0.95rem' }}
                />
                <button
                  type="button"
                  onClick={applyInvitationToken}
                  disabled={validatingToken || !invitationToken.trim()}
                  style={{ background: '#1565c0', color: '#fff', border: 'none', borderRadius: 5, padding: '10px 18px', fontWeight: 600, cursor: 'pointer', whiteSpace: 'nowrap' }}
                >
                  {validatingToken ? 'Applying...' : 'Apply Token'}
                </button>
              </div>
              {tokenError && <div className="alert-error" style={{ marginBottom: 12 }}>{tokenError}</div>}
              {prefilled && <div className="prefill-notice">Details pre-filled from your invitation. Name and address are read-only.</div>}

              <hr className="divider" />
              <div className="section-label">Personal Details</div>

              <div className="form-row">
                <div className="form-group">
                  <label>First Name *</label>
                  <input type="text" value={form.firstName} onChange={e => set('firstName', e.target.value)} required readOnly={prefilled} placeholder="Jane" />
                </div>
                <div className="form-group">
                  <label>Last Name *</label>
                  <input type="text" value={form.lastName} onChange={e => set('lastName', e.target.value)} required readOnly={prefilled} placeholder="Smith" />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Email Address *</label>
                  <input type="email" value={form.email} onChange={e => set('email', e.target.value)} required placeholder="jane@example.com" />
                </div>
                <div className="form-group">
                  <label>Phone Number</label>
                  <input type="tel" value={form.phone} onChange={e => set('phone', e.target.value)} placeholder="+1 555 000 0000" />
                </div>
              </div>

              <hr className="divider" />
              <div className="section-label">Home Address</div>

              <div className="form-group">
                <label>Address Line 1 *</label>
                <input type="text" value={form.addressLine1} onChange={e => set('addressLine1', e.target.value)} required readOnly={prefilled} placeholder="123 Main Street" />
              </div>
              <div className="form-group">
                <label>Address Line 2</label>
                <input type="text" value={form.addressLine2} onChange={e => set('addressLine2', e.target.value)} readOnly={prefilled} placeholder="Apt 4B" />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>City *</label>
                  <input type="text" value={form.city} onChange={e => set('city', e.target.value)} required readOnly={prefilled} placeholder="Los Angeles" />
                </div>
                <div className="form-group">
                  <label>State *</label>
                  <select value={form.state} onChange={e => set('state', e.target.value)} required disabled={prefilled}>
                    {US_STATES.map(s => <option key={s} value={s}>{s}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label>ZIP Code *</label>
                  <input type="text" value={form.postcode} onChange={e => set('postcode', e.target.value)} required readOnly={prefilled} placeholder="90001" maxLength={10} />
                </div>
              </div>

              <div className="tab-actions">
                <span />
                <button type="button" className="btn-primary" style={{ width: 'auto' }} onClick={nextTab}>
                  Next: Employment &amp; Loan →
                </button>
              </div>
            </div>
          )}

          {tab === 1 && (
            <div>
              <div className="section-label" style={{ marginTop: 0 }}>Employment Details</div>

              <div className="form-row">
                <div className="form-group">
                  <label>Employment Type *</label>
                  <select value={form.employmentType} onChange={e => set('employmentType', e.target.value)} required>
                    {EMPLOYMENT_TYPES.map(t => <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label>Employer Name</label>
                  <input type="text" value={form.employerName} onChange={e => set('employerName', e.target.value)} placeholder="Acme Corp" />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Annual Income ($) *</label>
                  <input type="number" min="0" step="1" value={form.annualIncome} onChange={e => set('annualIncome', e.target.value)} required placeholder="65000" />
                </div>
                <div className="form-group">
                  <label>Monthly Obligations ($) *</label>
                  <input type="number" min="0" step="1" value={form.monthlyObligations} onChange={e => set('monthlyObligations', e.target.value)} required placeholder="800" />
                </div>
              </div>

              <hr className="divider" />
              <div className="section-label">Loan Details</div>

              <div className="form-row">
                <div className="form-group">
                  <label>Requested Amount ($) *</label>
                  <input type="number" min="1000" max="100000" step="500" value={form.requestedAmount} onChange={e => set('requestedAmount', e.target.value)} required placeholder="15000" />
                </div>
                <div className="form-group">
                  <label>Term *</label>
                  <select value={form.requestedTermMonths} onChange={e => set('requestedTermMonths', e.target.value)} required>
                    <option value="12">12 months</option>
                    <option value="24">24 months</option>
                    <option value="36">36 months</option>
                    <option value="48">48 months</option>
                    <option value="60">60 months</option>
                    <option value="72">72 months</option>
                  </select>
                </div>
              </div>

              <div className="form-group">
                <label>Loan Purpose *</label>
                <select value={form.loanPurpose} onChange={e => set('loanPurpose', e.target.value)} required>
                  {LOAN_PURPOSES.map(p => <option key={p} value={p}>{p.replace(/_/g, ' ')}</option>)}
                </select>
              </div>

              <div className="tab-actions">
                <button type="button" className="btn-back" onClick={prevTab}>← Back</button>
                <button type="button" className="btn-primary" style={{ width: 'auto' }} onClick={nextTab}>
                  Next: Identity →
                </button>
              </div>
            </div>
          )}

          {tab === 2 && (
            <div>
              <div className="section-label" style={{ marginTop: 0 }}>Identity Verification</div>
              <p style={{ fontSize: '0.88rem', color: '#5a6a7e', marginBottom: 16 }}>
                Your information is encrypted and stored securely. SSN is never stored in plain text.
              </p>

              <div className="form-row">
                <div className="form-group">
                  <label>Date of Birth *</label>
                  <input type="date" value={form.dateOfBirth} onChange={e => set('dateOfBirth', e.target.value)} required />
                </div>
                <div className="form-group">
                  <label>Social Security Number *</label>
                  <input
                    type="password"
                    value={form.ssn}
                    onChange={e => set('ssn', e.target.value)}
                    required
                    placeholder="XXX-XX-XXXX"
                    autoComplete="off"
                    maxLength={11}
                  />
                </div>
              </div>

              <hr className="divider" />
              <div className="section-label">Citizenship</div>

              <div className="form-group">
                <label>Citizenship Status *</label>
                <select value={form.citizenshipStatus} onChange={e => set('citizenshipStatus', e.target.value)} required>
                  {CITIZENSHIP_STATUSES.map(c => <option key={c.value} value={c.value}>{c.label}</option>)}
                </select>
              </div>

              <div className="alert-info" style={{ fontSize: '0.85rem', marginTop: 8, marginBottom: 16 }}>
                By submitting this application you authorise us to perform a soft credit check.
                A hard credit check will only be conducted after you select and confirm a loan offer.
              </div>

              {error && <div className="alert-error">{error}</div>}

              <div className="tab-actions">
                <button type="button" className="btn-back" onClick={prevTab}>← Back</button>
                <button type="submit" className="btn-primary" style={{ width: 'auto' }} disabled={submitting}>
                  {submitting ? 'Submitting...' : 'Submit Application'}
                </button>
              </div>
            </div>
          )}
        </form>
      </div>
    </div>
  );
}
