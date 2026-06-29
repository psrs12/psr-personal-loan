import { useState, useEffect } from 'react'

interface Declaration {
  declarationId: string
  declarationType: string
  title: string
  content: string
  mandatory: boolean
}

interface Props {
  applicationId: string
  sessionToken: string
  offerAcceptanceApiBaseUrl: string
  onComplete: () => void
}

export default function OfferAcceptanceMfe({
  applicationId,
  sessionToken,
  offerAcceptanceApiBaseUrl,
  onComplete,
}: Props) {
  const [declarations, setDeclarations] = useState<Declaration[]>([])
  const [accepted, setAccepted] = useState<Set<string>>(new Set())
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetch(`${offerAcceptanceApiBaseUrl}/applications/${applicationId}/declarations`, {
      headers: { Authorization: `Bearer ${sessionToken}` },
    })
      .then(r => r.json())
      .then(setDeclarations)
      .catch(() => setError('Failed to load declarations.'))
      .finally(() => setLoading(false))
  }, [applicationId, sessionToken, offerAcceptanceApiBaseUrl])

  function toggleAccepted(id: string) {
    setAccepted(prev => {
      const next = new Set(prev)
      next.has(id) ? next.delete(id) : next.add(id)
      return next
    })
  }

  const allMandatoryAccepted = declarations
    .filter(d => d.mandatory)
    .every(d => accepted.has(d.declarationId))

  async function handleSign() {
    setSubmitting(true)
    setError(null)
    try {
      const res = await fetch(`${offerAcceptanceApiBaseUrl}/applications/${applicationId}/esign`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${sessionToken}`,
        },
        body: JSON.stringify({ acceptedDeclarationIds: Array.from(accepted) }),
      })
      if (!res.ok) {
        const detail = await res.json().catch(() => ({}))
        setError(detail.detail ?? 'Failed to submit signature.')
        return
      }
      onComplete()
    } catch {
      setError('Unable to connect. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return <div className="p-8 text-center text-gray-500">Loading declarations…</div>

  return (
    <div className="max-w-2xl mx-auto p-6">
      <h2 className="text-xl font-semibold text-gray-900 mb-1">Review and sign your loan offer</h2>
      <p className="text-sm text-gray-500 mb-6">Please read and accept the following declarations to proceed.</p>

      {error && (
        <div className="mb-4 rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-sm text-red-700">{error}</div>
      )}

      <div className="space-y-4 mb-8">
        {declarations.map(d => (
          <label key={d.declarationId} className="flex gap-3 items-start cursor-pointer">
            <input
              type="checkbox"
              className="mt-0.5 h-4 w-4 rounded border-gray-300 text-blue-600"
              checked={accepted.has(d.declarationId)}
              onChange={() => toggleAccepted(d.declarationId)}
            />
            <div>
              <p className="text-sm font-medium text-gray-800">
                {d.title}
                {d.mandatory && <span className="ml-1 text-red-500">*</span>}
              </p>
              <p className="text-xs text-gray-500 mt-0.5">{d.content}</p>
            </div>
          </label>
        ))}
      </div>

      <button
        onClick={handleSign}
        disabled={!allMandatoryAccepted || submitting}
        className="w-full bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white font-medium rounded-lg py-2.5 text-sm transition-colors"
      >
        {submitting ? 'Signing…' : 'Sign and accept offer'}
      </button>
      <p className="text-xs text-gray-400 text-center mt-2">* Required declarations</p>
    </div>
  )
}
