## Context

The pricing-orchestration-service currently terminates at three decision outcomes: APPROVED, DECLINED, REFERRED. No capability exists to handle what happens after the decision lands — applicants cannot accept an offer, upload documents, or re-enter the journey. The platform is effectively incomplete beyond the decision step.

The Decision Engine is an enterprise system not owned by the personal loan platform. Integration must be at the event contract boundary only. The personal loan platform must not embed Decision Engine rule logic.

The application-management-ui currently serves the pre-decision flow (invitation, form, pricing). It must be extended as a micro-frontend shell that orchestrates all post-decision flows based on application state.

---

## Goals / Non-Goals

**Goals:**
- Complete the acquisition lifecycle for APPROVED, DECLINED, and DOCUMENTS_REQUIRED outcomes
- Introduce document-service and offer-acceptance-service as independent bounded contexts
- Integrate with the Decision Engine via a versioned shared event contract and an ACL adapter
- Enable applicant re-entry via applicationId + last4SSN + DOB login (stubbed verification)
- Extend application-management-ui as the micro-frontend shell and routing orchestrator

**Non-Goals:**
- Generating or evaluating document requirements (owned by Decision Engine)
- Executing the final lending decision (owned by Decision Engine)
- Virus scanning implementation (document-service publishes to a scan queue; scanner is external)
- Adverse action notification content (owned by Notification Orchestration)
- Replacing the stub applicant login with the real verification-service (future change)
- Underwriting workflow for REFERRED applications (separate capability, future change)

---

## Decisions

### Decision 1: DOCUMENTS_REQUIRED is a distinct outcome from REFERRED

**Decision**: Add `DOCUMENTS_REQUIRED` as a fourth `FinalDecisionOutcome` value. `REFERRED` is retained exclusively for true manual underwriting where no document list is returned.

**Rationale**: The two paths have fundamentally different downstream flows. `DOCUMENTS_REQUIRED` is automated — documents are listed by the Decision Engine, collected by document-service, and processing continues automatically on completion. `REFERRED` is manual — an underwriter reviews the case with no predefined document list. Conflating them forces either path to carry data it does not need.

**Alternative considered**: Treating `REFERRED` as the parent state and inferring documents-needed from the presence/absence of a document list in the event payload. Rejected because conditional branching on payload shape is fragile and makes the state machine ambiguous.

---

### Decision 2: Anti-Corruption Layer lives in document-service, not in pricing-orchestration-service

**Decision**: The ACL that maps Decision Engine document type codes to the personal loan domain's `DocumentType` enum lives in document-service's infrastructure adapter layer.

**Rationale**: document-service owns the document domain. The mapping from external codes to internal types is a document concern, not a pricing orchestration concern. Placing the ACL in pricing-orchestration-service would give it knowledge of document types it does not own and cannot reason about.

**Alternative considered**: Shared library containing the mapping. Rejected — shared libraries between services create implicit coupling and deployment dependencies.

---

### Decision 3: Decision Engine integration is event-only, no synchronous callback

**Decision**: pricing-orchestration-service publishes `FinalDecisionDocumentsRequired` after receiving the `DOCUMENTS_REQUIRED` outcome from the Decision Engine. document-service listens to this event. No synchronous call from document-service to pricing-orchestration-service.

**Rationale**: Consistent with the platform's choreography model. The Decision Engine's response arrives synchronously inside the existing `requestFinalDecision` call; the platform then re-publishes the outcome as a domain event for downstream services. This isolates downstream services from the Decision Engine's API entirely.

---

### Decision 4: offer-acceptance-service triggers on FinalDecisionApproved event, not on API call

**Decision**: offer-acceptance-service listens to the `FinalDecisionApproved` Kafka event to initialise the e-sign session. The UI calls offer-acceptance-service's REST API to retrieve declarations and submit the e-sign. offer-acceptance-service does not call pricing-orchestration-service.

