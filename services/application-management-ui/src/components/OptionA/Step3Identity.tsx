import { useState } from 'react'
import { ApplicationFormData } from '../../types'

interface Props {
  data: Partial<ApplicationFormData>
  onChange: (patch: Partial<ApplicationFormData>) => void
  onSubmit: () => void
  onBack: () => void
  submitting: boolean
}

function SSNInput({ value, onChange }: { value: string; onChange: (v: string) => void }) {
  const [visible, setVisible] = useState(false)

  const format = (raw: string) => {
    const digits = raw.replace(/\D/g, '').slice(0, 9)
    if (digits.length <= 3) return digits
    if (digits.length <= 5) return `${digits.slice(0, 3)}-${digits.slice(3)}`
    return `${digits.slice(0, 3)}-${digits.slice(3, 5)}-${digits.slice(5)}`
  }

  return (
    <div className="relative">
      <input
        type={visible ? 'text' : 'password'}
        placeholder="XXX-XX-XXXX"
        value={format(value)}
        onChange={e => onChange(e.target.value.replace(/\D/g, ''))}
        maxLength={11}
        className="w-full border border-gray-300 rounded-xl px-4 py-3 pr-24 focus:ring-2 focus:ring-brand-500 outline-none font-mono tracking-widest"
      />
      <button
        type="button"
        onClick={() => setVisible(v => !v)}
        className="absolute right-3 top-3 text-xs text-brand-600 hover:text-brand-800 font-medium"
      >
        {visible ? 'Hide' : 'Show'}
      </button>
    </div>
  )
}

export default function Step3Identity({ data, onChange, onSubmit, onBack, submitting }: Props) {
  const valid =
    (data.ssn?.length ?? 0) === 9 &&
    !!data.dateOfBirth &&
    !!data.citizenship &&
    data.consentElectronicRecords &&
    data.consentPrivacyPolicy

  return (
    <div className="space-y-6">
      <div className="text-center">
        <h1 className="text-3xl font-bold text-gray-900">Almost done</h1>
        <p className="mt-2 text-gray-500">We need a few identity details to verify you</p>
      </div>

      <div className="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="md:col-span-2">
            <label className="block text-xs font-medium text-gray-500 mb-1">
              Social Security Number
            </label>
            <SSNInput value={data.ssn ?? ''} onChange={v => onChange({ ssn: v })} />
            <p className="mt-1 text-xs text-gray-400 flex items-center gap-1">
              <span>🔒</span>
              Your SSN is tokenized before leaving your device
            </p>
          </div>

          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Date of birth</label>
            <input
              type="date"
              value={data.dateOfBirth ?? ''}
              onChange={e => onChange({ dateOfBirth: e.target.value })}
              className="w-full border border-gray-300 rounded-xl px-4 py-3 focus:ring-2 focus:ring-brand-500 outline-none"
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Citizenship status</label>
            <select
              value={data.citizenship ?? ''}
              onChange={e => onChange({ citizenship: e.target.value })}
              className="w-full border border-gray-300 rounded-xl px-4 py-3 focus:ring-2 focus:ring-brand-500 outline-none"
            >
              <option value="">Select status</option>
              <option value="US_CITIZEN">US Citizen</option>
              <option value="PERMANENT_RESIDENT">Permanent Resident</option>
              <option value="VISA_HOLDER">Visa Holder</option>
            </select>
          </div>
        </div>
      </div>

      {/* Security badge */}
      <div className="bg-green-50 border border-green-200 rounded-xl p-4 flex items-start gap-3">
        <span className="text-green-600 text-xl mt-0.5">🛡️</span>
        <div>
          <p className="text-sm font-medium text-green-800">Bank-level security</p>
          <p className="text-xs text-green-700 mt-0.5">
            Your SSN is tokenized via BOLT before it reaches our servers. We never store raw SSNs.
          </p>
        </div>
      </div>

      {/* Soft pull notice */}
      <div className="bg-blue-50 border border-blue-200 rounded-xl p-4 flex items-start gap-3">
        <span className="text-blue-500 text-lg mt-0.5">ℹ️</span>
        <div>
          <p className="text-sm font-medium text-blue-800">No impact to your credit score</p>
          <p className="text-xs text-blue-700 mt-0.5">
            Submitting your application uses a <strong>soft credit check</strong> only.
            Your score will not be affected at this stage.
          </p>
        </div>
      </div>

      {/* Consent */}
      <div className="space-y-3">
        {[
          {
            key: 'consentElectronicRecords' as keyof ApplicationFormData,
            label: 'I consent to receive electronic records and disclosures',
          },
          {
            key: 'consentPrivacyPolicy' as keyof ApplicationFormData,
            label: 'I have read and agree to the Privacy Policy and Terms of Service',
          },
        ].map(item => (
          <label key={item.key} className="flex items-start gap-3 cursor-pointer">
            <input
              type="checkbox"
              checked={(data[item.key] as boolean) ?? false}
              onChange={e => onChange({ [item.key]: e.target.checked })}
              className="mt-1 h-4 w-4 rounded border-gray-300 text-brand-600 focus:ring-brand-500"
            />
            <span className="text-sm text-gray-600">{item.label}</span>
          </label>
        ))}
      </div>

      <div className="flex gap-3">
        <button
          onClick={onBack}
          disabled={submitting}
          className="flex-1 py-4 border border-gray-300 text-gray-700 font-semibold rounded-xl hover:bg-gray-50 transition-colors"
        >
          Back
        </button>
        <button
          onClick={onSubmit}
          disabled={!valid || submitting}
          className="flex-[2] py-4 bg-brand-600 hover:bg-brand-700 disabled:bg-gray-300 text-white font-semibold rounded-xl transition-colors flex items-center justify-center gap-2"
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
            'Submit Application'
          )}
        </button>
      </div>
    </div>
  )
}
