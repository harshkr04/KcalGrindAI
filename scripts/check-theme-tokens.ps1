# Strict lint check: Fails if any Color(0x...) literals exist outside com/kcalgrindai/app/core/designsystem
$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$appDir = Resolve-Path "$scriptDir\..\app\src\main\java"

$violations = @()

Get-ChildItem -Path $appDir -Recurse -Filter "*.kt" | Where-Object {
    $_.FullName -notmatch "core[\\/]designsystem"
} | ForEach-Object {
    $filePath = $_.FullName
    $relPath = $filePath.Replace("$appDir\", "")
    $lineNum = 1
    Get-Content $filePath | ForEach-Object {
        if ($_ -match "Color\(0x[0-9a-fA-F]+") {
            $violations += "$($relPath):$($lineNum): $($_.Trim())"
        }
        $lineNum++
    }
}

if ($violations.Count -gt 0) {
    Write-Host "FAILED: Found $($violations.Count) hardcoded Color(0x...) outside theme package:" -ForegroundColor Red
    $violations | ForEach-Object { Write-Host "  $_" -ForegroundColor Yellow }
    exit 1
} else {
    Write-Host "SUCCESS: Zero hardcoded Color(0x...) found outside theme package." -ForegroundColor Green
    exit 0
}
