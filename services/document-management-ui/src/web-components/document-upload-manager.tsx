import { createRoot, Root } from 'react-dom/client'
import DocumentManager from '../components/DocumentManager'

class DocumentUploadManager extends HTMLElement {
  private root: Root | null = null
  private container: HTMLDivElement | null = null

  static get observedAttributes() {
    return ['api-base-url', 'application-id', 'session-token']
  }

  connectedCallback() {
    this.container = document.createElement('div')
    this.appendChild(this.container)
    this.root = createRoot(this.container)
    this.render()
  }

  disconnectedCallback() {
    this.root?.unmount()
    this.root = null
    this.container = null
  }

  attributeChangedCallback() {
    if (this.root) this.render()
  }

  private render() {
    const apiBaseUrl = this.getAttribute('api-base-url') ?? ''
    const applicationId = this.getAttribute('application-id') ?? ''
    const sessionToken = this.getAttribute('session-token') ?? ''

    this.root?.render(
      <DocumentManager
        apiBaseUrl={apiBaseUrl}
        applicationId={applicationId}
        sessionToken={sessionToken}
      />
    )
  }
}

export function register() {
  if (!customElements.get('document-upload-manager')) {
    customElements.define('document-upload-manager', DocumentUploadManager)
  }
}
