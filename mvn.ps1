# CoreHTTP Portable Maven Wrapper
# Use this to run Maven without installing it on your system!

$MavenVersion = "3.9.6"
$MavenDir = Join-Path $PSScriptRoot ".maven"
$MavenBin = Join-Path $MavenDir "apache-maven-$MavenVersion\bin\mvn.cmd"
$MavenZip = Join-Path $MavenDir "maven.zip"

if (-not (Test-Path $MavenBin)) {
    Write-Host "Setting up portable Maven ($MavenVersion) in project folder..." -ForegroundColor Cyan
    if (-not (Test-Path $MavenDir)) { New-Item -ItemType Directory -Path $MavenDir | Out-Null }
    
    $Url = "https://archive.apache.org/dist/maven/maven-3/$MavenVersion/binaries/apache-maven-$MavenVersion-bin.zip"
    Write-Host "Downloading Maven... (this will only happen once)" -ForegroundColor Yellow
    Invoke-WebRequest -Uri $Url -OutFile $MavenZip
    
    Write-Host "Extracting..." -ForegroundColor Yellow
    Expand-Archive -Path $MavenZip -DestinationPath $MavenDir -Force
    Remove-Item $MavenZip
    Write-Host "Maven setup complete!" -ForegroundColor Green
}

# Pass all arguments to the actual maven command
& $MavenBin $args
