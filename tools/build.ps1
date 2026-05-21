# Build script: compiles sources and creates a JAR including the res/ folder
$projRoot = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $projRoot

Write-Host "Generating tools/sources.txt..."
Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object {
    $_.FullName.Substring((Get-Location).Path.Length + 1).Replace('\\', '/')
} | Set-Content -Path tools/sources.txt -Encoding UTF8

if (Test-Path bin) { Remove-Item -Recurse -Force bin }
New-Item -ItemType Directory -Path bin | Out-Null

Write-Host "Compiling Java sources..."
cmd /c "javac -d bin @tools/sources.txt"
if ($LASTEXITCODE -ne 0) {
    Write-Error "Compilation failed (javac exit code $LASTEXITCODE)."
    exit $LASTEXITCODE
}

New-Item -ItemType Directory -Force -Path ..\Installer\Jar | Out-Null

Write-Host "Creating JAR (including res/)..."
jar cfm ..\Installer\Jar\HawakKoAngBit.jar `
tools\manifest.txt `
-C bin . `
-C . res
if ($LASTEXITCODE -ne 0) {
    Write-Error "Jar creation failed (exit $LASTEXITCODE)."
    exit $LASTEXITCODE
}

Write-Host "Built ..\Installer\Jar\HawakKoAngBit.jar"
Write-Host "Checking for res/ entries inside the JAR:"
try {
    $entries = & jar tf ..\Installer\Jar\HawakKoAngBit.jar 2>$null
    if ($entries) {
        $resLines = $entries | Select-String '^res/' -SimpleMatch
        if ($resLines) {
            $resLines | ForEach-Object { Write-Host $_.Line }
        } else {
            Write-Host "No res/ entries found in the JAR."
        }
    } else {
        Write-Host "Unable to list jar entries (jar tool may not be in PATH)."
    }
} catch {
    Write-Host "Failed to inspect JAR contents: $_"
}
