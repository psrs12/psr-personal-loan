import React, { useEffect, useRef, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header.jsx';
import OfferAcceptanceMfe from '../components/OfferAcceptanceMfe.jsx';
import { getApplication } from '../api/client.js';
import { useWebComponentScript, useCustomEvent } from '../hooks/useWebComponent.js';
import { API } from '../api/config.js';

const POLL_INTERVAL_MS = 8000;

const PROCESSING_STATES = new Set(['SUBMITTED', 'PROCESSING', 'STARTED', 'CREATED', 'IN_PROGRESS']);
const UNDER_REVIEW_STATES = new Set(['UNDERWRITING', 'COMPLIANCE_HOLD']);
const POST_ACCEPTANCE_STATES = new Set(['OFFER_ACCEPTED', 'FUNDING_PENDING', 'FUNDED', 'COMPLETED']);

export default function StatusPage() {
  const navigate = useNavigate();
  const applicationId = sessionStorage.getItem('applicationId');

  const [application, setApplication] = useState(null);
  const [loadError, setLoadError] = useState(null);
  const pollRef = useRef(null);

  const pricingScript = useWebComponentScript(API.pricingOffersUiJs);
  const documentScript = useWebComponentScript(API.documentManagementUiJs);

  const pricingRef = useRef(null);

  useEffect(() => {
    if (!applicationId) {
      navigate('/portal/login');
      return;
    }
    fetchStatus();
    return () => { if (pollRef.current) clearTimeout(pollRef.current); };
  }, []);

  async function fetchStatus() {
    try {
      const data = await getApplication(applicationId);
      setApplication(data);
      const status = data.applicationStatus ?? data.status;
      if (shouldPoll(status)) {
        pollRef.current = setTimeout(fetchStatus, POLL_INTERVAL_MS);
      }
    } catch (e) {
      if (e.message.includes('401') || e.message.includes('403')) {
        sessionStorage.clear();
        navigate('/portal/login');
        return;
      }
      setLoadError(e.message);
      pollRef.current = setTimeout(fetchStatus, POLL_INTERVAL_MS);
    }
  }

  function shouldPoll(status) {
    return PROCESSING_STATES.has(status) || UNDER_REVIEW_STATES.has(status);
  }

  const handleOfferConfirmed = useCallback(() => {
    if (pollRef.current) clearTimeout(pollRef.current);
    setTimeout(fetchStatus, 2000);
  }, []);

  useCustomEvent(pricingRef, 'offer-confirmed', handleOfferConfirmed);

  if (loadError && !application) {
    return (
      <div className="page-shell">
        <Header />
        <div className="card">
          <div className="alert-error">{loadError}</div>
          <button className="btn-primary" onClick={fetchStatus} style={{ marginTop: 16 }}>Retry</button>
        </div>
      </div>
    );
  }

  if (!application) {
    return (
      <div className="page-shell">
        <Header />
        <div className="status-center">
          <div className="spinner" />
          <p style={{ color: '#6b7280' }}>Loading your application...</p>
        </div>
      </div>
    );
  }

  const status = application.applicationStatus ?? application.status;

  return (
    <div className="page-shell">
      <Header />
      {renderContent(status, application, applicationId, pricingRef, pricingScript, documentScript)}
    </div>
  );
}

function renderContent(status, application, applicationId, pricingRef, pricingScript, documentScript) {
  if (PROCESSING_STATES.has(status) && status !== 'PROCESSING') {
    return (
      <div className="status-center">
        <div className="spinner" />
        <h3 style={{ color: '#1a1a1a' }}>Application Under Review</h3>
        <p style={{ color: '#6b7280' }}>Your application is being processed. This usually takes a few minutes.<br />This page will update automatically.</p>
      </div>
    );
  }

  if (status === 'PROCESSING') {
    if (pricingScript.error) {
      return (
        <div className="card">
          <div className="alert-error">Unable to load offer selector: {pricingScript.error}</div>
        </div>
      );
    }
    if (!pricingScript.loaded) {
      return (
        <div className="status-center">
          <div className="spinner" />
          <p style={{ color: '#6b7280' }}>Loading your offers...</p>
        </div>
      );
    }
    return (
      <div style={{ maxWidth: 760, margin: '40px auto', padding: '0 20px' }}>
        <pricing-offer-selector
          ref={pricingRef}
          application-id={applicationId}
          api-base-url={API.pricing}
        />
      </div>
    );
  }

  if (status === 'APPROVED') {
    return (
      <div className="card" style={{ maxWidth: 700 }}>
        <h2>Your Loan Has Been Approved</h2>
        <p className="card-subtitle">Congratulations! Review your offer and accept the declarations below to proceed.</p>
        <hr className="divider" />
        <OfferAcceptanceMfe applicationId={applicationId} application={application} />
      </div>
    );
  }

  if (status === 'DOCUMENTS_REQUIRED') {
    if (documentScript.error) {
      return (
        <div className="card">
          <div className="alert-error">Unable to load document manager: {documentScript.error}</div>
        </div>
      );
    }
    if (!documentScript.loaded) {
      return (
        <div className="status-center">
          <div className="spinner" />
          <p style={{ color: '#6b7280' }}>Loading document portal...</p>
        </div>
      );
    }
    return (
      <div style={{ maxWidth: 800, margin: '40px auto', padding: '0 20px' }}>
        <document-upload-manager
          application-id={applicationId}
          api-base-url={API.document}
          session-token={sessionStorage.getItem('sessionToken')}
        />
      </div>
    );
  }

  if (UNDER_REVIEW_STATES.has(status)) {
    return (
      <div className="status-center">
        <div className="spinner" />
        <h3 style={{ color: '#1a1a1a' }}>Under Review by Our Team</h3>
        <p style={{ color: '#6b7280' }}>Your application is being reviewed by our underwriting team.<br />You will be notified once a decision has been made.</p>
      </div>
    );
  }

  if (POST_ACCEPTANCE_STATES.has(status)) {
    const statusLabel = {
      OFFER_ACCEPTED: 'Offer Accepted',
      FUNDING_PENDING: 'Funding in Progress',
      FUNDED: 'Funded',
      COMPLETED: 'Completed',
    }[status] ?? status;

    return (
      <div className="status-center">
        <div style={{ fontSize: '3.5rem', marginBottom: 16 }}>✓</div>
        <h2 style={{ color: '#15803d' }}>{statusLabel}</h2>
        <p style={{ color: '#6b7280', maxWidth: 460, margin: '0 auto' }}>
          {status === 'OFFER_ACCEPTED' && 'Your offer has been accepted. We are now arranging the transfer of funds.'}
          {status === 'FUNDING_PENDING' && 'Your loan funds are being prepared and will be disbursed shortly.'}
          {status === 'FUNDED' && 'Your loan has been funded. The funds have been sent to your nominated account.'}
          {status === 'COMPLETED' && 'Your loan application is complete. Thank you for choosing us.'}
        </p>
      </div>
    );
  }

  if (status === 'DECLINED') {
    return (
      <div className="status-center" style={{ maxWidth: 520 }}>
        <div style={{ fontSize: '3rem', color: '#b71c1c', marginBottom: 16 }}>✗</div>
        <h2 style={{ color: '#b71c1c' }}>Application Declined</h2>
        <p style={{ color: '#6b7280' }}>
          Unfortunately, we are unable to approve your loan application at this time.
          You will receive a written notice with the reasons for this decision in accordance with the Fair Credit Reporting Act.
        </p>
        <p style={{ color: '#6b7280', fontSize: '0.9rem' }}>
          If you have questions, please contact our support team.
        </p>
      </div>
    );
  }

  if (status === 'CANCELLED' || status === 'EXPIRED') {
    return (
      <div className="status-center">
        <h2 style={{ color: '#6b7280' }}>Application {status === 'CANCELLED' ? 'Cancelled' : 'Expired'}</h2>
        <p style={{ color: '#9ca3af' }}>This application is no longer active. Please contact support if you believe this is an error.</p>
      </div>
    );
  }

  return (
    <div className="status-center">
      <div className="spinner" />
      <h3 style={{ color: '#1a1a1a' }}>Processing</h3>
      <p style={{ color: '#6b7280' }}>Status: {status}</p>
    </div>
  );
}
