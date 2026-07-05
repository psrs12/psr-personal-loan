#!/usr/bin/env bash
# Create Kafka topics on Confluent Cloud
# Requires: Confluent Cloud account, cluster created, API key with cluster-level access
#
# Install Confluent CLI first:
#   brew install confluentinc/tap/cli   (Mac)
#   curl -sL --http1.1 https://cnfl.io/cli | sh -s -- latest  (Linux)

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/../.env"

topics=(
  "application-events:3:1"
  "consent-events:3:1"
  "pricing-events:3:1"
  "offer-acceptance-events:3:1"
  "document-events:3:1"
)

echo "==> Creating Kafka topics on Confluent Cloud cluster ${CONFLUENT_CLUSTER_ID}"

# Use Confluent REST API (no CLI required)
CONFLUENT_REST_URL="https://pkc-xxxxx.region.confluent.cloud"  # Replace with your cluster REST endpoint

for entry in "${topics[@]}"; do
  IFS=: read -r topic partitions replication <<< "${entry}"
  echo "--- Creating topic: ${topic} (partitions=${partitions}) ---"

  response=$(curl -s -X POST \
    "https://pkc-xxxxx.region.confluent.cloud/kafka/v3/clusters/${CONFLUENT_CLUSTER_ID}/topics" \
    -u "${CONFLUENT_API_KEY}:${CONFLUENT_API_SECRET}" \
    -H "Content-Type: application/json" \
    -d "{
      \"topic_name\": \"${topic}\",
      \"partitions_count\": ${partitions},
      \"replication_factor\": ${replication},
      \"configs\": [
        {\"name\": \"retention.ms\", \"value\": \"604800000\"},
        {\"name\": \"cleanup.policy\", \"value\": \"delete\"}
      ]
    }")

  if echo "${response}" | grep -q '"topic_name"'; then
    echo "  Created: ${topic}"
  elif echo "${response}" | grep -q 'already exists'; then
    echo "  Already exists: ${topic}"
  else
    echo "  Response: ${response}"
  fi
done

echo ""
echo "==> Confluent topic setup complete."
echo ""
echo "==> Confluent Cloud consumer group SASL config for application.yml:"
echo "    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}"
echo "    properties:"
echo "      security.protocol: SASL_SSL"
echo "      sasl.mechanism: PLAIN"
echo "      sasl.jaas.config: org.apache.kafka.common.security.plain.PlainLoginModule required username='${CONFLUENT_API_KEY}' password='${CONFLUENT_API_SECRET}';"
