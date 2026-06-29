import { useEffect, useRef, useState } from 'react'
import { ApplicationFormData, EmploymentType, LoanPurpose, PrefillData, PricingOffer } from '../../types'
import { useSessionTimer } from '../../hooks/useSessionTimer'
import OfferFlow from '../../../../pricing-offers-ui/src/components/OfferFlow'

interface Props {
  prefill: PrefillData | null
  selectedOffer: PricingOffer
  onSubmit: (data: ApplicationFormData) => Promise<{ applicationId: string }>
  apiBaseUrl?: string
}

const PURPOSES: { value: LoanPurpose; label: string; icon: string }[] = [
  { value: 'HOME_IMPROVEMENT', label: 'Home Improvement', icon: '🏠' },
  { value: 'AUTO',             label: 'Auto Expense',    icon: '🚗' },
  { value: 'DEBT_CONSOLIDATION', label: 'Debt Consolidation', icon: '💳' },
  { value: 'MEDICAL',          label: 'Medical',         icon: '💊' },
  { value: 'MAJOR_PURCHASE',   label: 'Major Purchase',  icon: '📦' },
  { value: 'OTHER',            label: 'Other',           icon: '✨' },
]

const EMPLOYMENT_TYPES: { value: EmploymentType; label: string }[] = [
  { value: 'FULL_TIME',     label: 'Full-time' },
  { value: 'PART_TIME',     label: 'Part-time' },
  { value: 'SELF_EMPLOYED', label: 'Self-employed' },
  { value: 'RETIRED',       label: 'Retired' },
  { value: 'UNEMPLOYED',    label: 'Unemployed' },
  { value: 'OTHER',         label: 'Other' },
]

// ─── SSN input ──────────────────────────────────────────────────────────────

function SSNInput({ value, onChange }: { value: string; onChange: (v: string) => void }) {
  const [visible, setVisible] = useState(false)
  const format = (raw: string) => {
    const d = raw.replace(/\D/g, '').slice(0, 9)
    if (d.length <= 3) return d
    if (d.length <= 5) return `${d.slice(0, 3)}-${d.slice(3)}`
    return `${d.slice(0, 3)}-${d.slice(3, 5)}-${d.slice(5)}`
  }
  return (
    <div className="relative">
      <input
        type={visible ? 'text' : 'password'}
        placeholder="XXX-XX-XXXX"
        value={format(value)}
        onChange={e => onChange(e.target.value.replace(/\D/g, ''))}
        maxLength={11}
        className="w-full border-2 border-gray-200 rounded-2xl px-5 py-4 pr-20 text-lg focus:border-brand-500 outline-none font-mono tracking-widest transition-colors"
      />
      <button
        type="button"
        onClick={() => setVisible(v => !v)}
        className="absolute right-4 top-4 text-xs text-brand-600 font-semibold"
      >
        {visible ? 'Hide' : 'Show'}
      </button>
    </div>
  )
}

// ─── Section wrapper (slides in) ────────────────────────────────────────────

function Section({ visible, children }: { visible: boolean; children: React.ReactNode }) {
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (visible && ref.current) {
      ref.current.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
  }, [visible])

  if (!visible) return null

  return (
    <div
      ref={ref}
      className="animate-in slide-in-from-bottom-4 fade-in duration-500 space-y-6"
      style={{ animation: 'slideIn 0.4s ease-out' }}
    >
      {children}
    </div>
  )
}

// ─── Currency input ──────────────────────────────────────────────────────────

function CurrencyInput({ label, value, onChange, optional }: {
  label: string; value: string; onChange: (v: string) => void; optional?: boolean
}) {
  return (
    <div>
      <label className="block text-sm font-medium text-gray-600 mb-2">
        {label}{optional && <span className="text-gray-400 text-xs ml-1">(optional)</span>}
      </label>
      <div className="relative">
        <span className="absolute left-4 top-4 text-gray-400 text-lg">$</span>
        <input
          type="number"
          placeholder="0"
          value={value}
          onChange={e => onChange(e.target.value)}
          className="w-full border-2 border-gray-200 rounded-2xl pl-8 pr-4 py-4 text-lg focus:border-brand-500 outline-none transition-colors"
        />
      </div>
    </div>
  )
}

// ─── Main component ──────────────────────────────────────────────────────────

