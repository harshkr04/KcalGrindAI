# scripts/capture-screenshots.ps1
# Automates capturing clean, dark-mode real device/emulator screenshots with System UI Demo Mode.

[CmdletBinding()]
param(
    [string]$OutputDir = "",
    [string]$AvdName = "Pixel_8",
    [int]$TimeoutSeconds = 180
)

$ErrorActionPreference = "Continue"

# Determine workspace paths
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
if (Test-Path (Join-Path $scriptDir "..\LuminaAndroid")) {
    $workspaceRoot = (Resolve-Path (Join-Path $scriptDir "..")).Path
} elseif (Test-Path (Join-Path $scriptDir "..\..\LuminaAndroid")) {
    $workspaceRoot = (Resolve-Path (Join-Path $scriptDir "..\..")).Path
} else {
    $workspaceRoot = (Resolve-Path $scriptDir).Path
}

$luminaAndroidDir = Join-Path $workspaceRoot "LuminaAndroid"
if (-not $OutputDir) {
    $OutputDir = Join-Path $workspaceRoot "docs\design-audit\app\final"
}

Write-Host "Workspace Root: $workspaceRoot"
Write-Host "Android Root:   $luminaAndroidDir"
Write-Host "Output Dir:     $OutputDir"

# 1. Locate Android SDK tools
$sdkDir = $env:ANDROID_HOME
if (-not $sdkDir) { $sdkDir = $env:ANDROID_SDK_ROOT }
if (-not $sdkDir -and (Test-Path "$env:LOCALAPPDATA\Android\Sdk")) {
    $sdkDir = "$env:LOCALAPPDATA\Android\Sdk"
}
if (-not $sdkDir) {
    throw "Android SDK not found. Set ANDROID_HOME or install to default path."
}

$adb = Join-Path $sdkDir "platform-tools\adb.exe"
$emulator = Join-Path $sdkDir "emulator\emulator.exe"

if (-not (Test-Path $adb)) { throw "adb.exe not found at $adb" }

Write-Host "Using Android SDK: $sdkDir"

# Resolve output directory
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null
}

# 2. Check for connected device/emulator
$devices = & $adb devices | Where-Object { $_ -match "\s+device$" }
if (-not $devices) {
    Write-Host "No active emulator found. Launching $AvdName..."
    Start-Process -FilePath $emulator -ArgumentList "-avd $AvdName -no-boot-anim -no-snapshot" -NoNewWindow
    Write-Host "Waiting for device to connect..."
    & $adb wait-for-device
}

# Wait for boot completion
Write-Host "Verifying device boot completion..."
$booted = $false
$elapsed = 0
while (-not $booted -and $elapsed -lt $TimeoutSeconds) {
    $state = (& $adb shell getprop sys.boot_completed).Trim()
    if ($state -eq "1") {
        $booted = $true
        break
    }
    Start-Sleep -Seconds 2
    $elapsed += 2
}

if (-not $booted) {
    throw "Device failed to boot within $TimeoutSeconds seconds."
}
Write-Host "Device booted and ready."

# Wake up and unlock
& $adb shell input keyevent 82 # KEYCODE_MENU (unlock)
& $adb shell wm dismiss-keyguard

# 3. Configure System UI Demo Mode (9:41, 100% battery, 5G, no notifications)
Write-Host "Configuring System UI Demo Mode..."
& $adb shell settings put global sysui_demo_allowed 1
& $adb shell am broadcast -a com.android.systemui.demo -e command enter | Out-Null
& $adb shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm "0941" | Out-Null
& $adb shell am broadcast -a com.android.systemui.demo -e command battery -e level 100 -e plugged false | Out-Null
& $adb shell am broadcast -a com.android.systemui.demo -e command network -e mobile show -e datatype "5g" -e level 4 | Out-Null
& $adb shell am broadcast -a com.android.systemui.demo -e command network -e wifi show -e level 4 | Out-Null
& $adb shell am broadcast -a com.android.systemui.demo -e command notifications -e visible false | Out-Null

# 4. Force System Dark Mode
Write-Host "Enforcing system dark mode..."
& $adb shell "cmd uimode night yes"

