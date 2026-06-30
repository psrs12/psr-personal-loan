#!/usr/bin/env bash
# Deploy or update backend services on the Oracle Cloud VM
# Usage: ./deploy/scripts/05-deploy-backend.sh [--tag v1.0.0]

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
source "${SCRIPT_DIR}/../.env"

IMAGE_TAG="${IMAGE_TAG:-latest}"

while [[ $# -gt 0 ]]; do
  case $1 in
    --tag) IMAGE_TAG="$2"; shift 2 ;;
    *) echo "Unknown arg: $1"; exit 1 ;;
  esac
done

REMOTE_DIR="/opt/personal-loan"

echo "==> Uploading compose and config to ${ORACLE_VM_IP}"

# Upload files
scp -i "${ORACLE_SSH_KEY_PATH}" \
  "${SCRIPT_DIR}/../docker-compose.prod.yml" \
  "${ORACLE_VM_USER}@${ORACLE_VM_IP}:${REMOTE_DIR}/docker-compose.prod.yml"

scp -i "${ORACLE_SSH_KEY_PATH}" \
  "${SCRIPT_DIR}/../.env" \
  "${ORACLE_VM_USER}@${ORACLE_VM_IP}:${REMOTE_DIR}/.env"

# Upload Grafana Alloy config
scp -i "${ORACLE_SSH_KEY_PATH}" \
  -r "${SCRIPT_DIR}/../grafana" \
  "${ORACLE_VM_USER}@${ORACLE_VM_IP}:${REMOTE_DIR}/grafana"

echo "==> Deploying services (IMAGE_TAG=${IMAGE_TAG})"

ssh -i "${ORACLE_SSH_KEY_PATH}" "${ORACLE_VM_USER}@${ORACLE_VM_IP}" bash <<REMOTE
  set -euo pipefail
  cd ${REMOTE_DIR}

  echo "--- Logging in to OCIR ---"
  echo "${OCI_AUTH_TOKEN}" | docker login ${OCI_REGION}.ocir.io \
    -u "${OCI_TENANCY_NAMESPACE}/psrs@rocketmail.com" \
    --password-stdin

  echo "--- Pulling latest images ---"
  IMAGE_TAG=${IMAGE_TAG} docker compose -f docker-compose.prod.yml --env-file .env pull

  echo "--- Starting/updating services ---"
  IMAGE_TAG=${IMAGE_TAG} docker compose -f docker-compose.prod.yml --env-file .env up -d --remove-orphans

  echo "--- Pruning old images ---"
  docker image prune -f

  echo "--- Service status ---"
  docker compose -f docker-compose.prod.yml ps
REMOTE

echo ""
echo "==> Deployment complete."
echo ""
echo "    Health checks:"
echo "      application-management : http://${ORACLE_VM_IP}:8081/api/v1/application-management/actuator/health"
echo "      pricing-orchestration  : http://${ORACLE_VM_IP}:8082/actuator/health"
echo "      document-service       : http://${ORACLE_VM_IP}:8084/actuator/health"
echo "      offer-acceptance       : http://${ORACLE_VM_IP}:8085/actuator/health"
