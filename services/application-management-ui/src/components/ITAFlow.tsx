import { useState } from 'react'
import { PrefillData, PricingOffer, ApplicationFormData } from '../types'
import InvitationEntry from './shared/InvitationEntry'

type Screen = 'entry' | 'form'

// Placeholder until ita-003 (pricing engine). Direct path and ITA path both
// go to the form with no offer pre-selected; offer selection happens post-credit-assessment.
const PLACEHOLDER_OFFER: PricingOffer = {
  id: 'tbd',
  label: 'Pending pricing',
  amount: 0,
  termMonths: 0,
  apr: 0,
  monthlyPayment: 0,
}

interface Props {
  tokenFromUrl: string | null
  apiBaseUrl: string
  FormComponent: React.ComponentType<{
    prefill: PrefillData | null
    selectedOffer: PricingOffer
    apiBaseUrl?: string
    onSubmit: (d: ApplicationFormData) => Promise<{ applicationId: string }>
  }>
  onSubmit: (data: ApplicationFormData) => Promise<{ applicationId: string }>
}

export default function ITAFlow({ tokenFromUrl, apiBaseUrl, FormComponent, onSubmit }: Props) {
  const [screen, setScreen] = useState<Screen>('entry')
  const [prefill, setPrefill] = useState<PrefillData | null>(null)

  if (screen === 'entry') {
    return (
      <InvitationEntry
        tokenFromUrl={tokenFromUrl}
        apiBaseUrl={apiBaseUrl}
        onITASuccess={data => {
          setPrefill(data)
          setScreen('form')
        }}
        onDirect={() => {
          setPrefill(null)  // Direct — no pre-fill
          setScreen('form')
        }}
      />
    )
  }

  return (
    <FormComponent
      prefill={prefill}
      selectedOffer={PLACEHOLDER_OFFER}
      apiBaseUrl={apiBaseUrl}
      onSubmit={onSubmit}
    />
  )
}
