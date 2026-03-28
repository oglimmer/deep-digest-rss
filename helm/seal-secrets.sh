#!/usr/bin/env bash
# Reads plain-text secrets from helm/secrets.env, creates a sealed secret,
# and writes it to the Helm chart template.
#
# Usage:
#   1. cp helm/secrets.env.example helm/secrets.env
#   2. Edit helm/secrets.env with real values
#   3. ./helm/seal-secrets.sh
#
# Requires: kubectl, kubeseal

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ENV_FILE="${SCRIPT_DIR}/secrets.env"
SEALED_FILE="${SCRIPT_DIR}/deep-digest-rss/templates/sealed-secret.yaml"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Error: ${ENV_FILE} not found."
  echo "Copy secrets.env.example to secrets.env and fill in your values:"
  echo "  cp ${SCRIPT_DIR}/secrets.env.example ${SCRIPT_DIR}/secrets.env"
  exit 1
fi

# Build --from-literal args from the env file
ARGS=()
while IFS='=' read -r key value; do
  # Skip comments and empty lines
  [[ -z "$key" || "$key" == \#* ]] && continue
  # Strip surrounding quotes from value
  value="${value#\"}"
  value="${value%\"}"
  value="${value#\'}"
  value="${value%\'}"
  ARGS+=("--from-literal=${key}=${value}")
done < "$ENV_FILE"

if [[ ${#ARGS[@]} -eq 0 ]]; then
  echo "Error: No secrets found in ${ENV_FILE}"
  exit 1
fi

echo "Sealing ${#ARGS[@]} secret keys..."

kubectl create secret generic news-secrets \
  --namespace default \
  --dry-run=client \
  --output yaml \
  "${ARGS[@]}" \
| kubeseal \
  --format yaml \
  --controller-name sealed-secrets-controller \
  --controller-namespace kube-system \
  > "$SEALED_FILE"

echo "Sealed secret written to ${SEALED_FILE}"
