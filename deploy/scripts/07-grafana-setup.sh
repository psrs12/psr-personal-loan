#!/usr/bin/env bash
# Configure Grafana Cloud datasources via API
# After running this, import the dashboard JSON from deploy/grafana/dashboard.json
# in the Grafana Cloud UI: Dashboards > Import

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/../.env"

GRAFANA_ORG_SLUG="your-org-slug"   # replace with your Grafana Cloud org slug
GRAFANA_HOST="https://${GRAFANA_ORG_SLUG}.grafana.net"

echo "==> Configuring Grafana Cloud at ${GRAFANA_HOST}"

# Create Prometheus datasource
echo "--- Creating Prometheus datasource ---"
curl -s -X POST "${GRAFANA_HOST}/api/datasources" \
  -u "${GRAFANA_CLOUD_USER}:${GRAFANA_CLOUD_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "PersonalLoan-Prometheus",
    "type": "prometheus",
    "url": "'"${GRAFANA_CLOUD_PROMETHEUS_URL}"'",
    "access": "proxy",
    "basicAuth": true,
    "basicAuthUser": "'"${GRAFANA_CLOUD_USER}"'",
    "secureJsonData": {"basicAuthPassword": "'"${GRAFANA_CLOUD_API_KEY}"'"},
    "isDefault": true
  }' | python3 -m json.tool 2>/dev/null || true

# Create Loki datasource
echo "--- Creating Loki datasource ---"
curl -s -X POST "${GRAFANA_HOST}/api/datasources" \
  -u "${GRAFANA_CLOUD_USER}:${GRAFANA_CLOUD_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "PersonalLoan-Loki",
    "type": "loki",
    "url": "'"${GRAFANA_CLOUD_LOKI_URL}"'",
    "access": "proxy",
    "basicAuth": true,
    "basicAuthUser": "'"${GRAFANA_CLOUD_USER}"'",
    "secureJsonData": {"basicAuthPassword": "'"${GRAFANA_CLOUD_API_KEY}"'"}
  }' | python3 -m json.tool 2>/dev/null || true

echo ""
echo "==> Grafana datasources configured."
echo ""
echo "    Next steps:"
echo "    1. Open ${GRAFANA_HOST}"
echo "    2. Go to Dashboards > Import"
echo "    3. Import deploy/grafana/dashboard.json"
echo "    4. Select PersonalLoan-Prometheus as the data source"
