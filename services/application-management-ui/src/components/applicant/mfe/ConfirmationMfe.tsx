interface Props {
  applicationId: string
}

export default function ConfirmationMfe({ applicationId }: Props) {
  return (
    <div className="max-w-lg mx-auto p-6 text-center">
      <div className="mb-6">
        <div className="inline-flex h-16 w-16 items-center justify-center rounded-full bg-green-50 mb-4">
          <svg className="h-8 w-8 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M5 13l4 4L19 7" />
          </svg>
        </div>
        <h2 className="text-xl font-semibold text-gray-900 mb-2">Offer accepted</h2>
        <p className="text-sm text-gray-500">
          Your loan offer has been signed and accepted. Your application is now being processed for funding.
        </p>
      </div>

      <div className="bg-gray-50 rounded-xl border border-gray-200 p-5 text-left mb-6">
        <h3 className="text-sm font-medium text-gray-700 mb-2">What happens next</h3>
        <ul className="text-sm text-gray-500 space-y-1 list-disc list-inside">
          <li>Your application moves to funding review</li>
          <li>You will receive a confirmation email shortly</li>
          <li>Funds are typically disbursed within 1–3 business days</li>
        </ul>
      </div>

      <p className="text-xs text-gray-400">
        Application reference: <span className="font-mono">{applicationId}</span>
      </p>
    </div>
  )
}
