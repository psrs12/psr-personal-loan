import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header.jsx';
import { createApplication } from '../api/client.js';

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

export default function DirectApplyPage() {
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    dateOfBirth: '',
    ssn: '',
    email: '',
    phone: '',
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
  });

  function set(field, value) {
    setForm(f => ({ ...f, [field]: value }));
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
        phone: form.phone.trim(),
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
        <p className="card-subtitle">Complete the form below to start your loan application. All fields marked * are required.</p>

        {error && <div className="alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="section-label">Personal Details</div>

          <div className="form-row">
            <div className="form-group">
              <label>First Name *</label>
              <input type="text" value={form.firstName} onChange={e => set('firstName', e.target.value)} required placeholder="Jane" />
            </div>
            <div className="form-group">
              <label>Last Name *</label>
              <input type="text" value={form.lastName} onChange={e => set('lastName', e.target.value)} required placeholder="Smith" />
            </div>
          </div>

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
            <input type="text" value={form.addressLine1} onChange={e => set('addressLine1', e.target.value)} required placeholder="123 Main Street" />
          </div>
          <div className="form-group">
            <label>Address Line 2</label>
            <input type="text" value={form.addressLine2} onChange={e => set('addressLine2', e.target.value)} placeholder="Apt 4B" />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>City *</label>
              <input type="text" value={form.city} onChange={e => set('city', e.target.value)} required placeholder="Los Angeles" />
            </div>
            <div className="form-group">
              <label>State *</label>
              <select value={form.state} onChange={e => set('state', e.target.value)} required>
                {US_STATES.map(s => <option key={s} value={s}>{s}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label>ZIP Code *</label>
              <input type="text" value={form.postcode} onChange={e => set('postcode', e.target.value)} required placeholder="90001" maxLength={10} />
            </div>
          </div>

          <hr className="divider" />
          <div className="section-label">Employment &amp; Income</div>

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

          <div className="alert-info" style={{ fontSize: '0.85rem', marginTop: 8 }}>
            By submitting this application you authorise us to perform a soft credit check. A hard credit check will only
            be conducted after you select and confirm a loan offer.
          </div>

          {error && <div className="alert-error">{error}</div>}

          <button type="submit" className="btn-primary" disabled={submitting} style={{ marginTop: 16 }}>
            {submitting ? 'Submitting...' : 'Submit Application'}
          </button>
        </form>
      </div>
    </div>
  );
}
