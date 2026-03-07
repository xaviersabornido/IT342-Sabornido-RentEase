-- Grants for Supabase API (anon / authenticated)
-- RLS still enforces who can do what; these allow the API to attempt the operations.

GRANT USAGE ON SCHEMA public TO anon, authenticated;

GRANT SELECT ON public.users TO authenticated;
GRANT UPDATE ON public.users TO authenticated;

GRANT SELECT, INSERT, DELETE ON public.refresh_tokens TO authenticated;

GRANT SELECT ON public.listings TO anon, authenticated;
GRANT INSERT, UPDATE, DELETE ON public.listings TO authenticated;

GRANT SELECT, INSERT, UPDATE ON public.rental_requests TO authenticated;

GRANT SELECT, INSERT ON public.ratings TO authenticated;

GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO authenticated;

-- View for owner rating (read-only for display)
GRANT SELECT ON public.owner_avg_rating TO anon, authenticated;
