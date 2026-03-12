-- Extend listings with structured details for the property details page

ALTER TABLE public.listings
  ADD COLUMN IF NOT EXISTS bedrooms INT CHECK (bedrooms >= 0),
  ADD COLUMN IF NOT EXISTS bathrooms INT CHECK (bathrooms >= 0),
  ADD COLUMN IF NOT EXISTS area_sq_ft INT CHECK (area_sq_ft >= 0),
  ADD COLUMN IF NOT EXISTS parking_spaces INT CHECK (parking_spaces >= 0),
  ADD COLUMN IF NOT EXISTS available_from DATE,
  ADD COLUMN IF NOT EXISTS lease_term_months INT CHECK (lease_term_months >= 0),
  ADD COLUMN IF NOT EXISTS deposit DECIMAL(12, 2) CHECK (deposit >= 0),
  ADD COLUMN IF NOT EXISTS furnished BOOLEAN,
  ADD COLUMN IF NOT EXISTS pets_allowed BOOLEAN,
  ADD COLUMN IF NOT EXISTS utilities_estimate DECIMAL(12, 2) CHECK (utilities_estimate >= 0);

