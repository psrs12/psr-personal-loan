import { useEffect, useState } from 'react'
import { ConfirmedOffer } from '../types'

export function useConfirmedOffer(pricingApiBaseUrl: string, applicationId: string, sessionToken: string): ConfirmedOffer | null {
  const [offer, setOffer] = useState<ConfirmedOffer | null>(null)

  useEffect(() => {
    let cancelled = false
    fetch(`${pricingApiBaseUrl}/applications/${applicationId}/selected-offer`, {
      headers: { Authorization: `Bearer ${sessionToken}` },
      cache: 'no-store',
    })
      .then(res => (res.ok ? res.json() : null))
      .then(data => { if (!cancelled && data) setOffer(data) })
      .catch(() => {})
    return () => { cancelled = true }
  }, [pricingApiBaseUrl, applicationId, sessionToken])

  return offer
}
