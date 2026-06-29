import { useDocumentRequirements } from '../hooks/useDocumentRequirements'
import RequirementCard from './RequirementCard'

interface Props {
  apiBaseUrl: string
  applicationId: string
  sessionToken: string
}

export default function DocumentManager({ apiBaseUrl, applicationId, sessionToken }: Props) {
  const { requirements, loading, error, reload } = useDocumentRequirements(
    apiBaseUrl, applicationId, sessionToken
  )

  if (loading) {
    return (
      <div className="p-8 text-center text-gray-400 text-sm">Loading document requirements…</div>
    )
  }

  if (error) {
    return (
      <div className="p-6">
        <div className="rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
        <button
          onClick={reload}
          className="mt-3 text-sm text-blue-600 hover:underline"
        >
          Try again
        </button>
      </div>
    )
  }

  const total = requirements.length
  const completed = requirements.filter(r => r.status === 'COMPLETED').length
  const allComplete = total > 0 && completed === total

  return (
    <div className="max-w-2xl mx-auto p-6">
      <div className="mb-5">
        <h2 className="text-xl font-semibold text-gray-900">Required documents</h2>
        <p className="text-sm text-gray-500 mt-1">
          Upload the documents listed below to continue your application.
        </p>
      </div>

      {allComplete ? (
        <div className="mb-5 rounded-lg bg-green-50 border border-green-200 px-4 py-3 text-sm text-green-700">
          All documents received. Your application is now under review.
        </div>
      ) : (
        <div className="mb-5 flex items-center gap-2">
          <div className="flex-1 h-1.5 bg-gray-200 rounded-full overflow-hidden">
            <div
              className="h-full bg-blue-500 rounded-full transition-all"
              style={{ width: `${total > 0 ? (completed / total) * 100 : 0}%` }}
            />
          </div>
          <span className="text-xs text-gray-500 whitespace-nowrap">
            {completed} of {total} complete
          </span>
        </div>
      )}

      <div className="space-y-3">
        {requirements.map(req => (
          <RequirementCard
            key={req.requirementId}
            requirement={req}
            apiBaseUrl={apiBaseUrl}
            applicationId={applicationId}
            sessionToken={sessionToken}
            onUploadComplete={reload}
          />
        ))}
      </div>
    </div>
  )
}
