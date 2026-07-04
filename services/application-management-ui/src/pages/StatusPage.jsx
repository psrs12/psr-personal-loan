import React, { useEffect, useRef, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header.jsx';
import OfferAcceptanceMfe from '../components/OfferAcceptanceMfe.jsx';
import { getApplication } from '../api/client.js';
import { useWebComponentScript, useCustomEvent } from '../hooks/useWebComponent.js';
import { API } from '../api/config.js';
import { PROCESSING_STATES, UNDER_REVIEW_STATES } from '../navigation/navigationConfig.js';
import { resolveScreen, UnmappedApplicationStatusError } from '../navigation/resolveScreen.js';

const POLL_INTERVAL_MS = 8000;

const WEB_COMPONENT_SCRIPTS = {
  pricingOffersUiJs: (pricingScript) => pricingScript,
  documentManagementUiJs: (_pricingScript, documentScript) => documentScript,
};

export default function StatusPage() {
  const navigate = useNavigate();
  const applicationId = sessionStorage.getItem('applicationId');

  const offerConfirmedKey = `offerConfirmed:${applicationId}`;

  const [application, setApplication] = useState(null);
  const [loadError, setLoadError] = useState(null);
  const [offerConfirmed, setOfferConfirmed] = useState(
    () => sessionStorage.getItem(offerConfirmedKey) === 'true'
  );
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
      const status = data.applicationStatus ?? data.status;
      setApplication(data);
      if (status !== 'OFFER_PENDING') {
        sessionStorage.removeItem(offerConfirmedKey);
      }
      if (shouldPoll(status)) {
        pollRef.current = setTimeout(fetchStatus, POLL_INTERVAL_MS);
      }
    } catch (e) {
      if (e.status === 401 || e.status === 403) {
        sessionStorage.clear();
        navigate('/portal/login');
        return;
      }
      setLoadError(e.message);
      pollRef.current = setTimeout(fetchStatus, POLL_INTERVAL_MS);
    }
  }

  function shouldPoll(status) {
    return (
      PROCESSING_STATES.has(status) ||
      UNDER_REVIEW_STATES.has(status) ||
      (status === 'OFFER_PENDING' && sessionStorage.getItem(offerConfirmedKey) === 'true')
    );
  }

  const handleOfferConfirmed = useCallback(() => {
    sessionStorage.setItem(offerConfirmedKey, 'true');
    setOfferConfirmed(true);
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

  let descriptor;
  let resolveError = null;
  try {
    descriptor = resolveScreen(status);
  } catch (e) {
    if (e instanceof UnmappedApplicationStatusError) {
      resolveError = e;
    } else {
      throw e;
    }
  }

  return (
    <div className="page-shell">
      <Header />
      {resolveError
        ? renderUnmappedStatusFallback(resolveError, status)
        : renderScreen(descriptor, { status, application, applicationId, pricingRef, pricingScript, documentScript, offerConfirmed })}
    </div>
  );
}

function renderUnmappedStatusFallback(error, status) {
  // eslint-disable-next-line no-console
  console.error(error);
  return (
    <div className="status-center">
      <div className="spinner" />
      <h3 style={{ color: '#1a1a1a' }}>Processing</h3>
      <p style={{ color: '#6b7280' }}>Status: {status}</p>
    </div>
  );
}

function renderScreen(descriptor, ctx) {
  switch (descriptor.kind) {
    case 'spinner':
      return renderSpinner(descriptor);
    case 'web-component':
      return renderWebComponent(descriptor, ctx);
    case 'internal-mfe':
      return renderInternalMfe(descriptor, ctx);
    case 'static-block':
      return renderStaticBlock(descriptor, ctx);
    default:
      return renderUnmappedStatusFallback(new Error(`Unknown descriptor kind: ${descriptor.kind}`), ctx.status);
  }
}

function renderSpinner(descriptor) {
  return (
    <div className="status-center">
      <div className="spinner" />
      <h3 style={{ color: '#1a1a1a' }}>{descriptor.label}</h3>
      <p style={{ color: '#6b7280' }}>
        {descriptor.description.split('\n').map((line, i) => (
          <React.Fragment key={i}>
            {i > 0 && <br />}
            {line}
          </React.Fragment>
        ))}
      </p>
    </div>
  );
}

function renderWebComponent(descriptor, { status, applicationId, pricingRef, pricingScript, documentScript, offerConfirmed }) {
  const script = WEB_COMPONENT_SCRIPTS[descriptor.scriptUrlKey](pricingScript, documentScript);
  const apiBaseUrl = API[descriptor.apiBaseUrlKey];
  const sessionToken = sessionStorage.getItem('sessionToken');

  if (script.error) {
    return (
      <div className="card">
        <div className="alert-error">Unable to load {descriptor.tag}: {script.error}</div>
      </div>
    );
  }

  if (descriptor.tag === 'pricing-offer-selector') {
    const overlayHidden = offerConfirmed || !script.loaded;
    return (
      <>
        {!script.loaded && (
          <div className="status-center">
            <div className="spinner" />
            <p style={{ color: '#6b7280' }}>Loading your offers...</p>
          </div>
        )}
        {offerConfirmed && (
          <div className="status-center">
            <div className="spinner" />
            <h3 style={{ color: '#1a1a1a' }}>Offer Confirmed</h3>
            <p style={{ color: '#6b7280' }}>We're now running a full credit check. This page will update automatically.</p>
          </div>
        )}
        <div style={{ maxWidth: 760, margin: '40px auto', padding: '0 20px', display: overlayHidden ? 'none' : 'block' }}>
          <pricing-offer-selector
            ref={pricingRef}
            application-id={applicationId}
            api-base-url={apiBaseUrl}
            session-token={sessionToken}
          />
        </div>
      </>
    );
  }

  if (!script.loaded) {
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
        api-base-url={apiBaseUrl}
        session-token={sessionToken}
      />
    </div>
  );
}

function renderInternalMfe(descriptor, { applicationId, application }) {
  if (descriptor.component === 'OfferAcceptanceMfe') {
    return (
      <div className="card" style={{ maxWidth: 700 }}>
        <h2>Your Loan Has Been Approved</h2>
        <p className="card-subtitle">Congratulations! Review your offer and accept the declarations below to proceed.</p>
        <hr className="divider" />
        <OfferAcceptanceMfe applicationId={applicationId} application={application} />
      </div>
    );
  }
  return null;
}

function renderStaticBlock(descriptor, { status }) {
  if (descriptor.block === 'declined') {
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

  if (descriptor.block === 'cancelled-expired') {
    return (
      <div className="status-center">
        <h2 style={{ color: '#6b7280' }}>Application {descriptor.label}</h2>
        <p style={{ color: '#9ca3af' }}>This application is no longer active. Please contact support if you believe this is an error.</p>
      </div>
    );
  }

  if (descriptor.block === 'post-acceptance') {
    return (
      <div className="status-center">
        <div style={{ fontSize: '3.5rem', marginBottom: 16 }}>✓</div>
        <h2 style={{ color: '#15803d' }}>{descriptor.label}</h2>
        <p style={{ color: '#6b7280', maxWidth: 460, margin: '0 auto' }}>{descriptor.message}</p>
      </div>
    );
  }

  return (
    <div className="status-center">
      <p style={{ color: '#6b7280' }}>Status: {status}</p>
    </div>
  );
}
