#!/usr/bin/env bash
# Create the Oracle Cloud A1 ARM VM using OCI CLI + your API key credentials
# This script:
#   1. Generates a local SSH key pair for VM access
#   2. Creates the compute instance via OCI API
#   3. Waits for it to be RUNNING
#   4. Prints the public IP to add to deploy/.env
#
# Prerequisites:
#   Install OCI CLI:  brew install oci-cli  OR  pip install oci-cli
#   Then run:         oci setup config       (paste your API key details when prompted)
#
# Your OCI credentials needed during `oci setup config`:
#   - Tenancy OCID      : OCI Console > Profile > Tenancy
#   - User OCID         : OCI Console > Profile > User Settings > User Information
#   - Region            : e.g. us-ashburn-1, uk-london-1, ap-sydney-1
#   - API Key fingerprint + private key path : from your API key download

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/../.env"

# ---- Configuration — fill these in ----
OCI_COMPARTMENT_ID="${OCI_COMPARTMENT_ID:?Set OCI_COMPARTMENT_ID in deploy/.env}"
OCI_AVAILABILITY_DOMAIN="${OCI_AVAILABILITY_DOMAIN:?Set OCI_AVAILABILITY_DOMAIN in deploy/.env}"
# Example: "FaFa:US-ASHBURN-AD-1"  — find yours with:
#   oci iam availability-domain list --compartment-id <tenancy-ocid>

VM_NAME="personal-loan-vm"
VM_SHAPE="VM.Standard.A1.Flex"
VM_OCPUS=4
VM_MEMORY_GB=24
# Always Free A1: max 4 OCPUs + 24 GB across ALL instances in your tenancy

# Oracle Linux 8 ARM image — find the latest with:
#   oci compute image list --compartment-id <compartment-id> --operating-system "Oracle Linux" \
#     --operating-system-version "8" --shape VM.Standard.A1.Flex --query "data[0].id"
OCI_IMAGE_ID="${OCI_IMAGE_ID:?Set OCI_IMAGE_ID in deploy/.env}"

# VCN subnet — find with:
#   oci network subnet list --compartment-id <compartment-id>
OCI_SUBNET_ID="${OCI_SUBNET_ID:?Set OCI_SUBNET_ID in deploy/.env}"
# ----------------------------------------

SSH_KEY_PATH="${ORACLE_SSH_KEY_PATH:-${HOME}/.ssh/oracle_personal_loan}"

# Step 1 — Generate SSH key pair if it doesn't exist
if [[ ! -f "${SSH_KEY_PATH}" ]]; then
  echo "==> Generating SSH key pair at ${SSH_KEY_PATH}"
  ssh-keygen -t ed25519 -f "${SSH_KEY_PATH}" -N "" -C "personal-loan-oracle-vm"
  echo "    Private key: ${SSH_KEY_PATH}"
  echo "    Public key:  ${SSH_KEY_PATH}.pub"
else
  echo "==> SSH key already exists at ${SSH_KEY_PATH}"
fi

SSH_PUBLIC_KEY=$(cat "${SSH_KEY_PATH}.pub")

# Step 2 — Create the VM instance
echo ""
echo "==> Creating Oracle Cloud VM: ${VM_NAME}"
echo "    Shape: ${VM_SHAPE} (${VM_OCPUS} OCPUs, ${VM_MEMORY_GB} GB RAM)"

instance_json=$(oci compute instance launch \
  --compartment-id "${OCI_COMPARTMENT_ID}" \
  --availability-domain "${OCI_AVAILABILITY_DOMAIN}" \
  --display-name "${VM_NAME}" \
  --shape "${VM_SHAPE}" \
  --shape-config "{\"ocpus\": ${VM_OCPUS}, \"memoryInGBs\": ${VM_MEMORY_GB}}" \
  --image-id "${OCI_IMAGE_ID}" \
  --subnet-id "${OCI_SUBNET_ID}" \
  --assign-public-ip true \
  --ssh-authorized-keys-file "${SSH_KEY_PATH}.pub" \
  --wait-for-state RUNNING \
  --max-wait-seconds 300)

INSTANCE_ID=$(echo "${instance_json}" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])")
echo "    Instance ID: ${INSTANCE_ID}"

# Step 3 — Get the public IP
echo ""
echo "==> Fetching public IP..."
sleep 10  # brief pause for IP assignment

PUBLIC_IP=$(oci compute instance list-vnics \
  --instance-id "${INSTANCE_ID}" \
  --query "data[0].\"public-ip\"" \
  --raw-output)

echo ""
echo "==> VM is RUNNING"
echo ""
echo "    Public IP: ${PUBLIC_IP}"
echo ""
echo "==> Add these to deploy/.env:"
echo "    ORACLE_VM_IP=${PUBLIC_IP}"
echo "    ORACLE_SSH_KEY_PATH=${SSH_KEY_PATH}"
echo "    OCI_INSTANCE_ID=${INSTANCE_ID}"
echo ""
echo "==> Then run: ./deploy/scripts/01-oracle-provision.sh"
