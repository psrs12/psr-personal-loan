export interface DocumentRequirement {
  requirementId: string
  documentType: string
  count: number
  description: string
  status: 'PENDING' | 'UPLOADED' | 'REJECTED' | 'COMPLETED'
}

export interface DocumentRecord {
  documentId: string
  documentType: string
  status: 'UPLOADED' | 'SCANNING' | 'VERIFIED' | 'REJECTED'
}
