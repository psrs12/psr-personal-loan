# Deployment Guide — Personal Loan Platform

**Stack**: Oracle Cloud A1 ARM (compute) · Neon (PostgreSQL) · Confluent Cloud (Kafka) · Cloudflare Pages (frontends) · Grafana Cloud (observability)

**Total cost**: $0 (all free tiers)

---

## Prerequisites

| Tool | Purpose | Install |
|------|---------|---------|
| Docker + Docker Buildx | Build ARM images | `brew install docker` |
| `wrangler` | Deploy to Cloudflare Pages | `npm install -g wrangler` |
| `curl`, `jq`, `python3` | API calls in scripts | bundled with macOS |

---

## One-Time Setup

### Step 1 — Copy and fill .env

```bash
cp deploy/.env.template deploy/.env
# Edit deploy/.env — fill in OCI credentials before Step 2
```

### Step 2 — Install and configure OCI CLI

OCI CLI uses your **API key** (not SSH) to call Oracle Cloud APIs.

```bash
# Install
brew install oci-cli        # macOS
# or: pip install oci-cli   # any platform

# Configure — paste your OCI details when prompted:
#   Tenancy OCID  : OCI Console > Profile > Tenancy
#   User OCID     : OCI Console > Profile > User Settings > User Information
#   Region        : e.g. us-ashburn-1
#   API key path  : path to your downloaded .pem private key
oci setup config

# Verify
oci iam region list
```

Then fill in the OCI values in `deploy/.env`:

```bash
# Find your availability domain
oci iam availability-domain list --compartment-id <tenancy-ocid>

# Find Oracle Linux 8 ARM image ID
oci compute image list \
  --compartment-id <tenancy-ocid> \
  --operating-system "Oracle Linux" \
  --operating-system-version "8" \
  --shape VM.Standard.A1.Flex \
  --query "data[0].id" --raw-output

# Find your subnet ID (create a VCN first if you have none)
oci network subnet list --compartment-id <tenancy-ocid> --query "data[0].id" --raw-output
```

### Step 3 — Create the Oracle Cloud VM

This script generates an SSH key pair and creates the A1 ARM instance via OCI CLI. You do **not** need to create an SSH key manually — the script does it.

```bash
chmod +x deploy/scripts/*.sh
./deploy/scripts/00-create-vm.sh
```

The script prints the VM public IP. Add it to `deploy/.env`:
```
ORACLE_VM_IP=<printed IP>
ORACLE_SSH_KEY_PATH=~/.ssh/oracle_personal_loan   # generated automatically
```

### Step 4 — Provision Docker on the VM

```bash
./deploy/scripts/01-oracle-provision.sh
```

### Step 5 — Create Neon databases

1. Sign up at [neon.tech](https://neon.tech)
2. Create a project, copy the **Project ID** and **API key** into `.env`
3. Run:

```bash
./deploy/scripts/02-neon-setup.sh
```

4. Copy the connection strings into `deploy/.env` as instructed

### Step 4 — Create Confluent Cloud Kafka cluster

1. Sign up at [confluent.cloud](https://confluent.cloud)
2. Create a **Basic** cluster (free tier — pick any region)
3. Create an **API key** with cluster-level access
4. Copy `KAFKA_BOOTSTRAP_SERVERS`, `CONFLUENT_API_KEY`, `CONFLUENT_API_SECRET`, `CONFLUENT_CLUSTER_ID` into `.env`
5. Update the REST endpoint URL inside `03-confluent-setup.sh`, then run:

```bash
./deploy/scripts/03-confluent-setup.sh
```

### Step 5 — Check actuator/prometheus dependencies

```bash
./deploy/scripts/08-add-actuator-prometheus.sh
```

Add any missing dependencies to the relevant `pom.xml` files as shown.

### Step 6 — Set up Grafana Cloud

1. Sign up at [grafana.com](https://grafana.com/auth/sign-up/create-user) (free tier)
2. Copy the Prometheus push URL, Loki URL, instance ID, and API key into `.env`
3. Update `GRAFANA_ORG_SLUG` in `07-grafana-setup.sh`, then run:

```bash
./deploy/scripts/07-grafana-setup.sh
```

4. Import the dashboard: Grafana Cloud UI → Dashboards → Import → upload `deploy/grafana/dashboard.json`

### Step 7 — Authenticate Cloudflare

```bash
wrangler login
```

---

## Deployments

### Deploy backend (every release)

```bash
# Build ARM images and push to Oracle Container Registry (uses your Auth Token)
./deploy/scripts/04-build-push.sh --tag v1.0.0

# Or push to Docker Hub instead
./deploy/scripts/04-build-push.sh --tag v1.0.0 --registry dockerhub

# SSH deploy to Oracle VM
IMAGE_TAG=v1.0.0 ./deploy/scripts/05-deploy-backend.sh --tag v1.0.0
```

### Deploy frontends (every release)

```bash
./deploy/scripts/06-deploy-frontends.sh
```

---

## Local Development

Run the full stack locally with embedded PostgreSQL and Kafka:

```bash
# Build images locally
docker compose -f deploy/docker-compose.local.yml build

# Start everything
docker compose -f deploy/docker-compose.local.yml up -d

# Tail logs
docker compose -f deploy/docker-compose.local.yml logs -f
```

---

## Service Ports

| Service | Port | Health endpoint |
|---------|------|----------------|
| application-management-service | 8081 | `/api/v1/application-management/actuator/health` |
| pricing-orchestration-service | 8082 | `/actuator/health` |
| document-service | 8084 | `/actuator/health` |
| offer-acceptance-service | 8085 | `/actuator/health` |

---

## Kafka Topics

| Topic | Consumers |
|-------|----------|
| `application-events` | pricing-orchestration-service, compliance-orchestration-service |
| `consent-events` | application-management-service |
| `pricing-events` | offer-acceptance-service, document-service |
| `offer-acceptance-events` | application-management-service |
| `document-events` | application-management-service |

---

## Troubleshooting

**Services not starting** — check logs on Oracle VM:
```bash
ssh -i ~/.ssh/oracle_cloud_key opc@<VM_IP> \
  "cd /opt/personal-loan && docker compose -f docker-compose.prod.yml logs --tail=50"
```

**Kafka connection refused** — verify SASL config. Confluent Cloud requires `SASL_SSL`. Check that `SPRING_KAFKA_PROPERTIES_SASL_JAAS_CONFIG` is set correctly in `.env`.

**Neon connection timeout** — Neon auto-suspends after inactivity on the free tier. The first request after a cold start may take 2–5 seconds. This is expected behaviour.

**Port conflict** — `pricing-orchestration-service` application.yml currently declares port `8085` but CLAUDE.md specifies `8082`. The `docker-compose.prod.yml` overrides with `SERVER_PORT=8082`. If running outside Docker, set `SERVER_PORT=8082` in the process environment.

**Out of memory on Oracle VM** — reduce JVM heap per service by setting `JAVA_TOOL_OPTIONS=-Xmx384m` in the compose environment block for the offending service.
