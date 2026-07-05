import { useState } from 'react'
import { PricingOffer } from '../types'

interface Props {
  selectedOffer: PricingOffer
  onConfirm: () => void
  onBack: () => void
  submitting: boolean
}

function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }).format(amount)
}

function formatPercent(rate: number): string {
  return (rate * 100).toFixed(2) + '%'
}

export default function ConsentStep({ selectedOffer, onConfirm, onBack, submitting }: Props) {
  const [hardPullConsent, setHardPullConsent] = useState(false)
  const [offerConsent, setOfferConsent] = useState(false)

  const canConfirm = hardPullConsent && offerConsent && !submitting

  return (
    <div className="space-y-6">
      {/* Back link */}
      <button
        onClick={onBack}
        className="flex items-center gap-1.5 text-sm text-brand-600 hover:text-brand-700 font-medium"
      >
        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
        </svg>
        Back to offers
      </button>

      <div>
        <h2 className="text-xl font-bold text-gray-900">Confirm your offer</h2>
        <p className="mt-1 text-sm text-gray-500">
          Review your selected offer and provide your consent before we run a full credit check.
        </p>
      </div>

      {/* Selected offer summary */}
      <div className="rounded-xl border-2 border-brand-200 bg-brand-50 p-5 space-y-3">
        <p className="text-xs font-semibold text-brand-600 uppercase tracking-wide">Selected offer</p>
        <div className="grid grid-cols-2 gap-y-3 gap-x-4 text-sm">
          <div>
            <p className="text-gray-500">Loan amount</p>
            <p className="font-semibold text-gray-900">{formatCurrency(selectedOffer.approvedAmount)}</p>
          </div>
          <div>
            <p className="text-gray-500">Term</p>
            <p className="font-semibold text-gray-900">{selectedOffer.termMonths} months</p>
          </div>
          <div>
            <p className="text-gray-500">APR</p>
            <p className="font-semibold text-gray-900">{formatPercent(selectedOffer.apr)}</p>
          </div>
          <div>
            <p className="text-gray-500">Monthly repayment</p>
            <p className="font-semibold text-gray-900">{formatCurrency(selectedOffer.monthlyRepayment)}/mo</p>
          </div>
          <div className="col-span-2">
            <p className="text-gray-500">Total repayable</p>
            <p className="font-semibold text-gray-900">{formatCurrency(selectedOffer.totalRepayable)}</p>
          </div>
        </div>
      </div>

      {/* Consent checkboxes */}
      <div className="space-y-4">
        <label className="flex items-start gap-3 cursor-pointer group">
          <div className="mt-0.5">
            <input
              type="checkbox"
              checked={hardPullConsent}
              onChange={e => setHardPullConsent(e.target.checked)}
              className="w-4 h-4 rounded border-gray-300 text-brand-600 focus:ring-brand-500"
            />
          </div>
          <span className="text-sm text-gray-700 leading-relaxed">
            <span className="font-medium">I authorise a full credit check.</span> I understand that confirming this offer
            will result in a hard enquiry on my credit report, which may temporarily affect my credit score.
          </span>
        </label>

        <label className="flex items-start gap-3 cursor-pointer group">
          <div className="mt-0.5">
            <input
              type="checkbox"
              checked={offerConsent}
              onChange={e => setOfferConsent(e.target.checked)}
              className="w-4 h-4 rounded border-gray-300 text-brand-600 focus:ring-brand-500"
            />
          </div>
          <span className="text-sm text-gray-700 leading-relaxed">
            <span className="font-medium">I accept the offer terms above.</span> I have reviewed and agree to the
            loan amount, interest rate, APR, monthly repayment, and total repayable shown.
          </span>
        </label>
      </div>

      <button
        disabled={!canConfirm}
        onClick={onConfirm}
        className={`w-full py-3 px-6 rounded-xl text-sm font-semibold transition-all flex items-center justify-center gap-2 ${
          canConfirm
            ? 'bg-brand-600 text-white hover:bg-brand-700 active:scale-[0.99]'
            : 'bg-gray-100 text-gray-400 cursor-not-allowed'
        }`}
      >
        {submitting ? (
          <>
            <svg className="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
            </svg>
            Processing…
          </>
        ) : (
          'Confirm and proceed'
        )}
      </button>

      <p className="text-xs text-center text-gray-400">
        Your information is protected with bank-level encryption.
      </p>
    </div>
  )
}
