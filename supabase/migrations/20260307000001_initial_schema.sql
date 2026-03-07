-- RentEase Database Schema (aligned with ERD)
-- Uses Supabase Auth: public.users extends auth.users with role (RENTER | OWNER)

-- Enable UUID extension if not already
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================================================
-- PUBLIC USERS (profile + role; auth.users holds password)
-- id matches auth.users.id for Supabase Auth integration
-- =============================================================================
CREATE TABLE public.users (
  id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  email VARCHAR(255) NOT NULL UNIQUE,
  firstname VARCHAR(100) NOT NULL,
  lastname VARCHAR(100) NOT NULL,
  role VARCHAR(20) NOT NULL CHECK (role IN ('RENTER', 'OWNER')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON public.users(email);
CREATE INDEX idx_users_role ON public.users(role);

-- Trigger: create public.users row when a new auth user signs up
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO public.users (id, email, firstname, lastname, role)
  VALUES (
    NEW.id,
    COALESCE(NEW.email, ''),
    COALESCE(NEW.raw_user_meta_data->>'firstname', ''),
    COALESCE(NEW.raw_user_meta_data->>'lastname', ''),
    COALESCE(UPPER(NEW.raw_user_meta_data->>'role'), 'RENTER')
  );
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- Trigger: keep updated_at in sync
CREATE OR REPLACE FUNCTION public.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- REFRESH TOKENS (optional; Supabase Auth has its own – use if you need custom)
-- =============================================================================
CREATE TABLE public.refresh_tokens (
  id BIGSERIAL PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
  token TEXT NOT NULL,
  expiry_date TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_user_id ON public.refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expiry ON public.refresh_tokens(expiry_date);

-- =============================================================================
-- LISTINGS (owner-only create; owner_id = users.id where role = OWNER)
-- =============================================================================
CREATE TABLE public.listings (
  id BIGSERIAL PRIMARY KEY,
  owner_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  price DECIMAL(12, 2) NOT NULL CHECK (price >= 0),
  location VARCHAR(255) NOT NULL,
  property_type VARCHAR(50) NOT NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'available' CHECK (status IN ('available', 'rented', 'pending')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER listings_updated_at
  BEFORE UPDATE ON public.listings
  FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE INDEX idx_listings_owner_id ON public.listings(owner_id);
CREATE INDEX idx_listings_status ON public.listings(status);
CREATE INDEX idx_listings_location ON public.listings(location);

-- =============================================================================
-- RENTAL REQUESTS (renter sends request for a listing; status PENDING | APPROVED | DECLINED)
-- =============================================================================
CREATE TABLE public.rental_requests (
  id BIGSERIAL PRIMARY KEY,
  listing_id BIGINT NOT NULL REFERENCES public.listings(id) ON DELETE CASCADE,
  renter_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'DECLINED')),
  requested_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (listing_id, renter_id)
);

CREATE TRIGGER rental_requests_updated_at
  BEFORE UPDATE ON public.rental_requests
  FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE INDEX idx_rental_requests_listing_id ON public.rental_requests(listing_id);
CREATE INDEX idx_rental_requests_renter_id ON public.rental_requests(renter_id);
CREATE INDEX idx_rental_requests_status ON public.rental_requests(status);

-- =============================================================================
-- RATINGS (renter rates owner after rental request approved; 1–5 stars + optional comment)
-- =============================================================================
CREATE TABLE public.ratings (
  id BIGSERIAL PRIMARY KEY,
  owner_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
  renter_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
  rental_request_id BIGINT NOT NULL REFERENCES public.rental_requests(id) ON DELETE CASCADE,
  rating_value INT NOT NULL CHECK (rating_value >= 1 AND rating_value <= 5),
  comment TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (rental_request_id)
);

CREATE INDEX idx_ratings_owner_id ON public.ratings(owner_id);
CREATE INDEX idx_ratings_renter_id ON public.ratings(renter_id);
CREATE INDEX idx_ratings_rental_request_id ON public.ratings(rental_request_id);

-- =============================================================================
-- USERS table updated_at trigger
-- =============================================================================
CREATE TRIGGER users_updated_at
  BEFORE UPDATE ON public.users
  FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- Optional: view for average owner rating (for listing display)
CREATE OR REPLACE VIEW public.owner_avg_rating AS
SELECT
  owner_id,
  ROUND(AVG(rating_value)::numeric, 2) AS owner_rating,
  COUNT(*) AS rating_count
FROM public.ratings
GROUP BY owner_id;

COMMENT ON TABLE public.users IS 'Profile and role (RENTER | OWNER); id matches auth.users';
COMMENT ON COLUMN public.users.role IS 'RENTER or OWNER only';
COMMENT ON TABLE public.listings IS 'Rental listings owned by users with role OWNER';
COMMENT ON TABLE public.rental_requests IS 'Renter requests for listings; status PENDING/APPROVED/DECLINED';
COMMENT ON TABLE public.ratings IS 'Renter ratings for owners after approved request; 1-5 stars';
