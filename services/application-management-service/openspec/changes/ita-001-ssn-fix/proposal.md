## Why

The original `ita-001-invitation-processing` implementation encrypted SSN using AES/GCM within the service boundary. The correct approach for a banking platform is external tokenization via BOLT — the same tokenization used by the SSN verification service — so that raw SSNs never enter the service and comparisons are possible without either side holding the raw value.

## What Changes

- **BREAKING** Replace `SSNEncryptionService` (AES/GCM) with `BoltTokenizationAdapter` (external BOLT API)
- **BREAKING** Replace `ssn_encrypted BYTEA` column with `ssn_token VARCHAR` in the `applicant` table
- Update `SSNVerificationAdapter` to tokenize SSN via BOLT before sending to the verification service
- Update `CreateApplicationUseCase` to store BOLT token instead of AES-encrypted bytes
- Update `Applicant` domain entity — `ssnEncrypted: byte[]` becomes `ssnToken: String`
- Remove `SSNEncryptionService` and its configuration (`security.ssn-encryption-key`)
- Add Flyway migration to alter `applicant.ssn_encrypted` → `applicant.ssn_token`
- Update all tests that reference the old encryption approach

## Capabilities

### New Capabilities
- `ssn-tokenization`: Tokenize raw SSN via BOLT API before any processing or storage. Raw SSN is dropped immediately after tokenization and never persisted.

### Modified Capabilities
- `application-creation`: SSN handling changes from encrypt-and-store to tokenize-via-BOLT-and-store. Behaviour is the same from the applicant's perspective but the internal mechanism and storage format change.

## Impact

- `applicant` table schema change (migration required)
- `Applicant` domain entity and JPA entity
- `CreateApplicationUseCase` — encryption step replaced by tokenization lookup
- `SSNVerificationAdapter` — sends BOLT token not raw SSN
- `SSNEncryptionService` — deleted
- `application.yml` — `security.ssn-encryption-key` removed, BOLT config added
- All unit and integration tests touching SSN handling
- No API contract changes — SSN is still submitted by the client the same way
