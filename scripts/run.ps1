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

    Write-Host "Starting Task Planner App (Demo)..." -ForegroundColor Green
    & $MvnScript -pl task-planner-app exec:java -D"exec.mainClass"="io.github.jhanvi857.taskplanner.DemoApplication"
}
finally {
    # Always return to the original directory
    Pop-Location
}
