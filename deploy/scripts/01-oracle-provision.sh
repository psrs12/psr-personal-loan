#!/usr/bin/env bash
# Provision Oracle Cloud A1 ARM VM and install dependencies
# Run this ONCE after creating the VM in the Oracle Cloud Console
#
# Prerequisites:
#   1. Create an Always Free A1 instance in Oracle Cloud Console
#      Shape: VM.Standard.A1.Flex — 4 OCPUs, 24 GB RAM
#      OS: Oracle Linux 8 or Ubuntu 22.04
#   2. Download the SSH key and set ORACLE_SSH_KEY_PATH in deploy/.env
#   3. Source deploy/.env before running this script

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/../.env"

echo "==> Connecting to Oracle VM at ${ORACLE_VM_IP}"

ssh -i "${ORACLE_SSH_KEY_PATH}" -o StrictHostKeyChecking=no "${ORACLE_VM_USER}@${ORACLE_VM_IP}" bash <<'REMOTE'
set -euo pipefail

echo "--- Updating system ---"
sudo dnf update -y 2>/dev/null || sudo apt-get update -y

# --- Install Docker ---
if ! command -v docker &>/dev/null; then
  echo "--- Installing Docker ---"
  # Oracle Linux 8
  if command -v dnf &>/dev/null; then
    sudo dnf config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
    sudo dnf install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
  else
    # Ubuntu
    curl -fsSL https://get.docker.com | sudo sh
  fi
  sudo systemctl enable --now docker
  sudo usermod -aG docker "${USER}"
fi

# --- Install Docker Compose standalone (for env-file support) ---
if ! command -v docker-compose &>/dev/null; then
  echo "--- Installing Docker Compose ---"
  COMPOSE_VERSION=$(curl -s https://api.github.com/repos/docker/compose/releases/latest | grep '"tag_name"' | cut -d'"' -f4)
  sudo curl -L "https://github.com/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-linux-aarch64" \
    -o /usr/local/bin/docker-compose
  sudo chmod +x /usr/local/bin/docker-compose
fi

# --- Install Loki Docker driver (log forwarding to Grafana Cloud) ---
if ! docker plugin ls | grep -q loki; then
  echo "--- Installing Loki Docker log driver ---"
  docker plugin install grafana/loki-docker-driver:2.9.2 --alias loki --grant-all-permissions || true
fi

# --- Open firewall ports ---
echo "--- Configuring firewall ---"
if command -v firewall-cmd &>/dev/null; then
  sudo firewall-cmd --permanent --add-port=8080-8090/tcp
  sudo firewall-cmd --permanent --add-port=80/tcp
  sudo firewall-cmd --permanent --add-port=443/tcp
  sudo firewall-cmd --reload
fi

# --- Create app directory ---
sudo mkdir -p /opt/personal-loan
sudo chown "${USER}:${USER}" /opt/personal-loan

echo "==> Oracle VM provisioning complete."
echo "    Re-login or run: newgrp docker"
REMOTE

echo "==> Provisioning complete. VM is ready at ${ORACLE_VM_IP}"
