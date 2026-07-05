## ADDED Requirements

### Requirement: Standalone Offer Acceptance Web Component
The platform SHALL provide a standalone, independently deployable web-component MFE (`offer-acceptance-ui`, custom element `<offer-acceptance-flow>`) that owns declarations review and e-signature capture, sourced from `offer-acceptance-service`.

#### Scenario: Component is embedded by the shell
- **WHEN** `application-management-ui`'s `StatusPage` resolves the `APPROVED` status
- **THEN** it mounts `<offer-acceptance-flow>` with `application-id`, `api-base-url` (pointing at `offer-acceptance-service`), `pricing-api-base-url` (pointing at `pricing-orchestration-service`), and `session-token` attributes, and renders no acceptance-specific content itself

#### Scenario: Component owns its full internal presentation
- **WHEN** `<offer-acceptance-flow>` is mounted
- **THEN** it SHALL render its own loading, declarations-list, submission, and submitted-confirmation states internally, matching the behavior previously provided by `OfferAcceptanceMfe.jsx`

### Requirement: Declarations Session Retry
The component SHALL tolerate the `OfferAcceptanceSession` not yet existing immediately after an `APPROVED` decision, since session creation is driven asynchronously by a `FinalDecisionApproved` Kafka event.

#### Scenario: Declarations not yet available
- **WHEN** `GET /applications/{id}/declarations` returns 404
- **THEN** the component SHALL retry up to 5 times at a 2-second interval before surfacing an error to the applicant

### Requirement: Mandatory Declaration Gating
The component SHALL only allow e-sign submission once every mandatory declaration has been explicitly accepted.

#### Scenario: Submit disabled until all mandatory declarations are checked
- **WHEN** one or more mandatory declarations are unchecked
- **THEN** the "Accept and Sign" action SHALL be disabled

#### Scenario: Submit enabled once all mandatory declarations are checked
- **WHEN** every mandatory declaration is checked
- **THEN** the "Accept and Sign" action SHALL be enabled and, on submission, SHALL call `POST /applications/{id}/esign` with the accepted declaration IDs

### Requirement: Offer Summary Display
The component SHALL display the applicant's confirmed offer terms (amount, term, APR, monthly payment) alongside the declarations, read from `pricing-orchestration-service`.

#### Scenario: Confirmed offer is shown above the declarations list
- **WHEN** the component loads
- **THEN** it SHALL fetch and display the confirmed offer's amount, term, APR, and monthly payment, tolerating any of these fields being absent

### Requirement: Completion Event
The component SHALL fire a custom DOM event on successful e-sign submission so the shell can resume status polling without rendering acceptance-specific state itself.

#### Scenario: E-sign submission completes
- **WHEN** `POST /applications/{id}/esign` succeeds
- **THEN** the component SHALL render its own "submitted" confirmation state internally and fire the `offer-accepted` custom event
