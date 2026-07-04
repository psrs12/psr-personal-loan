## Context

`StatusPage.jsx` (`application-management-ui`) resolves a `ScreenDescriptor` per `applicationStatus` (see `openspec/changes/configurable-navigation-flow/`) and dispatches to one of four render kinds: `spinner`, `web-component`, `internal-mfe`, `static-block`. For `internal-mfe` (`OfferAcceptanceMfe`), the embedded React component owns 100% of its own presentation — the shell only mounts it. For `web-component` (`pricing-offer-selector`, `document-upload-manager`), `StatusPage.renderWebComponent()` currently also renders:

- A "Loading your offers…" spinner, gated on `!script.loaded` (the `<script>` tag for the remote bundle not yet loaded).
- An "Offer Confirmed — we're now running a full credit check" spinner, gated on local `offerConfirmed` state, shown *instead of* the mounted `<pricing-offer-selector>` element after the `offer-confirmed` event fires.
- A "Loading document portal…" spinner, also gated on `!script.loaded`.

Investigation shows `pricing-offer-selector`'s inner `OfferFlow` component already has its own `'loading'` step (fetching offers via `usePricingOffers`) with its own "Loading your personalised offers…" copy. So there are actually two distinct loading states layered on top of each other today: (1) shell-level "is the remote JS bundle loaded" and (2) component-level "is the offer data loaded" — and only the shell-level one is currently visible before the component mounts, then the component's own internal loading state takes over once mounted. The genuinely misplaced piece is the **post-confirmation "running a full credit check" state**, which is purely a `pricing-offer-selector` concern (it's about that component's own lifecycle after the applicant acts inside it) but currently lives in the shell as a local `offerConfirmed` boolean plus a `sessionStorage` flag (`offerConfirmed:${applicationId}`) that also drives polling.

`document-upload-manager` has no equivalent internal loading state today — `DocumentManager` renders directly without a loading skeleton, so `StatusPage`'s "Loading document portal…" during script load is the only loading feedback that exists.

## Goals / Non-Goals

**Goals:**
- `StatusPage.jsx` mounts `<pricing-offer-selector>` and `<document-upload-manager>` unconditionally once their script has loaded, passing only the documented attributes — it renders no functional loading/transition copy for either component.
- The "offer confirmed / running full credit check" state moves into `pricing-offer-selector` (`OfferFlow`), which already owns the state machine driving `currentStep`.
- `document-upload-manager` (`DocumentManager`) gains its own initial loading state so removing the shell's "Loading document portal…" text does not leave a blank flash.
- The shell keeps exactly one responsibility for these components: resolving *when* to show them (via `resolveScreen`) and mounting/unmounting the custom element with attributes. It does not know or care what the component is doing internally.
- The `offer-confirmed` custom event contract is unchanged — the shell still needs *some* signal to resume polling `GET /applications/{id}` after the applicant confirms an offer, since polling cadence is a shell/orchestration concern (state machine transition), not a component-rendering concern.

**Non-Goals:**
- No change to `resolveScreen`/`navigationConfig` (the configurable-navigation-flow change already correctly separates *which* screen to show).
- No change to REST/event contracts between UI and backend services.
- Not attempting to remove the shell's script-loading mechanism (`useWebComponentScript`) — loading the remote JS bundle is shell infrastructure (it doesn't know what's inside the bundle), distinct from the functional rendering the bundle produces once loaded. A brief "loading module…" fallback during that window is acceptable shell responsibility since the component doesn't exist in the DOM yet to render anything itself.

## Decisions

**1. Keep script-bundle loading feedback in the shell; move everything else into the component.**
Alternative considered: have the shell render nothing at all until the script loads (blank screen). Rejected — a bare loading indicator for "the module hasn't arrived yet" is unavoidably a shell concern, since no custom element instance exists yet to delegate to. The line is drawn at "is there a mounted element" — anything after that point is the component's job.

