import { useState, useEffect } from 'react'
import { useDeclarations } from '../hooks/useDeclarations'
import { useConfirmedOffer } from '../hooks/useConfirmedOffer'
import OfferSummary from './OfferSummary'
import DeclarationsList from './DeclarationsList'

interface Props {
  apiBaseUrl: string
  pricingApiBaseUrl: string
  applicationId: string
  sessionToken: string
  onComplete?: () => void
  onError?: (error: string) => void
}

export default function AcceptanceFlow({ apiBaseUrl, pricingApiBaseUrl, applicationId, sessionToken, onComplete, onError }: Props) {
  const { declarations, loading, error } = useDeclarations(apiBaseUrl, applicationId, sessionToken)
  const offer = useConfirmedOffer(pricingApiBaseUrl, applicationId, sessionToken)

  const [checked, setChecked] = useState<Record<string, boolean>>({})
  const [submitting, setSubmitting] = useState(false)
  const [submitted, setSubmitted] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)

  useEffect(() => {
    const initial: Record<string, boolean> = {}
    declarations.forEach(d => { initial[d.declarationId] = false })
    setChecked(initial)
  }, [declarations])

  function toggle(id: string) {
    setChecked(c => ({ ...c, [id]: !c[id] }))
  }

  const mandatoryAll = declarations.filter(d => d.mandatory).every(d => checked[d.declarationId])

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setSubmitting(true)
    setSubmitError(null)
    try {
      const acceptedDeclarationIds = Object.entries(checked).filter(([, v]) => v).map(([id]) => id)
      const res = await fetch(`${apiBaseUrl}/applications/${applicationId}/esign`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionToken}` },
        body: JSON.stringify({ acceptedDeclarationIds }),
      })
      if (!res.ok) throw new Error(`E-sign submission failed (${res.status})`)
      setSubmitted(true)
      onComplete?.()
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Something went wrong. Please try again.'
      setSubmitError(message)
      onError?.(message)
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center py-16 gap-4">
        <svg className="animate-spin w-8 h-8 text-brand-600" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
        </svg>
        <p className="text-sm text-gray-500">Loading your declarations…</p>
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center py-16 gap-4 text-center">
        <div className="w-12 h-12 rounded-full bg-red-100 flex items-center justify-center">
          <svg className="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          </svg>
        </div>
        <p className="text-sm font-medium text-gray-900">Unable to continue</p>
        <p className="text-sm text-gray-500 max-w-xs">{error}</p>
      </div>
    )
  }

  if (submitted) {
    return (
      <div className="flex flex-col items-center justify-center py-16 gap-4 text-center">
        <div className="w-12 h-12 rounded-full bg-green-100 flex items-center justify-center">
          <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
          </svg>
        </div>
        <p className="text-sm font-medium text-gray-900">E-Signature Submitted</p>
        <p className="text-sm text-gray-500 max-w-xs">Your offer has been accepted. Your application is now being finalised.</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <OfferSummary offer={offer} />
      <hr className="border-gray-200" />
      <div>
        <p className="text-xs font-semibold text-brand-600 uppercase tracking-wide mb-2">Declarations &amp; E-Signature</p>
        <p className="text-sm text-gray-500 mb-4">
          Please read and accept all mandatory declarations below to complete your loan offer acceptance.
          Items marked <strong className="text-red-700">*</strong> are required.
        </p>
        <DeclarationsList
          declarations={declarations}
          checked={checked}
          onToggle={toggle}
          onSubmit={handleSubmit}
          mandatoryAll={mandatoryAll}
          submitting={submitting}
          submitError={submitError}
        />
      </div>
    </div>
  )
}
