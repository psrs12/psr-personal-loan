export interface PricingOffer {
  pricingOfferId: string
  approvedAmount: number
  interestRate: number
  apr: number
  termMonths: number
  monthlyRepayment: number
  totalRepayable: number
  offerExpiryDate: string
}

export interface ConsentPayload {
  selectedPricingOfferId: string
  consentChannel: string
  applicantReference?: string
}

export type OfferFlowStep = 'loading' | 'offer-list' | 'consent' | 'submitting' | 'error' | 'done'
