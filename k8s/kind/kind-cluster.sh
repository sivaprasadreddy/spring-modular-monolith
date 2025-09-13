#!/bin/bash

set -euo pipefail

parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
cd "$parent_path"

# Detect flags (don't consume positional args)
FORCE_HOSTPORTS=0
for arg in "$@"; do
    if [ "$arg" = "--force-hostports" ]; then
        FORCE_HOSTPORTS=1
    fi
done

function _fail() {
  echo "ERROR: $*" 1>&2
  exit 1
}

function create() {
    echo "📦 Initializing Kubernetes cluster..."
        # Determine target cluster name from config (fallback to 'kind')
        CLUSTER_NAME=$(awk '/^name:/{print $2; exit}' kind-config.yml 2>/dev/null || echo "kind")
        # Check for host port conflicts that would prevent hostPort mappings
        PORTS_TO_CHECK="80 443 30090 30091 30092"
        conflict=0
        if [ "$FORCE_HOSTPORTS" -eq 1 ]; then
            echo "Forcing hostPort mappings (override requested) - skipping host port checks"
        else
            for p in $PORTS_TO_CHECK; do
                if command -v ss >/dev/null 2>&1; then
                    if ss -ltn | awk '{print $4}' | grep -q ":$p$"; then
                        echo "Host port $p appears in use; will skip extra port mappings"
                        conflict=1
                        break
                    fi
                elif command -v lsof >/dev/null 2>&1; then
                    if lsof -iTCP -sTCP:LISTEN -P -n | grep -q ":$p"; then
                        echo "Host port $p appears in use; will skip extra port mappings"
                        conflict=1
                        break
                    fi
                fi
            done
        fi

            # If the cluster already exists, reuse it
            if kind get clusters | grep -q "^${CLUSTER_NAME}$"; then
                echo "Cluster '${CLUSTER_NAME}' already exists — reusing"
            else
                if [ "$conflict" -eq 1 ]; then
                    echo "Creating kind cluster named '${CLUSTER_NAME}' without host port mappings (conflict detected)"
                    kind create cluster --name "${CLUSTER_NAME}" || _fail "kind create cluster failed"
                else
                    kind create cluster --config kind-config.yml || _fail "kind create cluster failed"
                fi
            fi

    echo "\n-----------------------------------------------------\n"
    echo "🔌 Installing NGINX Ingress..."
    kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml || _fail "kubectl apply failed"

    echo "\n-----------------------------------------------------\n"
    echo "⌛ Waiting for NGINX Ingress to be ready..."
    sleep 10
    if ! kubectl wait --namespace ingress-nginx \
      --for=condition=ready pod \
      --selector=app.kubernetes.io/component=controller \
      --timeout=180s; then
        kubectl get pods -n ingress-nginx || true
        _fail "NGINX Ingress pods did not become ready within timeout"
    fi

    echo "\n"
    echo "⛵ Happy Sailing!"
}

function destroy() {
    echo "🏴‍☠️ Destroying Kubernetes cluster..."
    # Determine cluster name from config (fallback to 'kind')
    CLUSTER_NAME=$(awk '/^name:/{print $2; exit}' kind-config.yml 2>/dev/null || echo "kind")
    kind delete cluster --name "${CLUSTER_NAME}" || _fail "kind delete cluster failed"
}

function help() {
    echo "Usage: ./kind-cluster [--force-hostports] create|destroy"
    echo "  --force-hostports   Force using hostPort mappings even if host ports appear in use"
}

action="help"

# Handle the --force-hostports flag and action separately
if [[ "$#" != "0" ]]; then
    if [[ "$1" == "--force-hostports" ]]; then
        if [[ "$#" -gt 1 ]]; then
            action="${@:2}"  # Take all arguments after --force-hostports
        fi
    else
        action="$@"
    fi
fi

case "$action" in
    "create"|"destroy"|"help")
        $action
        ;;
    *)
        help
        ;;
esac
