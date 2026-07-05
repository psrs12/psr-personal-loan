#!/usr/bin/env bash
# Create one database per service on Neon (serverless PostgreSQL)
# Requires: Neon account, project created, API key
# Docs: https://api-docs.neon.tech/reference/createprojectbranch
#
# After running this script, copy the connection strings printed at the end
# into deploy/.env

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/../.env"

NEON_API="https://console.neon.tech/api/v2"
AUTH_HEADER="Authorization: Bearer ${NEON_API_KEY}"

databases=(
  "app_management"
  "pricing_orchestration"
  "offer_acceptance"
  "document_service"
)

echo "==> Setting up Neon databases in project ${NEON_PROJECT_ID}"

for db in "${databases[@]}"; do
  echo "--- Creating database: ${db} ---"

  # Create database in the default branch
  response=$(curl -s -X POST \
    "${NEON_API}/projects/${NEON_PROJECT_ID}/databases" \
    -H "${AUTH_HEADER}" \
    -H "Content-Type: application/json" \
    -d "{\"database\": {\"name\": \"${db}\", \"owner_name\": \"neondb_owner\"}}")

  if echo "${response}" | grep -q '"name"'; then
    echo "  Created: ${db}"
  else
    echo "  Warning: ${response}"
  fi
done

# Get connection string for the default branch
echo ""
echo "==> Fetching connection details..."
conn_info=$(curl -s \
  "${NEON_API}/projects/${NEON_PROJECT_ID}/connection_uri?database_name=app_management&role_name=neondb_owner" \
  -H "${AUTH_HEADER}")

host=$(echo "${conn_info}" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('connection_parameters',{}).get('host',''))" 2>/dev/null || echo "see Neon console")

echo ""
echo "==> Add these to deploy/.env:"
echo ""
for db in "${databases[@]}"; do
  varname=$(echo "${db}" | tr '[:lower:]' '[:upper:]')
  echo "  # ${db}"
  echo "  # DB URL: jdbc:postgresql://${host}/${db}?sslmode=require"
done

echo ""
echo "==> Get your password from the Neon console > Project Settings > Connection Details"
echo "    Connection string format: postgresql://neondb_owner:<password>@<host>/<db>?sslmode=require"
