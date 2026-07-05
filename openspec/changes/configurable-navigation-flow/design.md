## Context

`application-management-ui` is the micro-frontend shell for the applicant journey. Today, `services/application-management-ui/src/pages/StatusPage.jsx` polls `GET /applications/{id}` every 8s and, inside a single `renderContent()` function, uses a long if/else chain keyed on `application.applicationStatus` to decide what to render: a spinner, an embedded web component (`<pricing-offer-selector>`, `<document-upload-manager>`), an internal MFE (`OfferAcceptanceMfe`), or a static confirmation/decline block. Three ad-hoc `Set`s (`PROCESSING_STATES`, `UNDER_REVIEW_STATES`, `POST_ACCEPTANCE_STATES`) group states for shared treatment. Status strings used here have drifted from the canonical names in `docs/architecture/002-application-state-machine.md` §9.

This is purely a shell-side rendering decision — `application-management-service` remains the sole owner of state and transitions (per CLAUDE.md); nothing here changes the state machine, its API, or its events.

## Goals / Non-Goals

**Goals:**
- Replace the if/else chain with a single declarative config (data, not code) mapping `applicationStatus` → a render descriptor.
- Support the four render descriptor kinds already in use: `spinner`, `web-component`, `internal-mfe`, `static-block`.
- Make it possible to add/reorder/retarget a state's screen by editing config, without touching `StatusPage.jsx` logic.
- Reconcile config state keys with the canonical state machine names.
- Fail loudly (not silently fall through) when a status has no config entry, so gaps are caught in dev/test rather than showing a blank screen in production.

**Non-Goals:**
- No new backend service, admin UI, or runtime-editable config store — the config is a static asset versioned with `application-management-ui` and deployed with it (still requires a UI redeploy to change, but no longer requires touching component logic/tests).
- No change to `pricing-offers-ui`'s internal step machine (`OfferFlow.tsx`) or `document-management-ui` — only the shell's outer routing decision is configurable.
- No change to polling mechanism, session-token handling, or the `offer-confirmed` custom event contract.
- Does not attempt to model transition *guards* beyond a single status key (e.g. no cross-field conditional routing) — out of scope until a concrete need arises.

## Decisions

**1. Config shape: flat map of status → descriptor, not a full state-transition graph.**
The UI does not *drive* transitions — the backend advances state and the UI just reflects whatever `applicationStatus` polling returns. So the config only needs to answer "given this status, what do I render?", not "from A can I go to B?". A flat `Record<ApplicationStatus, ScreenDescriptor>` is sufficient and far simpler than a graph/FSM. Alternative considered: encoding a full graph with edges and guards — rejected as speculative complexity with no current consumer (the backend, not the UI, enforces valid transitions per `002-application-state-machine.md`).

**2. Descriptor kinds as a tagged union.**
```
type ScreenDescriptor =
  | { kind: 'spinner'; label: string; group: 'processing' | 'under-review' }
  | { kind: 'web-component'; tag: string; scriptUrlKey: string; attributes: string[] }
  | { kind: 'internal-mfe'; component: 'OfferAcceptanceMfe' | 'ConfirmationMfe' | 'DenialMfe' }
  | { kind: 'static-block'; block: 'declined' | 'cancelled-expired' | 'processing-fallback'; labelMap?: Record<string,string> }
```
`scriptUrlKey` references a key already present in `services/application-management-ui/src/api/config.js` (e.g. `pricingOffersUiJs`); `attributes` lists which standard attrs (`application-id`, `api-base-url`, `session-token`) to set on the custom element. This keeps config declarative while the resolver + `useWebComponent` hook retain the actual DOM/script-loading mechanics.

**3. Config lives as a plain JS/TS module, not JSON.**
`services/application-management-ui/src/navigation/navigationConfig.js` exports the map as a typed JS object rather than a `.json` file. Reasoning: it needs to reference the `ApplicationStatus` constants already used in code (for compile-time typo safety) and the descriptor kinds benefit from lightweight JSDoc/TS typing; a `.json` file would lose both. Alternative considered: JSON config loaded at runtime — rejected because it would need a schema validator at load time for no real benefit, since the file is bundled at build time anyway (Non-Goal: no runtime-editable config).

