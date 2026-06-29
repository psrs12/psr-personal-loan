## Decisions

### D-001: BOLT as the sole SSN tokenization mechanism

Raw SSN enters the service at the API boundary and is immediately tokenized via BOLT. The raw value is not passed to any downstream method, not logged, and not stored. The BOLT token is the only SSN representation that travels through the system.

```
API Layer receives raw SSN
      │
      ▼ (immediate)
BoltTokenizationAdapter.tokenize(ssn) ──▶ BOLT API
      │                                        │
      │                                  token returned
      ▼
raw SSN reference dropped
      │
      ▼
ssnToken (String) flows through use case → stored in applicant table
```

**Why not keep AES/GCM as a fallback:** Having two SSN representations creates inconsistency and complicates comparison with external systems. BOLT is the single source of truth.

### D-002: Tokenization happens in the Application layer, not the API layer

`BoltTokenizationAdapter` is called from `CreateApplicationUseCase`, not from the controller. The `CreateApplicationCommand` carries the raw SSN string from the controller — this is acceptable because the command is an in-process object, not serialised or logged.

**Why not tokenize in the controller:** Controllers are thin; business orchestration (including security-sensitive transformation) belongs in the use case.

### D-003: SSNVerificationAdapter sends BOLT token, not raw SSN

The external SSN verification service also uses BOLT. So the flow is:

```
VerifySSNUseCase receives raw SSN
      │
      ▼
BoltTokenizationAdapter.tokenize(ssn) ──▶ BOLT token
      │
      ▼
SSNVerificationAdapter sends token to verification service
(verification service tokenizes independently and compares)
```

Raw SSN never leaves the JVM boundary.

### D-004: Database migration — alter column, no data migration needed

`ita-001` has not been deployed to any environment. The migration is a new Flyway script that alters `applicant.ssn_encrypted BYTEA` to `applicant.ssn_token VARCHAR(255)`. No data migration required.

### D-005: BoltTokenizationAdapter uses Resilience4j circuit breaker

BOLT is an external dependency on the critical path of application creation. Circuit breaker and retry are applied. If BOLT is unavailable, application creation fails fast with a `TokenizationUnavailableException` rather than proceeding with unprotected data.

### D-006: BOLT configuration

```yaml
integration:
  bolt:
    base-url: ${BOLT_URL}
    api-key: ${BOLT_API_KEY}
    timeout-seconds: 2

resilience4j:
  circuitbreaker:
    instances:
      bolt:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
  retry:
    instances:
      bolt:
        max-attempts: 2
        wait-duration: 300ms
```

`BOLT_API_KEY` is injected from secrets management — never hardcoded or logged.
