import { LitElement, html, css } from 'lit';

const POLL_INTERVAL_MS = 10000;

class PricingOfferSelector extends LitElement {
  static properties = {
    applicationId: { type: String, attribute: 'application-id' },
    apiBaseUrl: { type: String, attribute: 'api-base-url' },
    _offers: { state: true },
    _selectedOffer: { state: true },
    _step: { state: true },
    _loading: { state: true },
    _error: { state: true },
    _confirming: { state: true },
  };

  static styles = css`
    :host {
      display: block;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
      color: #1a1a1a;
    }

    .card {
      background: #fff;
      border: 1px solid #e5e7eb;
      border-radius: 10px;
      padding: 32px 36px;
      max-width: 720px;
      margin: 0 auto;
    }

    h2 {
      margin: 0 0 8px;
      font-size: 1.4rem;
      font-weight: 700;
      color: #1a1a1a;
    }

    .subtitle {
      margin: 0 0 28px;
      color: #6b7280;
      font-size: 0.95rem;
    }

    .waiting {
      text-align: center;
      padding: 48px 0;
      color: #6b7280;
    }

    .spinner {
      display: inline-block;
      width: 44px;
      height: 44px;
      border: 3px solid #e5e7eb;
      border-top-color: #e8520a;
      border-radius: 50%;
      animation: spin 0.85s linear infinite;
      margin-bottom: 20px;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }

    .offer-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .offer-row {
      display: grid;
      grid-template-columns: 1fr 1fr 1fr 1fr auto;
      align-items: center;
      gap: 16px;
      background: #fff8f5;
      border: 1px solid #fbd5c0;
      border-radius: 6px;
      padding: 16px 20px;
      transition: border-color 0.15s, background 0.15s;
    }

    .offer-row:hover {
      border-color: #e8520a;
      background: #fff3ee;
    }

    .label {
      font-size: 0.72rem;
      text-transform: uppercase;
      letter-spacing: 0.07em;
      color: #9ca3af;
      font-weight: 700;
      margin-bottom: 4px;
    }

    .value {
      font-size: 1.05rem;
      font-weight: 700;
      color: #e8520a;
    }

    button.select {
      background: #e8520a;
      color: #fff;
      border: none;
      border-radius: 30px;
      padding: 10px 22px;
      font-size: 0.9rem;
      font-weight: 700;
      cursor: pointer;
      white-space: nowrap;
      transition: background 0.15s;
    }

    button.select:hover {
      background: #c94a0a;
    }

    .consent-box {
      background: #fff8f5;
      border: 1px solid #fbd5c0;
      border-radius: 6px;
      padding: 24px;
      margin-bottom: 24px;
      line-height: 1.7;
      font-size: 0.95rem;
      color: #374151;
    }

    .consent-box strong {
      color: #c94a0a;
    }

    .selected-summary {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 12px;
      margin-bottom: 24px;
    }

    .selected-summary > div {
      background: #fff8f5;
      border: 1px solid #fbd5c0;
      border-radius: 8px;
      padding: 14px 16px;
    }

    .actions {
      display: flex;
      gap: 12px;
    }

    button.confirm {
      flex: 1;
      background: #e8520a;
      color: #fff;
      border: none;
      border-radius: 30px;
      padding: 15px 28px;
      font-size: 1rem;
      font-weight: 700;
      cursor: pointer;
      transition: background 0.15s;
    }

    button.confirm:hover:not(:disabled) {
      background: #c94a0a;
    }

    button.confirm:disabled {
      background: #f5c4ae;
      cursor: not-allowed;
    }

    button.back {
      background: #fff;
      color: #6b7280;
      border: 1.5px solid #d1d5db;
      border-radius: 30px;
      padding: 15px 24px;
      font-size: 1rem;
      font-weight: 600;
      cursor: pointer;
      white-space: nowrap;
      transition: border-color 0.15s, color 0.15s;
    }

    button.back:hover {
      border-color: #9ca3af;
      color: #374151;
    }

    button.retry {
      background: #fff;
      color: #e8520a;
      border: 1.5px solid #e8520a;
      border-radius: 30px;
      padding: 10px 24px;
      font-size: 0.9rem;
      font-weight: 600;
      cursor: pointer;
      margin-top: 16px;
      transition: background 0.15s, color 0.15s;
    }

    button.retry:hover {
      background: #e8520a;
      color: #fff;
    }

    .error-msg {
      color: #b91c1c;
      background: #fff5f5;
      border: 1px solid #fca5a5;
      border-radius: 6px;
      padding: 12px 16px;
      margin-top: 16px;
      font-size: 0.9rem;
    }

    .success {
      text-align: center;
      padding: 40px 0;
      color: #15803d;
    }

    .success-icon {
      font-size: 3rem;
      margin-bottom: 12px;
    }
  `;

  constructor() {
    super();
    this._offers = [];
    this._selectedOffer = null;
    this._step = 'loading';
    this._loading = true;
    this._error = null;
    this._confirming = false;
    this._pollTimer = null;
  }

  connectedCallback() {
    super.connectedCallback();
    this._fetchOffers();
  }

  disconnectedCallback() {
    super.disconnectedCallback();
    if (this._pollTimer) clearTimeout(this._pollTimer);
  }

  async _fetchOffers() {
    this._loading = true;
    this._error = null;
    try {
      const res = await fetch(
        `${this.apiBaseUrl}/applications/${this.applicationId}/offers`,
      );
      if (!res.ok) throw new Error(`Server error: ${res.status}`);
      const data = await res.json();
      const offers = Array.isArray(data) ? data : (data.offers ?? []);
      if (offers.length === 0) {
        this._step = 'waiting';
        this._schedulePoll();
      } else {
        this._offers = offers;
        this._step = 'list';
      }
    } catch (e) {
      this._error = e.message;
      this._step = 'waiting';
    } finally {
      this._loading = false;
    }
  }

