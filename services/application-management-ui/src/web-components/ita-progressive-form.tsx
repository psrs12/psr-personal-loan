import React from 'react'
import ReactDOM from 'react-dom/client'
import ITAFlow from '../components/ITAFlow'
import ProgressiveForm from '../components/OptionB/ProgressiveForm'
import { ApplicationFormData } from '../types'
import '../index.css'

class ITAProgressiveFormElement extends HTMLElement {
  private root: ReactDOM.Root | null = null

  static get observedAttributes() {
    return ['token', 'api-base-url']
  }

  connectedCallback() {
    const container = document.createElement('div')
    this.appendChild(container)
    this.root = ReactDOM.createRoot(container)
    this.render()
  }

  attributeChangedCallback() {
    this.render()
  }

  disconnectedCallback() {
    this.root?.unmount()
  }

  private render() {
    const tokenFromUrl = this.getAttribute('token')
    const apiBaseUrl = this.getAttribute('api-base-url') ?? '/api/v1/application-management'
    const el = this

    const handleSubmit = async (data: ApplicationFormData) => {
      const response = await fetch(`${apiBaseUrl}/applications`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data),
      })
      if (!response.ok) throw new Error('Submission failed')
      const result = await response.json()
      el.dispatchEvent(new CustomEvent('application-submitted', { detail: result, bubbles: true }))
      return result as { applicationId: string }
    }

    this.root?.render(
      React.createElement(ITAFlow, {
        tokenFromUrl,
        apiBaseUrl,
        FormComponent: ProgressiveForm,
        onSubmit: handleSubmit,
      })
    )
  }
}

customElements.define('ita-progressive-form', ITAProgressiveFormElement)
