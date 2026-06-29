import { useState } from 'react'
import { PrefillData } from '../../types'

type InvitationState = 'idle' | 'loading' | 'success' | 'error'

interface Props {
  tokenFromUrl: string | null
  apiBaseUrl: string
  onITASuccess: (prefill: PrefillData) => void
  onDirect: () => void
}

const ERROR_MESSAGES: Record<number, string> = {
  404: "We couldn't find an offer for that Invitation ID. Please check your email and try again.",
  410: 'This invitation has expired. Please contact us to request a new offer.',
}

export default function InvitationEntry({ tokenFromUrl, apiBaseUrl, onITASuccess, onDirect }: Props) {
  const [invitationId, setInvitationId] = useState(tokenFromUrl ?? '')
  const [state, setState] = useState<InvitationState>('idle')
  const [prefill, setPrefill] = useState<PrefillData | null>(null)
  const [errorMsg, setErrorMsg] = useState('')
  const [expanded, setExpanded] = useState(!!tokenFromUrl)

  const applyInvitation = async () => {
    if (!invitationId.trim()) return
    setState('loading')
    setErrorMsg('')
    try {
      const res = await fetch(`${apiBaseUrl}/invitations/${invitationId.trim()}/intake`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
      })
      if (!res.ok) {
        setErrorMsg(ERROR_MESSAGES[res.status] ?? 'Something went wrong. Please try again.')
        setState('error')
        return
      }
      const data: PrefillData = await res.json()
      setPrefill(data)
      setState('success')
    } catch {
      setErrorMsg('Unable to reach our servers. Please check your connection.')
      setState('error')
    }
  }

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') applyInvitation()
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50 font-sans flex flex-col">

      {/* Nav */}
      <header className="bg-white border-b border-gray-100 px-6 py-4 flex items-center gap-3">
        <div className="w-9 h-9 bg-brand-600 rounded-xl flex items-center justify-center">
          <span className="text-white text-sm font-bold">PL</span>
        </div>
        <span className="font-semibold text-gray-900">Personal Loan</span>
      </header>

      <main className="flex-1 flex items-center justify-center px-4 py-10">
        <div className="w-full max-w-lg space-y-5">

          {/* ── ITA strip ─────────────────────────────────────────── */}
          <div className={`rounded-2xl border transition-all overflow-hidden ${
            state === 'success'
              ? 'border-green-300 bg-green-50'
              : state === 'error'
              ? 'border-red-200 bg-red-50'
              : 'border-brand-200 bg-brand-50'
          }`}>

            {/* Header row — always visible */}
            <button
              onClick={() => setExpanded(e => !e)}
              className="w-full flex items-center gap-3 px-5 py-4 text-left"
            >
              <span className="text-lg">🎫</span>
              <div className="flex-1">
                {state === 'success' && prefill ? (
                  <p className="text-sm font-semibold text-green-800">
                    Invitation verified — welcome, {prefill.firstName}!
                  </p>
                ) : (
                  <p className="text-sm font-semibold text-brand-800">
                    Have an invitation code?
                  </p>
                )}
                <p className="text-xs text-gray-500 mt-0.5">
                  {state === 'success'
                    ? 'Your name and address have been pre-filled'
                    : 'Enter your Invitation ID to pre-fill your details'}
                </p>
              </div>
              {state === 'success' ? (
                <span className="text-green-500 text-xl">✓</span>
              ) : (
                <span className={`text-gray-400 text-xs transition-transform ${expanded ? 'rotate-180' : ''}`}>
                  ▼
                </span>
              )}
            </button>

            {/* Expandable input area */}
            {expanded && state !== 'success' && (
              <div className="px-5 pb-4 space-y-3 border-t border-brand-100">
                <div className="flex gap-2 pt-3">
                  <input
                    type="text"
                    value={invitationId}
                    onChange={e => { setInvitationId(e.target.value); setState('idle') }}
                    onKeyDown={handleKeyDown}
                    placeholder="e.g. INV-2025-XXXXX"
                    autoFocus
                    className={`flex-1 border-2 rounded-xl px-4 py-2.5 text-sm font-mono tracking-widest outline-none transition-all ${
                      state === 'error'
                        ? 'border-red-400 bg-red-50'
                        : 'border-gray-200 focus:border-brand-500 bg-white'
                    }`}
                  />
                  <button
                    onClick={applyInvitation}
                    disabled={!invitationId.trim() || state === 'loading'}
                    className="px-4 py-2.5 bg-brand-600 hover:bg-brand-700 disabled:bg-gray-300 text-white text-sm font-semibold rounded-xl transition-colors flex items-center gap-1.5 whitespace-nowrap"
                  >
                    {state === 'loading' ? (
                      <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z"/>
                      </svg>
                    ) : (
                      'Apply →'
                    )}
                  </button>
                </div>

                {state === 'error' && (
                  <p className="text-xs text-red-600 flex items-start gap-1.5">
                    <span className="flex-shrink-0 mt-0.5">⚠</span>
                    {errorMsg}
                  </p>
                )}

                <p className="text-xs text-gray-400">
                  Find your Invitation ID in the email or SMS we sent you
                </p>
              </div>
            )}
          </div>

          {/* ── Main card ─────────────────────────────────────────── */}
          <div className="bg-white rounded-3xl shadow-sm border border-gray-100 p-8 space-y-8">

            {/* Headline */}
            <div className="text-center space-y-2">
              <h1 className="text-3xl font-extrabold text-gray-900">
                {state === 'success' && prefill
                  ? `Let's finish your application,\n${prefill.firstName}.`
                  : 'Apply for a personal loan'}
              </h1>
              <p className="text-gray-400 text-sm">
                {state === 'success'
                  ? 'Your name and address are pre-filled. Please provide your contact details below.'
                  : 'Simple. Fast. No hidden fees.'}
              </p>
            </div>

            {/* Pre-fill summary (ITA success state) */}
            {state === 'success' && prefill && (
              <div className="bg-brand-50 border border-brand-100 rounded-2xl px-5 py-4 space-y-2">
                <div className="flex items-center justify-between">
                  <p className="text-xs font-semibold text-brand-700 uppercase tracking-wider">
                    Pre-filled from your profile
                  </p>
                  <span className="text-xs text-brand-500 bg-brand-100 px-2 py-0.5 rounded-full">
                    🔒 Read-only
                  </span>
                </div>
                <div className="grid grid-cols-2 gap-x-4 gap-y-1.5 text-sm">
                  <div>
                    <span className="text-gray-400 text-xs">First name</span>
                    <p className="font-medium text-gray-800">{prefill.firstName}</p>
                  </div>
                  <div>
                    <span className="text-gray-400 text-xs">Last name</span>
                    <p className="font-medium text-gray-800">{prefill.lastName}</p>
                  </div>
                  {prefill.address && (
                    <div className="col-span-2">
                      <span className="text-gray-400 text-xs">Address</span>
                      <p className="font-medium text-gray-800">
                        {[prefill.address.street, prefill.address.city, prefill.address.state, prefill.address.zip]
                          .filter(Boolean).join(', ')}
                      </p>
                    </div>
                  )}
                </div>
                <p className="text-xs text-gray-400 pt-1">
                  These details are locked to confirm you're the invited applicant.
                  Phone and email must be entered by you.
                </p>
              </div>
            )}

            {/* What you'll need */}
            {state !== 'success' && (
              <div className="space-y-3">
                {[
                  { icon: '🕐', label: 'Takes about 5 minutes' },
                  { icon: '📄', label: 'Employment and income details' },
                  { icon: '🔒', label: 'SSN — tokenized, never stored in raw form' },
                  { icon: '📊', label: 'No impact to your credit score at this stage' },
                ].map((item, i) => (
                  <div key={i} className="flex items-center gap-3 text-sm text-gray-600">
                    <span className="text-base">{item.icon}</span>
                    <span>{item.label}</span>
                  </div>
                ))}
              </div>
            )}

            {/* CTA */}
            <button
              onClick={() => state === 'success' && prefill ? onITASuccess(prefill) : onDirect()}
              className="w-full py-5 bg-brand-600 hover:bg-brand-700 text-white font-bold rounded-2xl
                transition-all shadow-xl shadow-brand-200 text-base"
            >
              {state === 'success'
                ? `Continue my application →`
                : 'Start my application →'}
            </button>

            {state !== 'success' && (
              <p className="text-center text-xs text-gray-400">
                🔒 Bank-level encryption · Your data is never shared without consent
              </p>
            )}
          </div>
        </div>
      </main>
    </div>
  )
}
