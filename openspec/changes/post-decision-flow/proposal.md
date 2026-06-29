## Why

The Personal Loan Acquisition Platform currently routes final decisions to APPROVED, DECLINED, or REFERRED but has no capability to handle the post-decision flows: offer acceptance via e-sign, document collection driven by the Decision Engine, or applicant re-entry to the journey. Without these, the platform cannot complete the acquisition lifecycle for any applicant.

## What Changes

- Extend `FinalDecisionResponse` in pricing-orchestration-service with a `DOCUMENTS_REQUIRED` outcome; `REFERRED` is retained for true manual underwriting with no document list
- Introduce `document-service` as a new bounded context owning document requirements, upload, lifecycle, and completion tracking; integrates with the Decision Engine via a shared event contract and an Anti-Corruption Layer
- Introduce `offer-acceptance-service` as a new bounded context owning e-sign and declaration capture after an APPROVED decision
- Extend `application-management-service` with two new application states (`DOCUMENTS_REQUIRED`, `OFFER_ACCEPTED`) and a stubbed applicant login endpoint (applicationId + last4SSN + DOB; wired to a verification-service later)
- Extend `application-management-ui` as a micro-frontend shell that reads application state and routes applicants to the correct micro-frontend for each post-decision flow

## Capabilities

### New Capabilities

- `document-collection`: Owns the full document lifecycle — consuming the Decision Engine's `decision.documents-required` event, applying the ACL mapping from Decision Engine codes to domain document types, storing per-application document requirements, handling upload, tracking status (pending, uploaded, rejected, completed), and publishing `DocumentsCompleted` when all requirements are satisfied
- `offer-acceptance`: Owns e-sign and declaration capture after an APPROVED decision — presenting declarations to the applicant, capturing the e-sign record, transitioning application state to `OFFER_ACCEPTED`, and publishing `ESignCompleted`
- `applicant-login`: Owns the applicant re-entry authentication flow — accepting applicationId, last 4 digits of SSN, and DOB; delegating verification to a stub (to be replaced by a dedicated verification-service); returning a short-lived session token

### Modified Capabilities

- `pricing-orchestration`: Decision outcome extended with `DOCUMENTS_REQUIRED`; pricing-orchestration-service publishes `FinalDecisionDocumentsRequired` event when the Decision Engine signals documents are needed
- `application-state-machine`: Two new states added — `DOCUMENTS_REQUIRED` (terminal pending) and `OFFER_ACCEPTED` (post e-sign); valid transitions defined for both

## Impact

- **New services**: `document-service`, `offer-acceptance-service`
- **Extended service**: `pricing-orchestration-service` — new outcome type, new event published
- **Extended service**: `application-management-service` — new states, new login endpoint
- **Extended UI**: `application-management-ui` — shell routing logic, applicant login page, denial page, document upload micro-frontend, offer acceptance micro-frontend
- **External dependency**: Decision Engine — shared Kafka topic `decision.documents-required` with versioned event schema; document type code vocabulary agreed at contract level
- **New domain events**: `FinalDecisionDocumentsRequired`, `ESignCompleted`, `DocumentsSubmitted`, `DocumentsCompleted`, `DocumentRejected`
- **Shared event contract**: Document type code vocabulary (`BANK_STMT_3M`, `PAYSLIP_2`, etc.) versioned via schema registry
