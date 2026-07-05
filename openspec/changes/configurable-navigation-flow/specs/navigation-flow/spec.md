## ADDED Requirements

### Requirement: Declarative navigation configuration
`application-management-ui` SHALL maintain a single declarative navigation configuration mapping each `applicationStatus` value to a screen descriptor, and SHALL NOT use ad-hoc if/else branching on `applicationStatus` string values within `StatusPage.jsx` to decide what to render.

#### Scenario: Every canonical application state has a config entry
- **WHEN** the navigation configuration module is loaded
- **THEN** it SHALL contain exactly one screen descriptor entry for every state listed in `docs/architecture/002-application-state-machine.md`, including terminal states (`DECLINED`, `CANCELLED`, `EXPIRED`, `COMPLETED`)

#### Scenario: Adding a new screen requires only a config change
- **WHEN** a developer needs to change which screen renders for a given `applicationStatus`
- **THEN** the change SHALL be achievable by editing only the navigation configuration entry for that status, without modifying rendering logic in `StatusPage.jsx`

### Requirement: Screen descriptor kinds
The navigation configuration SHALL express each state's target screen as one of four descriptor kinds: `spinner`, `web-component`, `internal-mfe`, or `static-block`, each carrying the fields needed to render it (label/group for `spinner`; tag, script URL key, and attribute list for `web-component`; component identifier for `internal-mfe`; block identifier and optional label map for `static-block`).

#### Scenario: Web component descriptor renders with required attributes
- **WHEN** the resolver returns a `web-component` descriptor for the current `applicationStatus` (e.g. mapping to `<pricing-offer-selector>` or `<document-upload-manager>`)
- **THEN** the shell SHALL mount the named custom element tag, load its script via the referenced config URL key, and set the `application-id`, `api-base-url`, and `session-token` attributes as specified by the descriptor

#### Scenario: Internal MFE descriptor renders the named component
- **WHEN** the resolver returns an `internal-mfe` descriptor (e.g. for `APPROVED` → `OfferAcceptanceMfe`)
- **THEN** the shell SHALL render the corresponding internal React MFE component

### Requirement: Navigation resolver with fail-fast lookup
`application-management-ui` SHALL provide a resolver function that, given an `applicationStatus`, returns the matching screen descriptor from the navigation configuration, and SHALL treat an unmapped status as an explicit error condition rather than silently defaulting.

#### Scenario: Known status resolves to its configured descriptor
- **WHEN** `resolveScreen` is called with an `applicationStatus` present in the navigation configuration
- **THEN** it SHALL return the exact screen descriptor configured for that status

#### Scenario: Unmapped status raises a descriptive error
- **WHEN** `resolveScreen` is called with an `applicationStatus` not present in the navigation configuration
- **THEN** it SHALL throw an error identifying the unmapped status, rather than returning a default or undefined value

#### Scenario: Unmapped status degrades gracefully in the UI
- **WHEN** `StatusPage.jsx` calls the resolver and it throws for an unmapped status
- **THEN** the page SHALL catch the error, log it, and render a generic safe fallback screen instead of crashing or leaving a blank page

### Requirement: Status key alignment with canonical state machine
The navigation configuration SHALL key its entries using the canonical `applicationStatus` names defined in `docs/architecture/002-application-state-machine.md`, and any discrepancy discovered between those canonical names and the values actually emitted by `application-management-service` SHALL be documented rather than silently aliased.

#### Scenario: Config key matches canonical state name
- **WHEN** the navigation configuration is reviewed against `docs/architecture/002-application-state-machine.md`
- **THEN** every config key SHALL match a state name defined in that document

#### Scenario: Discovered naming discrepancy is documented, not silently patched
- **WHEN** a status string used by the current UI implementation does not match any canonical state name
- **THEN** the discrepancy SHALL be recorded (e.g. in the change's design notes or a follow-up item) rather than being resolved by inventing an undocumented alias in the config
