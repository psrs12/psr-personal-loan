import React from 'react'
import ReactDOM from 'react-dom/client'
import ITAFlow from '../components/ITAFlow'
import TraditionalForm from '../components/OptionA/TraditionalForm'
import { ApplicationFormData } from '../types'
import '../index.css'

class ITATraditionalFormElement extends HTMLElement {
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
      el.dispatchEvent(new CustomEvent('application-submitted', {
        detail: await response.json(),
        bubbles: true,
      }))
    }

    this.root?.render(
      React.createElement(ITAFlow, {
        tokenFromUrl,
        apiBaseUrl,
        FormComponent: TraditionalForm,
        onSubmit: handleSubmit,
      })
    )
  }
}

customElements.define('ita-traditional-form', ITATraditionalFormElement)
