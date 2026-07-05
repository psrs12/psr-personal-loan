## Why

Screen-to-screen navigation in `application-management-ui` is currently hardcoded: `StatusPage.jsx` contains a large if/else chain keyed off `application.applicationStatus` that decides which micro-frontend or web component to render next (e.g. `OFFER_PENDING` → `<pricing-offer-selector>`, `DOCUMENTS_REQUIRED` → `<document-upload-manager>`, `APPROVED` → `OfferAcceptanceMfe`). Every new state, new screen, or reordering of the post-decision journey requires a code change and redeploy of the shell. This makes it slow and risky to evolve the flow (e.g. adding a new compliance-hold screen, or changing which screen a state routes to) and has already caused the rendered status strings to drift from the canonical names in `docs/architecture/002-application-state-machine.md`.

## What Changes

- Introduce a declarative **navigation configuration** that maps application states (and relevant sub-conditions) to the screen/component that should be rendered, replacing the if/else chain in `StatusPage.jsx`.
- Define the configuration schema: per-state entry specifying the screen type (embedded web component vs. internal MFE vs. static block), the web component tag/attributes or MFE identifier to render, and any grouping (e.g. which states share a "processing" spinner treatment).
- Add a navigation resolver module in `application-management-ui` that reads the config and returns the render decision for a given `applicationStatus`, replacing direct status-string branching in `StatusPage.jsx`.
- Reconcile status strings used in the config with the canonical state names in `docs/architecture/002-application-state-machine.md`.
- Config is shipped as a versioned static asset within `application-management-ui` (not a new backend service or runtime admin UI) — this is a frontend refactor, not a new orchestration capability.
- **BREAKING** (internal only): removes the existing hardcoded `renderContent()` branching in `StatusPage.jsx`; any direct consumers of that function (none known outside this file) must migrate to the resolver.

## Capabilities

### New Capabilities
- `navigation-flow`: Declarative configuration and resolution of application-state-to-screen navigation within `application-management-ui`, including schema definition, validation, and the runtime resolver used by `StatusPage.jsx`.

### Modified Capabilities
(none — no backend/API or cross-service requirement changes; `application-management-service` remains sole owner of application state and its transition rules are unchanged)

## Impact

- **Affected code**: `services/application-management-ui/src/pages/StatusPage.jsx` (remove if/else chain), new config module/file under `services/application-management-ui/src` (e.g. `navigation/navigationConfig.js` + resolver), `services/application-management-ui/src/hooks/useWebComponent.js` (may be driven by resolver output).
- **No changes** to `pricing-offers-ui` internal step machine (`OfferFlow.tsx`) or `document-management-ui` — those remain internally self-contained web components; only the shell's decision of *which* component to mount is being made configurable.
- **No backend changes**: `application-management-service` state machine, events, and APIs are unchanged.
- **Docs**: update `docs/architecture/002-application-state-machine.md` §9 (shell routing table) if the config format changes how that table should be read/maintained.
