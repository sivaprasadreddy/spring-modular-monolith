#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
cd "$SCRIPT_DIR/.." || exit 1

fail() { echo "ERROR: $*" >&2; exit 1; }
info() { echo "INFO: $*"; }

command -v kubectl >/dev/null 2>&1 || fail "kubectl not found in PATH"

info "kubectl version:"; kubectl version --client --short || true

# cluster name from kind-config.yml if present
if [[ -f k8s/kind/kind-config.yml ]]; then
  CLUSTER_NAME=$(awk '/^\s*name:\s*/{print $2; exit}' k8s/kind/kind-config.yml || true)
else
  CLUSTER_NAME="kind"
fi

info "Cluster name: ${CLUSTER_NAME:-kind}"

info "Nodes:"
kubectl get nodes -o wide || true

info "Pods (all namespaces):"
kubectl get pods -A -o wide || true

info "Pods not in Running phase (if any):"
kubectl get pods -A --field-selector=status.phase!=Running || echo "(none)"

info "Pods with restart count > 0 (if any):"
kubectl get pods -A --no-headers | awk '$4+0>0 {print $0}' || echo "(none)"

# Collect list of suspect pods (not Running or restart >0)
mapfile -t suspect < <(kubectl get pods -A --no-headers | awk '$4+0>0 || $4 ~ /0\// || $3!~/Running/ {print $1"/"$2}' | sort -u || true)

if [[ ${#suspect[@]} -eq 0 ]]; then
  info "No suspect pods detected."
  exit 0
fi

info "Inspecting ${#suspect[@]} suspect pods for diagnostics..."
for p in "${suspect[@]}"; do
  ns=${p%%/*}
  name=${p#*/}
  echo
  echo "--- Pod: ${ns}/${name} ---"
  kubectl describe pod "$name" -n "$ns" || true
  echo
  echo "--- Logs (current) for ${name} ---"
  kubectl logs "$name" -n "$ns" --all-containers || true
  echo
  echo "--- Logs (previous) for ${name} (if any) ---"
  kubectl logs "$name" -n "$ns" --all-containers --previous || echo "(no previous logs)"
done

exit 0
