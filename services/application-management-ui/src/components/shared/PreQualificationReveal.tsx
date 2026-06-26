import { useState } from 'react'
import { PrefillData } from '../../types'

interface LoanProduct {
  id: string
  label: string
  amount: number
  termMonths: number
  apr: number
  monthlyPayment: number
  tag?: string
}

interface Props {
  prefill: PrefillData
  onStart: (selectedProduct: LoanProduct) => void
}

// Mock products — will come from Pricing Engine once ita-003 is wired.
// Pricing Engine receives: offer details + applicant details + soft credit report.
const MOCK_PRODUCTS: LoanProduct[] = [
  {
    id: 'prod-a',
    label: 'Standard',
    amount: 15000,
    termMonths: 36,
    apr: 11.49,
    monthlyPayment: 493,
    tag: 'Most popular',
  },
  {
    id: 'prod-b',
    label: 'Lower monthly',
    amount: 15000,
    termMonths: 60,
    apr: 12.99,
    monthlyPayment: 341,
  },
  {
    id: 'prod-c',
    label: 'Pay off faster',
    amount: 15000,
    termMonths: 24,
    apr: 10.49,
    monthlyPayment: 693,
    tag: 'Lowest total cost',
  },
]

function ProductCard({
  product,
  selected,
  onSelect,
}: {
  product: LoanProduct
  selected: boolean
  onSelect: () => void
}) {
  const totalCost = product.monthlyPayment * product.termMonths

  return (
    <button
      onClick={onSelect}
      className={`w-full text-left rounded-2xl border-2 p-5 transition-all relative ${
        selected
          ? 'border-brand-600 bg-brand-50 shadow-md shadow-brand-100'
          : 'border-gray-100 bg-white hover:border-gray-300'
      }`}
    >
      {product.tag && (
        <span className="absolute -top-3 left-4 bg-brand-600 text-white text-xs font-bold px-3 py-1 rounded-full">
          {product.tag}
        </span>
      )}

      <div className="flex items-start justify-between">
        <div>
          <p className={`text-xs font-semibold uppercase tracking-wider mb-1 ${selected ? 'text-brand-600' : 'text-gray-400'}`}>
            {product.label}
          </p>
          <p className="text-3xl font-extrabold text-gray-900">
            ${product.monthlyPayment}
            <span className="text-base font-medium text-gray-400">/mo</span>
          </p>
        </div>
        <div className={`w-5 h-5 rounded-full border-2 flex-shrink-0 mt-1 flex items-center justify-center transition-all ${
          selected ? 'border-brand-600 bg-brand-600' : 'border-gray-300'
        }`}>
          {selected && <span className="text-white text-xs">✓</span>}
        </div>
      </div>

      <div className="mt-4 grid grid-cols-3 gap-2 text-center">
        <div className="bg-gray-50 rounded-xl py-2">
          <p className="text-xs text-gray-400">Amount</p>
          <p className="text-sm font-semibold text-gray-800">${product.amount.toLocaleString()}</p>
        </div>
        <div className="bg-gray-50 rounded-xl py-2">
          <p className="text-xs text-gray-400">Term</p>
          <p className="text-sm font-semibold text-gray-800">{product.termMonths} mo</p>
        </div>
        <div className="bg-gray-50 rounded-xl py-2">
          <p className="text-xs text-gray-400">APR</p>
          <p className="text-sm font-semibold text-gray-800">{product.apr}%</p>
        </div>
      </div>

      <p className="mt-3 text-xs text-gray-400 text-right">
        Total repayment ~${totalCost.toLocaleString()}
      </p>
    </button>
  )
}

export default function PreQualificationReveal({ prefill, onStart }: Props) {
  const [selectedId, setSelectedId] = useState<string>(MOCK_PRODUCTS[0].id)

  const selected = MOCK_PRODUCTS.find(p => p.id === selectedId)!

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50 flex flex-col font-sans">

      {/* Nav */}
      <header className="bg-white border-b border-gray-100 px-6 py-4 flex items-center gap-3">
        <div className="w-9 h-9 bg-brand-600 rounded-xl flex items-center justify-center">
          <span className="text-white text-sm font-bold">PL</span>
        </div>
        <span className="font-semibold text-gray-900">Personal Loan</span>
      </header>

      <main className="flex-1 px-4 py-8">
        <div className="max-w-lg mx-auto space-y-6">

          {/* Greeting */}
          <div className="text-center space-y-2">
            <div className="inline-flex items-center gap-2 bg-green-100 text-green-800 text-xs font-semibold px-4 py-2 rounded-full">
              <span className="w-2 h-2 bg-green-500 rounded-full" />
              Your personalised offers are ready
            </div>
            <h1 className="text-3xl font-extrabold text-gray-900">
              {prefill.firstName ? `Here are your offers, ${prefill.firstName}.` : 'Here are your offers.'}
            </h1>
            <p className="text-gray-500 text-sm">
              Based on your profile — no impact to your credit score
            </p>
          </div>

          {/* Pricing engine notice — placeholder until ita-003 */}
          <div className="flex items-center gap-2.5 bg-amber-50 border border-amber-200 rounded-xl px-4 py-3 text-xs text-amber-700">
            <span className="text-base">⚙️</span>
            <span>
              <strong>Prototype:</strong> Offers shown are mock data.
              Live offers will be returned by the Pricing Engine after soft credit assessment.
            </span>
          </div>

          {/* Soft pull notice */}
          <div className="flex items-center gap-2.5 bg-blue-50 border border-blue-100 rounded-xl px-4 py-3 text-xs text-blue-700">
            <span className="text-base">🔍</span>
            <span>
              A <strong>soft credit check</strong> was used to generate these offers.
              This does <strong>not</strong> affect your credit score.
              A full credit check will only happen after you consent.
            </span>
          </div>

          {/* Product cards */}
          <div className="space-y-4">
            <p className="text-sm font-semibold text-gray-700">Select an offer to continue</p>
            {MOCK_PRODUCTS.map(p => (
              <ProductCard
                key={p.id}
                product={p}
                selected={selectedId === p.id}
                onSelect={() => setSelectedId(p.id)}
              />
            ))}
          </div>

          {/* What happens next */}
          <div className="bg-white rounded-2xl border border-gray-100 p-5 space-y-3">
            <p className="text-sm font-semibold text-gray-700">What happens next</p>
            {[
              { icon: '📝', step: 'Complete your application', sub: 'About 5 minutes' },
              { icon: '🔍', step: 'Full credit check', sub: 'Only after your consent' },
              { icon: '✅', step: 'Final offer confirmed', sub: 'Based on verified details' },
              { icon: '💰', step: 'Funds in your account', sub: 'As fast as next business day' },
            ].map((s, i) => (
              <div key={i} className="flex items-start gap-3">
                <div className="w-9 h-9 bg-brand-50 rounded-xl flex items-center justify-center text-lg flex-shrink-0">
                  {s.icon}
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-800">{s.step}</p>
                  <p className="text-xs text-gray-400">{s.sub}</p>
                </div>
              </div>
            ))}
          </div>

          {/* CTA */}
          <button
            onClick={() => onStart(selected)}
            className="w-full py-5 bg-brand-600 hover:bg-brand-700 text-white font-bold rounded-2xl
              transition-all shadow-xl shadow-brand-200 text-base flex items-center justify-center gap-2"
          >
            Continue with ${selected.monthlyPayment}/mo offer →
          </button>

          <p className="text-center text-xs text-gray-400">
            🔒 Bank-level encryption · You can change your selection before submitting
          </p>
        </div>
      </main>
    </div>
  )
}
