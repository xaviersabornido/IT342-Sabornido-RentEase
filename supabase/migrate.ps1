# Load .env and run supabase db push (builds URL from JDBC-style vars)
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
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

$host = [Environment]::GetEnvironmentVariable("SUPABASE_DB_HOST", "Process")
$port = [Environment]::GetEnvironmentVariable("SUPABASE_DB_PORT", "Process")
$name = [Environment]::GetEnvironmentVariable("SUPABASE_DB_NAME", "Process")
$user = [Environment]::GetEnvironmentVariable("SUPABASE_DB_USER", "Process")
$pass = [Environment]::GetEnvironmentVariable("SUPABASE_DB_PASSWORD", "Process")
if (-not ($host -and $user -and $pass)) {
    Write-Error "Set SUPABASE_DB_HOST, SUPABASE_DB_USER, SUPABASE_DB_PASSWORD in .env"
}
if (-not $port) { $port = "6543" }
if (-not $name) { $name = "postgres" }
$dbUrl = "postgresql://${user}:${pass}@${host}:${port}/${name}"
Set-Location $root
npx supabase db push --db-url $dbUrl
