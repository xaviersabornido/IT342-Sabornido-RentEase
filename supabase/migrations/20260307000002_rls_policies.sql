-- Row Level Security (RLS) for RentEase
-- Ensures users only access data according to role (RENTER | OWNER)

-- =============================================================================
-- Enable RLS on all public tables
-- =============================================================================
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.refresh_tokens ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.listings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.rental_requests ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ratings ENABLE ROW LEVEL SECURITY;

-- =============================================================================
-- Helper: current user's profile row (for role checks)
-- =============================================================================
CREATE OR REPLACE FUNCTION public.current_user_role()
RETURNS VARCHAR(20) AS $$
  SELECT role FROM public.users WHERE id = auth.uid();
$$ LANGUAGE sql STABLE SECURITY DEFINER;

-- =============================================================================
-- USERS: users can read and update their own row only
-- =============================================================================
CREATE POLICY "Users can read own profile"
  ON public.users FOR SELECT
  USING (id = auth.uid());

CREATE POLICY "Users can update own profile"
  ON public.users FOR UPDATE
  USING (id = auth.uid())
  WITH CHECK (id = auth.uid());

-- Service role / trigger inserts; no policy needed for INSERT (trigger runs as definer)

-- =============================================================================
-- REFRESH TOKENS: users can manage their own tokens only
-- =============================================================================
CREATE POLICY "Users can read own refresh tokens"
  ON public.refresh_tokens FOR SELECT
  USING (user_id = auth.uid());

CREATE POLICY "Users can insert own refresh tokens"
  ON public.refresh_tokens FOR INSERT
  WITH CHECK (user_id = auth.uid());

CREATE POLICY "Users can delete own refresh tokens"
  ON public.refresh_tokens FOR DELETE
  USING (user_id = auth.uid());

-- =============================================================================
-- LISTINGS: public read; only owner can insert/update/delete own listings
-- =============================================================================
CREATE POLICY "Listings are readable by everyone"
  ON public.listings FOR SELECT
  USING (true);

CREATE POLICY "Owners can create listings"
  ON public.listings FOR INSERT
  WITH CHECK (
    auth.uid() = owner_id
    AND public.current_user_role() = 'OWNER'
  );

CREATE POLICY "Owners can update own listings"
  ON public.listings FOR UPDATE
  USING (owner_id = auth.uid())
  WITH CHECK (owner_id = auth.uid());

CREATE POLICY "Owners can delete own listings"
  ON public.listings FOR DELETE
  USING (owner_id = auth.uid());

-- =============================================================================
-- RENTAL_REQUESTS: renters create and read own; owners read/update for their listings
-- =============================================================================
CREATE POLICY "Renters can read own requests"
  ON public.rental_requests FOR SELECT
  USING (renter_id = auth.uid());

CREATE POLICY "Owners can read requests for their listings"
  ON public.rental_requests FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM public.listings l
      WHERE l.id = rental_requests.listing_id AND l.owner_id = auth.uid()
    )
  );

CREATE POLICY "Renters can create requests"
  ON public.rental_requests FOR INSERT
  WITH CHECK (
    renter_id = auth.uid()
    AND public.current_user_role() = 'RENTER'
  );

CREATE POLICY "Owners can update status of requests for their listings"
  ON public.rental_requests FOR UPDATE
  USING (
    EXISTS (
      SELECT 1 FROM public.listings l
      WHERE l.id = rental_requests.listing_id AND l.owner_id = auth.uid()
    )
  )
  WITH CHECK (true);

-- =============================================================================
-- RATINGS: renters can insert (for approved requests); everyone can read
-- =============================================================================
CREATE POLICY "Ratings are readable by everyone"
  ON public.ratings FOR SELECT
  USING (true);

CREATE POLICY "Renters can create rating for approved request they made"
  ON public.ratings FOR INSERT
  WITH CHECK (
    renter_id = auth.uid()
    AND public.current_user_role() = 'RENTER'
    AND EXISTS (
      SELECT 1 FROM public.rental_requests rr
      WHERE rr.id = rental_request_id
        AND rr.renter_id = auth.uid()
        AND rr.status = 'APPROVED'
    )
  );