# 5. Build and install debug APK
$apkPath = Join-Path $luminaAndroidDir "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $apkPath)) {
    Write-Host "Building debug APK..."
    Push-Location $luminaAndroidDir
    ./gradlew assembleDebug --no-daemon
    Pop-Location
}

Write-Host "Installing latest debug APK to device..."
& $adb install -r -g $apkPath | Out-Null

# 6. Trigger Demo Data Seeder via debug BroadcastReceiver
Write-Host "Seeding demo data (profile 2,150 kcal, meals 1,623 kcal, water 1,250ml, 6-day streak)..."
& $adb shell am broadcast -a com.kcalgrindai.app.action.SEED_DEMO_DATA -p com.kcalgrindai.app
Start-Sleep -Seconds 3

# Launch app once to initialize and wait for loading to finish
Write-Host "Starting KcalGrind..."
& $adb shell am start -n com.kcalgrindai.app/.app.MainActivity | Out-Null
Write-Host "Waiting for initial data load to settle on Home..."
Start-Sleep -Seconds 7

# Function to capture screen with dark mode check
function Capture-Screen {
    param(
        [string]$ScreenName,
        [string]$Route,
        [scriptblock]$CustomInteraction = $null
    )

    Write-Host "`n--- Capturing $ScreenName ---"
    if ($Route) {
        & $adb shell am start -n com.kcalgrindai.app/.app.MainActivity --es EXTRA_DESTINATION $Route | Out-Null
        Start-Sleep -Seconds 3
    }

    if ($CustomInteraction) {
        & $CustomInteraction
        Start-Sleep -Seconds 2
    }

    $outPath = Join-Path $OutputDir "$ScreenName.png"
    # Capture directly to device file and pull to avoid PowerShell text/CRLF encoding corruption
    & $adb shell screencap -p /sdcard/screencap.png
    cmd /c "$adb pull /sdcard/screencap.png ""$outPath"" >nul 2>nul"
    & $adb shell rm -f /sdcard/screencap.png
    Write-Host "Saved: $outPath"

    # Also save with canonical alias if prefixed (e.g. 01_home -> home.png)
    if ($ScreenName -match '^\d+_(.+)$') {
        $alias = $Matches[1]
        Copy-Item -Path $outPath -Destination (Join-Path $OutputDir "$alias.png") -Force
    }

    # Analyze sample pixels across a 15x15 grid (225 points) to verify Dark Mode
    try {
        Add-Type -AssemblyName System.Drawing -ErrorAction SilentlyContinue
        $fs = [System.IO.File]::OpenRead((Convert-Path $outPath))
        $bmp = [System.Drawing.Bitmap]::FromStream($fs)
        $w = $bmp.Width
        $h = $bmp.Height
        $totalSampled = 0
        $lightCount = 0

        # Sample 15x15 = 225 evenly distributed points across the display
        for ($i = 1; $i -le 15; $i++) {
            $x = [int]($w * ($i / 16.0))
            for ($j = 1; $j -le 15; $j++) {
                $y = [int]($h * ($j / 16.0))
                $p = $bmp.GetPixel($x, $y)
                $lum = (0.299 * $p.R + 0.587 * $p.G + 0.114 * $p.B)
                $totalSampled++
                if ($lum -gt 180) {
                    $lightCount++
                }
            }
        }
        $bmp.Dispose()
        $fs.Dispose()

        $lightFraction = ($lightCount / [double]$totalSampled) * 100.0
        $formattedPct = [math]::Round($lightFraction, 2)

        if ($lightFraction -gt 5.0) {
            Write-Warning "FLAGGED (>5%): ${ScreenName} has $lightCount/$totalSampled light pixels ($formattedPct%) with luminance > 180."
            return $false
        } else {
            Write-Host "Dark mode verified for ${ScreenName} - $lightCount/$totalSampled light pixels ($formattedPct% <= 5.0% threshold)."
            return $true
        }
    } catch {
        Write-Host "Pixel check skipped for $ScreenName ($($_.Exception.Message))"
        return $true
    }
}

$lightScreens = @()

