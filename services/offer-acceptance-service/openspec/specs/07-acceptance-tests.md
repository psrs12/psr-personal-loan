# Acceptance Tests

# Offer Acceptance Service

Version: 1.0

---

# Feature: Session Creation

## Scenario 1 — Session created on FinalDecisionApproved

**Given** a `FinalDecisionApproved` event is received for `applicationId: APP123`

**When** the `FinalDecisionApprovedConsumer` processes the event

**Then** an `OfferAcceptanceSession` shall exist for `APP123` with status `PENDING`

**And** the session shall contain all five standard declarations

---

## Scenario 2 — Duplicate FinalDecisionApproved is idempotent

**Given** an `OfferAcceptanceSession` already exists for `APP123`

**When** a second `FinalDecisionApproved` event is received for `APP123`

**Then** no second session shall be created

**And** the existing session shall be unchanged

---

# Feature: Declarations Retrieval

## Scenario 3 — Declarations returned for approved application

**Given** an `OfferAcceptanceSession` exists for `APP123` with status `PENDING`

**When** `GET /applications/APP123/declarations` is called

**Then** the response shall contain five declarations

**And** four declarations shall have `mandatory: true`

**And** one declaration (`MARKETING`) shall have `mandatory: false`

---

## Scenario 4 — Declarations not found — no session exists

**Given** no `OfferAcceptanceSession` exists for `APP999`

**When** `GET /applications/APP999/declarations` is called

**Then** the response shall be `404` with error code `ACCEPTANCE_SESSION_NOT_FOUND`

---

# Feature: E-Sign Capture

## Scenario 5 — Successful e-sign with all mandatory declarations accepted

**Given** an `OfferAcceptanceSession` exists for `APP123` with status `PENDING`

**When** `POST /applications/APP123/esign` is called with all four mandatory declaration IDs

**Then** the response shall be `200 OK` with `eSignId` and `signedAt`

**And** the session status shall be `SIGNED`

**And** an `ESignRecord` shall be persisted with the accepted declaration IDs and IP address

**And** an `ESignCompleted` event shall be published to `offer-acceptance.esign-completed`

---

## Scenario 6 — E-sign rejected — missing mandatory declaration

**Given** an `OfferAcceptanceSession` exists for `APP123` with status `PENDING`

**When** `POST /applications/APP123/esign` is called with only three of the four mandatory declarations

**Then** the response shall be `422` with error code `MANDATORY_DECLARATION_NOT_ACCEPTED`

**And** the session status shall remain `PENDING`

**And** no `ESignCompleted` event shall be published

---

## Scenario 7 — E-sign with marketing declaration accepted

**Given** an `OfferAcceptanceSession` exists for `APP123`

**When** `POST /applications/APP123/esign` is called with all four mandatory declarations plus the MARKETING declaration

**Then** the response shall be `200 OK`

**And** the `ESignRecord.acceptedDeclarationIds` shall contain all five declaration IDs

---

## Scenario 8 — E-sign rejected — session already signed

**Given** an `OfferAcceptanceSession` exists for `APP123` with status `SIGNED`

**When** `POST /applications/APP123/esign` is called

**Then** the response shall be `409` with error code `ESIGN_ALREADY_COMPLETED`

**And** no duplicate `ESignRecord` shall be created

---

## Scenario 9 — E-sign rejected — no session exists

**Given** no `OfferAcceptanceSession` exists for `APP999`

**When** `POST /applications/APP999/esign` is called

**Then** the response shall be `404` with error code `ACCEPTANCE_SESSION_NOT_FOUND`

**And** no `ESignRecord` shall be created and no event shall be published

---

# Non-Functional Acceptance Criteria

## Performance

All API responses shall complete within 500ms excluding Kafka publish latency.

## Security

- IP address captured from `HttpServletRequest.getRemoteAddr()` — never from a request header
- `Authorization: Bearer <token>` required on all endpoints
- No PII in log output

## Idempotency

- Duplicate `FinalDecisionApproved` events must not create duplicate sessions
- Duplicate e-sign attempts on a SIGNED session must return `409`, not create duplicate records

---

# Related Documents

```
services/offer-acceptance-service/openspec/specs/001-capability-spec.md
services/offer-acceptance-service/openspec/specs/02-domain-model.md
services/offer-acceptance-service/openspec/specs/04-sequence-diagrams.md
services/offer-acceptance-service/openspec/specs/05-api-contracts.md
services/offer-acceptance-service/openspec/specs/06-persistence-model.md
openspec/offer-acceptance/spec.md
```
