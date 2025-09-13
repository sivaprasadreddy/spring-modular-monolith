param()

$ErrorActionPreference = 'Stop'

function Info($m) { Write-Host "INFO: $m" }
function Fail($m) { Write-Host "ERROR: $m" -ForegroundColor Red; exit 1 }

if (-not (Get-Command kubectl -ErrorAction SilentlyContinue)) { Fail 'kubectl not found in PATH' }

Info "kubectl version:"; kubectl version --client 2>&1 | Write-Host

$config = Join-Path -Path $PSScriptRoot -ChildPath 'kind/kind-config.yml'
if (Test-Path $config) {
    try {
        $lines = Get-Content $config -ErrorAction SilentlyContinue
        foreach ($l in $lines) { if ($l -match '^\s*name:\s*(\S+)') { $clusterName=$Matches[1]; break } }
    } catch { $clusterName = 'kind' }
} else { $clusterName = 'kind' }

Info "Cluster name: $clusterName"

Info "Nodes:"; kubectl get nodes -o wide | Write-Host
Info "Pods (all namespaces):"; kubectl get pods -A -o wide | Write-Host

# Use JSON to reliably inspect pod states and container restart counts
try {
    $podsJson = kubectl get pods -A -o json | ConvertFrom-Json
} catch {
    Fail "Failed to get pods as JSON: $_"
}

$suspect = @()
foreach ($item in $podsJson.items) {
    $ns = $item.metadata.namespace
    $name = $item.metadata.name
    $phase = $item.status.phase
    $restartCount = 0
    if ($null -ne $item.status.containerStatuses) {
        foreach ($cs in $item.status.containerStatuses) { $restartCount += ($cs.restartCount -as [int]) }
    }
    if ($phase -ne 'Running' -or $restartCount -gt 0) {
        $suspect += @{ ns = $ns; name = $name; phase = $phase; restarts = $restartCount }
    }
}

if ($suspect.Count -eq 0) { Info 'No suspect pods detected.'; exit 0 }

Info "Inspecting $($suspect.Count) suspect pods for diagnostics..."
foreach ($p in $suspect) {
    $ns = $p.ns; $name = $p.name
    Write-Host "`n--- Pod: $ns/$name (phase=$($p.phase), restarts=$($p.restarts)) ---"
    kubectl describe pod $name -n $ns | Write-Host
    Write-Host "`n--- Logs (current) for $name ---"
    kubectl logs $name -n $ns --all-containers | Write-Host
    Write-Host "`n--- Logs (previous) for $name (if any) ---"
    try { kubectl logs $name -n $ns --all-containers --previous | Write-Host } catch { Write-Host '(no previous logs)' }
}

exit 0
