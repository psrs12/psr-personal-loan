import { useState, useEffect, useRef } from 'react'

interface DocumentRequirement {
  requirementId: string
  documentType: string
  count: number
  description: string
  status: string
}

interface Props {
  applicationId: string
  sessionToken: string
  documentApiBaseUrl: string
}

export default function DocumentUploadMfe({ applicationId, sessionToken, documentApiBaseUrl }: Props) {
  const [requirements, setRequirements] = useState<DocumentRequirement[]>([])
  const [loading, setLoading] = useState(true)
  const [uploading, setUploading] = useState<Record<string, boolean>>({})
  const [errors, setErrors] = useState<Record<string, string>>({})
  const fileRefs = useRef<Record<string, HTMLInputElement | null>>({})

  function fetchRequirements() {
    fetch(`${documentApiBaseUrl}/applications/${applicationId}/documents/requirements`, {
      headers: { Authorization: `Bearer ${sessionToken}` },
    })
      .then(r => r.json())
      .then(setRequirements)
      .catch(() => {})
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchRequirements() }, [applicationId])

  async function handleUpload(requirementId: string, documentType: string) {
    const input = fileRefs.current[requirementId]
    if (!input?.files?.length) return

    const file = input.files[0]
    const formData = new FormData()
    formData.append('documentType', documentType)
    formData.append('file', file)

    setUploading(prev => ({ ...prev, [requirementId]: true }))
    setErrors(prev => ({ ...prev, [requirementId]: '' }))

    try {
      const res = await fetch(
        `${documentApiBaseUrl}/applications/${applicationId}/documents/upload`,
        {
          method: 'POST',
          headers: { Authorization: `Bearer ${sessionToken}` },
          body: formData,
        }
      )
      if (!res.ok) {
        const detail = await res.json().catch(() => ({}))
        setErrors(prev => ({ ...prev, [requirementId]: detail.detail ?? 'Upload failed.' }))
        return
      }
      input.value = ''
      fetchRequirements()
    } catch {
      setErrors(prev => ({ ...prev, [requirementId]: 'Unable to connect.' }))
    } finally {
      setUploading(prev => ({ ...prev, [requirementId]: false }))
    }
  }

  if (loading) return <div className="p-8 text-center text-gray-500">Loading document requirements…</div>

  const allComplete = requirements.length > 0 && requirements.every(r => r.status === 'COMPLETED')

  return (
    <div className="max-w-2xl mx-auto p-6">
      <h2 className="text-xl font-semibold text-gray-900 mb-1">Upload required documents</h2>
      <p className="text-sm text-gray-500 mb-6">
        Please upload the following documents to continue processing your application.
      </p>

      {allComplete && (
        <div className="mb-6 rounded-lg bg-green-50 border border-green-200 px-4 py-3 text-sm text-green-700">
          All documents received. Your application is being reviewed.
        </div>
      )}

      <div className="space-y-4">
        {requirements.map(req => (
          <div key={req.requirementId} className="border border-gray-200 rounded-xl p-4">
            <div className="flex items-start justify-between mb-2">
              <div>
                <p className="text-sm font-medium text-gray-800">{req.documentType.replace(/_/g, ' ')}</p>
                <p className="text-xs text-gray-500">{req.description} — {req.count} required</p>
              </div>
              <StatusBadge status={req.status} />
            </div>

            {req.status !== 'COMPLETED' && (
              <div className="mt-3 flex gap-2 items-center">
                <input
                  type="file"
                  accept=".pdf,.jpg,.jpeg,.png"
                  ref={el => { fileRefs.current[req.requirementId] = el }}
                  className="text-xs text-gray-500 file:mr-2 file:py-1 file:px-3 file:rounded file:border-0 file:text-xs file:font-medium file:bg-blue-50 file:text-blue-700"
                />
                <button
                  onClick={() => handleUpload(req.requirementId, req.documentType)}
                  disabled={uploading[req.requirementId]}
                  className="text-xs bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white font-medium rounded px-3 py-1.5 transition-colors"
                >
                  {uploading[req.requirementId] ? 'Uploading…' : 'Upload'}
                </button>
              </div>
            )}

            {errors[req.requirementId] && (
              <p className="text-xs text-red-600 mt-1">{errors[req.requirementId]}</p>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}

function StatusBadge({ status }: { status: string }) {
  const map: Record<string, { label: string; cls: string }> = {
    PENDING: { label: 'Pending', cls: 'bg-gray-100 text-gray-600' },
    UPLOADED: { label: 'Uploaded', cls: 'bg-yellow-100 text-yellow-700' },
    REJECTED: { label: 'Rejected', cls: 'bg-red-100 text-red-700' },
    COMPLETED: { label: 'Complete', cls: 'bg-green-100 text-green-700' },
  }
  const { label, cls } = map[status] ?? { label: status, cls: 'bg-gray-100 text-gray-600' }
  return <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${cls}`}>{label}</span>
}
