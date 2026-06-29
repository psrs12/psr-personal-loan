## MODIFIED Requirements

### Requirement: Application Lifecycle States
The system SHALL define and enforce the complete set of application lifecycle states and their valid transitions. Two new states are added: `DOCUMENTS_REQUIRED` and `OFFER_ACCEPTED`.

#### Scenario: Application transitions to DOCUMENTS_REQUIRED
- **WHEN** a `FinalDecisionDocumentsRequired` event is received
- **THEN** the application state SHALL transition from `DECISION_PENDING` to `DOCUMENTS_REQUIRED`
- **THEN** this transition SHALL be recorded in the application audit trail with timestamp and correlation ID

#### Scenario: Application transitions to OFFER_ACCEPTED
- **WHEN** an `ESignCompleted` event is received
- **THEN** the application state SHALL transition from `APPROVED` to `OFFER_ACCEPTED`
- **THEN** this transition SHALL be recorded in the application audit trail

#### Scenario: Invalid transition from DOCUMENTS_REQUIRED is rejected
- **WHEN** any state transition other than `UNDERWRITING` is requested for an application in `DOCUMENTS_REQUIRED`
- **THEN** the system SHALL reject the transition
- **THEN** the system SHALL return an `InvalidStateTransitionException`

#### Scenario: Invalid transition from OFFER_ACCEPTED is rejected
- **WHEN** any state transition other than `FUNDING_PENDING` is requested for an application in `OFFER_ACCEPTED`
- **THEN** the system SHALL reject the transition
- **THEN** the system SHALL return an `InvalidStateTransitionException`

---

## ADDED Requirements

### Requirement: State Transition Rules for Post-Decision States
The system SHALL enforce the following valid state transitions for the two new states.

#### Scenario: DOCUMENTS_REQUIRED to UNDERWRITING on documents completion
- **WHEN** a `DocumentsCompleted` event is received for an application in `DOCUMENTS_REQUIRED`
- **THEN** the application state SHALL transition to `UNDERWRITING`

#### Scenario: OFFER_ACCEPTED to FUNDING_PENDING on funding initiation
- **WHEN** a funding request is initiated for an application in `OFFER_ACCEPTED`
- **THEN** the application state SHALL transition to `FUNDING_PENDING`

#### Scenario: New states are included in state change audit
- **WHEN** an application transitions into or out of `DOCUMENTS_REQUIRED` or `OFFER_ACCEPTED`
- **THEN** the application audit trail SHALL record: previous state, new state, timestamp, actor, correlation ID
- **THEN** the `APPLICATION_STATE_CHANGED` audit event SHALL be written to the `application_audit` table
