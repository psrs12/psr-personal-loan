import { LitElement, html, css } from 'lit';

const STATUS_LABEL = {
  PENDING: 'Pending',
  UPLOADED: 'Uploaded',
  SCANNING: 'Scanning',
  COMPLETED: 'Completed',
  REJECTED: 'Rejected',
};

const STATUS_COLOR = {
  PENDING: '#9ca3af',
  UPLOADED: '#e8520a',
  SCANNING: '#c94a0a',
  COMPLETED: '#15803d',
  REJECTED: '#b91c1c',
};

class DocumentUploadManager extends LitElement {
  static properties = {
    applicationId: { type: String, attribute: 'application-id' },
    apiBaseUrl: { type: String, attribute: 'api-base-url' },
    sessionToken: { type: String, attribute: 'session-token' },
    _requirements: { state: true },
    _loading: { state: true },
    _error: { state: true },
    _uploading: { state: true },
    _uploadErrors: { state: true },
  };

  static styles = css`
    :host {
      display: block;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
      color: #1a1a1a;
    }

    .shell {
      max-width: 760px;
      margin: 0 auto;
    }

    h2 {
      margin: 0 0 6px;
      font-size: 1.4rem;
      font-weight: 700;
      color: #1a1a1a;
    }

    .subtitle {
      margin: 0 0 24px;
      color: #6b7280;
      font-size: 0.95rem;
    }

    .progress-bar-track {
      background: #e5e7eb;
      border-radius: 999px;
      height: 10px;
      margin-bottom: 6px;
    }

    .progress-bar-fill {
      background: #e8520a;
      border-radius: 999px;
      height: 10px;
      transition: width 0.4s ease;
    }

    .progress-label {
      font-size: 0.85rem;
      color: #6b7280;
      margin-bottom: 28px;
    }

    .req-card {
      background: #fff;
      border: 1px solid #e5e7eb;
      border-radius: 10px;
      padding: 20px 24px;
      margin-bottom: 16px;
    }

    .req-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
    }

    .req-type {
      font-weight: 700;
      font-size: 1rem;
      color: #1a1a1a;
    }

    .status-badge {
      font-size: 0.78rem;
      font-weight: 700;
      padding: 3px 10px;
      border-radius: 999px;
      background: #f9fafb;
    }

    .upload-row {
      display: flex;
      gap: 12px;
      align-items: center;
      flex-wrap: wrap;
    }

    input[type="file"] {
      flex: 1;
      min-width: 200px;
      font-size: 0.9rem;
      color: #374151;
    }

    button.upload-btn {
      background: #e8520a;
      color: #fff;
      border: none;
      border-radius: 30px;
      padding: 9px 22px;
      font-size: 0.9rem;
      font-weight: 700;
      cursor: pointer;
      white-space: nowrap;
      transition: background 0.15s;
    }

    button.upload-btn:hover:not(:disabled) {
      background: #c94a0a;
    }

    button.upload-btn:disabled {
      background: #f5c4ae;
      cursor: not-allowed;
    }

    .upload-error {
      color: #b91c1c;
      font-size: 0.85rem;
      margin-top: 8px;
    }

    .all-done {
      text-align: center;
      padding: 48px 0;
      color: #15803d;
    }

    .all-done-icon {
      font-size: 3.5rem;
      margin-bottom: 12px;
    }

    .loading-msg {
      text-align: center;
      padding: 40px 0;
      color: #6b7280;
    }

    .global-error {
      color: #b91c1c;
      background: #fff5f5;
      border: 1px solid #fca5a5;
      border-radius: 6px;
      padding: 12px 16px;
      margin-bottom: 20px;
    }

    .req-meta {
      font-size: 0.82rem;
      color: #9ca3af;
      margin-bottom: 10px;
    }
  `;

  constructor() {
    super();
    this._requirements = [];
    this._loading = true;
    this._error = null;
    this._uploading = {};
    this._uploadErrors = {};
  }

  connectedCallback() {
    super.connectedCallback();
    this._fetchRequirements();
  }

  get _headers() {
    return { Authorization: `Bearer ${this.sessionToken}` };
  }

