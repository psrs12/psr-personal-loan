import { useState, useEffect, useCallback } from 'react'
import OfferAcceptanceMfe from '../mfe/OfferAcceptanceMfe'
import DenialMfe from '../mfe/DenialMfe'
import DocumentUploadMfe from '../mfe/DocumentUploadMfe'
import ConfirmationMfe from '../mfe/ConfirmationMfe'

type ApplicationStatus =
  | 'APPROVED'
  | 'DECLINED'
  | 'DOCUMENTS_REQUIRED'
  | 'OFFER_ACCEPTED'
  | 'UNDERWRITING'
  | 'FUNDING_PENDING'
  | 'FUNDED'
  | 'COMPLETED'
  | string

interface SessionContext {
  sessionToken: string
  applicationId: string
  applicationStatus: ApplicationStatus
}

interface Props {
  session: SessionContext
  appManagementApiBaseUrl: string
  offerAcceptanceApiBaseUrl: string
  documentApiBaseUrl: string
}

const TERMINAL_STATES: ApplicationStatus[] = ['DECLINED', 'FUNDED', 'COMPLETED']
const POLL_INTERVAL_MS = 3000

export default function ApplicantShell({
  session,
  appManagementApiBaseUrl,
  offerAcceptanceApiBaseUrl,
  documentApiBaseUrl,
}: Props) {
  const [status, setStatus] = useState<ApplicationStatus>(session.applicationStatus)

  const poll = useCallback(async () => {
    try {
      const res = await fetch(
        `${appManagementApiBaseUrl}/applications/${session.applicationId}`,
        { headers: { Authorization: `Bearer ${session.sessionToken}` } }
      )
      if (res.ok) {
        const data = await res.json()
        setStatus(data.status)
      }
    } catch {
      // silently continue polling on network error
    }
  }, [appManagementApiBaseUrl, session.applicationId, session.sessionToken])

  useEffect(() => {
    if (TERMINAL_STATES.includes(status)) return
    const id = setInterval(poll, POLL_INTERVAL_MS)
    return () => clearInterval(id)
  }, [status, poll])

  return (
    <div className="min-h-screen bg-gray-50 py-10">
      <div className="max-w-2xl mx-auto">
        <StatusHeader status={status} />
        <MfeRouter
          status={status}
          applicationId={session.applicationId}
          sessionToken={session.sessionToken}
          offerAcceptanceApiBaseUrl={offerAcceptanceApiBaseUrl}
          documentApiBaseUrl={documentApiBaseUrl}
          onESignComplete={() => setStatus('OFFER_ACCEPTED')}
        />
      </div>
    </div>
  )
}

interface RouterProps {
  status: ApplicationStatus
  applicationId: string
  sessionToken: string
  offerAcceptanceApiBaseUrl: string
  documentApiBaseUrl: string
  onESignComplete: () => void
}

function MfeRouter({
  status,
  applicationId,
  sessionToken,
  offerAcceptanceApiBaseUrl,
  documentApiBaseUrl,
  onESignComplete,
}: RouterProps) {
  switch (status) {
    case 'APPROVED':
      return (
        <OfferAcceptanceMfe
          applicationId={applicationId}
          sessionToken={sessionToken}
          offerAcceptanceApiBaseUrl={offerAcceptanceApiBaseUrl}
          onComplete={onESignComplete}
        />
      )
    case 'DECLINED':
      return <DenialMfe applicationId={applicationId} />

    case 'DOCUMENTS_REQUIRED':
      return (
        <DocumentUploadMfe
          applicationId={applicationId}
          sessionToken={sessionToken}
          documentApiBaseUrl={documentApiBaseUrl}
        />
      )
    case 'OFFER_ACCEPTED':
    case 'FUNDING_PENDING':
    case 'FUNDED':
    case 'COMPLETED':
      return <ConfirmationMfe applicationId={applicationId} />

    default:
      return (
        <div className="text-center py-12 text-gray-500">
          <p className="text-sm">Your application is being processed. We'll update you shortly.</p>
          <p className="text-xs text-gray-400 mt-1">Status: {status}</p>
        </div>
      )
  }
}

function StatusHeader({ status }: { status: string }) {
  const labels: Record<string, string> = {
    APPROVED: 'Offer ready for review',
    DECLINED: 'Application decision',
    DOCUMENTS_REQUIRED: 'Documents required',
    OFFER_ACCEPTED: 'Offer accepted',
    UNDERWRITING: 'Under review',
    FUNDING_PENDING: 'Funding in progress',
    FUNDED: 'Funded',
    COMPLETED: 'Complete',
  }
  return (
    <div className="mb-6 px-4">
      <p className="text-xs font-medium text-blue-600 uppercase tracking-wide">
        {labels[status] ?? 'Application status'}
      </p>
    </div>
  )
}
