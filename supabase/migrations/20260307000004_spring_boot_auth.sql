-- Spring Boot Auth: users table with password_hash (no Supabase Auth dependency)
-- Run this if using Spring Boot for auth instead of Supabase Auth

-- Remove Supabase Auth dependency
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;

-- Drop FK to auth.users (constraint name may vary)
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = 'public' AND table_name = 'users'
    AND constraint_name = 'users_id_fkey'
  ) THEN
    ALTER TABLE public.users DROP CONSTRAINT users_id_fkey;
  END IF;
END $$;

-- Add password_hash for Spring Boot bcrypt
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);

-- Set default UUID for new inserts (Spring Boot will generate)
ALTER TABLE public.users ALTER COLUMN id SET DEFAULT gen_random_uuid();
