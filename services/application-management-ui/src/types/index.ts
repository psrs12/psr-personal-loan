export interface PrefillData {
  firstName: string
  lastName: string
  invitationToken: string
  expiresAt: string
  // Address from Customer Profile Platform (no phone/email — never pre-filled)
  address?: {
    street?: string
    city?: string
    state?: string
    zip?: string
  }
}

// Returned by the Pricing Engine (ita-003). Mocked in prototype.
export interface PricingOffer {
  id: string
  label: string
  amount: number
  termMonths: number
  apr: number
  monthlyPayment: number
  tag?: string
}

export interface ApplicationFormData {
  // Personal — locked (ITA) or entered (Direct)
  firstName: string
  lastName: string
  streetAddress: string
  // Contact — always entered by prospect regardless of path
  phone: string
  email: string
  // Loan
  loanAmount: number
  loanPurpose: string
  loanTermMonths: number
  // Employment
  employmentType: string
  employerName: string
  occupation: string
  annualIncome: string
  additionalIncome: string
  monthlyHousingPayment: string
  // Identity
  ssn: string
  dateOfBirth: string
  citizenship: string
  // Consent
  consentElectronicRecords: boolean
  consentCreditCheck: boolean
  consentPrivacyPolicy: boolean
}

export type LoanPurpose =
  | 'HOME_IMPROVEMENT'
  | 'AUTO'
  | 'DEBT_CONSOLIDATION'
  | 'MEDICAL'
  | 'MAJOR_PURCHASE'
  | 'OTHER'

export type EmploymentType =
  | 'FULL_TIME'
  | 'PART_TIME'
  | 'SELF_EMPLOYED'
  | 'RETIRED'
  | 'UNEMPLOYED'
  | 'OTHER'
