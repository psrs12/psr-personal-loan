## Context

`application-management-ui` currently renders three functional experiences directly or indirectly: offer selection (`<pricing-offer-selector>` web component, external service), document upload (`<document-upload-manager>` web component, external service), and offer acceptance/e-sign (`OfferAcceptanceMfe.jsx`, an internal React component bundled into the shell itself). The `mfe-rendering-ownership` change already established that a functional MFE owns its own loading/ready/transition rendering and fires a completion event; the shell only mounts it and orchestrates navigation. Offer acceptance is the one capability that doesn't follow this pattern, and it's also the one backend service (`offer-acceptance-service`, port 8085) with no dedicated frontend.

`offer-acceptance-service`'s REST contract (`openspec/offer-acceptance/spec.md`) already exposes everything the existing `OfferAcceptanceMfe.jsx` uses: `GET /applications/{id}/declarations` and `POST /applications/{id}/esign`. The component additionally reads the confirmed offer (amount, term, APR, monthly payment) via `getSelectedOffer(applicationId)`, which calls `pricing-orchestration-service`, not `offer-acceptance-service` — this cross-service read must be preserved, not routed through compliance-orchestration-service (which owns only the async TILA audit, not pre-esign disclosure content).

## Goals / Non-Goals

**Goals:**
- Extract offer acceptance (declarations + e-sign) into a standalone, independently deployable web-component MFE, `offer-acceptance-ui`, structurally identical to `pricing-offers-ui`/`document-management-ui`.
- Preserve all existing behavior: declarations retry-on-404 polling (the session may not exist yet immediately after `APPROVED`), mandatory-declaration gating, submitted confirmation state, offer summary display.
- Fire a `offer-accepted` completion event so the shell can react (immediate refetch) without rendering any acceptance-specific content itself.
- Change `application-management-ui`'s `APPROVED` navigation descriptor from `internal-mfe` to `web-component`, using the exact same `WebComponentScreen` container already used for the other two MFEs — no new screen kind needed.

**Non-Goals:**
- No changes to `offer-acceptance-service`'s backend API or domain model — its contract already supports this UI.
- No changes to `compliance-orchestration-service` or its gates; this MFE never calls it.
- No change to how/when the Decision Engine approves online vs. offline — irrelevant to this MFE, which only ever mounts once the application reaches `APPROVED` regardless of path taken.
- No retirement of the `internal-mfe` screen kind/registry infrastructure itself — it may still be useful for a future genuinely-internal screen; only the `OfferAcceptanceMfe` entry is removed.

## Decisions

**1. Reuse `WebComponentScreen` unchanged.** The generic container built in `mfe-rendering-ownership` (mount by `descriptor.tag`, attributes from a shared value map, `useCustomEvent` wired to `descriptor.completionEvent`) already supports this without modification — this is exactly the abstraction it was built for. Alternative considered: a bespoke container for offer-acceptance — rejected, would reintroduce per-MFE branching the prior change eliminated.

**2. New descriptor for `APPROVED`:**
```js
APPROVED: {
  kind: 'web-component',
  tag: 'offer-acceptance-flow',
  scriptUrlKey: 'offerAcceptanceUiJs',
  apiBaseUrlKey: 'offerAcceptance',
  attributes: ['application-id', 'api-base-url', 'session-token'],
  containerWidth: 760,
  completionEvent: 'offer-accepted',
},
```
`apiBaseUrlKey: 'offerAcceptance'` maps to the already-existing `API.offerAcceptance` entry in `api/config.js` (added for a prior change, currently unused by any component — this is its first consumer).

**3. Offer summary read stays a second, internal fetch inside the new MFE**, mirroring how `pricing-offers-ui` and `document-management-ui` each independently call `application-management-service`/their own backend rather than receiving all data as props. The new web component will need its own `api-base-url` for `pricing` reads. Two options:
   - (a) Pass a second attribute (`pricing-api-base-url`) so the component can call `pricing-orchestration-service` directly for the confirmed offer.
   - (b) Have `offer-acceptance-service` expose the confirmed-offer summary itself (proxy/cache), so the MFE only ever talks to one backend.

   **Decision: (a)** — add `pricing-api-base-url` as an extra attribute on this descriptor only (`WebComponentScreen` already builds attributes generically from `descriptor.attributes`, so this requires no shell code change, only a config + attribute-value-map entry for `pricing-api-base-url` → `API.pricing`). Rejected (b) because it would require `offer-acceptance-service` to take on pricing-data ownership it doesn't have today, violating its documented scope ("Offer generation and pricing" is explicitly Out of Scope in `openspec/offer-acceptance/spec.md`).

