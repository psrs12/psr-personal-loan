interface Props {
  applicationId: string
}

export default function DenialMfe({ applicationId }: Props) {
  return (
    <div className="max-w-lg mx-auto p-6 text-center">
      <div className="mb-6">
        <div className="inline-flex h-16 w-16 items-center justify-center rounded-full bg-red-50 mb-4">
          <svg className="h-8 w-8 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M6 18L18 6M6 6l12 12" />
          </svg>
        </div>
        <h2 className="text-xl font-semibold text-gray-900 mb-2">Application not approved</h2>
        <p className="text-sm text-gray-500">
          We were unable to approve your loan application at this time.
        </p>
      </div>

      <div className="bg-gray-50 rounded-xl border border-gray-200 p-5 text-left mb-6">
        <h3 className="text-sm font-medium text-gray-700 mb-2">What happens next</h3>
        <ul className="text-sm text-gray-500 space-y-1 list-disc list-inside">
          <li>You will receive an adverse action notice by mail within 30 days</li>
          <li>The notice will explain the primary reasons for this decision</li>
          <li>You have the right to a free copy of your credit report</li>
          <li>You may dispute inaccurate information with the credit bureaus</li>
        </ul>
      </div>

      <p className="text-xs text-gray-400">
        Application reference: <span className="font-mono">{applicationId}</span>
      </p>
    </div>
  )
}
