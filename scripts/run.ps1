# Set strict error handling
$ErrorActionPreference = "Stop"

$ProjectRoot = Resolve-Path "$PSScriptRoot\.."
$MvnScript = "$ProjectRoot\mvn.ps1"

# Save the current directory and move to project root
Push-Location $ProjectRoot

try {
    Write-Host "Building project with Maven..." -ForegroundColor Cyan
    & $MvnScript clean compile

    if ($LASTEXITCODE -ne 0) {
        throw "Maven build failed."
    }

    # The target/classes contains our code, and we need the dependency JARs on the classpath.
    # Maven's exec:java is the cleanest way to run without managing a long classpath string manually.
    Write-Host "Starting CoreHTTP Server..." -ForegroundColor Green
    & $MvnScript exec:java -D"exec.mainClass"="com.jhanvi857.coreHTTP.server.HttpServer"
}
finally {
    # Always return to the original directory
    Pop-Location
}