**2. Move the post-confirmation state into `OfferFlow`'s existing step state machine, not a new prop.**
`OfferFlow` already tracks `currentStep` (`'loading' | ... | onComplete`). Add a `'confirmed'` step entered when `onComplete` fires, rendering the "Offer Confirmed — running a full credit check" message internally, instead of immediately notifying the parent. The `offer-confirmed` custom event still fires (unchanged contract) so the shell can resume polling, but the shell no longer needs its own `offerConfirmed` render-gating state — `pricing-offer-selector` stays mounted and shows its own confirmed state instead of being hidden via `overlayHidden` styling.
Alternative considered: pass a `confirmed` booleen attribute from shell back into the element to control rendering. Rejected — this re-introduces the shell dictating the component's presentation instead of the component owning its own lifecycle end-to-end.

**3. Give `DocumentManager` a lightweight initial loading state.**
Add a `loading` boolean (data-fetch-based, matching the `usePricingOffers` pattern already used in `pricing-offers-ui`) to `useDocumentRequirements`, and render a "Loading document portal…" state inside `DocumentManager` while it's true. This preserves the same user-visible feedback that exists today, just owned by the correct component.

**4. `StatusPage.renderWebComponent()` becomes a thin mount.**
After changes, it: resolves the script per descriptor, shows a generic "loading module…" fallback while `!script.loaded`, and otherwise renders the bare custom element with its attributes. It no longer needs `offerConfirmed` state, the `offerConfirmedKey` sessionStorage read for *rendering* (it's still read for *polling* decisions in `shouldPoll`, which is a legitimate orchestration concern — polling cadence, not rendering).

## Risks / Trade-offs

- [Two components now diverge in how they signal "confirmed"/"complete" states to the shell] → Both already use the established custom-event pattern (`offer-confirmed`, `offer-error`); no new event types are introduced, keeping the shell's `useCustomEvent` listener code unchanged.
- [Removing shell-side `overlayHidden` hiding logic could cause a visible layout flash if `OfferFlow`'s new `'confirmed'` step isn't styled to match] → Mitigate by using the same `status-center` spinner classes/copy that the shell previously used, just relocated into `OfferFlow`.
- [`DocumentManager` previously had no loading state; adding one changes its default render output] → Low risk, additive only; covered by a new test asserting the loading state renders before requirements arrive.
- [This is a `**BREAKING**` change to two custom elements' internal rendering per the proposal] → Impact is contained because `application-management-ui` is the only consumer of both elements in this codebase.

## Migration Plan

1. Add `'confirmed'` step + rendering to `OfferFlow`; verify `offer-confirmed` event still fires at the same point in the flow.
2. Add loading state to `useDocumentRequirements` / `DocumentManager`.
3. Simplify `StatusPage.renderWebComponent()` to drop the offer-confirmation overlay and document-loading text, keeping only script-load fallback and bare mounting.
4. Run `pricing-offers-ui` and `document-management-ui` unit tests, then `application-management-ui` tests (`resolveScreen` tests are unaffected; no new StatusPage test harness exists — see Open Follow-up in `configurable-navigation-flow/tasks.md`).
5. No backend deploy or data migration involved; this is a UI-only change deployable independently per service.

## Open Questions

- Should `document-management-ui` and `pricing-offers-ui` gain a shared minimal "web component contract" doc (e.g., under `docs/design/`) describing required attributes and emitted events, so future MFEs follow this pattern without rediscovering it? Not required for this change but worth a follow-up.

---

## Addendum: Registry-Driven Screen Dispatch (folded in after initial implementation)

### Context

After the above was implemented, `StatusPage.jsx` still contained hardcoded dispatch logic one layer down from where `resolveScreen`/`navigationConfig` operate: a `switch` on `descriptor.kind` in `renderScreen`, an if/else on `descriptor.tag` in `renderWebComponent`, and an if/else chain on `descriptor.block` in `renderStaticBlock`. This defeats the purpose of the configurable-navigation-flow change — the shell should be pure orchestration (resolve status → screen, mount the right container), and every functional MFE/screen variant should own its own rendering, selected via data-driven lookup rather than branching.

### Goals / Non-Goals (addendum)

**Goals:**
- `StatusPage.jsx` performs zero conditional branching on descriptor shape. It fetches/polls application state, resolves a descriptor, looks up one container component in a registry keyed by `descriptor.kind`, and renders it.
- `WebComponentScreen` (the `kind: 'web-component'` container) mounts any custom element generically from descriptor data (`tag`, `scriptUrlKey`, `apiBaseUrlKey`, `attributes`, `containerWidth`, optional `completionEvent`) — no per-tag branching for `pricing-offer-selector` vs `document-upload-manager`.
- `internal-mfe` and `static-block` kinds use their own small sub-registries (component name → component, block name → component) so adding a new internal MFE or static block variant means adding a registry entry, not editing an if/else chain.
- Extensible for the forthcoming document/employment/income/fraud validation statuses mentioned earlier: new statuses need only a `navigationConfig.js` entry plus (if genuinely new UI) a registry entry — `StatusPage.jsx` itself never changes again for new statuses.

**Non-Goals:**
- Not changing the event/attribute contracts already established for `pricing-offer-selector` (`offer-confirmed`) or `document-upload-manager`.
- Not introducing a plugin/dynamic-import system — registries are static maps in source, matching the existing `navigationConfig.js` pattern.

### Decisions (addendum)

**5. Descriptor gains presentation-neutral data fields instead of StatusPage inferring them.**
Add `containerWidth` (replaces the tag-based `760`/`800` width branch) and `completionEvent` (replaces the tag-based decision to attach the `offer-confirmed` listener) to the relevant `navigationConfig.js` entries. `WebComponentScreen` reads these as plain data.

**6. `WebComponentScreen` owns its own custom-event ref/listener, driven by `descriptor.completionEvent` + an `eventHandlers` map from `StatusPage`.**
Alternative considered: keep `pricingRef` and `useCustomEvent` in `StatusPage` and pass the ref down. Rejected — that requires `StatusPage` to know which descriptor needs a ref, which is exactly the per-tag branching being removed. Instead `WebComponentScreen` creates its own local ref, and if `descriptor.completionEvent` is set, listens for it and invokes `ctx.eventHandlers[descriptor.completionEvent]` if present. `StatusPage` supplies `eventHandlers: { 'offer-confirmed': handleOfferConfirmed }` as plain data, same shape as everything else passed through `ctx`.

**7. `internal-mfe` and `static-block` kinds get their own registries, not their own if/else.**
`internalMfeRegistry.js`: `{ OfferAcceptanceMfe: OfferAcceptanceScreen }`. `staticBlockRegistry.js`: `{ declined: DeclinedScreen, 'cancelled-expired': CancelledExpiredScreen, 'post-acceptance': PostAcceptanceScreen }`. Each container is a small, focused component; `InternalMfeScreen`/`StaticBlockScreen` do a single map lookup (`registry[descriptor.component]` / `registry[descriptor.block]`), consistent with how `resolveScreen` itself works — a lookup, not a branch.

**8. Top-level dispatch: `screenRegistry.js` maps `descriptor.kind` → container component.**
`StatusPage.jsx` becomes: `const Screen = SCREEN_REGISTRY[descriptor.kind]; return <Screen descriptor={descriptor} ctx={ctx} />`. The `resolveError` (unmapped status) path remains a direct fallback render in `StatusPage`, since it's an exceptional path outside the descriptor-kind space, not itself a screen kind.

### Risks / Trade-offs (addendum)

- [More, smaller files under `src/navigation/screens/` increase navigation overhead for a small codebase] → Justified by making `StatusPage.jsx` genuinely stable as new statuses/MFEs are added (matches the user's stated plan to add document/employment/income/fraud validation statuses incrementally).
- [Registries are still static maps someone must remember to update] → Same cost as `navigationConfig.js` already accepted in the prior change; `resolveScreen.test.js`-style completeness tests can be extended to assert every `kind`/`component`/`block` used in `navigationConfig.js` has a registry entry.

### Migration Plan (addendum)

1. Extend `navigationConfig.js` descriptors with `containerWidth` and `completionEvent` where applicable.
2. Create `src/navigation/screens/{SpinnerScreen,WebComponentScreen,InternalMfeScreen,StaticBlockScreen}.jsx` and their sub-registries (`internalMfeRegistry.js`, `staticBlockRegistry.js`).
3. Create `src/navigation/screenRegistry.js` mapping `kind` → container.
4. Rewrite `StatusPage.jsx` to drop all render-dispatch functions in favor of the registry lookup; keep fetch/poll/event-handler orchestration logic as-is.
5. Run `application-management-ui` tests and build.
