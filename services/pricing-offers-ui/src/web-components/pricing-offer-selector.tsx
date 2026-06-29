import React from 'react'
import { createRoot, Root } from 'react-dom/client'
import OfferFlow from '../components/OfferFlow'

class PricingOfferSelector extends HTMLElement {
  private root: Root | null = null
  private container: HTMLDivElement | null = null

  static get observedAttributes() {
    return ['api-base-url', 'application-id', 'applicant-reference']
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
    const applicantReference = this.getAttribute('applicant-reference') ?? undefined

    this.root?.render(
      <OfferFlow
        apiBaseUrl={apiBaseUrl}
        applicationId={applicationId}
        applicantReference={applicantReference}
        onComplete={offerId => {
          this.dispatchEvent(new CustomEvent('offer-confirmed', { detail: { offerId }, bubbles: true, composed: true }))
        }}
        onError={error => {
          this.dispatchEvent(new CustomEvent('offer-error', { detail: { error }, bubbles: true, composed: true }))
        }}
      />
    )
  }
}

export function register() {
  if (!customElements.get('pricing-offer-selector')) {
    customElements.define('pricing-offer-selector', PricingOfferSelector)
  }
}
