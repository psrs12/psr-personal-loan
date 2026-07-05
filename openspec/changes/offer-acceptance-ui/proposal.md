## Why

Every other post-decision functional capability in the platform has a dedicated, independently deployable micro-frontend that owns its own rendering: `pricing-offers-ui` (offer selection + hard-pull consent) and `document-management-ui` (document upload lifecycle). Offer acceptance — declarations review and e-signature — is the one remaining post-decision capability still implemented as an internal React component (`OfferAcceptanceMfe.jsx`) embedded directly in `application-management-ui`'s own bundle. This breaks the established 1:1 backend-service-to-frontend-MFE pattern (`offer-acceptance-service` has no corresponding standalone UI), duplicates the `mfe-rendering-ownership` principle inconsistently, and blocks `offer-acceptance-service` API/UI changes from shipping independently of the shell.

## What Changes

- Introduce a new standalone frontend service, `offer-acceptance-ui`, mirroring the structure of `pricing-offers-ui`/`document-management-ui`: its own `package.json`, `vite.config.ts`, `Dockerfile`, `nginx.conf`, `.env.example`, and a web component (`<offer-acceptance-flow>`) registered via `customElements.define`.
- Move all declarations-review and e-sign functionality currently in `services/application-management-ui/src/components/OfferAcceptanceMfe.jsx` into the new service, sourced entirely from `offer-acceptance-service`'s existing `GET /applications/{id}/declarations` and `POST /applications/{id}/esign` endpoints and from the applicant's `ConfirmedOffer` (via `pricing-orchestration-service`, same read the old component performed through `getSelectedOffer`).
- **BREAKING**: Remove `OfferAcceptanceMfe.jsx` and the `internal-mfe` / `OfferAcceptanceMfe` registry entry from `application-management-ui`; the `APPROVED` status descriptor in `navigationConfig.js` changes `kind` from `internal-mfe` to `web-component`, mounting `<offer-acceptance-flow>` the same way `<pricing-offer-selector>` and `<document-upload-manager>` are mounted today.
- Add a completion event contract (`offer-accepted`, fired by the new web component on successful e-sign submission) so the shell can react (e.g. trigger an immediate status refetch) without owning any acceptance-flow rendering, matching the `offer-confirmed` pattern already used by `pricing-offers-ui`.
- No change to `offer-acceptance-service` itself — its REST contract already supports this UI as-is (see `openspec/offer-acceptance/spec.md`).

## Capabilities

### New Capabilities
- `offer-acceptance-ui`: Standalone web-component MFE for declarations review and e-signature capture, owning its own loading/ready/submitted rendering, sourced from `offer-acceptance-service` and `pricing-orchestration-service`.

### Modified Capabilities
- `mfe-rendering-ownership`: extends the "one functional capability, one owning MFE" boundary already established for pricing/document to cover offer acceptance — `application-management-ui` no longer renders any functional MFE content directly, only orchestrates.

## Impact

- New service directory `services/offer-acceptance-ui/` (build, deploy, nginx config, new container in `deploy/docker-compose*.yml`).
- `services/application-management-ui/src/components/OfferAcceptanceMfe.jsx`, `src/navigation/internalMfeRegistry.js`, `src/navigation/screens/OfferAcceptanceScreen.jsx` — removed.
- `services/application-management-ui/src/navigation/navigationConfig.js` — `APPROVED` descriptor changed from `internal-mfe` to `web-component`.
- `services/application-management-ui/src/api/config.js` — add `offerAcceptanceUiJs` script URL, matching `pricingOffersUiJs`/`documentManagementUiJs`.
- `docs/architecture/002-application-state-machine.md` §9 — update the screen-mapping table's `APPROVED` row.
- Deployment: one new frontend container to build/push/deploy alongside the existing two UI MFEs.
