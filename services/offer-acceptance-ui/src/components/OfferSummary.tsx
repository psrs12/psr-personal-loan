import { ConfirmedOffer } from '../types'

interface Props {
  offer: ConfirmedOffer | null
}

function fmtCurrency(value?: number | null): string {
  if (value === null || value === undefined) return '—'
  return '$' + Number(value).toLocaleString()
}

export default function OfferSummary({ offer }: Props) {
  const amount = offer?.approvedAmount ?? null
  const term = offer?.termMonths ?? null
  const apr = offer?.apr ?? null
  const monthlyPayment = offer?.monthlyRepayment ?? null

  return (
    <div>
      <p className="text-xs font-semibold text-brand-600 uppercase tracking-wide mb-3">Your Approved Offer</p>
      <div className="rounded-xl border-2 border-brand-200 bg-brand-50 p-5 grid grid-cols-2 gap-y-3 gap-x-4 text-sm">
        <div>
          <p className="text-gray-500">Loan Amount</p>
          <p className="font-semibold text-gray-900">{fmtCurrency(amount)}</p>
        </div>
        <div>
          <p className="text-gray-500">Term</p>
          <p className="font-semibold text-gray-900">{term ? `${term} months` : '—'}</p>
        </div>
        {apr !== null && (
          <div>
            <p className="text-gray-500">APR</p>
            <p className="font-semibold text-gray-900">{apr}%</p>
          </div>
        )}
        {monthlyPayment !== null && (
          <div>
            <p className="text-gray-500">Monthly Payment</p>
            <p className="font-semibold text-gray-900">{fmtCurrency(monthlyPayment)}/mo</p>
          </div>
        )}
      </div>
    </div>
  )
}
