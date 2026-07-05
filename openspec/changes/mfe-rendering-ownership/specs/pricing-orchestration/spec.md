## MODIFIED Requirements

### Requirement: UI Component
The `<pricing-offer-selector>` web component (served by `pricing-offers-ui`) handles:

- Display of retrieved offers, including its own loading state while offers are being fetched.
- Offer selection by applicant.
- Hard pull consent capture.
- Rendering its own post-confirmation state (e.g., "running a full credit check") after the applicant confirms an offer, without relying on the embedding shell to render or gate that state.
- Firing the `offer-confirmed` custom event consumed by the micro-frontend shell, used by the shell solely to resume application-status polling — not to control the component's own rendering.

#### Scenario: Component renders its own offer-loading state
- **WHEN** `<pricing-offer-selector>` is mounted and is fetching offers from `pricing-orchestration-service`
- **THEN** the component renders its own loading indicator without the embedding shell rendering an offer-specific loading message

#### Scenario: Component renders its own post-confirmation state
- **WHEN** the applicant selects an offer and confirms hard-pull consent
- **THEN** the component transitions to its own "offer confirmed" internal state and fires `offer-confirmed`, and remains mounted and visible in that state rather than being hidden by the embedding shell
