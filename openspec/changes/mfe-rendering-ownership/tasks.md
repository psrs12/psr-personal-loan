## 1. pricing-offers-ui: internal confirmation state

- [x] 1.1 Add a `'confirmed'` step to `OfferFlow`'s step state machine, entered when the applicant completes offer selection and hard-pull consent — already exists as the `'done'` step (`OfferFlow.tsx`), no code change needed
- [x] 1.2 Render the "Offer Confirmed — we're now running a full credit check" message internally when `currentStep === 'confirmed'`, matching the shell's current copy/spinner styling — already rendered by the existing `'done'` step; copy verified equivalent to what was removed from `StatusPage.jsx`
- [x] 1.3 Confirm `offer-confirmed` custom event still fires at the same point in the flow (on entering the confirmed step), preserving the existing event contract consumed by `StatusPage` — verified: `onComplete?.()` fires immediately after `setStep('done')` in `handleConsentConfirm`, unchanged
- [ ] 1.4 Add/update unit tests for `OfferFlow` covering the new `'confirmed'` step rendering — deferred; `pricing-offers-ui` has no test framework today (see Open Follow-up below)

## 2. document-management-ui: internal loading state

- [x] 2.1 Add a `loading` boolean to `useDocumentRequirements`, true until the initial requirements fetch resolves — already exists (`useDocumentRequirements.ts`), no code change needed
- [x] 2.2 Render a "Loading document portal…" state inside `DocumentManager` while `loading` is true — already exists ("Loading document requirements…" in `DocumentManager.tsx`), no code change needed
- [ ] 2.3 Add/update unit tests for `DocumentManager` covering the new loading state — deferred; `document-management-ui` has no test framework today (see Open Follow-up below)

## 3. application-management-ui: simplify StatusPage

- [x] 3.1 Remove the `offerConfirmed` render-gating state and `overlayHidden` logic from `renderWebComponent()` in `StatusPage.jsx` — `<pricing-offer-selector>` now stays mounted and renders its own confirmed state
- [x] 3.2 Remove the "Loading your offers…" and "Loading document portal…" component-specific loading text from `renderWebComponent()`; keep only a generic, component-agnostic fallback for the `!script.loaded` case
- [x] 3.3 Keep the existing `offerConfirmed`/`offerConfirmedKey` sessionStorage read in `shouldPoll()` unchanged — this is a polling-cadence (orchestration) concern, not a rendering concern, and stays in the shell
- [x] 3.4 Update or remove any `StatusPage`-level assertions that depended on the old rendered loading/confirmation copy — none existed (`application-management-ui` has no `StatusPage`-level test harness; see `configurable-navigation-flow/tasks.md` Open Follow-up)

## 4. Verification

- [x] 4.1 Run `pricing-offers-ui`, `document-management-ui`, and `application-management-ui` test suites; confirm all pass — no test suites exist for the first two (see follow-up); `application-management-ui`: `npm run test` (5/5 pass) and `npm run build` succeed
- [ ] 4.2 Manually verify (via `/verify` or cloud deployment) the OFFER_PENDING flow end-to-end: offers load, selection + consent captured, confirmed state shown, polling resumes, and DOCUMENTS_REQUIRED flow: requirements load and upload works
- [ ] 4.3 Update `openspec/pricing-orchestration/spec.md` §8 and `openspec/document-collection/spec.md` §12 per the delta specs once implementation is verified (handled at archive time)

## 5. application-management-ui: registry-driven screen dispatch

- [x] 5.1 Extend `navigationConfig.js`: add `containerWidth` to the `OFFER_PENDING` (760) and `DOCUMENTS_REQUIRED` (800) descriptors; add `completionEvent: 'offer-confirmed'` to the `OFFER_PENDING` descriptor
- [x] 5.2 Create `src/navigation/screens/SpinnerScreen.jsx` (moves `renderSpinner` body)
- [x] 5.3 Create `src/navigation/screens/WebComponentScreen.jsx` — generic mount by `descriptor.tag`, attribute values built from `descriptor.attributes` + a shared value map, own local ref + `useCustomEvent` wired to `descriptor.completionEvent`/`ctx.eventHandlers`, no per-tag branching
- [x] 5.4 Create `src/navigation/internalMfeRegistry.js` (`{ OfferAcceptanceMfe: OfferAcceptanceScreen }`) and `src/navigation/screens/OfferAcceptanceScreen.jsx` (moves `renderInternalMfe`'s `OfferAcceptanceMfe` branch) plus `src/navigation/screens/InternalMfeScreen.jsx` (registry lookup by `descriptor.component`)
- [x] 5.5 Create `src/navigation/staticBlockRegistry.js` (`{ declined, 'cancelled-expired', 'post-acceptance' }`) and one screen component per block (moves `renderStaticBlock`'s three branches) plus `src/navigation/screens/StaticBlockScreen.jsx` (registry lookup by `descriptor.block`)
- [x] 5.6 Create `src/navigation/screenRegistry.js` mapping `kind` → container component
- [x] 5.7 Rewrite `StatusPage.jsx`: remove `renderScreen`/`renderSpinner`/`renderWebComponent`/`renderInternalMfe`/`renderStaticBlock`; render via `SCREEN_REGISTRY[descriptor.kind]` lookup; keep fetch/poll/`shouldPoll`/`handleOfferConfirmed` orchestration logic unchanged
- [x] 5.8 Extend `resolveScreen.test.js` (or add a new test) asserting every `kind` used in `NAVIGATION_CONFIG` has a `screenRegistry` entry, every `component` has an `internalMfeRegistry` entry, and every `block` has a `staticBlockRegistry` entry — added `screenRegistry.test.js` (3 new tests)
- [x] 5.9 Run `application-management-ui` tests and build; confirm no behavior regression for each status — `npm run test` (8/8 pass) and `npm run build` succeed

## Open Follow-up

- Neither `pricing-offers-ui` nor `document-management-ui` has a test framework configured (same gap `application-management-ui` had before `configurable-navigation-flow` added vitest there). Recommend a follow-up change to add vitest + component tests to both, covering `OfferFlow`'s step transitions and `DocumentManager`'s loading/error/complete states — asked the user during this change whether to add it now; no response, deferred to keep this change scoped to the rendering-ownership fix.
