<#
.SYNOPSIS
  Stores Datadog credentials as *user-scope* environment variables (never in the repo).
  Keys are read with hidden input. Run it interactively in your own terminal.
#>
$site = Read-Host "Datadog site (ex.: datadoghq.com, us5.datadoghq.com, datadoghq.eu) [datadoghq.com]"
if ([string]::IsNullOrWhiteSpace($site)) { $site = "datadoghq.com" }

function Read-Secret($prompt) {
    $s = Read-Host $prompt -AsSecureString
    $b = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($s)
    try { [Runtime.InteropServices.Marshal]::PtrToStringBSTR($b) } finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($b) }
}

$api = Read-Secret "DD_API_KEY (Organization Settings > API Keys)"
$app = Read-Secret "DD_APP_KEY (Organization Settings > Application Keys, scope logs_read_data)"

[Environment]::SetEnvironmentVariable("DD_SITE", $site.Trim(), "User")
[Environment]::SetEnvironmentVariable("DD_API_KEY", $api.Trim(), "User")
[Environment]::SetEnvironmentVariable("DD_APP_KEY", $app.Trim(), "User")
Write-Output "OK: DD_SITE=$($site.Trim()), DD_API_KEY (len $($api.Trim().Length)), DD_APP_KEY (len $($app.Trim().Length)) saved for user $env:USERNAME."
