#!/usr/bin/env bash
# Build and deploy micro-frontends to Cloudflare Pages
# Requires: wrangler CLI — npm install -g wrangler
#
# Each UI is a separate Cloudflare Pages project.
# Run once to create projects, then on every deployment.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
source "${SCRIPT_DIR}/../.env"

export CLOUDFLARE_ACCOUNT_ID CLOUDFLARE_API_TOKEN

# Map: directory → Cloudflare Pages project name
declare -A UI_PROJECTS=(
  ["application-management-ui"]="personal-loan-app-management-ui"
  ["pricing-offers-ui"]="personal-loan-pricing-offers-ui"
  ["document-management-ui"]="personal-loan-document-management-ui"
)

# Backend URL for each UI (set as build-time env var if needed)
BACKEND_URL="http://${ORACLE_VM_IP}"

echo "==> Deploying micro-frontends to Cloudflare Pages"
echo ""

for ui_dir in "${!UI_PROJECTS[@]}"; do
  project="${UI_PROJECTS[$ui_dir]}"
  full_path="${REPO_ROOT}/services/${ui_dir}"

  echo "--- Building ${ui_dir} ---"

  cd "${full_path}"
  npm ci --silent
  VITE_BACKEND_URL="${BACKEND_URL}" npm run build

  echo "--- Deploying ${ui_dir} to ${project} ---"

  # Create project if it doesn't exist (first run only)
  wrangler pages project create "${project}" --production-branch main 2>/dev/null || true

  # Deploy
  wrangler pages deploy dist \
    --project-name "${project}" \
    --commit-dirty=true

  echo "  Deployed: ${project}"
  echo ""
done

echo "==> Frontend deployment complete."
echo ""
echo "    Cloudflare Pages URLs:"
for ui_dir in "${!UI_PROJECTS[@]}"; do
  project="${UI_PROJECTS[$ui_dir]}"
  echo "      https://${project}.pages.dev"
done
