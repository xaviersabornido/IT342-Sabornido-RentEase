-- Legacy duplicate column; application uses credit_check_agreed only.
ALTER TABLE IF EXISTS rental_requests DROP COLUMN IF EXISTS agree_credit_check;