**4. Completion event name: `offer-accepted`** (not reusing `offer-confirmed`, which is `pricing-offers-ui`'s event for a different transition). Shell reaction: same pattern as `handleOfferConfirmed` — trigger an immediate `fetchStatus()` after a short delay, since `ESignCompleted` drives `APPROVED → OFFER_ACCEPTED` asynchronously via Kafka and the UI should poll promptly rather than wait a full 8s cycle.

**5. Retire `OfferAcceptanceMfe.jsx`, `internalMfeRegistry.js`'s only entry, `screens/OfferAcceptanceScreen.jsx`, and `screens/InternalMfeScreen.jsx`.** Since `OfferAcceptanceMfe` was the only `internal-mfe` descriptor in `NAVIGATION_CONFIG`, removing it empties `INTERNAL_MFE_REGISTRY` and makes `InternalMfeScreen`/`internal-mfe` dead code. Delete them rather than leave an empty registry — nothing else uses the `internal-mfe` kind today, and the completeness test (`screenRegistry.test.js`) already asserts registry coverage against `NAVIGATION_CONFIG`, so a dangling unused kind would only pass by accident. If a future genuinely-internal screen is needed, `internal-mfe`/`InternalMfeScreen` can be reintroduced then with a real consumer.

## Risks / Trade-offs

- **[Risk] The declarations-session-not-yet-created retry loop (404 → retry up to 5x/2s) is UX-critical and easy to lose in the port.** → Mitigation: task list requires the new MFE's hook to be a near-verbatim port of `OfferAcceptanceMfe.jsx`'s `loadDeclarations` retry logic, and a manual verification task exercises the `APPROVED` transition end-to-end.
- **[Risk] Two API base URLs on one web component (`api-base-url` for offer-acceptance, `pricing-api-base-url` for the offer summary) is a slightly awkward attribute contract** compared to the single-base-URL pattern the other two MFEs use. → Accepted trade-off: the alternative (proxying through offer-acceptance-service) expands that service's scope in a way CLAUDE.md's ownership rules discourage.
- **[Risk] No test framework in `offer-acceptance-ui` at inception**, matching the same gap already logged as an open follow-up for `pricing-offers-ui`/`document-management-ui`. → Not blocking; tracked as the same deferred follow-up, not repeated per-service.

## Migration Plan

1. Scaffold `services/offer-acceptance-ui/` (package.json, vite config, Dockerfile, nginx.conf, `.env.example`) mirroring `pricing-offers-ui`.
2. Port declarations/e-sign/offer-summary logic from `OfferAcceptanceMfe.jsx` into new internal components + a web component wrapper (`offer-acceptance-flow`).
3. Add `offerAcceptanceUiJs` and `pricing-api-base-url`-supporting config to `application-management-ui`'s `api/config.js` and `navigationConfig.js`; update `WebComponentScreen`'s attribute-value map with the new attribute.
4. Delete `OfferAcceptanceMfe.jsx`, `internalMfeRegistry.js`, `screens/OfferAcceptanceScreen.jsx`, `screens/InternalMfeScreen.jsx`; remove `internal-mfe` from `SCREEN_REGISTRY` and the descriptor typedef.
5. Update `docs/architecture/002-application-state-machine.md` §9 table's `APPROVED` row.
6. Deploy: add `offer-acceptance-ui` container to `deploy/docker-compose*.yml` and build/push scripts, alongside the existing two frontend MFE containers.
7. Rollback: since this is additive-then-subtractive, rollback is reverting the `navigationConfig.js` descriptor change (back to `internal-mfe`) and un-deleting the old component from git history if the new MFE has issues in production — no data migration involved.

## Open Questions

- Should `offer-acceptance-ui` (and the two existing sibling MFEs) get a shared test-setup follow-up change now that three services share the same gap? Deferred — same as the existing open follow-up in `mfe-rendering-ownership/tasks.md`.
