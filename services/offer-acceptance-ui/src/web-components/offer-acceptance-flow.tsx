import { createRoot, Root } from 'react-dom/client'
import AcceptanceFlow from '../components/AcceptanceFlow'

class OfferAcceptanceFlow extends HTMLElement {
  private root: Root | null = null
  private container: HTMLDivElement | null = null

  static get observedAttributes() {
    return ['api-base-url', 'pricing-api-base-url', 'application-id', 'session-token']
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
    const pricingApiBaseUrl = this.getAttribute('pricing-api-base-url') ?? ''
    const applicationId = this.getAttribute('application-id') ?? ''
    const sessionToken = this.getAttribute('session-token') ?? ''

    this.root?.render(
      <AcceptanceFlow
        apiBaseUrl={apiBaseUrl}
        pricingApiBaseUrl={pricingApiBaseUrl}
        applicationId={applicationId}
        sessionToken={sessionToken}
        onComplete={() => {
          this.dispatchEvent(new CustomEvent('offer-accepted', { bubbles: true, composed: true }))
        }}
        onError={error => {
          this.dispatchEvent(new CustomEvent('offer-acceptance-error', { detail: { error }, bubbles: true, composed: true }))
        }}
      />
    )
  }
}

export function register() {
  if (!customElements.get('offer-acceptance-flow')) {
    customElements.define('offer-acceptance-flow', OfferAcceptanceFlow)
  }
}
