import { Declaration } from '../types'

interface Props {
  declarations: Declaration[]
  checked: Record<string, boolean>
  onToggle: (id: string) => void
  onSubmit: (e: React.FormEvent) => void
  mandatoryAll: boolean
  submitting: boolean
  submitError: string | null
}

export default function DeclarationsList({ declarations, checked, onToggle, onSubmit, mandatoryAll, submitting, submitError }: Props) {
  return (
    <form onSubmit={onSubmit}>
      <div className="space-y-3">
        {declarations.map(d => (
          <label
            key={d.declarationId}
            className={`flex items-start gap-3 p-4 rounded-xl border cursor-pointer transition-colors ${
              checked[d.declarationId] ? 'border-brand-300 bg-brand-50' : 'border-gray-200'
            }`}
          >
            <input
              type="checkbox"
              checked={!!checked[d.declarationId]}
              onChange={() => onToggle(d.declarationId)}
              className="mt-0.5 w-4 h-4 rounded border-gray-300 text-brand-600 focus:ring-brand-500"
            />
            <div>
              {d.title && (
                <div className="text-sm font-medium text-gray-900">
                  {d.mandatory && <span className="text-red-700 mr-1">*</span>}
                  {d.title}
                </div>
              )}
              <div className="text-sm text-gray-500 mt-0.5">
                {d.text ?? d.content ?? d.description}
              </div>
            </div>
          </label>
        ))}
      </div>

      {submitError && (
        <div className="mt-4 text-sm text-red-700 bg-red-50 border border-red-200 rounded-lg p-3">{submitError}</div>
      )}

      <button
        type="submit"
        disabled={!mandatoryAll || submitting}
        className={`mt-6 w-full py-3 px-6 rounded-xl text-sm font-semibold transition-all ${
          mandatoryAll && !submitting
            ? 'bg-brand-600 text-white hover:bg-brand-700 active:scale-[0.99]'
            : 'bg-gray-100 text-gray-400 cursor-not-allowed'
        }`}
      >
        {submitting ? 'Submitting…' : 'Accept and Sign'}
      </button>
    </form>
  )
}
