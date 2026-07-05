## Context

The Personal Loan Acquisition Platform currently has no compliance gate capability. Services operate in sequence (application → soft pull → offer → hard pull → decision → e-sign → documents → funding) with no regulatory checkpoints enforced by the platform itself. Compliance obligations under FCRA, ECOA, TILA, and the Bank Secrecy Act (AML) require specific controls at defined points in this flow that are currently absent.

The platform uses hexagonal architecture (ports and adapters), event-driven choreography via Kafka, and strict service ownership boundaries. Any compliance solution must fit within these patterns without coupling compliance logic into existing services.

## Goals / Non-Goals

**Goals:**

- Introduce a dedicated `compliance-orchestration-service` that owns all compliance gate logic behind port interfaces
- Enforce FCRA credit pull consent audit as a synchronous gate before hard pull initiation
- Enforce FCRA/ECOA adverse action notice generation and delivery on declined decisions
- Enforce TILA disclosure audit at e-sign — immutable record that required fields were presented and accepted
- Enforce AML/sanctions screening (Gate 1: pre-screening, Gate 5: pre-funding) via the enterprise Fraud/AML Platform
- Gate 5 must block funding if the AML re-check fails — application enters a compliance hold state

**Non-Goals:**

- Owning credit policy, decisioning, or offer pricing logic
- Replacing the enterprise Fraud/AML Platform — this service orchestrates, it does not execute
- Building a compliance case management UI
- Implementing regulatory reporting pipelines (out of scope for this change)
- Covering compliance obligations for loan servicing, payments, or collections

## Decisions

### Decision 1: Dedicated service over embedded checks

**Chosen**: A single `compliance-orchestration-service` owning all compliance gate logic.

**Rationale**: Regulatory obligations change independently of business flow logic. Embedding compliance checks in `pricing-orchestration-service` or `offer-acceptance-service` would create hidden coupling between business logic and regulatory interpretation. A dedicated service allows compliance/legal to own a single auditable boundary, and allows individual gate adapters to be replaced (e.g. when a regulation changes or a platform vendor changes) without touching business services.

**Alternative considered**: Embed checks in each service at the relevant step. Rejected because it scatters compliance logic across services, creates duplicated AML adapter code (Gates 1 and 5 both need it), and makes compliance audit trails fragmented across multiple services.

---

### Decision 2: Synchronous gate for FCRA consent audit (Gate 2), event-driven for others

**Chosen**: Gate 2 (FCRA consent) is synchronous — `pricing-orchestration-service` calls `compliance-orchestration-service` and waits for confirmation before initiating the hard pull. Gates 1, 3, 4, and 5 are event-driven.

**Rationale**: The hard pull is a legal trigger point — a credit inquiry cannot be initiated until consent is confirmed and recorded. This is a blocking regulatory requirement with no tolerance for eventual consistency. The other gates do not block an existing synchronous call in the same way: Gate 1 occurs before soft pull (which is already async), Gate 3 is triggered by a decision event, Gate 4 creates an audit record alongside e-sign (not before), and Gate 5 can block the funding request by consuming a pre-funding event and only releasing when the AML check passes.

**Alternative considered**: Make all gates event-driven. Rejected for Gate 2 because it creates a race condition — the hard pull could fire before the consent record is written, which is a FCRA violation.

---

### Decision 3: Compliance hold state for Gate 5 failures

**Chosen**: When the pre-funding AML re-check fails, the application enters a `COMPLIANCE_HOLD` state (owned by `application-management-service`). The compliance-orchestration-service publishes `PreFundingComplianceHeld`. Manual review resolves the hold.

**Rationale**: AML failures at funding cannot be silently dropped or automatically retried — they require human review. A hold state makes the application visible in operational queues and prevents funding from proceeding until the hold is resolved by an authorised operator.

**Alternative considered**: Reject the funding request and decline the application. Rejected because AML holds are often resolvable (false positives) and an automatic decline would inappropriately terminate applications that could proceed after review.

---

### Decision 4: TILA audit record as an immutable append (Gate 4)

**Chosen**: When `ESignCompleted` is received, `compliance-orchestration-service` creates an immutable `TilaDisclosureAuditRecord` containing APR, totalOfPayments, financeCharge, and loanTerm as they existed at signing. The record is never updated.

**Rationale**: TILA requires proof that the applicant was shown specific disclosure values at the time of signing. The record must be immutable and timestamped to serve as evidence in a regulatory examination. Any retroactive modification would compromise its value as a compliance artefact.

**Alternative considered**: Derive TILA values from the offer record at reporting time. Rejected because the offer could theoretically be modified after signing; the audit record must capture the values as presented at the moment of consent.

---

### Decision 5: Adverse action reason code mapping in compliance-orchestration-service

**Chosen**: `compliance-orchestration-service` owns the mapping from Decision Platform reason codes to FCRA/ECOA-compliant adverse action reason descriptions. It consumes `FinalDecisionDeclined` and produces an adverse action notice via the Notification Platform.

**Rationale**: Adverse action reason wording is a regulatory requirement under ECOA Regulation B. The mapping belongs in the compliance boundary, not in `pricing-orchestration-service` which owns routing, or in the Notification Platform which owns delivery.

## Risks / Trade-offs

**Gate 2 adds latency to hard pull initiation** → Mitigation: The consent audit write is a single database insert in `compliance-orchestration-service` with a synchronous REST response — target P99 under 200ms. The hard pull itself dominates latency.

**AML Platform unavailability blocks application progression (Gates 1 and 5)** → Mitigation: Circuit breaker on the AML adapter with a configurable timeout. Gate 1 failure holds the application in `PROCESSING` with an operational alert; Gate 5 failure places the application in `COMPLIANCE_HOLD`. Neither auto-approves to protect the platform.

**TILA field values must be sourced accurately at e-sign time** → Mitigation: `offer-acceptance-service` extends the `POST /esign` request body with TILA fields (APR, totalOfPayments, financeCharge, loanTerm) sourced from the confirmed offer. `compliance-orchestration-service` stores exactly what is passed — it does not re-derive values.

**Adverse action notice timing is regulated (ECOA requires notice within 30 days)** → Mitigation: `FinalDecisionDeclined` triggers adverse action generation immediately. A scheduled job monitors for any `DECLINED` applications without an associated `AdverseActionIssued` event older than 24 hours and raises an operational alert.

**Adding `COMPLIANCE_HOLD` state requires state machine extension** → Mitigation: `application-management-service` adds `COMPLIANCE_HOLD` as a non-terminal suspended state with a single valid transition: `COMPLIANCE_HOLD → FUNDING_PENDING` (on hold release) or `COMPLIANCE_HOLD → DECLINED` (on escalation). This follows the existing pattern for state machine extensions.

## Open Questions

1. **Adverse action delivery channel**: Should the adverse action notice be delivered by email, post, or both? Regulatory minimum is one durable channel. Confirm with legal/compliance which channels the Notification Platform supports and which are required.

2. **AML re-check timing at Gate 5**: Does the pre-funding AML re-check use the same Fraud/AML Platform endpoint as Gate 1, or is there a separate "funding clearance" operation? Confirm with the enterprise Fraud/AML Platform team.

3. **COMPLIANCE_HOLD resolution authority**: Which role (Compliance Analyst, Fraud Analyst, Operations) is authorised to release a hold? This determines the API auth model for the hold release endpoint.

4. **Consent audit retention**: FCRA requires credit pull consent records to be retained for a defined period. Confirm the retention requirement with legal and align with the data architecture's 7-year minimum.
