# Domain Model

## Bounded Context

Offer Acceptance

---

# Aggregate: OfferAcceptanceSession

Represents the e-sign session for an approved application.

Created when `FinalDecisionApproved` is received. One session per application — idempotent on duplicate events.

Attributes:

* sessionId (UUID)
* applicationId (UUID)
* declarations (List\<Declaration\>)
* status (PENDING | SIGNED)
* createdAt (LocalDateTime)

## Factory

`OfferAcceptanceSession.create(applicationId)` — assigns `UUID.randomUUID()`, loads `STANDARD_DECLARATIONS`, sets status `PENDING`.

`OfferAcceptanceSession.reconstitute(...)` — rehydrates from persistence.

## Behaviour

`session.sign(acceptedDeclarationIds, ipAddress)`:

1. Throws `AlreadySignedException` if status is already `SIGNED`
2. Finds all mandatory declarations not present in `acceptedDeclarationIds`
3. Throws `MandatoryDeclarationMissingException` if any are missing
4. Sets status to `SIGNED`
5. Returns a new `ESignRecord`

---

# Value Object: Declaration

Immutable. Defined as a Java record.

Attributes:

* declarationId (UUID) — assigned at session creation, stable within a session
* declarationType (String) — e.g. `TERMS_AND_CONDITIONS`
* title (String)
* content (String)
* mandatory (boolean)

Factory: `Declaration.of(declarationType, title, content, mandatory)` — assigns `UUID.randomUUID()`.

Standard declarations are defined as `OfferAcceptanceSession.STANDARD_DECLARATIONS` (static list):

```
TERMS_AND_CONDITIONS   mandatory: true
PRIVACY_POLICY         mandatory: true
CREDIT_REPORTING       mandatory: true
ELECTRONIC_SIGNATURE   mandatory: true
MARKETING              mandatory: false
```

---

# Entity: ESignRecord

Represents the captured e-sign for a session.

Created by `session.sign(...)`. Immutable after creation.

Attributes:

* eSignId (UUID)
* applicationId (UUID)
* sessionId (UUID)
* acceptedDeclarationIds (Set\<UUID\>)
* ipAddress (String) — sourced from `HttpServletRequest.getRemoteAddr()`
* signedAt (LocalDateTime)

Factory: `ESignRecord.create(applicationId, sessionId, acceptedDeclarationIds, ipAddress)`

---

# Ports (Domain Interfaces)

## OfferAcceptanceSessionRepository

```
Optional<OfferAcceptanceSession> findByApplicationId(UUID applicationId)
void save(OfferAcceptanceSession session)
```

## ESignRecordRepository

```
void save(ESignRecord record)
```

## OfferAcceptanceEventPublisher

```
void publishESignCompleted(UUID applicationId, LocalDateTime signedAt, String correlationId)
```

---

# Domain Exceptions

| Exception | Thrown When | HTTP Code |
|-----------|------------|-----------|
| `SessionNotFoundException` | No session found for applicationId | 404 |
| `AlreadySignedException` | Session is already SIGNED | 409 |
| `MandatoryDeclarationMissingException` | One or more mandatory declarations absent | 422 |
