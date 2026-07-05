export interface Declaration {
  declarationId: string
  declarationType?: string
  title?: string
  content?: string
  text?: string
  description?: string
  mandatory: boolean
}

export interface ConfirmedOffer {
  approvedAmount?: number
  termMonths?: number
  apr?: number
  monthlyRepayment?: number
}