  async _fetchRequirements() {
    this._loading = true;
    this._error = null;
    try {
      const res = await fetch(
        `${this.apiBaseUrl}/applications/${this.applicationId}/documents/requirements`,
        { headers: this._headers },
      );
      if (!res.ok) throw new Error(`Server error: ${res.status}`);
      const data = await res.json();
      this._requirements = Array.isArray(data) ? data : (data.requirements ?? []);
    } catch (e) {
      this._error = e.message;
    } finally {
      this._loading = false;
    }
  }

  async _upload(req, file) {
    const docType = req.documentType;
    this._uploading = { ...this._uploading, [docType]: true };
    this._uploadErrors = { ...this._uploadErrors, [docType]: null };

    const form = new FormData();
    form.append('documentType', docType);
    form.append('file', file);

    try {
      const res = await fetch(
        `${this.apiBaseUrl}/applications/${this.applicationId}/documents/upload`,
        { method: 'POST', headers: this._headers, body: form },
      );
      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.detail ?? `Upload failed: ${res.status}`);
      }
      await this._fetchRequirements();
    } catch (e) {
      this._uploadErrors = { ...this._uploadErrors, [docType]: e.message };
    } finally {
      this._uploading = { ...this._uploading, [docType]: false };
    }
  }

  _handleFileChange(req, e) {
    const file = e.target.files[0];
    if (file) this._upload(req, file);
  }

  get _completedCount() {
    return this._requirements.filter(r => r.status === 'COMPLETED').length;
  }

  get _allComplete() {
    return this._requirements.length > 0 && this._completedCount === this._requirements.length;
  }

  render() {
    if (this._loading) {
      return html`<div class="loading-msg">Loading document requirements...</div>`;
    }

    return html`
      <div class="shell">
        <h2>Document Submission</h2>
        <p class="subtitle">Please upload the required documents to support your loan application.</p>

        ${this._error ? html`<div class="global-error">${this._error}</div>` : ''}

        ${this._requirements.length > 0 ? html`
          <div class="progress-bar-track">
            <div class="progress-bar-fill" style="width:${(this._completedCount / this._requirements.length) * 100}%"></div>
          </div>
          <div class="progress-label">${this._completedCount} of ${this._requirements.length} documents completed</div>
        ` : ''}

        ${this._allComplete ? this._renderAllDone() : this._requirements.map(r => this._renderCard(r))}
      </div>
    `;
  }

  _renderCard(req) {
    const docType = req.documentType;
    const isUploading = this._uploading[docType];
    const uploadError = this._uploadErrors[docType];
    const isDone = req.status === 'COMPLETED';
    const isRejected = req.status === 'REJECTED';
    const statusColor = STATUS_COLOR[req.status] ?? '#9ca3af';

    return html`
      <div class="req-card">
        <div class="req-header">
          <span class="req-type">${this._formatDocType(docType)}</span>
          <span class="status-badge" style="color:${statusColor}">${STATUS_LABEL[req.status] ?? req.status}</span>
        </div>
        ${req.requiredCount ? html`<div class="req-meta">Required: ${req.requiredCount} file(s)</div>` : ''}

        ${!isDone ? html`
          <div class="upload-row">
            <input
              type="file"
              accept=".pdf,.jpg,.jpeg,.png"
              ?disabled=${isUploading}
              @change=${(e) => this._handleFileChange(req, e)}
            />
            ${isUploading ? html`<span style="color:#6b7280;font-size:0.9rem">Uploading...</span>` : ''}
          </div>
          ${isRejected ? html`<div class="upload-error">Your previous upload was rejected. Please upload a new file.</div>` : ''}
          ${uploadError ? html`<div class="upload-error">${uploadError}</div>` : ''}
        ` : html`
          <div style="color:#15803d;font-size:0.9rem;font-weight:600">Document received and verified.</div>
        `}
      </div>
    `;
  }

  _renderAllDone() {
    return html`
      <div class="all-done">
        <div class="all-done-icon">✓</div>
        <h2 style="color:#15803d">All documents submitted — thank you.</h2>
        <p style="color:#6b7280">Your documents are being reviewed. We will update you on the progress of your application.</p>
      </div>
    `;
  }

  _formatDocType(raw) {
    if (!raw) return '';
    return raw
      .replace(/_/g, ' ')
      .toLowerCase()
      .replace(/\b\w/g, c => c.toUpperCase());
  }
}

customElements.define('document-upload-manager', DocumentUploadManager);
