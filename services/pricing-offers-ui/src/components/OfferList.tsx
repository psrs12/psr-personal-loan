import { useState } from 'react'
import { PricingOffer } from '../types'
import OfferRow from './OfferRow'

interface Props {
  offers: PricingOffer[]
  onContinue: (selectedOfferId: string) => void
}

function resolveBadge(offer: PricingOffer, offers: PricingOffer[]): string | undefined {
  const lowestApr = Math.min(...offers.map(o => o.apr))
  const lowestPayment = Math.min(...offers.map(o => o.monthlyRepayment))

  if (offer.apr === lowestApr && offers.filter(o => o.apr === lowestApr).length === 1) {
    return 'Lowest rate'
  }
  if (offer.monthlyRepayment === lowestPayment && offers.filter(o => o.monthlyRepayment === lowestPayment).length === 1) {
    return 'Lowest payment'
  }
  return undefined
}

export default function OfferList({ offers, onContinue }: Props) {
  const [selectedId, setSelectedId] = useState<string | null>(
    offers.length === 1 ? offers[0].pricingOfferId : null
  )

  const canContinue = selectedId !== null

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-bold text-gray-900">Your personalised offers</h2>
        <p className="mt-1 text-sm text-gray-500">
          These offers are based on your credit profile. Selecting one won't affect your credit score yet.
        </p>
      </div>

      <div className="space-y-3">
        {offers.map((offer, idx) => (
          <OfferRow
            key={offer.pricingOfferId}
            offer={offer}
            selected={selectedId === offer.pricingOfferId}
            onSelect={setSelectedId}
            badge={idx === 0 && offers.length > 1 ? 'Best match' : resolveBadge(offer, offers)}
          />
        ))}
      </div>

      {offers.length === 0 && (
        <p className="text-center text-sm text-gray-500 py-8">
          No offers are currently available. Please check back shortly.
        </p>
      )}

      <button
        disabled={!canContinue}
        onClick={() => selectedId && onContinue(selectedId)}
        className={`w-full py-3 px-6 rounded-xl text-sm font-semibold transition-all ${
          canContinue
            ? 'bg-brand-600 text-white hover:bg-brand-700 active:scale-[0.99]'
            : 'bg-gray-100 text-gray-400 cursor-not-allowed'
        }`}
      >
        Continue with selected offer
      </button>
    </div>
  )
}
