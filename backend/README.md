# RentEase Backend (Spring Boot)

Connects to **Supabase Postgres** using the URL configured in `application.yaml`.

## Database password (required)

The backend uses **JDBC** and reads connection from environment variables (e.g. from root `.env` when you run `.\start.ps1`):

- **SUPABASE_DB_HOST** – e.g. `aws-1-ap-southeast-2.pooler.supabase.com`
- **SUPABASE_DB_PORT** – e.g. `6543`
- **SUPABASE_DB_NAME** – e.g. `postgres`
- **SUPABASE_DB_USER** – e.g. `postgres.uabtldpqfbkkwmqxtzmq`
- **SUPABASE_DB_PASSWORD** – your database password

`application.yaml` builds the JDBC URL from these; defaults are set for the current pooler. Copy `backend/.env.example` to the project root as `.env` and fill in the password; do not commit `.env`.

Schema and migrations are in the project’s `supabase/migrations/` folder.

### One-command start (migrate + backend)

From the **project root** (IT342-Sabornido-RentEase), run:

```cmd
start.cmd
```

If PowerShell blocks `.\start.ps1` (execution policy), use `start.cmd` or run:  
`powershell -ExecutionPolicy Bypass -File .\start.ps1`

This runs Supabase migrations (if `SUPABASE_DB_URL` is in `.env`), then starts the Spring Boot backend. The script loads `.env` so `SUPABASE_DB_PASSWORD` is available to the backend.

Last Update : April 11, 2026