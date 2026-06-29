import { ApplicationFormData, LoanPurpose, PrefillData } from '../../types'

interface Props {
  prefill: PrefillData | null
  data: Partial<ApplicationFormData>
  onChange: (patch: Partial<ApplicationFormData>) => void
  onNext: () => void
}

const PURPOSES: { value: LoanPurpose; label: string }[] = [
  { value: 'DEBT_CONSOLIDATION', label: 'Debt consolidation' },
  { value: 'HOME_IMPROVEMENT',   label: 'Home improvement' },
  { value: 'AUTO',               label: 'Auto expenses' },
  { value: 'MEDICAL',            label: 'Medical expenses' },
  { value: 'MAJOR_PURCHASE',     label: 'Major purchase' },
  { value: 'OTHER',              label: 'Other' },
]

function LockedField({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <label className="block text-xs font-medium text-gray-500 mb-1">{label}</label>
      <div className="flex items-center gap-2 border border-gray-200 bg-gray-50 rounded-xl px-4 py-3">
        <span className="flex-1 text-gray-800 text-sm">{value}</span>
        <span className="text-gray-400 text-xs">🔒</span>
      </div>
    </div>
  )
}

export default function Step1PersonalInfo({ prefill, data, onChange, onNext }: Props) {
  const isITA = !!prefill
  const address = prefill?.address
  const addressString = address
    ? [address.street, address.city, address.state, address.zip].filter(Boolean).join(', ')
    : ''

  const hasAddress = isITA ? (!!addressString || !!data.streetAddress) : !!data.streetAddress
  const valid =
    (isITA || (!!data.firstName && !!data.lastName)) &&
    hasAddress &&
    !!data.phone &&
    !!data.email &&
    !!data.loanPurpose &&
    !!data.loanAmount

  return (
    <div className="space-y-6">
      <div className="text-center">
        <h1 className="text-2xl font-bold text-gray-900">
          {isITA ? `Let's confirm your details, ${prefill.firstName}` : 'Tell us about yourself'}
        </h1>
        {isITA && (
          <p className="mt-1 text-sm text-gray-400">
            Name and address are pre-filled. Enter your contact details below.
          </p>
        )}
      </div>

      {/* Name */}
      <div className="space-y-3">
        <h2 className="text-sm font-semibold text-gray-700">Personal details</h2>
        <div className="grid grid-cols-2 gap-3">
          {isITA ? (
            <>
              <LockedField label="First name" value={prefill.firstName} />
              <LockedField label="Last name"  value={prefill.lastName} />
            </>
          ) : (
            <>
              <div>
                <label className="block text-xs font-medium text-gray-500 mb-1">First name</label>
                <input
                  type="text"
                  placeholder="First name"
                  value={data.firstName ?? ''}
                  onChange={e => onChange({ firstName: e.target.value })}
                  className="w-full border border-gray-300 rounded-xl px-4 py-3 focus:ring-2 focus:ring-brand-500 outline-none text-sm"
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-500 mb-1">Last name</label>
                <input
                  type="text"
                  placeholder="Last name"
                  value={data.lastName ?? ''}
                  onChange={e => onChange({ lastName: e.target.value })}
                  className="w-full border border-gray-300 rounded-xl px-4 py-3 focus:ring-2 focus:ring-brand-500 outline-none text-sm"
                />
              </div>
            </>
          )}
        </div>

        {/* Address */}
        {isITA ? (
          addressString ? (
            <LockedField label="Address" value={addressString} />
          ) : (
            // Partial prefill — customer lookup failed, prospect must enter address
            <div>
              <label className="block text-xs font-medium text-gray-500 mb-1">
                Street address <span className="text-amber-600">(not available from your profile)</span>
              </label>
              <input
                type="text"
                placeholder="123 Main St, City, State, ZIP"
                value={data.streetAddress ?? ''}
                onChange={e => onChange({ streetAddress: e.target.value })}
                className="w-full border border-gray-300 rounded-xl px-4 py-3 focus:ring-2 focus:ring-brand-500 outline-none text-sm"
              />
            </div>
          )
        ) : (
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Street address</label>
            <input
              type="text"
              placeholder="123 Main St, City, State, ZIP"
              value={data.streetAddress ?? ''}
              onChange={e => onChange({ streetAddress: e.target.value })}
              className="w-full border border-gray-300 rounded-xl px-4 py-3 focus:ring-2 focus:ring-brand-500 outline-none text-sm"
            />
          </div>
        )}
      </div>

      {/* Contact — always entered by prospect */}
      <div className="space-y-3">
        <div className="flex items-center gap-2">
          <h2 className="text-sm font-semibold text-gray-700">Contact details</h2>
          <span className="text-xs text-gray-400 bg-gray-100 px-2 py-0.5 rounded-full">
            {isITA ? 'Not pre-filled for security' : 'Required'}
          </span>
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Mobile phone</label>
            <input
              type="tel"
              placeholder="(555) 000-0000"
              value={data.phone ?? ''}
              onChange={e => onChange({ phone: e.target.value })}
              className="w-full border border-gray-300 rounded-xl px-4 py-3 focus:ring-2 focus:ring-brand-500 outline-none text-sm"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Email address</label>
            <input
              type="email"
              placeholder="you@example.com"
              value={data.email ?? ''}
              onChange={e => onChange({ email: e.target.value })}
              className="w-full border border-gray-300 rounded-xl px-4 py-3 focus:ring-2 focus:ring-brand-500 outline-none text-sm"
            />
          </div>
        </div>
      </div>

      {/* Loan details */}
      <div className="space-y-3">
        <h2 className="text-sm font-semibold text-gray-700">Loan details</h2>
        <div>
          <label className="block text-xs font-medium text-gray-500 mb-1">How much do you need?</label>
          <div className="relative">
            <span className="absolute left-4 top-3.5 text-gray-400">$</span>
            <input
              type="number"
              placeholder="0"
              value={data.loanAmount ?? ''}
              onChange={e => onChange({ loanAmount: Number(e.target.value) })}
              className="w-full border border-gray-300 rounded-xl pl-7 pr-4 py-3 focus:ring-2 focus:ring-brand-500 outline-none text-sm"
            />
          </div>
        </div>
        <div>
          <label className="block text-xs font-medium text-gray-500 mb-1">What will you use it for?</label>
          <select
            value={data.loanPurpose ?? ''}
            onChange={e => onChange({ loanPurpose: e.target.value })}
            className="w-full border border-gray-300 rounded-xl px-4 py-3 focus:ring-2 focus:ring-brand-500 outline-none text-sm"
          >
            <option value="">Select a purpose</option>
            {PURPOSES.map(p => (
              <option key={p.value} value={p.value}>{p.label}</option>
            ))}
          </select>
        </div>
      </div>

      <button
        onClick={onNext}
        disabled={!valid}
        className="w-full py-4 bg-brand-600 hover:bg-brand-700 disabled:bg-gray-300 text-white font-semibold rounded-xl transition-colors"
      >
        Continue
      </button>
    </div>
  )
}
