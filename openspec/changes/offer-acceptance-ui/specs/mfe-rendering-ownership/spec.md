## MODIFIED Requirements

### Requirement: MFE Shell Rendering Boundary
The `application-management-ui` micro-frontend shell SHALL NOT render functional loading, empty, error, or transition states that belong to an embedded functional web-component MFE (`<pricing-offer-selector>`, `<document-upload-manager>`, `<offer-acceptance-flow>`). The shell's rendering responsibility for these components is limited to: resolving which screen to show for the current `applicationStatus` (via `resolveScreen`), loading the component's remote script bundle, and mounting/unmounting the custom element with its documented attributes. No functional capability SHALL be rendered as an internal component within `application-management-ui`'s own bundle — every functional capability owns a standalone MFE.

#### Scenario: Shell mounts a functional web component without rendering its internal state
- **WHEN** `StatusPage` resolves a `web-component` screen descriptor for the current `applicationStatus` and the component's script bundle has loaded
- **THEN** the shell renders only the bare custom element with its documented attributes (`application-id`, `api-base-url`, `session-token`, and any component-specific attributes such as `pricing-api-base-url`) and renders no loading spinner, confirmation message, or other functional copy specific to that component's internal state

#### Scenario: Shell shows a generic fallback only while the script bundle itself is loading
- **WHEN** the remote script bundle for a `web-component` screen descriptor has not yet loaded
- **THEN** the shell MAY render a generic, component-agnostic loading indicator, since no custom element instance exists yet to delegate rendering to

#### Scenario: Functional MFE owns its full internal presentation
- **WHEN** `<pricing-offer-selector>`, `<document-upload-manager>`, or `<offer-acceptance-flow>` is mounted
- **THEN** that component SHALL render its own loading, ready, confirmed, and error states internally, without requiring the shell to gate its visibility or supply presentational props beyond its documented attributes

#### Scenario: No functional capability remains an internal shell component
- **WHEN** the `APPROVED` status is resolved
- **THEN** the shell SHALL mount `<offer-acceptance-flow>` as a `web-component` descriptor, not render an `internal-mfe` component, and the `internal-mfe` screen kind SHALL have no remaining consumers in `NAVIGATION_CONFIG`
