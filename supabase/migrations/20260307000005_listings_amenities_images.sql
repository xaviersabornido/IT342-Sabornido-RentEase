-- Add amenities and image_urls to listings

ALTER TABLE public.listings
  ADD COLUMN IF NOT EXISTS amenities TEXT,
  ADD COLUMN IF NOT EXISTS image_urls TEXT;

