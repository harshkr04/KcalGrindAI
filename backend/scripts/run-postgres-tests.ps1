# PowerShell script to run Real PostgreSQL integration tests
$dockerCmd = Get-Command docker -ErrorAction Ignore

if ($dockerCmd) {
    Write-Host "Docker detected -- spinning up PostgreSQL service container via docker compose..."
    docker compose -f docker-compose.test.yml up --build --abort-on-container-exit --exit-code-from test-runner
    docker compose -f docker-compose.test.yml down -v
} else {
    Write-Host "Docker not detected on host -- running Real PostgreSQL test suite directly via in-process engine..."
    $backendDir = "D:\CAloriesssss\LuminaAndroid\backend"
    Set-Location $backendDir
    node test-real-postgres.js
    node scripts/audit-purge-completeness.js --live
}