**Rationale**: Decouples offer acceptance from pricing orchestration. If the approval event is replayed or retried, the offer-acceptance-service can idempotently reinitialise without side effects.

---

### Decision 5: Application state is the single source of truth for UI routing

**Decision**: application-management-ui (the shell) polls or subscribes to application state from application-management-service. All micro-frontend routing decisions are made by the shell based on the current `ApplicationStatus` value.

**Rationale**: A single state source prevents the UI from getting out of sync with the backend. Micro-frontends do not independently decide which step they are on — they are rendered by the shell when the state matches.

**Alternative considered**: Each micro-frontend independently calls the backend to determine its context. Rejected — creates distributed routing logic with no single authoritative flow.

---

### Decision 6: Applicant login is stubbed in application-management-service

**Decision**: A `/applications/login` endpoint is added to application-management-service. It validates applicationId exists, performs a stub verification of last4SSN and DOB (returns success if fields are non-empty and applicationId exists), and returns a short-lived JWT. The stub is replaced by a verification-service adapter in a future change.

**Rationale**: Unblocks the UI login flow without waiting for the verification-service to be built. The stub is clearly isolated in an adapter (VerificationPort) so replacement requires no domain changes.

---

### Decision 7: Shared event contract is defined as an OpenAPI-format JSON schema

**Decision**: The `decision.documents-required` event schema (including the document type code vocabulary) is defined in `docs/contracts/decision-engine-events/v1/documents-required.json`. Both the personal loan platform and the Decision Engine team reference this file.

**Rationale**: A versioned schema file is the lightest-weight shared contract that is still explicit and change-controlled. It avoids a schema registry dependency for now while remaining compatible with Confluent Schema Registry if adopted later.

---

## Risks / Trade-offs

**Document type code vocabulary changes in the Decision Engine** → The ACL adapter in document-service must be updated. Risk: unknown codes silently fail mapping. Mitigation: the ACL throws `UnknownDocumentTypeException` for unmapped codes, which is caught and sent to the dead-letter queue with an alert.

**E-sign replay on FinalDecisionApproved event retry** → offer-acceptance-service must be idempotent. Mitigation: e-sign session initialisation checks for an existing session by applicationId before creating a new one.

**Applicant login stub accepted as permanent** → The stub has no real verification. Mitigation: the stub is behind a `VerificationPort` interface; a feature flag disables the endpoint in production until the real verification-service is wired.

**Shell polling adds latency to state transitions** → The UI may not reflect a state change immediately. Mitigation: short polling interval (3s) with exponential backoff; future upgrade path to SSE or WebSocket.

---

## Migration Plan

1. Deploy `application-management-service` schema migration: add `DOCUMENTS_REQUIRED` and `OFFER_ACCEPTED` states
2. Deploy updated `pricing-orchestration-service`: new outcome type, publishes `FinalDecisionDocumentsRequired`
3. Deploy `document-service`: listens to `FinalDecisionDocumentsRequired` and `decision.documents-required`
4. Deploy `offer-acceptance-service`: listens to `FinalDecisionApproved`
5. Deploy updated `application-management-ui`: shell routing, login page, denial page, document upload MFE, offer acceptance MFE
6. No migration of in-flight applications — new states only apply to applications created after deployment

**Rollback**: Each service is independently deployable. Disable document-service and offer-acceptance-service by stopping Kafka consumers. UI falls back to the existing post-decision dead-end page.

---

## Open Questions

1. **Document upload storage**: Should document-service write to S3, a document management platform, or a temporary store pending virus scan? Assumed S3 for now.
2. **E-sign provider**: Is there an existing enterprise e-sign provider (DocuSign, Adobe Sign), or is e-sign a simple declaration checkbox capture for now?
3. **Session token lifetime for applicant login**: How long should the stub JWT be valid? Assumed 30 minutes.
4. **Document rejection retry**: If a document fails virus scan, can the applicant re-upload? Assumed yes — document-service allows re-upload for rejected documents up to a configurable retry limit.
