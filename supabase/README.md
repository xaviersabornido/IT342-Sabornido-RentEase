# RentEase – Supabase Backend Setup

This folder contains the Supabase database schema and Row Level Security (RLS) for RentEase. User **role** is either **RENTER** or **OWNER**.

## Database design (from ERD)

| Table            | Purpose |
|------------------|--------|
| `public.users`  | Profile + role (RENTER \| OWNER). `id` = `auth.users.id`. |
| `public.refresh_tokens` | Optional custom refresh tokens. |
| `public.listings` | Rental listings; `owner_id` → users (OWNER). |
| `public.rental_requests` | Renter requests for listings; status PENDING / APPROVED / DECLINED. |
| `public.ratings` | Renter ratings for owners (1–5) after approved request; optional comment. |

## Prerequisites

- [Supabase CLI](https://supabase.com/docs/guides/cli) installed.
- A Supabase project (cloud or local).

## Option A: Push migrations via script (uses .env)

1. Create `.env` at project root with your Supabase connection:
   ```env
   SUPABASE_DB_PASSWORD=your_password
   SUPABASE_DB_URL=postgresql://postgres:your_password@db.uabtldpqfbkkwmqxtzmq.supabase.co:5432/postgres
   ```
2. Run the migrate script:
   ```powershell
   # From project root (IT342-Sabornido-RentEase)
   .\supabase\migrate.ps1
   ```
   This loads `.env` and runs `supabase db push` so migrations apply automatically to your Supabase project.

## Option B: Manual (Supabase Dashboard)

1. Go to [supabase.com](https://supabase.com) → your project.
2. In the Dashboard: **SQL Editor** → New query.
3. Run the migrations in order:
   - `supabase/migrations/20260307000001_initial_schema.sql`
   - `supabase/migrations/20260307000002_rls_policies.sql`
   - `supabase/migrations/20260307000003_grants.sql`
4. In **Authentication → Providers**: ensure **Email** is enabled and signup is allowed.
5. Use **Project Settings → API** for `SUPABASE_URL` and `SUPABASE_ANON_KEY` in your React/Android app.

## Option C: Local (Supabase CLI)

```bash
# From project root (IT342-Sabornido-RentEase)
npx supabase init   # if not already
npx supabase start
```

Migrations in `supabase/migrations/` run automatically. After `supabase start`:

- **API URL:** http://127.0.0.1:54321  
- **Anon key:** in `npx supabase status`  
- **Studio:** http://127.0.0.1:54323  

## Sign up and role (RENTER | OWNER)

Supabase Auth stores the account; `public.users` is filled by a trigger using **user metadata**.

On **sign up**, send `firstname`, `lastname`, and **role** in metadata:

```js
// Example (Supabase JS client)
await supabase.auth.signUp({
  email: 'user@email.com',
  password: 'password123',
  options: {
    data: {
      firstname: 'Juan',
      lastname: 'Dela Cruz',
      role: 'RENTER'   // or 'OWNER'
    }
  }
});
```

The trigger `handle_new_user()` copies these into `public.users` and sets **role** to `RENTER` or `OWNER` (defaults to `RENTER` if missing).

## API usage (high level)

- **Listings:** `GET /rest/v1/listings` (public). Create/update/delete only for authenticated **OWNER** (RLS).
- **Rental requests:** Create as **RENTER**; list/approve/decline as **OWNER** of the listing (RLS).
- **Ratings:** Insert as **RENTER** for an **APPROVED** request; read is public (RLS).

Use the Supabase client with the user’s JWT so RLS applies correctly.

## Owner average rating

View `public.owner_avg_rating` gives per-owner average rating and count. Join to `listings` by `owner_id` to show “owner rating” on listing cards.

## Notes

- Passwords are stored in **auth.users** (Supabase Auth), not in `public.users`.
- `refresh_tokens` table is optional; Supabase Auth manages refresh tokens by default.
- RLS ensures only OWNER can create/update/delete their listings, and only RENTER can create requests and ratings under the rules above.
