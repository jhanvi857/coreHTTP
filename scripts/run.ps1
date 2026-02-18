# Set strict error handling
$ErrorActionPreference = "Stop"

# Defining paths
$ScriptDir = $PSScriptRoot
$ProjectRoot = Resolve-Path "$ScriptDir\.."
$SrcDir = "$ProjectRoot\src\main\java"
$OutDir = "$ProjectRoot\out"
$MainClass = "com.jhanvi857.coreHTTP.server.HttpServer"

# Cleaning output directory
Write-Host "Cleaning output directory..." -ForegroundColor Cyan
if (Test-Path $OutDir) {
    Remove-Item -Recurse -Force $OutDir
}
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

# Compiling
Write-Host "Compiling source files..." -ForegroundColor Cyan
# Getting all java files recursively
$JavaFiles = Get-ChildItem -Path $SrcDir -Recurse -Filter *.java
$JavaFilePaths = $JavaFiles | Select-Object -ExpandProperty FullName

# Saving paths to a temporary file to avoid command line length issues
$SourceListFile = "$ProjectRoot\sources.txt"
$JavaFilePaths | Out-File -FilePath $SourceListFile -Encoding ASCII

# Running javac
javac -d $OutDir -sourcepath $SrcDir "@$SourceListFile"
Remove-Item $SourceListFile

# Checking for compilation errors
if ($LASTEXITCODE -ne 0) {
    Write-Error "Compilation failed."
}

# Running
Write-Host "Starting Server..." -ForegroundColor Green
java -cp $OutDir $MainClass
