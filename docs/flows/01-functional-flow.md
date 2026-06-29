# Personal Loan Acquisition Platform

## Functional Flow Document

Version: 1.0
Status: Current
Owner: Personal Loan Acquisition Platform

---

# 1. Purpose

This document describes the end-to-end functional flow of the Personal Loan Acquisition Platform from the first customer touchpoint through to loan funding. It is written in business terms and is intended for product owners, business analysts, and architects who need to understand the complete acquisition journey without diving into technical implementation detail.

---

# 2. High-Level Journey Overview

```
1. INVITATION          Customer receives a pre-approved personal loan invitation
        │
        ▼
2. APPLICATION         Customer completes and submits a personal loan application
        │
        ▼
3. EVALUATION          Identity, fraud, and credit checks are performed
        │
        ▼
4. PRICING             Eligible loan offers are retrieved and presented
        │
        ▼
5. OFFER SELECTION     Customer selects a loan offer and consents to a hard credit pull
        │
        ▼
6. DECISION            The Decision Engine issues a final credit decision
        │
   ┌────┴────────────────────┐
   │                         │
   ▼                         ▼
7a. APPROVED             7b. DECLINED
   │
   ├── DOCUMENTS_REQUIRED → 8. DOCUMENT COLLECTION
   │
   └── APPROVED DIRECT  → 9. OFFER ACCEPTANCE
        │
        ▼
10. FUNDING              Funds disbursed to customer
        │
        ▼
11. COMPLETED
```

---

# 3. Phase 1 — Invitation to Apply (ITA)

## Business Purpose
Allows the bank to pre-invite selected customers to apply for a personal loan. The invitation carries pre-approved terms, reducing the application burden and improving conversion.

## Flow

1. The bank generates an invitation for an eligible customer and issues an **invitation token**.
2. The customer receives the invitation via email, SMS, or partner channel containing a link with the token.
3. The customer clicks the link, landing on the **Invitation-to-Apply form** in the `application-management-ui`.
4. The form validates the token against the `invitation-service`, retrieving prefill data (name, address, pre-approved amount).
5. Name and address are displayed as read-only fields — the customer cannot edit them. Remaining application fields (employment, income, financial obligations) are completed by the customer.
6. On confirmation, an **Application** is created in `application-management-service` with status `STARTED`.

## Key Rules
- Invitation tokens are single-use and time-limited.
- **Name and address from the invitation are prefilled and locked — they cannot be edited by the customer.** This ensures the application is processed against the verified identity on the invitation.
- Only non-identity fields (employment, income, obligations) are editable by the applicant.
- Applications created from ITA carry the originating `intakeId` for event timeline continuity.

---

# 4. Phase 2 — Application Completion

## Business Purpose
Collect all information required to make a credit decision: personal details, employment, income, and financial obligations.

## Flow

1. The customer works through the application form (progressive or traditional format).
2. Progress is saved so the application can be resumed.
3. On final submission, the application transitions to `SUBMITTED`.
4. The `application-management-service` validates the submission and transitions the application to `PROCESSING`.

## Key Rules
- Applications in `IN_PROGRESS` can be saved and resumed.
- Submission triggers downstream evaluations automatically.

---

# 5. Phase 3 — Evaluation (Identity, Fraud, Credit)

## Business Purpose
Verify the customer's identity, detect fraud indicators, and assess creditworthiness before presenting offers.

## Flow

1. With the application in `PROCESSING`, orchestration services engage enterprise platforms concurrently where possible:
   - **Identity verification** — the Identity Platform confirms the applicant's identity.
   - **Fraud check** — the Fraud Platform screens for fraud indicators.
   - **Soft credit pull** — the Credit Platform retrieves the credit profile without affecting the applicant's credit score.
2. Results from each platform feed back into the application workflow via Kafka events.
3. The `pricing-orchestration-service` collects evaluation results and proceeds to offer pricing once all checks are complete.

## Key Rules
- A soft pull does **not** affect the applicant's credit score.
- A failed fraud or identity check will decline the application before offer presentation.

---

# 6. Phase 4 — Pricing and Offer Presentation

