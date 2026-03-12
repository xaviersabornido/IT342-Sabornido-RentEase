# RentEase

RentEase is a rental reservation and scheduling platform where renters can browse and book listings online while property owners manage their listings and bookings efficiently.

This repository currently implements Phase 1 only:

- **User Registration**
- **User Login**

Future SDD features (listings, rental requests, ratings, admin tools, OAuth, email, uploads, etc.) are intentionally out of scope for this phase.

## Current Architecture

- **Backend:** Spring Boot REST API (`backend/`)
- **Web Frontend:** React + Vite SPA (`web/`)
- **Database / migrations:** Supabase PostgreSQL (`supabase/`)

## Phase 1 Features Implemented

### Registration (`POST /api/v1/auth/register`)

- Required fields: `firstname`, `lastname`, `email`, `password`, `role` (RENTER or OWNER)
- Validates input
- Prevents duplicate email registration
- Stores password using BCrypt hashing
- Persists users in PostgreSQL (Supabase)
- Returns JWT access and refresh tokens

### Login (`POST /api/v1/auth/login`)

- Accepts email and password
- Validates credentials from the database
- Rejects invalid credentials
- Returns user info and JWT tokens for frontend dashboard access

## API Contract (Phase 1)

All responses use a wrapper:

```json
{
  "success": true,
  "data": { ... },
  "error": null,
  "timestamp": "2025-03-11T..."
}
```

### Register

**POST** `/api/v1/auth/register`

Request body:

```json
{
  "firstname": "Jane",
  "lastname": "Doe",
  "email": "jane@example.com",
  "password": "Password123",
  "role": "RENTER"
}
```

Success response (201): `data` contains:

```json
{
  "user": {
    "email": "jane@example.com",
    "firstname": "Jane",
    "lastname": "Doe",
    "role": "RENTER"
  },
  "accessToken": "...",
  "refreshToken": "..."
}
```

### Login

**POST** `/api/v1/auth/login`

Request body:

```json
{
  "email": "jane@example.com",
  "password": "Password123"
}
```

Success response (200): `data` contains:

```json
{
  "user": {
    "email": "jane@example.com",
    "firstname": "Jane",
    "lastname": "Doe",
    "role": "RENTER"
  },
  "accessToken": "...",
  "refreshToken": "..."
}
```

## Environment Setup

Create a root `.env` file (not committed to git) with your Supabase values:

```env
SUPABASE_DB_HOST=aws-1-ap-southeast-2.pooler.supabase.com
SUPABASE_DB_PORT=6543
SUPABASE_DB_NAME=postgres
SUPABASE_DB_USER=postgres.uabtldpqfbkkwmqxtzmq
SUPABASE_DB_PASSWORD=your-supabase-db-password
```

Optional: set `JWT_SECRET` in `.env` if you want a custom signing key (default is in `application.yaml`).

## Run Instructions

### 1. Backend

**Requirements:**

- Java 17+
- Maven Wrapper (included in `backend/`)

**Option A – from project root (runs migrations, then backend):**

```cmd
start.cmd
```

Or in PowerShell (if you get execution policy errors):

```powershell
powershell -ExecutionPolicy Bypass -File .\start.ps1
```

**Option B – backend only:**

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Backend runs on `http://localhost:8080`.

### 2. Web

**Requirements:**

- Node.js 18+

**Commands:**

```bash
cd web
npm install
npm run dev
```

Web app runs on http://localhost:5173 and connects to the backend at `http://localhost:8080`.

## Notes

- Spring Boot 3 requires Java 17 or newer.
- Phase 1 includes JWT (access + refresh tokens); role-based access and future modules (listings, rental requests, admin) are for later phases.
- Keep commit history focused and feature-based (registration and login milestones).
