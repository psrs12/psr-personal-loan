# API Contracts

# Pricing Orchestration Service

Version: 1.0

---

# External APIs (Outbound)

## Credit Management Platform

### Soft Pull

```
POST <credit-management-platform>/soft-pull
```

Request:

```json
{
  "applicationId": "APP123",
  "applicantReference": "APPLICANT-REF-abc"
}
```

Response:

```json
{
  "creditReportReferenceId": "CR-SOFT-abc123",
  "status": "COMPLETED"
}
```

### Hard Pull

```
POST <credit-management-platform>/hard-pull
```

Request:

```json
{
  "applicationId": "APP123",
  "applicantReference": "APPLICANT-REF-abc"
}
```

Response:

```json
{
  "creditReportReferenceId": "CR-HARD-xyz789",
  "status": "COMPLETED"
}
```

Adapter: `CreditManagementAdapter` implements `CreditManagementPort`.

---

## Decision Platform — Pricing Engine

```
POST <decision-platform>/pricing-engine/evaluate
```

Request (assembled by `PricingRequestAssemblyService`):

```json
{
  "applicationId": "APP123",
  "softPullCreditReportReferenceId": "CR-SOFT-abc123",
  "requestedAmount": 25000.00,
  "requestedTermMonths": 60,
  "loanPurpose": "DEBT_CONSOLIDATION",
  "annualIncome": 75000.00,
  "employmentStatus": "EMPLOYED",
  "campaignOfferId": "OFF456",
  "campaignOfferTerms": "..."
}
```

Response (OFFERS_GENERATED):

```json
{
  "outcome": "OFFERS_GENERATED",
  "offers": [
    {
      "pricingOfferId": "PO-001",
      "approvedAmount": 25000.00,
      "interestRate": 9.99,
      "apr": 10.49,
      "termMonths": 60,
      "monthlyRepayment": 531.25,
      "totalRepayable": 31875.00,
      "offerExpiryDate": "2026-07-30T00:00:00",
      "pricingModelRef": "PM-REF-abc",
      "bureauSnapshotRef": "BS-REF-xyz"
    }
  ],
  "declineReasonCode": null
}
```

Adapter: `DecisionPlatformAdapter` implements `DecisionPlatformPort`.

---

## Decision Platform — Final Decision

```
POST <decision-platform>/final-decision
```

Request:

```json
{
  "applicationId": "APP123",
  "selectedPricingOfferId": "PO-001",
  "hardPullCreditReportReferenceId": "CR-HARD-xyz789"
}
```

Response (APPROVED):

```json
{
  "outcome": "APPROVED",
  "reasonCode": null,
  "documents": null
}
```

Response (DOCUMENTS_REQUIRED):

```json
{
  "outcome": "DOCUMENTS_REQUIRED",
  "reasonCode": null,
  "documents": [
    { "decisionEngineCode": "BANK_STMT_3M", "count": 1 },
    { "decisionEngineCode": "PAYSLIP_2", "count": 1 }
  ]
}
```

---

# Kafka Events

## Consumed

| Topic | Event | Trigger |
|-------|-------|---------|
| `application.created` | `ApplicationCreatedEvent` | Initiates soft pull |
| `application.consent-captured` | `ConsentCapturedEvent` | Initiates hard pull |

## Published

| Topic | Event | Outcome |
|-------|-------|---------|
| `pricing.final-decision` | `FinalDecisionApproved` | APPROVED |
| `pricing.final-decision` | `FinalDecisionDeclined` | DECLINED |
| `pricing.final-decision` | `FinalDecisionReferred` | REFERRED |
| `pricing.final-decision` | `FinalDecisionDocumentsRequired` | DOCUMENTS_REQUIRED |

### FinalDecisionApproved

```json
{
  "applicationId": "APP123",
  "selectedOfferId": "PO-001",
  "correlationId": "corr-abc-123",
  "timestamp": "2026-06-30T10:00:00Z"
}
```

### FinalDecisionDocumentsRequired

```json
{
  "applicationId": "APP123",
  "correlationId": "corr-abc-123",
  "timestamp": "2026-06-30T10:00:00Z",
  "documents": [
    { "decisionEngineCode": "BANK_STMT_3M", "count": 1 },
    { "decisionEngineCode": "PAYSLIP_2", "count": 1 }
  ]
}
```

`documents` contains Decision Engine codes verbatim. ACL mapping is owned by `document-service`.