**4. Resolver module encapsulates lookup + fail-fast behavior.**
New `services/application-management-ui/src/navigation/resolveScreen.js` exports `resolveScreen(applicationStatus)` returning the matching `ScreenDescriptor` or throwing a descriptive error if the status is unmapped. `StatusPage.jsx`'s `renderContent()` is rewritten to call this resolver once and switch on `descriptor.kind` to pick a small render function per kind — replacing ~150 lines of nested if/else with ~4 render branches plus the config data.

**5. Status key reconciliation.**
The config's keys will use the canonical names from `docs/architecture/002-application-state-machine.md` §9. Where the backend currently emits a different string (e.g. `OFFER_PENDING` if that's not the canonical name), this change surfaces the mismatch as an open question (see below) rather than silently aliasing it, since resolving it may require a backend-side check with `application-management-service` ownership.

## Risks / Trade-offs

- [Risk] Unmapped status value reaches the UI (new state added to backend, config not updated) → previously fell through to a generic "Processing" default silently; now throws. **Mitigation**: wrap the resolver call in an error boundary within `StatusPage.jsx` that renders a safe generic fallback screen and logs the error, so a config gap degrades gracefully in production while still being loud in tests/dev (unit test asserts every `ApplicationStatus` enum value has a config entry).
- [Risk] Reconciling status strings could reveal real backend/UI drift requiring a coordinated fix outside this UI-only change. → **Mitigation**: treat reconciliation as a documentation/verification task in this change; if actual string mismatches are found, file a follow-up change rather than expanding this one's scope into backend territory.
- [Trade-off] Config is still redeploy-required (static asset), not dynamically editable. Accepted per Non-Goals — introducing a runtime config service is a larger architectural change not justified by current need.

## Migration Plan

1. Add `navigationConfig.js` + `resolveScreen.js` alongside existing `StatusPage.jsx` (additive, no behavior change yet).
2. Add unit tests asserting the config covers all canonical states and that `resolveScreen` output matches current `renderContent()` behavior for each state (characterization tests against current behavior before refactor).
3. Refactor `StatusPage.jsx` to use `resolveScreen`, removing the if/else chain and the three ad-hoc `Set`s.
4. Run existing StatusPage tests plus new ones; verify manually against key states (OFFER_PENDING, DOCUMENTS_REQUIRED, APPROVED, DECLINED, terminal states) using `/verify` or a local run.
5. Rollback strategy: since this is a pure frontend refactor behind no feature flag, rollback is a standard revert of the `application-management-ui` deployment/commit — no data migration involved.

## Open Questions

- Do any current `applicationStatus` values used in `StatusPage.jsx` (e.g. `OFFER_PENDING`, `SOFT_PULL_PENDING`, `PRICING_PENDING`, `HARD_PULL_PENDING`, `DECISION_PENDING`) actually match what `application-management-service` emits, or were they invented/approximate? This needs a quick check against the service's status enum before finalizing config keys.
- Should the config also cover `REFERRED`, which appears in the canonical state machine but wasn't mentioned in the current `StatusPage.jsx` branching? Needs confirmation of intended screen treatment.

## Future Extensibility

Additional intermediate statuses are expected to be introduced incrementally as underwriting sub-stages become independently observable — e.g. document validation, employment validation, income validation, and fraud validation stages. These are anticipated as new entries in `PROCESSING_STATES`/`UNDER_REVIEW_STATES`-equivalent groupings, each mapped to a `spinner` (or later, a more specific) descriptor. The flat config + fail-fast resolver design (Decision 1 and 4) accommodates this by construction: adding a new status requires only a new config entry plus a corresponding addition to the canonical state machine doc and the backend's emitted enum — no changes to `resolveScreen` or `StatusPage.jsx` rendering logic are needed. Each new status added this way must still satisfy Requirement "Every canonical application state has a config entry" (see `specs/navigation-flow/spec.md`), so the unit test enforcing full coverage will need updating alongside each addition — this is expected maintenance, not a design gap.