  _schedulePoll() {
    if (this._pollTimer) clearTimeout(this._pollTimer);
    this._pollTimer = setTimeout(() => this._fetchOffers(), POLL_INTERVAL_MS);
  }

  _selectOffer(offer) {
    this._selectedOffer = offer;
    this._step = 'consent';
  }

  async _confirmOffer() {
    if (this._confirming) return;
    this._confirming = true;
    this._error = null;
    try {
      const res = await fetch(
        `${this.apiBaseUrl}/applications/${this.applicationId}/offers/confirm`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ offerId: this._selectedOffer.offerId }),
        },
      );
      if (!res.ok) throw new Error(`Server error: ${res.status}`);
      this._step = 'confirmed';
      this.dispatchEvent(
        new CustomEvent('offer-confirmed', {
          detail: { offerId: this._selectedOffer.offerId },
          bubbles: true,
          composed: true,
        }),
      );
    } catch (e) {
      this._error = e.message;
    } finally {
      this._confirming = false;
    }
  }

  _fmt(n, decimals = 2) {
    if (n == null) return '—';
    return Number(n).toFixed(decimals);
  }

  _fmtCurrency(n) {
    if (n == null) return '—';
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(n);
  }

  render() {
    return html`
      <div class="card">
        ${this._step === 'loading' ? this._renderLoading() : ''}
        ${this._step === 'waiting' ? this._renderWaiting() : ''}
        ${this._step === 'list' ? this._renderList() : ''}
        ${this._step === 'consent' ? this._renderConsent() : ''}
        ${this._step === 'confirmed' ? this._renderConfirmed() : ''}
      </div>
    `;
  }

  _renderLoading() {
    return html`
      <div class="waiting">
        <div class="spinner"></div>
        <p>Loading your loan offers...</p>
      </div>
    `;
  }

  _renderWaiting() {
    return html`
      <div class="waiting">
        <div class="spinner"></div>
        <p>Your application is being reviewed, please wait...</p>
        <p style="font-size:0.85rem;color:#9ca3af">We will automatically check for offers every 10 seconds.</p>
        ${this._error ? html`<div class="error-msg">${this._error}</div>` : ''}
        <button class="retry" @click=${this._fetchOffers}>Check Now</button>
      </div>
    `;
  }

  _renderList() {
    return html`
      <h2>Your Personalised Loan Offers</h2>
      <p class="subtitle">Review the offers below and select the one that best suits you. Selecting an offer will initiate a credit check.</p>
      <div class="offer-list">
        ${this._offers.map(o => html`
          <div class="offer-row">
            <div>
              <div class="label">Loan Amount</div>
              <div class="value">${this._fmtCurrency(o.amount ?? o.loanAmount)}</div>
            </div>
            <div>
              <div class="label">Term</div>
              <div class="value">${o.termMonths} months</div>
            </div>
            <div>
              <div class="label">APR</div>
              <div class="value">${this._fmt(o.apr)}%</div>
            </div>
            <div>
              <div class="label">Monthly Payment</div>
              <div class="value">${this._fmtCurrency(o.monthlyPayment)}</div>
            </div>
            <button class="select" @click=${() => this._selectOffer(o)}>Select</button>
          </div>
        `)}
      </div>
      ${this._error ? html`<div class="error-msg">${this._error}</div>` : ''}
    `;
  }

  _renderConsent() {
    const o = this._selectedOffer;
    return html`
      <h2>Confirm Your Offer</h2>
      <p class="subtitle">Please review your selected offer and authorise a credit check to proceed.</p>
      <div class="selected-summary">
        <div>
          <div class="label">Amount</div>
          <div class="value">${this._fmtCurrency(o.amount ?? o.loanAmount)}</div>
        </div>
        <div>
          <div class="label">Term</div>
          <div class="value">${o.termMonths} months</div>
        </div>
        <div>
          <div class="label">APR</div>
          <div class="value">${this._fmt(o.apr)}%</div>
        </div>
        <div>
          <div class="label">Monthly Payment</div>
          <div class="value">${this._fmtCurrency(o.monthlyPayment)}</div>
        </div>
      </div>
      <div class="consent-box">
        <strong>Credit Check Authorisation</strong><br/>
        By clicking "Confirm and Proceed" you authorise us to conduct a <strong>hard credit enquiry</strong> with
        credit reference agencies in connection with your loan application. This may affect your credit score.
        Your personal data will be processed in accordance with our Privacy Policy and the Fair Credit Reporting Act (FCRA).
      </div>
      ${this._error ? html`<div class="error-msg">${this._error}</div>` : ''}
      <div class="actions">
        <button class="back" @click=${() => { this._step = 'list'; this._error = null; }}>Back</button>
        <button class="confirm" ?disabled=${this._confirming} @click=${this._confirmOffer}>
          ${this._confirming ? 'Submitting...' : 'Confirm and Proceed'}
        </button>
      </div>
    `;
  }

  _renderConfirmed() {
    return html`
      <div class="success">
        <div class="success-icon">✓</div>
        <h2 style="color:#15803d">Offer Confirmed</h2>
        <p style="color:#6b7280">Your offer has been confirmed. We are now processing your application.<br/>You will be notified of the final decision shortly.</p>
      </div>
    `;
  }
}

customElements.define('pricing-offer-selector', PricingOfferSelector);
