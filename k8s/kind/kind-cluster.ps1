<#
  Cross-platform helper for kind cluster create/destroy on Windows (PowerShell)
  - Detects host port conflicts and falls back to creating a cluster without hostPort mappings
  - Parses cluster name from `kind-config.yml` (fallback 'kind')
  - Reuses existing cluster if present
  - Installs ingress-nginx and waits for controller readiness
#>


$ErrorActionPreference = 'Stop'

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $ScriptDir

# Parse flags from automatic $args
$ForceHostPorts = $false
foreach ($a in $args) {
    if ($a -eq '--force-hostports') { $ForceHostPorts = $true }
}

function Fail([string]$msg) {
    Write-Host "ERROR: $msg" -ForegroundColor Red
    exit 1
}

function Get-LastExit() {
    $v = Get-Variable -Name LASTEXITCODE -Scope Global -ErrorAction SilentlyContinue
    if ($null -eq $v) { return $null }
    return $v.Value
}

function Check-LastExit([string]$msg) {
    $c = Get-LastExit
    if ($null -eq $c) { Fail "$msg (no exit code)" }
    if ($c -ne 0) { Fail "$msg (exit code $c)" }
}

function Read-ClusterName() {
    $name = 'kind'
    if (Test-Path 'kind-config.yml') {
        try {
            $lines = Get-Content 'kind-config.yml' -ErrorAction SilentlyContinue
            foreach ($l in $lines) {
                if ($l -match '^\s*name:\s*(\S+)') { $name = $Matches[1]; break }
            }
        } catch { }
    }
    return $name
}

function Test-Port([int]$port) {
    # Use Test-NetConnection when available, otherwise fallback to netstat parse
    if (Get-Command Test-NetConnection -ErrorAction SilentlyContinue) {
        $r = Test-NetConnection -ComputerName '127.0.0.1' -Port $port -WarningAction SilentlyContinue
        return ($r -and $r.TcpTestSucceeded)
    } else {
        $net = netstat -an 2>$null
        return ($net -and ($net -match "LISTENING.*:$port\b"))
    }
}

function Create-Cluster {
    Write-Host 'Initializing Kubernetes cluster...'
    $portsToCheck = 80,443,30090,30091,30092
    $conflict = $false
    if ($ForceHostPorts) {
        Write-Host 'Forcing hostPort mappings (override requested) - skipping host port checks'
    } else {
        foreach ($p in $portsToCheck) {
            try {
                if (Test-Port -port $p) {
                    Write-Host "Host port $p appears in use; will skip hostPort mappings"
                    $conflict = $true
                    break
                }
            } catch { }
        }
    }

    $clusterName = Read-ClusterName
    Write-Host ("DEBUG: resolved clusterName='{0}'" -f $clusterName)

    # Show kind location for debugging
    $kindCmd = Get-Command kind -ErrorAction SilentlyContinue
    if ($null -eq $kindCmd) { Fail 'kind binary not found in PATH' }
    Write-Host "DEBUG: kind: $($kindCmd.Path)"

    # Print current clusters (debug)
    try {
        & kind get clusters 2>$null | ForEach-Object { Write-Host "KIND: $_" }
    } catch { }

    $exists = & kind get clusters 2>$null | Select-String "^$clusterName$" -Quiet
    if ($exists) {
        Write-Host ("Cluster '{0}' exists - reusing" -f $clusterName)
    } else {
        if ($conflict) {
            Write-Host ("Creating kind cluster named '{0}' without host port mappings (conflict detected)" -f $clusterName)
            kind create cluster --name $clusterName
            Check-LastExit 'kind create cluster failed'
        } else {
            if (-not (Test-Path 'kind-config.yml')) { Fail 'kind-config.yml not found' }
            kind create cluster --config kind-config.yml
            Check-LastExit 'kind create cluster failed'
        }
    }

    Write-Host 'Installing NGINX Ingress controller (provider: kind)'
    kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml
    Check-LastExit 'kubectl apply failed'

    Write-Host 'Waiting for NGINX Ingress controller to be ready (timeout 180s)'
    Start-Sleep -Seconds 5
    kubectl wait --namespace ingress-nginx --for=condition=ready pod --selector=app.kubernetes.io/component=controller --timeout=180s
    $code = Get-LastExit
    if ($null -eq $code -or $code -ne 0) {
        Write-Host '--- Ingress pod status ---'
        kubectl get pods -n ingress-nginx -o wide | Out-String | Write-Host
        Write-Host '--- Pod describe for troubleshooting ---'
        $pod = kubectl get pods -n ingress-nginx -o name | Select-Object -First 1
        if ($pod) { kubectl describe $pod -n ingress-nginx | Out-String | Write-Host }
        Fail 'NGINX Ingress pods did not become ready within timeout'
    }

    Write-Host 'Kind cluster ready.'
}

function Destroy-Cluster {
    Write-Host 'Destroying Kubernetes cluster...'
    $clusterName = Read-ClusterName
    kind delete cluster --name $clusterName
    Check-LastExit 'kind delete cluster failed'
}

function Show-Help {
    Write-Host 'Usage: .\kind-cluster.ps1 [--force-hostports] create|destroy'
    Write-Host '  --force-hostports   Force using hostPort mappings even if host ports appear in use'
}

$action = 'help'
foreach ($a in $args) {
    if ($a -eq 'create' -or $a -eq 'destroy') { $action = $a; break }
}

switch ($action.ToLower()) {
    'create' { Create-Cluster }
    'destroy' { Destroy-Cluster }
    default { Show-Help }
}
