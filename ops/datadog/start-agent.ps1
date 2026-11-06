<#
.SYNOPSIS
  Starts the official Datadog Agent in Docker, tailing ../../logs/regression-lab.json.
  Reads DD_API_KEY / DD_SITE from the environment (or user-scope env vars). The key is passed
  via `-e NAME` (inherited), so it never appears on the command line.
#>
$ErrorActionPreference = "Stop"
$repo = Resolve-Path "$PSScriptRoot\..\.."
foreach ($n in "DD_API_KEY", "DD_SITE") {
    if (-not (Get-Item "env:$n" -ErrorAction SilentlyContinue)) {
        Set-Item "env:$n" ([Environment]::GetEnvironmentVariable($n, "User"))
    }
}
if (-not $env:DD_API_KEY) { throw "DD_API_KEY not set. Run scripts\setup-datadog-env.ps1" }
New-Item -ItemType Directory -Force "$repo\logs" | Out-Null

$ErrorActionPreference = "Continue"
docker rm -f dd-agent *> $null
docker run -d --name dd-agent `
    -e DD_API_KEY -e DD_SITE `
    -e DD_HOSTNAME=regression-lab-host `
    -e DD_LOGS_ENABLED=true `
    -e DD_APM_ENABLED=false -e DD_PROCESS_AGENT_ENABLED=false `
    -v "$repo\ops\datadog\conf.d\regression-lab.d:/conf.d/regression-lab.d:ro" `
    -v "$repo\logs:/lab-logs:ro" `
    gcr.io/datadoghq/agent:7
