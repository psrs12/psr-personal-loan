#!/usr/bin/env bash
# Checks that spring-boot-starter-actuator and micrometer-registry-prometheus
# are present in each service pom.xml. Prints a warning for any that are missing.
#
# These dependencies are required for Grafana metrics scraping.
# Add them manually if missing:
#
#   <dependency>
#     <groupId>org.springframework.boot</groupId>
#     <artifactId>spring-boot-starter-actuator</artifactId>
#   </dependency>
#   <dependency>
#     <groupId>io.micrometer</groupId>
#     <artifactId>micrometer-registry-prometheus</artifactId>
#   </dependency>

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

services=(
  "application-management-service"
  "pricing-orchestration-service"
  "document-service"
  "offer-acceptance-service"
)

echo "==> Checking actuator + prometheus dependencies"
echo ""

for svc in "${services[@]}"; do
  pom="${REPO_ROOT}/services/${svc}/pom.xml"
  missing=()

  grep -q "spring-boot-starter-actuator" "${pom}" || missing+=("spring-boot-starter-actuator")
  grep -q "micrometer-registry-prometheus" "${pom}" || missing+=("micrometer-registry-prometheus")

  if [[ ${#missing[@]} -eq 0 ]]; then
    echo "  OK  ${svc}"
  else
    echo "  MISSING ${svc}: ${missing[*]}"
    echo "          Add to ${pom}"
  fi
done

echo ""
echo "==> Also ensure each service application.yml exposes the prometheus endpoint:"
echo '    management:'
echo '      endpoints:'
echo '        web:'
echo '          exposure:'
echo '            include: health,info,metrics,prometheus'
