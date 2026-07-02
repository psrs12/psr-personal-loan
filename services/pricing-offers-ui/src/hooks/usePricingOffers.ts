import { useState, useEffect, useCallback } from 'react'
import { PricingOffer } from '../types'

interface UsePricingOffersResult {
  offers: PricingOffer[]
  loading: boolean
  error: string | null
  reload: () => void
}

export function usePricingOffers(apiBaseUrl: string, applicationId: string, sessionToken: string): UsePricingOffersResult {
  const [offers, setOffers] = useState<PricingOffer[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [tick, setTick] = useState(0)

  const reload = useCallback(() => setTick(t => t + 1), [])

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError(null)

    fetch(`${apiBaseUrl}/applications/${applicationId}/pricing-offers`, {
      headers: { Authorization: `Bearer ${sessionToken}` },
    })
      .then(async res => {
        if (!res.ok) throw new Error(`Failed to load offers (${res.status})`)
        return res.json() as Promise<PricingOffer[]>
      })
      .then(data => {
        if (!cancelled) {
          setOffers(data)
          setLoading(false)
        }
      })
      .catch(err => {
        if (!cancelled) {
          setError(err.message ?? 'Unable to load offers')
          setLoading(false)
        }
      })

    return () => { cancelled = true }
  }, [apiBaseUrl, applicationId, sessionToken, tick])

  return { offers, loading, error, reload }
}
