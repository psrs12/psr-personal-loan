import { ApplicationFormData, PricingOffer } from '../../types'

interface Props {
  selectedOffer: PricingOffer
  data: Partial<ApplicationFormData>
  onChange: (patch: Partial<ApplicationFormData>) => void
  onNext: () => void
}

const PURPOSES = [
  { value: 'DEBT_CONSOLIDATION', label: 'Debt consolidation' },
  { value: 'HOME_IMPROVEMENT',   label: 'Home improvement' },
  { value: 'AUTO',               label: 'Auto expenses' },
  { value: 'MEDICAL',            label: 'Medical expenses' },
  { value: 'MAJOR_PURCHASE',     label: 'Major purchase' },
  { value: 'OTHER',              label: 'Other' },
]

export default function Step1LoanDetails({ selectedOffer, data, onChange, onNext }: Props) {
  const valid = !!data.loanPurpose

  return (
    <div className="space-y-8">
      <div className="text-center">
        <h1 className="text-3xl font-bold text-gray-900">Confirm your loan details</h1>
        <p className="mt-2 text-gray-500">Based on the offer you selected</p>
      </div>

      {/* Selected offer — read-only summary */}
      <div className="bg-brand-50 border border-brand-100 rounded-2xl p-5">
        <p className="text-xs font-semibold text-brand-600 uppercase tracking-wider mb-3">
          Your selected offer
        </p>
        <div className="grid grid-cols-3 gap-3 text-center">
          <div className="bg-white rounded-xl py-3 shadow-sm">
            <p className="text-xs text-gray-400">Loan amount</p>
            <p className="text-lg font-bold text-gray-900">${selectedOffer.amount.toLocaleString()}</p>
          </div>
          <div className="bg-white rounded-xl py-3 shadow-sm">
            <p className="text-xs text-gray-400">Term</p>
            <p className="text-lg font-bold text-gray-900">{selectedOffer.termMonths} mo</p>
          </div>
          <div className="bg-white rounded-xl py-3 shadow-sm">
            <p className="text-xs text-gray-400">APR</p>
            <p className="text-lg font-bold text-gray-900">{selectedOffer.apr}%</p>
          </div>
        </div>
        <div className="mt-3 text-center">
          <span className="text-2xl font-extrabold text-brand-700">
            ~${selectedOffer.monthlyPayment}/mo
          </span>
          <span className="text-xs text-gray-400 ml-2">estimated monthly payment</span>
        </div>
        <p className="mt-2 text-center text-xs text-gray-400">
          * Final rate confirmed after full credit check (with your consent)
        </p>
      </div>

      {/* Purpose — only thing customer decides here */}
      <div className="space-y-2">
        <label className="block text-sm font-medium text-gray-700">
          What will you use it for?
        </label>
        <select
          value={data.loanPurpose ?? ''}
          onChange={e => onChange({ loanPurpose: e.target.value })}
          className="w-full border border-gray-300 rounded-xl px-4 py-3 text-gray-900 focus:ring-2 focus:ring-brand-500 focus:border-transparent outline-none"
        >
          <option value="">Select a purpose</option>
          {PURPOSES.map(p => (
            <option key={p.value} value={p.value}>{p.label}</option>
          ))}
        </select>
      </div>

      <button
        onClick={onNext}
        disabled={!valid}
        className="w-full py-4 bg-brand-600 hover:bg-brand-700 disabled:bg-gray-300 text-white font-semibold rounded-xl transition-colors text-lg"
      >
        Continue
      </button>
    </div>
  )
}
