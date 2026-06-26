## ADDED Requirements

### Requirement: SSN tokenized via BOLT before processing
The system SHALL tokenize the raw SSN using the BOLT tokenization API immediately upon receipt. The raw SSN SHALL NOT be passed to any downstream component, persisted, or written to logs.

#### Scenario: Successful tokenization
- **WHEN** a raw SSN is received at the application creation boundary
- **THEN** the system calls BOLT API and receives a tokenized SSN
- **THEN** the raw SSN is dropped from memory
- **THEN** only the BOLT token is passed to downstream processing

#### Scenario: BOLT unavailable
- **WHEN** the BOLT API is unreachable or returns a 5xx error
- **THEN** the system SHALL throw `TokenizationUnavailableException`
- **THEN** application creation SHALL be aborted
- **THEN** no partial data SHALL be persisted

#### Scenario: SSN never appears in logs
- **WHEN** tokenization succeeds or fails
- **THEN** the raw SSN value SHALL NOT appear in any log output
- **THEN** the BOLT token MAY be logged for audit at DEBUG level only

### Requirement: SSN verification uses BOLT token
The system SHALL tokenize the SSN via BOLT before sending to the external SSN verification service. The verification service uses the same BOLT tokenization, enabling comparison without either party holding the raw value.

#### Scenario: SSN verification with token
- **WHEN** a prospect submits their SSN for verification
- **THEN** the system SHALL tokenize via BOLT first
- **THEN** the BOLT token SHALL be sent to the SSN verification service
- **THEN** the raw SSN SHALL NOT be sent over the network

### Requirement: BOLT token stored as applicant SSN reference
The system SHALL store the BOLT token in the `applicant.ssn_token` column. No raw or AES-encrypted SSN SHALL be stored.

#### Scenario: Token persisted on application creation
- **WHEN** an application is successfully created
- **THEN** `applicant.ssn_token` SHALL contain the BOLT token string
- **THEN** `applicant.ssn_encrypted` column SHALL NOT exist

## MODIFIED Requirements

### Requirement: Application creation — SSN handling
The system SHALL accept raw SSN from the applicant in the `CreateApplicationRequest`. The raw SSN SHALL be immediately tokenized via BOLT. The BOLT token SHALL be stored in the applicant record. The raw SSN SHALL be discarded after tokenization.

#### Scenario: ITA application creation with BOLT tokenization
- **WHEN** a prospect submits a valid application with a raw SSN and a valid SSN verification token
- **THEN** the system SHALL tokenize the SSN via BOLT
- **THEN** the BOLT token SHALL be stored in `applicant.ssn_token`
- **THEN** the application SHALL be created successfully

#### Scenario: DIRECT application creation with BOLT tokenization
- **WHEN** a prospect submits a DIRECT application with a raw SSN
- **THEN** the system SHALL tokenize the SSN via BOLT
- **THEN** the BOLT token SHALL be stored in `applicant.ssn_token`
- **THEN** the application SHALL be created successfully
