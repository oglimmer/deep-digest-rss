-- The legacy long-lived per-user auth_token mechanism is gone; sessions are now
-- managed server-side in Redis via Spring Session, and service-to-service callers
-- use the api_keys table.
ALTER TABLE users DROP COLUMN auth_token;
ALTER TABLE users DROP COLUMN auth_token_valid_until;
