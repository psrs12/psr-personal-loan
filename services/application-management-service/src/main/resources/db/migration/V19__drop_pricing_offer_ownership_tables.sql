-- Ownership of pricing offers, offer selection, and consent records has moved to
-- pricing-orchestration-service. Drop the tables in FK-safe order (dependents first).
DROP TABLE IF EXISTS consent_record;
DROP TABLE IF EXISTS offer_selection;
DROP TABLE IF EXISTS pricing_offer;
