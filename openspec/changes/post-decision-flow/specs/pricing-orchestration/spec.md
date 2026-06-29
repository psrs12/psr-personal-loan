## MODIFIED Requirements

### Requirement: Final Decision Routing
The system SHALL submit a final decision request to the Decision Platform after hard pull completion and route the application based on the decision outcome. Four outcomes are supported: APPROVED, DECLINED, REFERRED, and DOCUMENTS_REQUIRED.

#### Scenario: Final decision request submitted
- **WHEN** a `HardPullCompleted` event is received
- **THEN** the system SHALL submit a final decision request to the Decision Platform including `application_id`, `selected_offer_id`, and `hard_pull_credit_report_reference_id`

#### Scenario: Decision Platform returns approved
- **WHEN** the Decision Platform returns an approved decision
- **THEN** the application state SHALL transition to `APPROVED`
- **THEN** the selected offer SHALL be confirmed
- **THEN** a `FinalDecisionApproved` event SHALL be published

#### Scenario: Decision Platform returns declined
- **WHEN** the Decision Platform returns a declined decision
- **THEN** the application state SHALL transition to `DECLINED`
- **THEN** a `FinalDecisionDeclined` event SHALL be published with the reason code
- **THEN** the adverse action notification workflow SHALL be triggered

#### Scenario: Decision Platform returns referred
- **WHEN** the Decision Platform returns a referred decision with no document list
- **THEN** the application state SHALL transition to `REFERRED`
- **THEN** the applicant's selected offer SHALL be retained without modification
- **THEN** no new hard pull SHALL be initiated during manual review
- **THEN** a `FinalDecisionReferred` event SHALL be published

#### Scenario: Decision Platform returns documents required
- **WHEN** the Decision Platform returns a `DOCUMENTS_REQUIRED` outcome containing a list of document type codes
- **THEN** the application state SHALL transition to `DOCUMENTS_REQUIRED`
- **THEN** a `FinalDecisionDocumentsRequired` event SHALL be published containing the `applicationId` and the list of Decision Engine document type codes as received
- **THEN** the selected offer SHALL be retained without modification
- **THEN** the system SHALL NOT map or interpret the document type codes — it SHALL forward them verbatim in the event payload

#### Scenario: FinalDecisionDocumentsRequired event structure
- **WHEN** a `FinalDecisionDocumentsRequired` event is published
- **THEN** the event payload SHALL contain: `applicationId`, `correlationId`, `timestamp`, `documents` array where each entry has `decisionEngineCode` (string) and `count` (integer)
