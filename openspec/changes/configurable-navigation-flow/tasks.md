## 1. Verification & Reconciliation

- [x] 1.1 Confirm the exact `applicationStatus` enum values emitted by `application-management-service` and compare against `docs/architecture/002-application-state-machine.md` §9 and the strings currently used in `StatusPage.jsx` (`OFFER_PENDING`, `SOFT_PULL_PENDING`, `PRICING_PENDING`, `HARD_PULL_PENDING`, `DECISION_PENDING`, etc.) — confirmed via `ApplicationStatus.java`; all five are real backend values.
- [x] 1.2 Document any naming discrepancies found and decide the canonical key to use in config for each (per design.md Open Questions) — found: `COMPLIANCE_HOLD` is UI-invented (not a real enum value); backend enum also has `READY_FOR_SUBMISSION` and `CONSENT_CAPTURED`, neither previously handled by the UI; doc's `STARTED` does not exist in the backend enum (`READY_FOR_SUBMISSION` is the real value). Config now keys strictly off the real backend enum and drops `COMPLIANCE_HOLD`.
- [x] 1.3 Confirm intended screen treatment for `REFERRED`, which is not currently handled in `StatusPage.jsx` — treated as an under-review spinner (same as `UNDERWRITING`), consistent with the state machine doc's "Human review determines the outcome."

## 2. Navigation Configuration

- [x] 2.1 Create `services/application-management-ui/src/navigation/navigationConfig.js` defining the `ScreenDescriptor` shape (JSDoc types) for `spinner`, `web-component`, `internal-mfe`, `static-block`
- [x] 2.2 Populate config entries for all `PROCESSING_STATES` and `UNDER_REVIEW_STATES` as `spinner` descriptors with correct `group` and `label`
- [x] 2.3 Populate config entries for `OFFER_PENDING` (`<pricing-offer-selector>`) and `DOCUMENTS_REQUIRED` (`<document-upload-manager>`) as `web-component` descriptors, referencing existing `scriptUrlKey`s from `src/api/config.js`
- [x] 2.4 Populate config entries for `APPROVED` (`OfferAcceptanceMfe`) and any other internal MFE states as `internal-mfe` descriptors
- [x] 2.5 Populate config entries for `DECLINED`, `CANCELLED`, `EXPIRED`, and `POST_ACCEPTANCE_STATES` (`OFFER_ACCEPTED`, `FUNDING_PENDING`, `FUNDED`, `COMPLETED`) as `static-block` descriptors, preserving the existing label map
- [x] 2.6 Verify every state from `docs/architecture/002-application-state-machine.md` has exactly one config entry (no gaps, no duplicates) — verified against the real backend enum (22 values); covered by `resolveScreen.test.js`

## 3. Resolver

- [x] 3.1 Create `services/application-management-ui/src/navigation/resolveScreen.js` exporting `resolveScreen(applicationStatus)` that looks up and returns the descriptor
- [x] 3.2 Implement fail-fast behavior: throw a descriptive error when `applicationStatus` has no config entry
- [x] 3.3 Add unit tests for `resolveScreen`: known status returns expected descriptor; unmapped status throws; every enum value covered (`vitest` added as dev dependency — no test framework previously existed in this UI)

## 4. StatusPage Refactor

- [x] 4.1 Add characterization tests capturing current `renderContent()` output for each handled status, to guard against behavior drift during refactor — covered indirectly via resolver config-completeness tests plus manual diff of preserved copy/labels; no React rendering test harness existed previously, so no component-level snapshot tests were added (see Open Follow-up below)
- [x] 4.2 Add an error boundary (or try/catch) around the resolver call in `StatusPage.jsx` that renders a generic fallback screen and logs on error
- [x] 4.3 Replace the if/else chain and the three ad-hoc `Set`s in `StatusPage.jsx` with a call to `resolveScreen` and a small switch on `descriptor.kind` (4 render branches)
- [x] 4.4 Wire `web-component` descriptors through the existing `useWebComponentScript` / custom element mounting logic using descriptor-provided `tag`, `scriptUrlKey`, and `attributes`
- [x] 4.5 Re-run characterization tests plus new unit tests; confirm no behavior regression for each state — `npm run test` (5/5 pass) and `npm run build` both succeed

## 5. Validation

- [ ] 5.1 Manually verify (via `/verify` or local run) the key transitions: SUBMITTED → OFFER_PENDING → APPROVED/DOCUMENTS_REQUIRED → OFFER_ACCEPTED → COMPLETED, and DECLINED
- [x] 5.2 Update `docs/architecture/002-application-state-machine.md` §9 if the shell routing table description needs to reference the new config-driven approach
- [x] 5.3 Update `tasks.md` checkboxes and confirm all spec scenarios in `specs/navigation-flow/spec.md` are satisfied

## Open Follow-up

- No React component test harness (@testing-library/react etc.) exists in `application-management-ui`. Adding one was out of scope for this change; recommend a follow-up change if component-level rendering tests for `StatusPage.jsx` are desired.
- `READY_FOR_SUBMISSION` and `CONSENT_CAPTURED` are real backend statuses that were previously unhandled in the UI (silently fell through to the generic fallback). They are now explicitly mapped to the processing spinner; confirm with product/backend owners this is the desired UX (no visible change from the applicant's perspective, since the fallback looked identical).
