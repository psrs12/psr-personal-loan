#!/usr/bin/env bash
# Build Docker images for all UI services and push to OCIR.
# Usage: ./deploy/scripts/06-build-push-ui.sh [--tag v1.0.0]

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

# Source env without executing (avoids & in DB URLs breaking shell)
set -a
while IFS='=' read -r key value; do
  [[ "$key" =~ ^#.*$ || -z "$key" ]] && continue
  export "$key=$value"
done < "${SCRIPT_DIR}/../.env"
set +a

IMAGE_TAG="${IMAGE_TAG:-latest}"

while [[ $# -gt 0 ]]; do
  case $1 in
    --tag) IMAGE_TAG="$2"; shift 2 ;;
    *) echo "Unknown arg: $1"; exit 1 ;;
  esac
done

OCIR_HOST="${OCI_REGION}.ocir.io"
echo "==> Logging in to OCIR (${OCIR_HOST})"
echo "${OCI_AUTH_TOKEN}" | docker login "${OCIR_HOST}" \
  -u "${OCI_TENANCY_NAMESPACE}/psrs@rocketmail.com" \
  --password-stdin

ui_services=(
  "application-management-ui"
  "pricing-offers-ui"
  "document-management-ui"
)

echo ""
echo "==> Building and pushing UI images (tag: ${IMAGE_TAG})"
echo ""

for svc in "${ui_services[@]}"; do
  image="${IMAGE_REGISTRY_PREFIX}/${svc}:${IMAGE_TAG}"
  context="${REPO_ROOT}/services/${svc}"

  echo "--- Building ${svc} ---"

  if [[ "$svc" == "application-management-ui" ]]; then
    docker buildx build \
      --platform linux/arm64 \
      --tag "${image}" \
      --tag "${IMAGE_REGISTRY_PREFIX}/${svc}:latest" \
      --build-arg VITE_APP_MANAGEMENT_API_URL="${VITE_APP_MANAGEMENT_API_URL}" \
      --build-arg VITE_PRICING_API_URL="${VITE_PRICING_API_URL}" \
      --build-arg VITE_OFFER_ACCEPTANCE_API_URL="${VITE_OFFER_ACCEPTANCE_API_URL}" \
      --build-arg VITE_DOCUMENT_API_URL="${VITE_DOCUMENT_API_URL}" \
      --build-arg VITE_PRICING_OFFERS_UI_JS_URL="${VITE_PRICING_OFFERS_UI_JS_URL}" \
      --build-arg VITE_DOCUMENT_MANAGEMENT_UI_JS_URL="${VITE_DOCUMENT_MANAGEMENT_UI_JS_URL}" \
      --file "${context}/Dockerfile" \
      --push \
      "${context}"
  else
    docker buildx build \
      --platform linux/arm64 \
      --tag "${image}" \
      --tag "${IMAGE_REGISTRY_PREFIX}/${svc}:latest" \
      --file "${context}/Dockerfile" \
      --push \
      "${context}"
  fi

  echo "  Pushed: ${image}"
done

echo ""
echo "==> All UI images pushed."
