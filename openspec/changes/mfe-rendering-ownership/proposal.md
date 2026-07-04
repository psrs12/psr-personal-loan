## Why

`application-management-ui`'s `StatusPage.jsx` currently renders functional loading/transition states that belong to the embedded web components — "Loading your offers...", the post-confirmation "running a full credit check" message, and "Loading document portal..." — inside `renderWebComponent()`. This violates the intended MFE ownership boundary: the shell should only resolve which screen to show and mount/embed the right MFE for the current `applicationStatus`; each functional MFE (`pricing-offers-ui`, `document-management-ui`) should own all of its own rendering, including its loading, empty, and transition states. The inconsistency is visible today: the `internal-mfe` path (`OfferAcceptanceMfe`) already owns its full presentation, while the `web-component` path does not.

## What Changes

- Define a minimal lifecycle contract (attributes/events) that `<pricing-offer-selector>` and `<document-upload-manager>` expose so they can render their own loading and ready states internally, without the shell inferring or rendering those states on their behalf.
- Move the "Loading your offers..." and "Offer Confirmed / running a full credit check" presentational states out of `StatusPage.jsx` and into `pricing-offers-ui`'s `<pricing-offer-selector>`.
- Move the "Loading document portal..." presentational state out of `StatusPage.jsx` and into `document-management-ui`'s `<document-upload-manager>`.
- Simplify `StatusPage.jsx`'s `renderWebComponent()` to only mount the custom element with its required attributes (`application-id`, `api-base-url`, `session-token`) plus a script-load failure fallback (script loading itself is shell infrastructure, not functional MFE behavior).
- Preserve the existing `offer-confirmed` custom event contract used by the shell to resume polling after offer confirmation — no change to that event.
- **BREAKING**: `<pricing-offer-selector>` and `<document-upload-manager>` custom elements change their internal rendering; any code relying on the shell to gate visibility of these components during their loading phase must be updated (affects only `application-management-ui`, the sole consumer).

## Capabilities

### New Capabilities
- `mfe-rendering-ownership`: Cross-cutting rule and lifecycle contract establishing that functional rendering (loading/empty/ready/transition states) for embedded web-component MFEs is owned by the MFE itself, not by the `application-management-ui` shell/orchestrator.

### Modified Capabilities
- `pricing-orchestration`: UI Component section (§8) — `<pricing-offer-selector>` gains ownership of its own loading state and post-selection confirmation messaging, previously rendered by the shell.
- `document-collection`: UI Component section (§12) — `<document-upload-manager>` gains ownership of its own loading state, previously rendered by the shell.

## Impact

- `services/application-management-ui/src/pages/StatusPage.jsx` — simplify `renderWebComponent()`.
- `services/pricing-offers-ui/src/web-components/pricing-offer-selector.tsx` — add internal loading/ready rendering.
- `services/document-management-ui/src/web-components/document-upload-manager.tsx` — add internal loading rendering.
- No backend service or API contract changes; no database, Kafka, or state-machine impact.
