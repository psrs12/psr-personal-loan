#!/usr/bin/env bash
# Build Docker images for all backend services and push to a registry
#
# Supports two registries:
#   1. Oracle Container Registry (OCIR) — uses your OCI Auth Token (recommended)
#      Registry: <region>.ocir.io/<tenancy-namespace>/<image>
#   2. Docker Hub — uses DOCKER_USERNAME + DOCKER_PASSWORD
#
# Usage:
#   ./deploy/scripts/04-build-push.sh               # uses OCIR by default
#   ./deploy/scripts/04-build-push.sh --registry dockerhub
#   ./deploy/scripts/04-build-push.sh --tag v1.0.0

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
source "${SCRIPT_DIR}/../.env"

IMAGE_TAG="${IMAGE_TAG:-latest}"
REGISTRY="ocir"  # default: use Oracle Container Registry (free, uses auth token)

while [[ $# -gt 0 ]]; do
  case $1 in
    --tag)      IMAGE_TAG="$2";  shift 2 ;;
    --registry) REGISTRY="$2";  shift 2 ;;
    *) echo "Unknown arg: $1"; exit 1 ;;
  esac
done

services=(
  "application-management-service"
  "pricing-orchestration-service"
  "document-service"
  "offer-acceptance-service"
)

# ---- Registry login ----

if [[ "${REGISTRY}" == "ocir" ]]; then
  # OCIR: login with OCI Auth Token
  # Auth Token is from: OCI Console > Profile > User Settings > Auth Tokens
  OCIR_HOST="${OCI_REGION}.ocir.io"
  OCIR_NAMESPACE="${OCI_TENANCY_NAMESPACE}"
  OCIR_USERNAME="${OCI_TENANCY_NAMESPACE}/psrs@rocketmail.com"

  echo "==> Logging in to OCIR (${OCIR_HOST})"
  echo "${OCI_AUTH_TOKEN}" | docker login "${OCIR_HOST}" \
    -u "${OCIR_USERNAME}" \
    --password-stdin

  image_prefix="${OCIR_HOST}/${OCIR_NAMESPACE}/personal-loan"

elif [[ "${REGISTRY}" == "dockerhub" ]]; then
  echo "==> Logging in to Docker Hub as ${DOCKER_USERNAME}"
  echo "${DOCKER_PASSWORD}" | docker login -u "${DOCKER_USERNAME}" --password-stdin
  image_prefix="${DOCKER_USERNAME}"

else
  echo "Unknown registry: ${REGISTRY}. Use 'ocir' or 'dockerhub'."
  exit 1
fi

echo ""
echo "==> Building and pushing images (tag: ${IMAGE_TAG}, registry: ${REGISTRY})"
echo ""

for svc in "${services[@]}"; do
  image="${image_prefix}/${svc}:${IMAGE_TAG}"
  image_latest="${image_prefix}/${svc}:latest"

  echo "--- Building ${image} ---"

  docker buildx build \
    --platform linux/arm64 \
    --build-arg SERVICE_NAME="${svc}" \
    --tag "${image}" \
    --tag "${image_latest}" \
    --file "${REPO_ROOT}/deploy/Dockerfile" \
    --push \
    "${REPO_ROOT}"

  echo "  Pushed: ${image}"
done

echo ""
echo "==> All images pushed to ${REGISTRY}."
echo ""
echo "==> Update deploy/.env:"
echo "    IMAGE_REGISTRY_PREFIX=${image_prefix}"
echo "    IMAGE_TAG=${IMAGE_TAG}"

# Write resolved prefix to .env if not already set
if ! grep -q "IMAGE_REGISTRY_PREFIX" "${SCRIPT_DIR}/../.env"; then
  echo "" >> "${SCRIPT_DIR}/../.env"
  echo "# Populated by 04-build-push.sh" >> "${SCRIPT_DIR}/../.env"
  echo "IMAGE_REGISTRY_PREFIX=${image_prefix}" >> "${SCRIPT_DIR}/../.env"
fi
