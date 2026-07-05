import { useState, useEffect, useCallback } from 'react'
import { DocumentRequirement } from '../types'

export function useDocumentRequirements(
  apiBaseUrl: string,
  applicationId: string,
  sessionToken: string,
) {
  const [requirements, setRequirements] = useState<DocumentRequirement[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const fetch_ = useCallback(() => {
    setError(null)
    fetch(`${apiBaseUrl}/applications/${applicationId}/documents/requirements`, {
      headers: { Authorization: `Bearer ${sessionToken}` },
      cache: 'no-store',
    })
      .then(r => {
        if (!r.ok) throw new Error('Failed to load requirements')
        return r.json()
      })
      .then(setRequirements)
      .catch(e => setError(e.message))
      .finally(() => setLoading(false))
  }, [apiBaseUrl, applicationId, sessionToken])

  useEffect(() => { fetch_() }, [fetch_])

  return { requirements, loading, error, reload: fetch_ }
}
