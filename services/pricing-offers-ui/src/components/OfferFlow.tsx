import { useState } from 'react'
import { PricingOffer, OfferFlowStep } from '../types'
import { usePricingOffers } from '../hooks/usePricingOffers'
import OfferList from './OfferList'
import ConsentStep from './ConsentStep'

interface Props {
  apiBaseUrl: string
  applicationId: string
  applicantReference?: string
  onComplete?: (selectedOfferId: string) => void
  onError?: (error: string) => void
}

export default function OfferFlow({ apiBaseUrl, applicationId, applicantReference, onComplete, onError }: Props) {
  const { offers, loading, error: fetchError, reload } = usePricingOffers(apiBaseUrl, applicationId)
  const [step, setStep] = useState<OfferFlowStep>('offer-list')
  const [selectedOffer, setSelectedOffer] = useState<PricingOffer | null>(null)
  const [submitError, setSubmitError] = useState<string | null>(null)

  const currentStep: OfferFlowStep = fetchError
    ? 'error'
    : loading
    ? 'loading'
    : step

  function handleOfferContinue(offerId: string) {
    const offer = offers.find(o => o.pricingOfferId === offerId)
    if (!offer) return
    setSelectedOffer(offer)
    setStep('consent')
  }

  async function handleConsentConfirm() {
    if (!selectedOffer) return
    setStep('submitting')
    setSubmitError(null)

    try {
      const selectionRes = await fetch(`${apiBaseUrl}/applications/${applicationId}/offer-selection`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ selectedPricingOfferId: selectedOffer.pricingOfferId }),
      })
      if (!selectionRes.ok) throw new Error(`Offer selection failed (${selectionRes.status})`)

      const consentRes = await fetch(`${apiBaseUrl}/applications/${applicationId}/consent`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          selectedPricingOfferId: selectedOffer.pricingOfferId,
          consentChannel: 'WEB',
          applicantReference: applicantReference ?? '',
        }),
      })
      if (!consentRes.ok) throw new Error(`Consent submission failed (${consentRes.status})`)

      setStep('done')
      onComplete?.(selectedOffer.pricingOfferId)
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Something went wrong. Please try again.'
      setSubmitError(message)
      setStep('error')
      onError?.(message)
    }
  }

  if (currentStep === 'loading') {
    return (
      <div className="flex flex-col items-center justify-center py-16 gap-4">
        <svg className="animate-spin w-8 h-8 text-brand-600" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
        </svg>
        <p className="text-sm text-gray-500">Loading your personalised offers…</p>
      </div>
    )
  }

  if (currentStep === 'error') {
    const message = submitError ?? fetchError ?? 'Something went wrong.'
    return (
      <div className="flex flex-col items-center justify-center py-16 gap-4 text-center">
        <div className="w-12 h-12 rounded-full bg-red-100 flex items-center justify-center">
          <svg className="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          </svg>
        </div>
        <p className="text-sm font-medium text-gray-900">Unable to continue</p>
        <p className="text-sm text-gray-500 max-w-xs">{message}</p>
        <button
          onClick={() => { setStep('loading'); reload() }}
          className="mt-2 text-sm font-medium text-brand-600 hover:text-brand-700 underline underline-offset-2"
        >
          Try again
        </button>
      </div>
    )
  }

  if (currentStep === 'done') {
    return (
      <div className="flex flex-col items-center justify-center py-16 gap-4 text-center">
        <div className="w-12 h-12 rounded-full bg-green-100 flex items-center justify-center">
          <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
          </svg>
        </div>
        <p className="text-sm font-medium text-gray-900">Your offer has been confirmed</p>
        <p className="text-sm text-gray-500 max-w-xs">
          We're now running a full credit check. We'll be in touch shortly with a final decision.
        </p>
      </div>
    )
  }

  if (currentStep === 'consent' && selectedOffer) {
    return (
      <ConsentStep
        selectedOffer={selectedOffer}
        onConfirm={handleConsentConfirm}
        onBack={() => setStep('offer-list')}
        submitting={step === 'submitting'}
      />
    )
  }

  return (
    <OfferList
      offers={offers}
      onContinue={handleOfferContinue}
    />
  )
}
