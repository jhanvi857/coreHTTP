$ErrorActionPreference = "Stop"

$BaseUrl = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:8080" }
$ProjectRoot = Resolve-Path "$PSScriptRoot\.."
$ScriptPath = "$PSScriptRoot\k6-load-test.js"

if (-not (Get-Command k6 -ErrorAction SilentlyContinue)) {
    throw "k6 is required for load testing. Install from https://k6.io/docs/get-started/installation/."
}

Push-Location $ProjectRoot
try {
    Write-Host "Running k6 load test against $BaseUrl" -ForegroundColor Cyan
    k6 run --env BASE_URL=$BaseUrl $ScriptPath
}
finally {
    Pop-Location
}
