import { useEffect, useState, useCallback } from 'react'
import { Declaration } from '../types'

const DECLARATIONS_RETRY_DELAY_MS = 2000
const DECLARATIONS_MAX_ATTEMPTS = 5

interface UseDeclarationsResult {
  declarations: Declaration[]
  loading: boolean
  error: string | null
}

// The offer-acceptance session is created asynchronously from a Kafka event fired when
// the application reaches APPROVED, so it may not exist yet the instant this component loads.
// Retry a few times before surfacing an error to the applicant.
export function useDeclarations(apiBaseUrl: string, applicationId: string, sessionToken: string): UseDeclarationsResult {
  const [declarations, setDeclarations] = useState<Declaration[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(
    async (attempt: number, cancelledRef: { current: boolean }) => {
      try {
        const res = await fetch(`${apiBaseUrl}/applications/${applicationId}/declarations`, {
          headers: { Authorization: `Bearer ${sessionToken}` },
          cache: 'no-store',
        })
        if (cancelledRef.current) return
        if (res.status === 404 && attempt < DECLARATIONS_MAX_ATTEMPTS) {
          setTimeout(() => load(attempt + 1, cancelledRef), DECLARATIONS_RETRY_DELAY_MS)
          return
        }
        if (!res.ok) throw new Error(`Failed to load declarations (${res.status})`)
        const data = await res.json()
        const list: Declaration[] = Array.isArray(data) ? data : (data.declarations ?? [])
        setDeclarations(list)
        setLoading(false)
      } catch (err: unknown) {
        if (cancelledRef.current) return
        setError(err instanceof Error ? err.message : 'Unable to load declarations')
        setLoading(false)
      }
    },
    [apiBaseUrl, applicationId, sessionToken]
  )

  useEffect(() => {
    const cancelledRef = { current: false }
    load(1, cancelledRef)
    return () => { cancelledRef.current = true }
  }, [load])

  return { declarations, loading, error }
}
