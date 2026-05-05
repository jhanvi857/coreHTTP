Set-Location -Path $PSScriptRoot\target

Write-Host "Re-packaging into a strict Maven Repository structure..." -ForegroundColor Cyan

# Create Maven directory structure
$m2Dir = "io/github/jhanvi857/nioflow-cli/1.3.0"
if (Test-Path "io") { Remove-Item -Recurse -Force "io" }
New-Item -ItemType Directory -Force -Path $m2Dir | Out-Null

# Move all files into the directory
$files = Get-ChildItem -File -Name | Where-Object { $_ -match "^nioflow-cli-1\.3\.0" -and $_ -notmatch "bundle\.zip" }

foreach ($f in $files) {
    Copy-Item $f -Destination "$m2Dir\$f"
}

Write-Host "Creating strict-bundle.zip..." -ForegroundColor Cyan
if (Test-Path strict-bundle.zip) { Remove-Item strict-bundle.zip }

# Zip the 'io' directory
& jar cMf strict-bundle.zip io

Write-Host "Done! Upload this new file: $PWD\strict-bundle.zip" -ForegroundColor Magenta