## Business Purpose
Retrieve personalised loan offers based on the evaluated applicant profile and present them for selection.

## Flow

1. The `pricing-orchestration-service` requests offers from the **Offer Management Platform**, supplying the evaluated applicant profile.
2. Eligible offers are returned (amount, term, APR, monthly payment).
3. Offers are rendered by the **`<pricing-offer-selector>`** web component (`pricing-offers-ui`). The host page embeds this component with the `application-id` and `api-base-url` attributes.
4. The component fetches and displays available offers via `usePricingOffers`, rendering each as an `OfferRow` within the `OfferList`.
5. The applicant reviews all offers and selects their preferred one.

## Key Rules
- All available offers are shown; the applicant chooses one.
- No credit impact has occurred at this stage (soft pull only).
- The `<pricing-offer-selector>` is a standalone web component — it can be embedded in any host page without coupling to the shell framework.

---

# 7. Phase 5 — Offer Selection and Hard Pull Consent

## Business Purpose
Confirm the applicant's chosen offer and obtain consent for a hard credit pull, which will be used for the final credit decision.

## Flow

1. After offer selection, the `<pricing-offer-selector>` component transitions to the **`ConsentStep`**, presenting the hard pull disclosure to the applicant.
2. The applicant reads the disclosure and confirms consent.
3. The component fires an `offer-confirmed` custom DOM event carrying `{ offerId }`, which the host page handles to proceed.
4. The `pricing-orchestration-service` submits the selected offer to the **Decision Engine** along with the hard pull request.
5. The Decision Engine executes a hard credit enquiry (this is recorded on the applicant's credit file).

## Key Rules
- The applicant must explicitly confirm on the ConsentStep before any hard pull is triggered.
- Withdrawal before confirmation means no hard pull occurs and no credit file impact.
- The `offer-confirmed` event is the integration point between the `pricing-offers-ui` web component and the host application.

---

# 8. Phase 6 — Final Decision

## Business Purpose
The Decision Engine issues a binding credit decision based on the hard pull and the application data.

## Flow

The `pricing-orchestration-service` receives the Decision Engine's final outcome and routes it:

| Outcome | Description | Next Step |
|---------|-------------|-----------|
| `APPROVED` | Application is approved, no further documents required | Proceed to Offer Acceptance |
| `DECLINED` | Application is not approved | Send adverse action notice; application closed |
| `DOCUMENTS_REQUIRED` | Conditional approval pending document submission | Proceed to Document Collection |
| `REFERRED` | Requires manual underwriting review (no document list) | Referred to underwriting team |

The `application-management-service` is updated to the corresponding status via the `pricing-orchestration-service`.

---

# 9. Phase 7a — Offer Acceptance (APPROVED path)

## Business Purpose
Obtain the applicant's legally binding acceptance of the loan terms through electronic signature.

## Flow

1. The `offer-acceptance-service` receives the `FinalDecisionApproved` event and creates an `OfferAcceptanceSession` containing the standard declarations.
2. The applicant logs in to the self-service portal using their **Application ID + last 4 digits of SSN + date of birth**.
3. A session token is issued by `application-management-service`.
4. The portal shell detects status `APPROVED` and loads the **OfferAcceptanceMfe**.
5. The applicant reads and accepts all declarations (mandatory ones must be ticked).
6. On submission, the `offer-acceptance-service` records the **ESignRecord** and publishes `ESignCompleted`.
7. `application-management-service` consumes the event and transitions the application to `OFFER_ACCEPTED`.
8. The applicant is shown the **ConfirmationMfe**.

## Key Rules
- All mandatory declarations must be accepted; the sign button is disabled otherwise.
- E-sign is idempotent — a second submission for an already-signed session is rejected (409).
- The session token expires after 30 minutes.

---

# 10. Phase 7b — Document Collection (DOCUMENTS_REQUIRED path)

## Business Purpose
Collect supporting documents as required by the Decision Engine before a final credit decision can be confirmed.

## Flow

1. The `pricing-orchestration-service` publishes a `FinalDecisionDocumentsRequired` event containing Decision Engine document type codes and required counts.
2. The `document-service` consumes the event, applies the **Anti-Corruption Layer** mapping, and creates `DocumentRequirement` records.
3. The `document-service` calls `application-management-service` to confirm the status as `DOCUMENTS_REQUIRED`.
4. The applicant logs in to the self-service portal.
5. The portal shell detects status `DOCUMENTS_REQUIRED` and embeds the **`<document-upload-manager>`** web component (`document-management-ui`), passing the `api-base-url`, `application-id`, and `session-token` attributes.
6. The component fetches document requirements and renders one `RequirementCard` per required document type, with a progress bar showing overall completion.
7. For each document, the applicant uploads a file (PDF, JPEG, PNG).
8. Each upload is stored (S3), a `DocumentRecord` is created, and a virus scan is queued.
9. On a clean virus scan result, the `DocumentRecord` is marked `VERIFIED`. When the count for a requirement is met, the `DocumentRequirement` is marked `COMPLETED`.
10. When all requirements are `COMPLETED`, `document-service` publishes `DocumentsCompleted`.
11. `application-management-service` transitions the application to `UNDERWRITING`.
12. The underwriting team reviews all documents and issues a final determination.

## Key Rules
- Document type codes from the Decision Engine are mapped to platform domain types via the ACL in `document-service`.
- Rejected documents (virus detected) reset the requirement status; the applicant must re-upload.
- Document counts per type are enforced (e.g., 3 bank statements).

---

# 11. Phase 8 — Funding

## Business Purpose
Disburse the approved loan amount to the applicant's nominated bank account.

## Flow

1. With the application in `OFFER_ACCEPTED`, a funding request is submitted to the **Funding Platform** (planned: `funding-request-service`).
2. Application transitions to `FUNDING_PENDING`.
3. The Funding Platform confirms disbursement.
4. Application transitions to `FUNDED`, then `COMPLETED`.

## Key Rules
- Funding is not initiated until e-signature is captured.
- Funding confirmation triggers final notifications to the applicant.

---

# 12. Call Centre Agent Support

## Business Purpose
Enable call centre agents to view the complete application history to resolve customer queries quickly and accurately.

## Flow

1. Agent searches by `applicationId` or `intakeId` in the agent tooling.
2. `GET /applications/{id}/timeline` returns all events in chronological order.
3. Events span the full lifecycle including pre-application events from the invitation flow.
4. The agent can see every status transition, timestamp, and actor — giving full context for any customer query.

## Key Rules
- Timeline includes invitation events (via `intakeId` join) even though they predate application creation.
- No PII mutation is possible from this view — it is read-only.

---

# 13. Summary of Service Responsibilities

| Business Phase | Primary Service | Supporting UI | Supporting Services |
|---------------|-----------------|---------------|---------------------|
| Invitation | invitation-service | application-management-ui (ITA form) | — |
| Application | application-management-service | application-management-ui (ITA form) | — |
| Evaluation | pricing-orchestration-service | — | Identity, Fraud, Credit platforms |
| Pricing & Offer Display | pricing-orchestration-service | pricing-offers-ui (`<pricing-offer-selector>`) | Offer Management platform |
| Offer Selection & Hard Pull Consent | pricing-orchestration-service | pricing-offers-ui (`ConsentStep`) | Decision Engine platform |
| Final Decision | pricing-orchestration-service | — | Decision Engine platform |
| Offer Acceptance | offer-acceptance-service | application-management-ui (`OfferAcceptanceMfe`) | application-management-service |
| Document Collection | document-service | document-management-ui (`<document-upload-manager>`) | application-management-service |
| Funding | funding-request-service (planned) | application-management-ui (`ConfirmationMfe`) | Funding platform |
| Agent Support | application-management-service | — | — |

---

# 14. Related Documents

| Document | Location |
|----------|----------|
| Architecture Overview | `docs/architecture/000-architecture-overview.md` |
| Application State Machine | `docs/architecture/002-application-state-machine.md` |
| Component Interaction Flows | `docs/flows/02-component-interaction-flows.md` |
| Scenario Flows | `docs/flows/03-scenario-flows.md` |
