import { useState } from 'react'
import ApplicantLoginPage from './login/ApplicantLoginPage'
import ApplicantShell from './shell/ApplicantShell'

interface LoginResult {
  sessionToken: string
  applicationId: string
  applicationStatus: string
  expiresAt: string
}

interface Props {
  appManagementApiBaseUrl: string
  offerAcceptanceApiBaseUrl: string
  documentApiBaseUrl: string
}

export default function ApplicantPortal({
  appManagementApiBaseUrl,
  offerAcceptanceApiBaseUrl,
  documentApiBaseUrl,
}: Props) {
  const [session, setSession] = useState<LoginResult | null>(null)

  if (!session) {
    return (
      <ApplicantLoginPage
        apiBaseUrl={appManagementApiBaseUrl}
        onLoginSuccess={setSession}
      />
    )
  }

  return (
    <ApplicantShell
      session={session}
      appManagementApiBaseUrl={appManagementApiBaseUrl}
      offerAcceptanceApiBaseUrl={offerAcceptanceApiBaseUrl}
      documentApiBaseUrl={documentApiBaseUrl}
    />
  )
}