const FALLBACK_EXPIRY = new Date(Date.now() + 30 * 60 * 1000).toISOString()

export default function ProgressiveForm({ prefill, selectedOffer, onSubmit, apiBaseUrl = '/api/v1/application-management' }: Props) {
  const isITA = !!prefill
  const [data, setData] = useState<Partial<ApplicationFormData>>({
    loanAmount: selectedOffer.amount || undefined,
    loanTermMonths: selectedOffer.termMonths || undefined,
  })
  const [section, setSection] = useState<'loan' | 'employment' | 'identity' | 'done'>('loan')
  const [submitting, setSubmitting] = useState(false)
  const [applicationId, setApplicationId] = useState<string | null>(null)
  const { remaining, isExpired } = useSessionTimer(prefill?.expiresAt ?? FALLBACK_EXPIRY)

  const patch = (p: Partial<ApplicationFormData>) => setData(prev => ({ ...prev, ...p }))


  const loanComplete =
    (isITA || (!!data.firstName && !!data.lastName && !!data.streetAddress)) &&
    !!data.phone && !!data.email &&
    !!data.loanAmount && !!data.loanPurpose
  const employmentComplete =
    !!data.employmentType && !!data.annualIncome && !!data.monthlyHousingPayment &&
    !!data.phone && !!data.email
  const identityComplete =
    (data.ssn?.length ?? 0) === 9 && !!data.dateOfBirth && !!data.citizenship &&
    data.consentElectronicRecords && data.consentPrivacyPolicy

  const handleSubmit = async () => {
    setSubmitting(true)
    try {
      const result = await onSubmit(data as ApplicationFormData)
      setApplicationId(result.applicationId)
    } finally {
      setSubmitting(false)
    }
  }

  if (isITA && isExpired) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50 flex items-center justify-center p-4">
        <div className="bg-white rounded-3xl shadow-xl p-10 max-w-sm text-center space-y-4">
          <div className="text-5xl">⏰</div>
          <h2 className="text-2xl font-bold">Session expired</h2>
          <p className="text-gray-500">Return to your invitation email to restart.</p>
        </div>
      </div>
    )
  }

  if (applicationId) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50 font-sans">
        <header className="sticky top-0 z-10 bg-white/80 backdrop-blur-md border-b border-gray-100 px-4 py-3">
          <div className="max-w-xl mx-auto flex items-center gap-2">
            <div className="w-8 h-8 bg-brand-600 rounded-lg flex items-center justify-center">
              <span className="text-white text-xs font-bold">PL</span>
            </div>
            <span className="font-semibold text-gray-900 text-sm">Personal Loan</span>
          </div>
        </header>
        <main className="max-w-xl mx-auto px-4 py-8">
          <div className="mb-4">
            <div className="flex items-center gap-2 text-sm text-green-600 font-medium mb-1">
              <span className="text-green-500">✓</span> Application submitted
            </div>
            <p className="text-xs text-gray-400">
              We ran a soft credit check — this hasn't affected your score.
              Select an offer below to proceed.
            </p>
          </div>
          <div className="bg-white rounded-3xl shadow-sm border border-gray-100 p-6">
            <OfferFlow
              apiBaseUrl={apiBaseUrl}
              applicationId={applicationId}
              applicantReference={prefill?.invitationToken}
              onComplete={() => {}}
              onError={() => {}}
            />
          </div>
        </main>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50 font-sans">
      {/* Header */}
      <header className="sticky top-0 z-10 bg-white/80 backdrop-blur-md border-b border-gray-100 px-4 py-3">
        <div className="max-w-xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 bg-brand-600 rounded-lg flex items-center justify-center">
              <span className="text-white text-xs font-bold">PL</span>
            </div>
            <span className="font-semibold text-gray-900 text-sm">Personal Loan</span>
          </div>
          {isITA && (
            <div className={`flex items-center gap-1.5 text-xs px-3 py-1.5 rounded-full font-medium ${
              remaining < '05:00' ? 'bg-red-50 text-red-600' : 'bg-amber-50 text-amber-700'
            }`}>
              <span>⏱</span>
              <span>Session expires <strong>{remaining}</strong></span>
            </div>
          )}
        </div>
      </header>

      <main className="max-w-xl mx-auto px-4 py-8 space-y-6 pb-20">

        {/* Welcome / path card */}
        <div className="bg-white rounded-3xl shadow-sm border border-gray-100 p-6 space-y-4">
          <div className="flex items-center gap-4">
            <div className={`w-14 h-14 rounded-2xl flex items-center justify-center text-white text-2xl font-bold shadow-lg ${
              isITA ? 'bg-gradient-to-br from-brand-500 to-brand-700' : 'bg-gradient-to-br from-gray-500 to-gray-700'
            }`}>
              {isITA ? prefill.firstName[0] : '👤'}
            </div>
            <div>
              <h1 className="text-xl font-bold text-gray-900">
                {isITA ? `Welcome, ${prefill.firstName} 👋` : 'Personal Loan Application'}
              </h1>
              <div className={`inline-flex items-center gap-1.5 text-xs font-semibold px-2 py-0.5 rounded-full mt-1 ${
                isITA ? 'bg-brand-100 text-brand-700' : 'bg-gray-100 text-gray-600'
              }`}>
                <span className={`w-1.5 h-1.5 rounded-full ${isITA ? 'bg-brand-500' : 'bg-gray-400'}`} />
                {isITA ? 'Invitation to Apply' : 'Direct Application'}
              </div>
            </div>
          </div>

          {/* ITA: pre-fill summary */}
          {isITA && (
            <div className="bg-brand-50 border border-brand-100 rounded-2xl px-4 py-3">
              <div className="flex items-center justify-between mb-2">
                <p className="text-xs font-semibold text-brand-700">Pre-filled from your profile</p>
                <span className="text-xs text-gray-400">🔒 Read-only</span>
              </div>
              <div className="grid grid-cols-2 gap-x-4 gap-y-1 text-sm">
                <div>
                  <span className="text-xs text-gray-400">Name</span>
                  <p className="font-medium text-gray-800">{prefill.firstName} {prefill.lastName}</p>
                </div>
                {prefill.address && (
                  <div>
                    <span className="text-xs text-gray-400">Address</span>
                    <p className="font-medium text-gray-800 text-xs">
                      {[prefill.address.street, prefill.address.city, prefill.address.state].filter(Boolean).join(', ')}
                    </p>
                  </div>
                )}
              </div>
              <p className="text-xs text-gray-400 mt-2">
                Phone and email are not pre-filled — please enter them below.
              </p>
            </div>
          )}
        </div>

        {/* ── Section 1: Personal info + loan details ── */}
        <div className="bg-white rounded-3xl shadow-sm border border-gray-100 p-6 space-y-5">
          <h2 className="text-lg font-bold text-gray-900">Your details</h2>

          {/* Name + Address — locked (ITA) or editable (Direct) */}
          {isITA ? (
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium text-gray-600">Pre-filled details</span>
                <span className="text-xs text-gray-400 bg-gray-100 px-2 py-0.5 rounded-full">🔒 Read-only</span>
              </div>
              <div className="grid grid-cols-2 gap-3">
                {/* First name — locked */}
                <div>
                  <label className="block text-xs font-medium text-gray-500 mb-1.5">First name</label>
                  <div className="flex items-center gap-2 border-2 border-gray-100 bg-gray-50 rounded-2xl px-5 py-4">
                    <span className="flex-1 text-gray-800">{prefill!.firstName}</span>
                    <span className="text-gray-300 text-sm">🔒</span>
                  </div>
                </div>
                {/* Last name — locked */}
                <div>
                  <label className="block text-xs font-medium text-gray-500 mb-1.5">Last name</label>
                  <div className="flex items-center gap-2 border-2 border-gray-100 bg-gray-50 rounded-2xl px-5 py-4">
                    <span className="flex-1 text-gray-800">{prefill!.lastName}</span>
                    <span className="text-gray-300 text-sm">🔒</span>
                  </div>
                </div>
              </div>
              {/* Address — locked if available, editable if partial prefill */}
              {prefill!.address ? (
                <div>
                  <label className="block text-xs font-medium text-gray-500 mb-1.5">Address</label>
                  <div className="flex items-center gap-2 border-2 border-gray-100 bg-gray-50 rounded-2xl px-5 py-4">
                    <span className="flex-1 text-gray-800 text-sm">
                      {[prefill!.address.street, prefill!.address.city, prefill!.address.state, prefill!.address.zip]
                        .filter(Boolean).join(', ')}
                    </span>
                    <span className="text-gray-300 text-sm">🔒</span>
                  </div>
                </div>
              ) : (
                <div>
                  <label className="block text-xs font-medium text-gray-500 mb-1.5">
                    Address <span className="text-amber-600">(not available from your profile)</span>
                  </label>
                  <input
                    type="text" placeholder="123 Main St, City, State, ZIP"
                    value={data.streetAddress ?? ''}
                    onChange={e => patch({ streetAddress: e.target.value })}
                    className="w-full border-2 border-gray-200 rounded-2xl px-5 py-4 focus:border-brand-500 outline-none transition-colors"
                  />
                </div>
              )}
            </div>
          ) : (
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-sm font-medium text-gray-600 mb-2">First name</label>
                <input
                  type="text" placeholder="First name"
                  value={data.firstName ?? ''}
                  onChange={e => patch({ firstName: e.target.value })}
                  className="w-full border-2 border-gray-200 rounded-2xl px-5 py-4 focus:border-brand-500 outline-none transition-colors"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-600 mb-2">Last name</label>
                <input
                  type="text" placeholder="Last name"
                  value={data.lastName ?? ''}
                  onChange={e => patch({ lastName: e.target.value })}
                  className="w-full border-2 border-gray-200 rounded-2xl px-5 py-4 focus:border-brand-500 outline-none transition-colors"
                />
              </div>
              <div className="col-span-2">
                <label className="block text-sm font-medium text-gray-600 mb-2">Street address</label>
                <input
                  type="text" placeholder="123 Main St, City, State, ZIP"
                  value={data.streetAddress ?? ''}
                  onChange={e => patch({ streetAddress: e.target.value })}
                  className="w-full border-2 border-gray-200 rounded-2xl px-5 py-4 focus:border-brand-500 outline-none transition-colors"
                />
              </div>
            </div>
          )}

          {/* Contact — always manual */}
          <div>
            <div className="flex items-center gap-2 mb-3">
              <label className="text-sm font-medium text-gray-600">Contact details</label>
              {isITA && (
                <span className="text-xs text-amber-700 bg-amber-50 border border-amber-100 px-2 py-0.5 rounded-full">
                  Not pre-filled for security
                </span>
              )}
            </div>
            <div className="grid grid-cols-2 gap-3">
              <input
                type="tel" placeholder="Mobile phone"
                value={data.phone ?? ''}
                onChange={e => patch({ phone: e.target.value })}
                className="border-2 border-gray-200 rounded-2xl px-5 py-4 focus:border-brand-500 outline-none transition-colors text-sm"
              />
              <input
                type="email" placeholder="Email address"
                value={data.email ?? ''}
                onChange={e => patch({ email: e.target.value })}
                className="border-2 border-gray-200 rounded-2xl px-5 py-4 focus:border-brand-500 outline-none transition-colors text-sm"
              />
            </div>
          </div>

          {/* Loan amount + purpose */}
          <div className="space-y-3">
            <label className="block text-sm font-medium text-gray-600">How much do you need?</label>
            <div className="relative">
              <span className="absolute left-5 top-4 text-gray-400 text-lg">$</span>
              <input
                type="number" placeholder="0"
                value={data.loanAmount ?? ''}
                onChange={e => patch({ loanAmount: Number(e.target.value) })}
                className="w-full border-2 border-gray-200 rounded-2xl pl-9 pr-5 py-4 text-lg focus:border-brand-500 outline-none transition-colors"
              />
            </div>
          </div>

          <div>
            <p className="text-sm font-medium text-gray-600 mb-3">What will you use it for?</p>
            <div className="grid grid-cols-3 gap-2">
              {PURPOSES.map(p => (
                <button
                  key={p.value}
                  onClick={() => patch({ loanPurpose: p.value })}
                  className={`flex flex-col items-center gap-1.5 p-3 rounded-2xl border-2 text-xs font-medium transition-all ${
                    data.loanPurpose === p.value
                      ? 'border-brand-600 bg-brand-50 text-brand-800 shadow-sm'
                      : 'border-gray-100 bg-gray-50 text-gray-600 hover:border-gray-200'
                  }`}
                >
                  <span className="text-2xl">{p.icon}</span>
                  <span>{p.label}</span>
                </button>
              ))}
            </div>
          </div>

          {loanComplete && section === 'loan' && (
            <button
              onClick={() => setSection('employment')}
              className="w-full py-4 bg-brand-600 hover:bg-brand-700 text-white font-semibold rounded-2xl transition-all shadow-lg shadow-brand-200 text-base"
            >
              Continue →
            </button>
          )}

          {section !== 'loan' && loanComplete && (
            <div className="flex items-center gap-2 text-sm text-green-600 font-medium">
              <span className="text-green-500">✓</span> Personal details confirmed
            </div>
          )}
        </div>

        {/* ── Section 2: Employment ── */}
        <Section visible={section !== 'loan'}>
          <div className="bg-white rounded-3xl shadow-sm border border-gray-100 p-6 space-y-5">
            <div>
              <h2 className="text-lg font-bold text-gray-900">
                {prefill?.firstName ? `Great, ${prefill.firstName}. Almost there.` : 'Almost there.'}
              </h2>
              <p className="text-sm text-gray-500 mt-1">Let's understand your financial picture</p>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div className="col-span-2">
                <label className="block text-sm font-medium text-gray-600 mb-2">Employment type</label>
                <div className="grid grid-cols-3 gap-2">
                  {EMPLOYMENT_TYPES.map(et => (
                    <button
                      key={et.value}
                      onClick={() => patch({ employmentType: et.value })}
                      className={`py-2.5 px-2 rounded-xl border-2 text-xs font-medium transition-all ${
                        data.employmentType === et.value
                          ? 'border-brand-600 bg-brand-50 text-brand-800'
                          : 'border-gray-100 text-gray-600 hover:border-gray-200'
                      }`}
                    >
                      {et.label}
                    </button>
                  ))}
                </div>
              </div>

              <div className="col-span-2">
                <label className="block text-sm font-medium text-gray-600 mb-2">Employer / Organization</label>
                <input
                  type="text"
                  placeholder="Company name"
                  value={data.employerName ?? ''}
                  onChange={e => patch({ employerName: e.target.value })}
                  className="w-full border-2 border-gray-200 rounded-2xl px-5 py-4 text-base focus:border-brand-500 outline-none transition-colors"
                />
              </div>

              <div className="col-span-2">
                <CurrencyInput
                  label="Annual income"
                  value={data.annualIncome ?? ''}
                  onChange={v => patch({ annualIncome: v })}
                />
              </div>

              <div className="col-span-2">
                <CurrencyInput
                  label="Additional household income"
                  value={data.additionalIncome ?? ''}
                  onChange={v => patch({ additionalIncome: v })}
                  optional
                />
              </div>

              <div className="col-span-2">
                <CurrencyInput
                  label="Monthly housing payment"
                  value={data.monthlyHousingPayment ?? ''}
                  onChange={v => patch({ monthlyHousingPayment: v })}
                />
              </div>
            </div>

            {/* Contact — deliberately not pre-filled */}
            <div className="pt-2 border-t border-gray-100">
              <p className="text-xs text-gray-400 mb-3">
                Enter your contact details — we don't pre-fill these to confirm it's really you
              </p>
              <div className="grid grid-cols-1 gap-3">
                <div>
                  <label className="block text-sm font-medium text-gray-600 mb-2">Mobile phone</label>
                  <input
                    type="tel"
                    placeholder="(555) 000-0000"
                    value={data.phone ?? ''}
                    onChange={e => patch({ phone: e.target.value })}
                    className="w-full border-2 border-gray-200 rounded-2xl px-5 py-4 text-base focus:border-brand-500 outline-none transition-colors"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-600 mb-2">Email address</label>
                  <input
                    type="email"
                    placeholder="you@example.com"
                    value={data.email ?? ''}
                    onChange={e => patch({ email: e.target.value })}
                    className="w-full border-2 border-gray-200 rounded-2xl px-5 py-4 text-base focus:border-brand-500 outline-none transition-colors"
                  />
                </div>
              </div>
            </div>

            {employmentComplete && section === 'employment' && (
              <button
                onClick={() => setSection('identity')}
                className="w-full py-4 bg-brand-600 hover:bg-brand-700 text-white font-semibold rounded-2xl transition-all shadow-lg shadow-brand-200 text-base"
              >
                Continue →
              </button>
            )}

            {section === 'identity' && employmentComplete && (
              <div className="flex items-center gap-2 text-sm text-green-600 font-medium">
                <span className="text-green-500">✓</span> Financial info saved
              </div>
            )}
          </div>
        </Section>

        {/* ── Section 3: Identity ── */}
        <Section visible={section === 'identity'}>
          <div className="bg-white rounded-3xl shadow-sm border border-gray-100 p-6 space-y-5">
            <div>
              <h2 className="text-lg font-bold text-gray-900">One last thing</h2>
              <p className="text-sm text-gray-500 mt-1">Identity verification — we need just a few details</p>
            </div>

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-600 mb-2">
                  Social Security Number
                </label>
                <SSNInput value={data.ssn ?? ''} onChange={v => patch({ ssn: v })} />
                <div className="mt-2 flex items-center gap-2 text-xs text-green-700 bg-green-50 rounded-xl px-3 py-2">
                  <span>🔒</span>
                  <span>
                    <strong>Bank-level protection</strong> — your SSN is tokenized before it leaves your device.
                    We never store raw SSNs.
                  </span>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-medium text-gray-600 mb-2">Date of birth</label>
                  <input
                    type="date"
                    value={data.dateOfBirth ?? ''}
                    onChange={e => patch({ dateOfBirth: e.target.value })}
                    className="w-full border-2 border-gray-200 rounded-2xl px-4 py-4 focus:border-brand-500 outline-none transition-colors"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-600 mb-2">Citizenship</label>
                  <select
                    value={data.citizenship ?? ''}
                    onChange={e => patch({ citizenship: e.target.value })}
                    className="w-full border-2 border-gray-200 rounded-2xl px-4 py-4 focus:border-brand-500 outline-none transition-colors"
                  >
                    <option value="">Select</option>
                    <option value="US_CITIZEN">US Citizen</option>
                    <option value="PERMANENT_RESIDENT">Perm. Resident</option>
                    <option value="VISA_HOLDER">Visa Holder</option>
                  </select>
                </div>
              </div>
            </div>

            {/* Soft pull notice */}
            <div className="bg-blue-50 border border-blue-100 rounded-2xl p-4 flex items-start gap-3">
              <span className="text-blue-500 text-base mt-0.5">ℹ️</span>
              <div>
                <p className="text-sm font-medium text-blue-800">No impact to your credit score</p>
                <p className="text-xs text-blue-700 mt-0.5">
                  Submitting uses a <strong>soft credit check</strong> only.
                  If you proceed to accept an offer, a hard check will be explained separately.
                </p>
              </div>
            </div>

            {/* Consent */}
            <div className="space-y-3 pt-2 border-t border-gray-100">
              {[
                { key: 'consentElectronicRecords' as const, label: 'I agree to receive electronic records and disclosures' },
                { key: 'consentPrivacyPolicy' as const,     label: 'I agree to the Privacy Policy and Terms of Service' },
              ].map(item => (
                <label key={item.key} className="flex items-start gap-3 cursor-pointer group">
                  <div className={`mt-0.5 w-5 h-5 rounded-md border-2 flex-shrink-0 flex items-center justify-center transition-all ${
                    data[item.key]
                      ? 'bg-brand-600 border-brand-600'
                      : 'border-gray-300 group-hover:border-brand-400'
                  }`}
                    onClick={() => patch({ [item.key]: !data[item.key] })}
                  >
                    {data[item.key] && <span className="text-white text-xs">✓</span>}
                  </div>
                  <span className="text-sm text-gray-600">{item.label}</span>
                </label>
              ))}
            </div>

            <button
              onClick={handleSubmit}
              disabled={!identityComplete || submitting}
              className="w-full py-4 bg-brand-600 hover:bg-brand-700 disabled:bg-gray-200 disabled:text-gray-400 text-white font-bold rounded-2xl transition-all shadow-lg shadow-brand-200 text-base flex items-center justify-center gap-2"
            >
              {submitting ? (
                <>
                  <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24" fill="none">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
                  </svg>
                  Submitting...
                </>
              ) : (
                'Submit My Application 🚀'
              )}
            </button>

            <p className="text-xs text-center text-gray-400">
              Submitting will not affect your credit score
            </p>
          </div>
        </Section>
      </main>
    </div>
  )
}
