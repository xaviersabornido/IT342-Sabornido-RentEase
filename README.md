# IT342-Sabornido-RentEase

Rental marketplace: Spring Boot backend, React web, Supabase Postgres.

## Quick start (backend)

1. Create `.env` at project root with JDBC-style vars:
   - `SUPABASE_DB_HOST=aws-1-ap-southeast-2.pooler.supabase.com`
   - `SUPABASE_DB_PORT=6543`
   - `SUPABASE_DB_NAME=postgres`
   - `SUPABASE_DB_USER=postgres.uabtldpqfbkkwmqxtzmq`
   - `SUPABASE_DB_PASSWORD=YOUR_PASSWORD`

2. From project root, run:
   ```cmd
   start.cmd
   ```
   Or in PowerShell (if you get "not digitally signed"):  
   `powershell -ExecutionPolicy Bypass -File .\start.ps1`  
   This runs Supabase migrations, then starts the Spring Boot backend.