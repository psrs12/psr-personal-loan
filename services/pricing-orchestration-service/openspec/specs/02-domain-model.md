# Domain Model

# Pricing Orchestration Service

Version: 1.0

---

# Bounded Context

Pricing Orchestration

---

# Value Object: PricingRequest

Assembled by `PricingRequestAssemblyService` from the application data and soft pull result.

Attributes:

* applicationId (UUID)
* softPullCreditReportReferenceId (String)
* requestedAmount (BigDecimal)
* requestedTermMonths (Integer)
* loanPurpose (String)
* annualIncome (BigDecimal)
* employmentStatus (String)
* campaignOfferId (String — nullable, ITA only)
* campaignOfferTerms (String — nullable, ITA only)

---

# Value Object: PricingEngineResponse

Returned by the Decision Platform Pricing Engine after a pricing request.

Attributes:

* outcome (PricingOutcome: OFFERS_GENERATED | DECLINED)
* offers (List\<PricingOfferData\> — populated when outcome is OFFERS_GENERATED)
* declineReasonCode (String — populated when outcome is DECLINED)

## PricingOfferData

* pricingOfferId (String)
* approvedAmount (BigDecimal)
* interestRate (BigDecimal)
* apr (BigDecimal)
* termMonths (int)
* monthlyRepayment (BigDecimal)
* totalRepayable (BigDecimal)
* offerExpiryDate (LocalDateTime)
* pricingModelRef (String)
* bureauSnapshotRef (String)

---

# Value Object: FinalDecisionResponse

Returned by the Decision Platform after a final decision request.

Attributes:

* outcome (DecisionOutcome: APPROVED | DECLINED | REFERRED | DOCUMENTS_REQUIRED)
* reasonCode (String — populated for DECLINED)
* documents (List\<DocumentCode\> — populated for DOCUMENTS_REQUIRED)

## DocumentCode

* decisionEngineCode (String) — forwarded verbatim; NOT mapped by this service
* count (int)

---

# Domain Events (Consumed)

## ApplicationCreatedEvent

Consumed from Kafka. Triggers soft pull.

Fields: `applicationId` (UUID), `correlationId` (String)

## ConsentCapturedEvent

Consumed from Kafka. Triggers hard pull. Fired when applicant selects an offer and consents to a hard pull.

Fields: `applicationId` (UUID), `selectedPricingOfferId` (UUID), `correlationId` (String)

## HardPullCompletedEvent

Consumed from Kafka (or internal trigger). Triggers final decision submission.

Fields: `applicationId` (UUID), `hardPullCreditReportReferenceId` (String), `selectedPricingOfferId` (UUID)

## SoftPullCompletedEvent

Consumed from Kafka (or internal trigger). Triggers pricing request assembly.

Fields: `applicationId` (UUID), `softPullCreditReportReferenceId` (String)

---

# Ports (Domain Interfaces)

## CreditManagementPort

```
CreditPullResponse initiateSoftPull(UUID applicationId, String applicantReference)
CreditPullResponse initiateHardPull(UUID applicationId, String applicantReference)
```

`CreditPullResponse`: `{creditReportReferenceId: String, status: String}`

## DecisionPlatformPort

```
PricingEngineResponse submitPricingRequest(PricingRequest request)
FinalDecisionResponse submitFinalDecision(UUID applicationId, UUID selectedPricingOfferId, String hardPullRef)
```

## ApplicationManagementPort

```
void updateApplicationStatus(UUID applicationId, String status)
```

## PricingEventPublisher

```
void publishFinalDecisionApproved(UUID applicationId, UUID selectedOfferId, String correlationId)
void publishFinalDecisionDeclined(UUID applicationId, String reasonCode, String correlationId)
void publishFinalDecisionReferred(UUID applicationId, String correlationId)
void publishFinalDecisionDocumentsRequired(UUID applicationId, List<DocumentCode> documents, String correlationId)
```
