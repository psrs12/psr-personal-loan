import React from 'react'
import { PricingOffer } from '../types'

interface Props {
  offer: PricingOffer
  selected: boolean
  onSelect: (id: string) => void
  badge?: string
}

function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }).format(amount)
}

function formatPercent(rate: number): string {
  return (rate * 100).toFixed(2) + '%'
}

export default function OfferRow({ offer, selected, onSelect, badge }: Props) {
  return (
    <label
      className={`flex items-center gap-4 p-4 rounded-xl border-2 cursor-pointer transition-all ${
        selected
          ? 'border-brand-600 bg-brand-50'
          : 'border-gray-200 bg-white hover:border-brand-300 hover:bg-gray-50'
      }`}
      onClick={() => onSelect(offer.pricingOfferId)}
    >
      {/* Radio indicator */}
      <div className={`w-5 h-5 rounded-full border-2 flex items-center justify-center shrink-0 ${
        selected ? 'border-brand-600' : 'border-gray-400'
      }`}>
        {selected && <div className="w-2.5 h-2.5 rounded-full bg-brand-600" />}
      </div>

      {/* Offer details */}
      <div className="flex-1 flex flex-wrap items-center gap-x-6 gap-y-1">
        <span className="text-base font-semibold text-gray-900">
          {formatCurrency(offer.approvedAmount)}
        </span>
        <span className="text-sm text-gray-600">
          {offer.termMonths} months
        </span>
        <span className="text-sm text-gray-600">
          {formatPercent(offer.apr)} APR
        </span>
        <span className="text-sm font-medium text-gray-800">
          {formatCurrency(offer.monthlyRepayment)}/mo
        </span>
      </div>

      {/* Badge */}
      {badge && (
        <span className="shrink-0 text-xs font-semibold px-2.5 py-1 rounded-full bg-brand-100 text-brand-700">
          {badge}
        </span>
      )}
    </label>
  )
}
