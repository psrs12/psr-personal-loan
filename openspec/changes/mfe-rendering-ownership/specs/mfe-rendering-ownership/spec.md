## ADDED Requirements

### Requirement: MFE Shell Rendering Boundary
The `application-management-ui` micro-frontend shell SHALL NOT render functional loading, empty, error, or transition states that belong to an embedded functional web-component MFE (`<pricing-offer-selector>`, `<document-upload-manager>`). The shell's rendering responsibility for these components is limited to: resolving which screen to show for the current `applicationStatus` (via `resolveScreen`), loading the component's remote script bundle, and mounting/unmounting the custom element with its documented attributes.

#### Scenario: Shell mounts a functional web component without rendering its internal state
- **WHEN** `StatusPage` resolves a `web-component` screen descriptor for the current `applicationStatus` and the component's script bundle has loaded
- **THEN** the shell renders only the bare custom element with its documented attributes (`application-id`, `api-base-url`, `session-token`) and renders no loading spinner, confirmation message, or other functional copy specific to that component's internal state

#### Scenario: Shell shows a generic fallback only while the script bundle itself is loading
- **WHEN** the remote script bundle for a `web-component` screen descriptor has not yet loaded
- **THEN** the shell MAY render a generic, component-agnostic loading indicator, since no custom element instance exists yet to delegate rendering to

#### Scenario: Functional MFE owns its full internal presentation
- **WHEN** `<pricing-offer-selector>` or `<document-upload-manager>` is mounted
- **THEN** that component SHALL render its own loading, ready, confirmed, and error states internally, without requiring the shell to gate its visibility or supply presentational props beyond its documented attributes

### Requirement: Shell-to-MFE Lifecycle Signaling
Functional web-component MFEs SHALL communicate lifecycle transitions the shell needs for orchestration (e.g., resuming status polling) via custom DOM events, not via shell-rendered presentational state.

#### Scenario: Offer confirmation signals the shell via event, not shell-rendered state
- **WHEN** the applicant completes offer selection and hard-pull consent inside `<pricing-offer-selector>`
- **THEN** the component SHALL render its own "offer confirmed" state internally and fire the `offer-confirmed` custom event so the shell can resume polling `GET /applications/{id}`, without the shell needing its own render-gating state for that transition
