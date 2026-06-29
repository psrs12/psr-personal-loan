import { useRef, useState } from 'react'
import { DocumentRequirement } from '../types'
import StatusBadge from './StatusBadge'

interface Props {
  requirement: DocumentRequirement
  apiBaseUrl: string
  applicationId: string
  sessionToken: string
  onUploadComplete: () => void
}

export default function RequirementCard({
  requirement,
  apiBaseUrl,
  applicationId,
  sessionToken,
  onUploadComplete,
}: Props) {
  const fileRef = useRef<HTMLInputElement>(null)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const isComplete = requirement.status === 'COMPLETED'

  async function handleUpload() {
    const file = fileRef.current?.files?.[0]
    if (!file) return

    const formData = new FormData()
    formData.append('documentType', requirement.documentType)
    formData.append('file', file)

    setUploading(true)
    setError(null)

    try {
      const res = await fetch(
        `${apiBaseUrl}/applications/${applicationId}/documents/upload`,
        {
          method: 'POST',
          headers: { Authorization: `Bearer ${sessionToken}` },
          body: formData,
        }
      )
      if (!res.ok) {
        const body = await res.json().catch(() => ({}))
        setError(body.detail ?? 'Upload failed. Please try again.')
        return
      }
      if (fileRef.current) fileRef.current.value = ''
      onUploadComplete()
    } catch {
      setError('Unable to connect. Please check your connection and try again.')
    } finally {
      setUploading(false)
    }
  }

  return (
    <div className={`border rounded-xl p-4 ${isComplete ? 'border-green-200 bg-green-50/30' : 'border-gray-200'}`}>
      <div className="flex items-start justify-between mb-1">
        <div>
          <p className="text-sm font-medium text-gray-800">
            {requirement.documentType.replace(/_/g, ' ')}
          </p>
          <p className="text-xs text-gray-500 mt-0.5">
            {requirement.description} &mdash; {requirement.count} required
          </p>
        </div>
        <StatusBadge status={requirement.status} />
      </div>

      {!isComplete && (
        <div className="mt-3 flex flex-wrap gap-2 items-center">
          <input
            type="file"
            accept=".pdf,.jpg,.jpeg,.png"
            ref={fileRef}
            className="text-xs text-gray-500 file:mr-2 file:py-1 file:px-3 file:rounded file:border-0 file:text-xs file:font-medium file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
          />
          <button
            onClick={handleUpload}
            disabled={uploading}
            className="text-xs bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white font-medium rounded px-3 py-1.5 transition-colors"
          >
            {uploading ? 'Uploading…' : 'Upload'}
          </button>
        </div>
      )}

      {requirement.status === 'REJECTED' && (
        <p className="text-xs text-red-600 mt-2">
          This file was rejected. Please upload a clean, unmodified copy.
        </p>
      )}

      {error && <p className="text-xs text-red-600 mt-2">{error}</p>}
    </div>
  )
}
