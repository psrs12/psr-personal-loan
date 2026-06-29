import { useState, FormEvent } from 'react'

interface LoginResult {
  sessionToken: string
  applicationId: string
  applicationStatus: string
  expiresAt: string
}

interface Props {
  apiBaseUrl: string
  onLoginSuccess: (result: LoginResult) => void
}

export default function ApplicantLoginPage({ apiBaseUrl, onLoginSuccess }: Props) {
  const [applicationId, setApplicationId] = useState('')
  const [last4SSN, setLast4SSN] = useState('')
  const [dateOfBirth, setDateOfBirth] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      const res = await fetch(`${apiBaseUrl}/applications/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ applicationId, last4SSN, dateOfBirth }),
      })
      if (res.status === 401) {
        setError('Verification failed. Please check your details and try again.')
        return
      }
      if (res.status === 404) {
        setError('Application not found.')
        return
      }
      if (!res.ok) {
        setError('An unexpected error occurred. Please try again.')
        return
      }
      const data: LoginResult = await res.json()
      onLoginSuccess(data)
    } catch {
      setError('Unable to connect. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-8 w-full max-w-md">
        <h1 className="text-2xl font-semibold text-gray-900 mb-2">Check your application</h1>
        <p className="text-sm text-gray-500 mb-6">Enter your details to access your loan application status.</p>

        {error && (
          <div className="mb-4 rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Application ID</label>
            <input
              type="text"
              required
              value={applicationId}
              onChange={e => setApplicationId(e.target.value)}
              placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Last 4 digits of SSN</label>
            <input
              type="password"
              required
              maxLength={4}
              pattern="\d{4}"
              value={last4SSN}
              onChange={e => setLast4SSN(e.target.value)}
              placeholder="••••"
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Date of birth</label>
            <input
              type="date"
              required
              value={dateOfBirth}
              onChange={e => setDateOfBirth(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white font-medium rounded-lg py-2.5 text-sm transition-colors"
          >
            {loading ? 'Verifying...' : 'Access my application'}
          </button>
        </form>
      </div>
    </div>
  )
}
