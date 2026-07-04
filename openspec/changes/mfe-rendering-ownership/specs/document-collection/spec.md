## MODIFIED Requirements

### Requirement: UI Component
The `<document-upload-manager>` web component (served by `document-management-ui`) handles:

- Its own loading state while document requirements are being fetched, rendered without relying on the embedding shell.
- Display of document requirements with status indicators.
- Per-requirement file upload with progress tracking.
- Status refresh after upload and after virus scan completion.

#### Scenario: Component renders its own requirements-loading state
- **WHEN** `<document-upload-manager>` is mounted and is fetching document requirements from `document-service`
- **THEN** the component renders its own loading indicator without the embedding shell rendering a document-portal-specific loading message
