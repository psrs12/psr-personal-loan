## 1. Domain Layer

- [x] 1.1 Update `Applicant` entity — replace `ssnEncrypted: byte[]` with `ssnToken: String`
- [x] 1.2 Add `TokenizationUnavailableException` to domain exceptions
- [x] 1.3 Create `BoltTokenizationPort` interface: `tokenize(ssn: String): String`

## 2. Database Migration

- [x] 2.1 Create Flyway migration V9: alter `applicant` table — drop `ssn_encrypted BYTEA`, add `ssn_token VARCHAR(255) NOT NULL`

## 3. Application Layer

- [x] 3.1 Update `CreateApplicationUseCase` — remove `SSNEncryptionService` dependency, inject `BoltTokenizationPort`, call `tokenize(ssn)` before persisting applicant
- [x] 3.2 Update `VerifySSNUseCase` — inject `BoltTokenizationPort`, tokenize SSN via BOLT before calling `SSNVerificationPort`
- [x] 3.3 Delete `SSNEncryptionService`

## 4. Infrastructure Layer

- [x] 4.1 Create `BoltTokenizationAdapter` implementing `BoltTokenizationPort` with Resilience4j circuit breaker and retry
- [x] 4.2 Create BOLT request/response DTOs: `BoltTokenizeRequest`, `BoltTokenizeResponse`
- [x] 4.3 Add `boltRestClient` bean to `RestClientConfig` with 2-second timeout
- [x] 4.4 Add BOLT configuration to `application.yml`: `integration.bolt.base-url`, `integration.bolt.api-key`
- [x] 4.5 Add Resilience4j circuit breaker and retry config for `bolt` in `application.yml`
- [x] 4.6 Update `SSNVerificationAdapter` — accept `ssnToken` (already tokenized) instead of raw SSN

## 5. JPA Infrastructure

- [x] 5.1 Update `ApplicantJpaEntity` — replace `ssnEncrypted: ByteArray` with `ssnToken: String`
- [x] 5.2 Update `ApplicationJpaAdapter` — map `ssnToken` in `toEntity` and `toDomain`

## 6. Configuration Cleanup

- [x] 6.1 Remove `security.ssn-encryption-key` from `application.yml`
- [x] 6.2 Remove AES/GCM dependencies from `pom.xml` if any were added explicitly

## 7. Tests

- [x] 7.1 Unit test `BoltTokenizationAdapter` with WireMock — success, BOLT unavailable, circuit breaker
- [x] 7.2 Update `CreateApplicationUseCaseTest` — mock `BoltTokenizationPort` instead of `SSNEncryptionService`
- [x] 7.3 Update `ApplicationAcceptanceTest` — verify `ssn_token` stored (not encrypted bytes)
- [x] 7.4 Verify SSN never appears in test log output