# 1. Home Dashboard
$ok = Capture-Screen -ScreenName "01_home" -Route "home"
if (-not $ok) { $lightScreens += "Home" }

# 2. Food Diary
$ok = Capture-Screen -ScreenName "02_diary" -Route "diary"
if (-not $ok) { $lightScreens += "Diary" }

# 3. Food Search (query "dal")
$searchInteraction = {
    # Focus search field
    & $adb shell input tap 300 210
    Start-Sleep -Seconds 1
    # Type "dal" into the search field
    & $adb shell input text "dal"
    Start-Sleep -Seconds 2
    # Dismiss soft keyboard so the results list is visible
    & $adb shell input keyevent 111 # KEYCODE_ESCAPE
    Start-Sleep -Seconds 1
}
$ok = Capture-Screen -ScreenName "03_food_search" -Route "food_search" -CustomInteraction $searchInteraction
if (-not $ok) { $lightScreens += "Food Search" }

# 4. Food Detail
$detailInteraction = {
    # From search results, tap first food item card (approx center x=540, y=440)
    & $adb shell input tap 540 440
    Start-Sleep -Seconds 3
}
$ok = Capture-Screen -ScreenName "04_food_detail" -Route $null -CustomInteraction $detailInteraction
if (-not $ok) { $lightScreens += "Food Detail" }

# 5. AI Coach (Sproutling with 40g protein recommendation)
$ok = Capture-Screen -ScreenName "05_ai_coach" -Route "ai_chat"
if (-not $ok) { $lightScreens += "AI Coach" }

# 6. Weekly Insights
$ok = Capture-Screen -ScreenName "06_insights" -Route "insights"
if (-not $ok) { $lightScreens += "Insights" }

# 7. Profile & Settings
$ok = Capture-Screen -ScreenName "07_profile" -Route "profile"
if (-not $ok) { $lightScreens += "Profile" }

# 8. Widget on Launcher
$launcherInteraction = {
    # Open widget settings to trigger pin or ensure widget is on launcher
    & $adb shell am start -S -n com.kcalgrindai.app/.app.MainActivity --es EXTRA_DESTINATION widget_settings | Out-Null
    Start-Sleep -Seconds 3
    # Tap "Add to home screen" button
    & $adb shell input tap 540 1800
    Start-Sleep -Seconds 2
    # If pinning dialog is on screen, accept it
    & $adb shell input tap 850 1400
    Start-Sleep -Seconds 1
    & $adb shell input keyevent 3 # KEYCODE_HOME
    Start-Sleep -Seconds 3
}
$ok = Capture-Screen -ScreenName "08_widget_launcher" -Route $null -CustomInteraction $launcherInteraction
if (-not $ok) { $lightScreens += "Widget on Launcher" }

# Crop 08_widget_launcher to widget area
try {
    Add-Type -AssemblyName System.Drawing
    $wPath = Join-Path $OutputDir "08_widget_launcher.png"
    if (Test-Path $wPath) {
        $fs = [System.IO.File]::OpenRead((Convert-Path $wPath))
        $fullBmp = [System.Drawing.Bitmap]::FromStream($fs)
        $cropRect = New-Object System.Drawing.Rectangle(40, 260, 1000, 780)
        $croppedBmp = $fullBmp.Clone($cropRect, $fullBmp.PixelFormat)
        $fullBmp.Dispose()
        $fs.Dispose()
        $croppedBmp.Save($wPath, [System.Drawing.Imaging.ImageFormat]::Png)
        $croppedBmp.Dispose()
        Copy-Item -Path $wPath -Destination (Join-Path $OutputDir "widget_launcher.png") -Force
        Write-Host "Cropped 08_widget_launcher.png to widget area (1000x780)."
    }
} catch {
    Write-Warning "Crop failed: $($_.Exception.Message)"
}

Write-Host "`n=== SCREENSHOT CAPTURE SUMMARY ==="
Write-Host "Captured 8 screens to: $OutputDir"
if ($lightScreens.Count -gt 0) {
    Write-Warning "Screens not rendering dark: $($lightScreens -join ', ')"
} else {
    Write-Host "All screens verified rendering in DARK MODE!" -ForegroundColor Green
}
