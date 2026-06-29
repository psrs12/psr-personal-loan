import { useState } from 'react'
import { ApplicationFormData, PrefillData, PricingOffer } from '../../types'
import { useSessionTimer } from '../../hooks/useSessionTimer'
import Step1PersonalInfo from './Step1PersonalInfo'
import Step2Employment from './Step2Employment'
import Step3Identity from './Step3Identity'
import OfferFlow from '../../../../pricing-offers-ui/src/components/OfferFlow'

interface Props {
  prefill: PrefillData | null
  selectedOffer: PricingOffer
  onSubmit: (data: ApplicationFormData) => Promise<{ applicationId: string }>
  apiBaseUrl?: string
}

const STEPS = ['Personal Info', 'Employment', 'Identity & Consent']

// 30 min from now if no session (Direct path)
const FALLBACK_EXPIRY = new Date(Date.now() + 30 * 60 * 1000).toISOString()

export default function TraditionalForm({ prefill, onSubmit, apiBaseUrl = '/api/v1/application-management' }: Props) {
  const [step, setStep] = useState(0)
  const [formData, setFormData] = useState<Partial<ApplicationFormData>>({})
  const [submitting, setSubmitting] = useState(false)
  const [applicationId, setApplicationId] = useState<string | null>(null)
  const { remaining, isExpired } = useSessionTimer(prefill?.expiresAt ?? FALLBACK_EXPIRY)

  const isITA = !!prefill
  const patch = (p: Partial<ApplicationFormData>) =>
    setFormData(prev => ({ ...prev, ...p }))

  const handleSubmit = async () => {
    setSubmitting(true)
    try {
      const result = await onSubmit(formData as ApplicationFormData)
      setApplicationId(result.applicationId)
    } finally {
      setSubmitting(false)
    }
  }

  if (isITA && isExpired) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
        <div className="bg-white rounded-2xl shadow-lg p-10 max-w-md text-center space-y-4">
          <div className="text-5xl">⏰</div>
          <h2 className="text-2xl font-bold text-gray-900">Your session has expired</h2>
          <p className="text-gray-500">Please return to your invitation email to restart.</p>
        </div>
      </div>
    )
  }

  if (applicationId) {
    return (
      <div className="min-h-screen bg-gray-50 font-sans">
        <header className="bg-white border-b border-gray-200 px-4 py-4">
          <div className="max-w-2xl mx-auto flex items-center gap-2">
            <div className="w-8 h-8 bg-brand-600 rounded-lg flex items-center justify-center">
              <span className="text-white text-xs font-bold">PL</span>
            </div>
            <span className="font-semibold text-gray-900">Personal Loan</span>
          </div>
        </header>
        <main className="max-w-2xl mx-auto px-4 py-8">
          <div className="mb-4">
            <div className="flex items-center gap-2 text-sm text-green-600 font-medium mb-1">
              <span className="text-green-500">✓</span> Application submitted
            </div>
            <p className="text-xs text-gray-400">
              We ran a soft credit check — this hasn't affected your score.
              Select an offer below to proceed.
            </p>
          </div>
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 md:p-8">
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
    <div className="min-h-screen bg-gray-50 font-sans">
      {/* Header */}
      <header className="bg-white border-b border-gray-200 px-4 py-4">
        <div className="max-w-2xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 bg-brand-600 rounded-lg flex items-center justify-center">
              <span className="text-white text-xs font-bold">PL</span>
            </div>
            <span className="font-semibold text-gray-900">Personal Loan</span>
          </div>
          {isITA && (
            <div className="flex items-center gap-2 text-sm text-amber-600 bg-amber-50 px-3 py-1.5 rounded-full">
              <span>⏱</span>
              <span>Session expires in <strong>{remaining}</strong></span>
            </div>
          )}
        </div>
      </header>

      {/* Path badge */}
      <div className={`border-b ${isITA ? 'bg-brand-600' : 'bg-gray-700'}`}>
        <div className="max-w-2xl mx-auto px-4 py-2 flex items-center gap-2 text-xs text-white/80">
          {isITA ? (
            <>
              <span className="bg-white/20 px-2 py-0.5 rounded-full font-semibold text-white">
                Invitation to Apply
              </span>
              <span>Name and address pre-filled from your profile</span>
            </>
          ) : (
            <>
              <span className="bg-white/20 px-2 py-0.5 rounded-full font-semibold text-white">
                Direct Application
              </span>
              <span>Please fill in all your details</span>
            </>
          )}
        </div>
      </div>

      {/* Progress */}
      <div className="bg-white border-b border-gray-100">
        <div className="max-w-2xl mx-auto px-4 py-4">
          <div className="flex items-center">
            {STEPS.map((label, i) => (
              <div key={i} className="flex items-center flex-1">
                <div className="flex flex-col items-center">
                  <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-semibold transition-colors ${
                    i < step ? 'bg-green-500 text-white' :
                    i === step ? 'bg-brand-600 text-white' :
                    'bg-gray-100 text-gray-400'
                  }`}>
                    {i < step ? '✓' : i + 1}
                  </div>
                  <span className={`mt-1 text-xs ${i === step ? 'text-brand-700 font-medium' : 'text-gray-400'}`}>
                    {label}
                  </span>
                </div>
                {i < STEPS.length - 1 && (
                  <div className={`flex-1 h-0.5 mx-2 mb-4 ${i < step ? 'bg-green-400' : 'bg-gray-200'}`} />
                )}
              </div>
            ))}
          </div>
        </div>
      </div>

      <main className="max-w-2xl mx-auto px-4 py-8">
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 md:p-8">
          {step === 0 && (
            <Step1PersonalInfo
              prefill={prefill}
              data={formData}
              onChange={patch}
              onNext={() => setStep(1)}
            />
          )}
          {step === 1 && (
            <Step2Employment
              data={formData}
              onChange={patch}
              onNext={() => setStep(2)}
              onBack={() => setStep(0)}
            />
          )}
          {step === 2 && (
            <Step3Identity
              data={formData}
              onChange={patch}
              onSubmit={handleSubmit}
              onBack={() => setStep(1)}
              submitting={submitting}
            />
          )}
        </div>
      </main>
    </div>
  )
}
