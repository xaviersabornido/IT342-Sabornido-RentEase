# RentEase: Migrate Supabase, then start Spring Boot backend
# Run from project root: .\start.ps1

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot

Write-Host "RentEase startup: migrate then backend" -ForegroundColor Cyan

# 1. Load .env (JDBC-style: HOST, PORT, NAME, USER, PASSWORD)
$envPath = Join-Path $root ".env"
if (-not (Test-Path $envPath)) {
    Write-Error ".env not found at $envPath. Set SUPABASE_DB_HOST, PORT, NAME, USER, PASSWORD."
}
Get-Content $envPath | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
        $key = $matches[1].Trim()
        $value = $matches[2].Trim()
        [Environment]::SetEnvironmentVariable($key, $value, "Process")
    }
}

# 2. Run Supabase migrations (build URL from JDBC vars)
# Note: avoid using $host (reserved PS variable)
$dbHost = [Environment]::GetEnvironmentVariable("SUPABASE_DB_HOST", "Process")
$user = [Environment]::GetEnvironmentVariable("SUPABASE_DB_USER", "Process")
$pass = [Environment]::GetEnvironmentVariable("SUPABASE_DB_PASSWORD", "Process")
if ($dbHost -and $user -and $pass) {
    Write-Host "Running Supabase migrations..." -ForegroundColor Yellow
    $port = [Environment]::GetEnvironmentVariable("SUPABASE_DB_PORT", "Process"); if (-not $port) { $port = "5432" }
    $name = [Environment]::GetEnvironmentVariable("SUPABASE_DB_NAME", "Process"); if (-not $name) { $name = "postgres" }
    $dbUrl = "postgresql://${user}:${pass}@${dbHost}:${port}/${name}"
    Set-Location $root
    npx supabase db push --db-url $dbUrl
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Migration failed (continuing to start backend)." -ForegroundColor Red
    } else {
        Write-Host "Migrations completed." -ForegroundColor Green
    }
} else {
    Write-Host "SUPABASE_DB_HOST/USER/PASSWORD not set; skipping migrations." -ForegroundColor Yellow
}

# 3. Start Spring Boot backend
Write-Host "Starting backend..." -ForegroundColor Yellow
Set-Location (Join-Path $root "backend")
& .\mvnw.cmd spring-boot:run
