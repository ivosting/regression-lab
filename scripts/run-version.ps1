<#
.SYNOPSIS
  Builds the current HEAD, starts it as a "deploy" and records the deploy in deploys/deploys.jsonl.
.EXAMPLE
  .\scripts\run-version.ps1 -Port 8080
#>
param(
    [int]$Port = 8080,
    [string]$Env = "lab"
)
$ErrorActionPreference = "Stop"
$repo = Split-Path $PSScriptRoot -Parent
Set-Location $repo

[Environment]::CurrentDirectory = $repo
# The project targets Java 21: prefer a JDK 21 install over whatever JAVA_HOME points to.
$jdk = Get-ChildItem 'C:\Program Files\Microsoft', 'C:\Program Files\Java', 'C:\Program Files\Eclipse Adoptium' -Directory -ErrorAction SilentlyContinue |
    Where-Object Name -like 'jdk-21*' | Select-Object -First 1 -ExpandProperty FullName
if ($jdk) { $env:JAVA_HOME = $jdk }

$sha = (git rev-parse --short=12 HEAD).Trim()
New-Item -ItemType Directory -Force "$repo\build" | Out-Null
$ErrorActionPreference = "Continue"
& "$repo\gradlew.bat" -p "$repo" bootJar -q --no-daemon *> "$repo\build\bootjar.log"
$ErrorActionPreference = "Stop"
if ($LASTEXITCODE -ne 0) { throw "bootJar failed, see build\bootjar.log" }
$jar = Get-ChildItem "$repo\build\libs" -Filter "*.jar" | Where-Object Name -notlike "*plain*" | Select-Object -First 1

New-Item -ItemType Directory -Force "$repo\logs", "$repo\deploys" | Out-Null
$env:APP_VERSION = $sha
$env:APP_ENV = $Env
$env:PORT = "$Port"
$env:LOG_FILE = "$repo\logs\regression-lab.json"

$proc = Start-Process -FilePath "$env:JAVA_HOME\bin\java.exe" -ArgumentList "-jar", "`"$($jar.FullName)`"" `
    -RedirectStandardOutput "$repo\logs\stdout.log" -RedirectStandardError "$repo\logs\stderr.log" `
    -WindowStyle Hidden -PassThru

$deadline = (Get-Date).AddSeconds(60)
do {
    Start-Sleep -Milliseconds 500
    try { $ok = (Invoke-RestMethod "http://localhost:$Port/actuator/health").status -eq "UP" } catch { $ok = $false }
} until ($ok -or (Get-Date) -gt $deadline)
if (-not $ok) { Stop-Process -Id $proc.Id -Force; throw "app did not become healthy" }

$record = [ordered]@{
    event      = "deploy"
    service    = "regression-lab"
    env        = $Env
    version    = $sha
    commit     = (git rev-parse HEAD).Trim()
    subject    = (git log -1 --format=%s).Trim()
    started_at = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
    pid        = $proc.Id
}
($record | ConvertTo-Json -Compress) | Add-Content "$repo\deploys\deploys.jsonl"
Write-Output "DEPLOYED version=$sha pid=$($proc.Id) port=$Port"
