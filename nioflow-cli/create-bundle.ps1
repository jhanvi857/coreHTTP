Set-Location -Path $PSScriptRoot

# Copy POM to target with correct naming
Copy-Item "pom.xml" -Destination "target/nioflow-cli-1.3.0.pom"
Set-Location -Path "target"

# Sign files using GPG
Write-Host "Signing artifacts... (A GPG prompt may appear for your passphrase)" -ForegroundColor Cyan
gpg -ab --yes nioflow-cli-1.3.0.jar
gpg -ab --yes nioflow-cli-1.3.0-sources.jar
gpg -ab --yes nioflow-cli-1.3.0-javadoc.jar
gpg -ab --yes nioflow-cli-1.3.0.pom

Write-Host "Generating MD5 and SHA1 checksums..." -ForegroundColor Cyan
$baseFiles = @(
    "nioflow-cli-1.3.0.jar",
    "nioflow-cli-1.3.0-sources.jar",
    "nioflow-cli-1.3.0-javadoc.jar",
    "nioflow-cli-1.3.0.pom"
)

foreach ($f in $baseFiles) {
    if (Test-Path $f) {
        $md5 = (Get-FileHash -Algorithm MD5 $f).Hash.ToLower()
        Set-Content -Path "$f.md5" -Value $md5 -NoNewline
        $sha1 = (Get-FileHash -Algorithm SHA1 $f).Hash.ToLower()
        Set-Content -Path "$f.sha1" -Value $sha1 -NoNewline
    }
}

Write-Host "Creating clean bundle.zip using the Java jar tool (guarantees no ./ prefix)..." -ForegroundColor Cyan
if (Test-Path bundle.zip) { Remove-Item bundle.zip }

# Collect all generated files
$allFiles = Get-ChildItem -File -Name | Where-Object { $_ -match "^nioflow-cli-1\.3\.0" -and $_ -notmatch "bundle\.zip" }

# Run jar command to create the zip
$argsArray = @("cMf", "bundle.zip") + $allFiles
& jar $argsArray

Write-Host "Done! Upload this file to Sonatype: $PWD\bundle.zip" -ForegroundColor Magenta
