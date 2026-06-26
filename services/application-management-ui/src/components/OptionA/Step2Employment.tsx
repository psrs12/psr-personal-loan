import { ApplicationFormData, EmploymentType } from '../../types'

interface Props {
  data: Partial<ApplicationFormData>
  onChange: (patch: Partial<ApplicationFormData>) => void
  onNext: () => void
  onBack: () => void
}

const EMPLOYMENT_TYPES: { value: EmploymentType; label: string }[] = [
  { value: 'FULL_TIME', label: 'Full-time employed' },
  { value: 'PART_TIME', label: 'Part-time employed' },
  { value: 'SELF_EMPLOYED', label: 'Self-employed' },
  { value: 'RETIRED', label: 'Retired' },
  { value: 'UNEMPLOYED', label: 'Not currently employed' },
  { value: 'OTHER', label: 'Other' },
]

export default function Step2Employment({ data, onChange, onNext, onBack }: Props) {
  const valid =
    !!data.employmentType &&
    !!data.annualIncome &&
    !!data.monthlyHousingPayment &&
    !!data.phone &&
    !!data.email

  return (
    <div className="space-y-6">
      <div className="text-center">
        <h1 className="text-3xl font-bold text-gray-900">Tell us about yourself</h1>
        <p className="mt-2 text-gray-500">Employment and contact information</p>
      </div>

      {/* Employment */}
      <div className="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm space-y-4">
        <h2 className="font-semibold text-gray-800">Employment</h2>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Employment type</label>
            <select
              value={data.employmentType ?? ''}
              onChange={e => onChange({ employmentType: e.target.value })}
              className="w-full border border-gray-300 rounded-xl px-4 py-3 text-gray-900 focus:ring-2 focus:ring-brand-500 outline-none"
            >
              <option value="">Select type</option>
              {EMPLOYMENT_TYPES.map(t => (
                <option key={t.value} value={t.value}>{t.label}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Employer name</label>
            <input
              type="text"
              placeholder="Company name"
              value={data.employerName ?? ''}
              onChange={e => onChange({ employerName: e.target.value })}
              className="w-full border border-gray-300 rounded-xl px-4 py-3 focus:ring-2 focus:ring-brand-500 outline-none"
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Occupation</label>
            <input
              type="text"
              placeholder="Your job title"
              value={data.occupation ?? ''}
              onChange={e => onChange({ occupation: e.target.value })}
              className="w-full border border-gray-300 rounded-xl px-4 py-3 focus:ring-2 focus:ring-brand-500 outline-none"
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Annual income</label>
            <div className="relative">
              <span className="absolute left-4 top-3.5 text-gray-400">$</span>
              <input
                type="number"
                placeholder="0"
                value={data.annualIncome ?? ''}
                onChange={e => onChange({ annualIncome: e.target.value })}
                className="w-full border border-gray-300 rounded-xl pl-7 pr-4 py-3 focus:ring-2 focus:ring-brand-500 outline-none"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">
              Additional household income <span className="text-gray-400">(optional)</span>
            </label>
            <div className="relative">
              <span className="absolute left-4 top-3.5 text-gray-400">$</span>
              <input
                type="number"
                placeholder="0"
                value={data.additionalIncome ?? ''}
                onChange={e => onChange({ additionalIncome: e.target.value })}
                className="w-full border border-gray-300 rounded-xl pl-7 pr-4 py-3 focus:ring-2 focus:ring-brand-500 outline-none"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Monthly housing payment</label>
            <div className="relative">
              <span className="absolute left-4 top-3.5 text-gray-400">$</span>
              <input
                type="number"
                placeholder="0"
                value={data.monthlyHousingPayment ?? ''}
                onChange={e => onChange({ monthlyHousingPayment: e.target.value })}
                className="w-full border border-gray-300 rounded-xl pl-7 pr-4 py-3 focus:ring-2 focus:ring-brand-500 outline-none"
              />
            </div>
          </div>
        </div>
      </div>

      {/* Contact — NOT pre-filled per security requirement */}
      <div className="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm space-y-4">
        <h2 className="font-semibold text-gray-800">Contact information</h2>
        <p className="text-xs text-gray-500">
          Please enter your contact details. These are not pre-filled to confirm your identity.
        </p>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Mobile phone</label>
            <input
              type="tel"
              placeholder="(555) 000-0000"
              value={data.phone ?? ''}
              onChange={e => onChange({ phone: e.target.value })}
              className="w-full border border-gray-300 rounded-xl px-4 py-3 focus:ring-2 focus:ring-brand-500 outline-none"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Email address</label>
            <input
              type="email"
              placeholder="you@example.com"
              value={data.email ?? ''}
              onChange={e => onChange({ email: e.target.value })}
              className="w-full border border-gray-300 rounded-xl px-4 py-3 focus:ring-2 focus:ring-brand-500 outline-none"
            />
          </div>
        </div>
      </div>

      <div className="flex gap-3">
        <button
          onClick={onBack}
          className="flex-1 py-4 border border-gray-300 text-gray-700 font-semibold rounded-xl hover:bg-gray-50 transition-colors"
        >
          Back
        </button>
        <button
          onClick={onNext}
          disabled={!valid}
          className="flex-[2] py-4 bg-brand-600 hover:bg-brand-700 disabled:bg-gray-300 text-white font-semibold rounded-xl transition-colors"
        >
          Continue
        </button>
      </div>
    </div>
  )
}
