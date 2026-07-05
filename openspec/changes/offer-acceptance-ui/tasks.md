## 1. offer-acceptance-ui: service scaffold

- [x] 1.1 Create `services/offer-acceptance-ui/` mirroring `services/pricing-offers-ui/`: `package.json`, `vite.config.ts`, `tsconfig.json`, `.env.example`, `Dockerfile`, `nginx.conf`
- [x] 1.2 Add `VITE_OFFER_ACCEPTANCE_API_URL` and `VITE_PRICING_API_URL` env vars to `.env.example`

## 2. offer-acceptance-ui: internal components

- [x] 2.1 Create `src/hooks/useDeclarations.ts` — port `loadDeclarations` retry logic (404 → retry up to 5x at 2s) from `OfferAcceptanceMfe.jsx`
- [x] 2.2 Create `src/hooks/useConfirmedOffer.ts` — port the `getSelectedOffer` read (amount, term, APR, monthly payment), tolerant of missing fields
- [x] 2.3 Create `src/components/OfferSummary.tsx` — port the offer-summary display markup
- [x] 2.4 Create `src/components/DeclarationsList.tsx` — port declarations rendering, mandatory-checkbox gating, and the submit button disabled state
- [x] 2.5 Create `src/components/AcceptanceFlow.tsx` — compose `OfferSummary` + `DeclarationsList`, own loading/error/submitted state, call `onComplete` on successful e-sign submission (mirrors `OfferFlow.tsx`'s `onComplete` pattern)

## 3. offer-acceptance-ui: web component wrapper

- [x] 3.1 Create `src/web-components/offer-acceptance-flow.tsx` registering custom element `offer-acceptance-flow` with `observedAttributes`: `api-base-url`, `pricing-api-base-url`, `application-id`, `session-token`
- [x] 3.2 Fire `offer-accepted` custom event (bubbles, composed) from `connectedCallback`'s rendered `AcceptanceFlow`'s `onComplete`, matching `pricing-offer-selector.tsx`'s `offer-confirmed` event pattern

## 4. application-management-ui: wire in new MFE, remove old one

- [x] 4.1 Add `offerAcceptanceUiJs: import.meta.env.VITE_OFFER_ACCEPTANCE_UI_JS_URL || '...'` to `src/api/config.js`, alongside `pricingOffersUiJs`/`documentManagementUiJs`
- [x] 4.2 Update `navigationConfig.js`: change `APPROVED` descriptor from `{ kind: 'internal-mfe', component: 'OfferAcceptanceMfe' }` to the `web-component` descriptor (tag `offer-acceptance-flow`, `scriptUrlKey: 'offerAcceptanceUiJs'`, `apiBaseUrlKey: 'offerAcceptance'`, `attributes: ['application-id', 'api-base-url', 'session-token', 'pricing-api-base-url']`, `containerWidth: 760`, `completionEvent: 'offer-accepted'`); remove `'internal-mfe'` from the `ScreenDescriptor` typedef
- [x] 4.3 Update `src/navigation/screens/WebComponentScreen.jsx`'s attribute-value map to include `'pricing-api-base-url': API.pricing`
- [x] 4.4 Add an `offerAcceptanceUiJs` `useWebComponentScript` call in `StatusPage.jsx` (mirroring `pricingScript`/`documentScript`) and include it in `ctx.scripts`
- [x] 4.5 Add an `offer-accepted` handler to `ctx.eventHandlers` in `StatusPage.jsx` that triggers an immediate `fetchStatus()` (short-delay pattern matching `handleOfferConfirmed`)
- [x] 4.6 Delete `src/components/OfferAcceptanceMfe.jsx`, `src/navigation/internalMfeRegistry.js`, `src/navigation/screens/OfferAcceptanceScreen.jsx`, `src/navigation/screens/InternalMfeScreen.jsx`
- [x] 4.7 Remove `'internal-mfe': InternalMfeScreen` from `src/navigation/screenRegistry.js` and the now-unused import
- [x] 4.8 Update `screenRegistry.test.js` if it references `INTERNAL_MFE_REGISTRY`/`internalMfeRegistry.js` — remove those assertions since no descriptor uses `internal-mfe` anymore

## 5. Documentation and deployment

- [x] 5.1 Update `docs/architecture/002-application-state-machine.md` §9 table: `APPROVED` row changes from `OfferAcceptanceMfe (application-management-ui)` to `<offer-acceptance-flow> (offer-acceptance-ui web component)`
- [x] 5.2 Add `offer-acceptance-ui` service to `deploy/docker-compose.local.yml` and `deploy/docker-compose.prod.yml`, alongside `pricing-offers-ui`/`document-management-ui` entries
- [x] 5.3 Add `offer-acceptance-ui` to the frontend build/push script (`deploy/scripts/06-build-push-ui.sh`) and frontend deploy script (`deploy/scripts/06-deploy-frontends.sh`)

## 6. Verification

- [x] 6.1 Run `application-management-ui` test suite and build; confirm no regressions from the descriptor/registry changes
- [ ] 6.2 Manually verify (via `/verify` or cloud deployment) the `APPROVED` flow end-to-end: `<offer-acceptance-flow>` mounts, declarations load (including the 404-retry path if the session isn't ready yet), offer summary displays, mandatory gating works, e-sign submits, `offer-accepted` fires, shell resumes polling and transitions to `OFFER_ACCEPTED`
- [ ] 6.3 Update `openspec/offer-acceptance/spec.md` (if a UI-facing capability doc section is warranted) and confirm `openspec/changes/mfe-rendering-ownership`'s delta spec content is reconciled at archive time

## Open Follow-up

- `offer-acceptance-ui` has no test framework at inception, same as `pricing-offers-ui`/`document-management-ui` — tracked as the same deferred cross-service follow-up already logged in `openspec/changes/mfe-rendering-ownership/tasks.md`, not repeated per-service.
