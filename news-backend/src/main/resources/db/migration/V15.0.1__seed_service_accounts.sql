-- Placeholder BCrypt hash; real password is written by StartupPasswordSyncer at boot
-- from the AUTH_*_PASSWORD env vars. The placeholder is structurally a valid BCrypt
-- string (so passwordEncoder.matches() returns false instead of throwing) but contains
-- no real password, so the account is unusable until startup syncs the real hash.

INSERT IGNORE INTO users (email, password, timezone) VALUES
    ('write', '$2a$10$xxxxxxxxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', 'Europe/Berlin'),
    ('actuator', '$2a$10$xxxxxxxxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', 'Europe/Berlin'),
    ('swagger', '$2a$10$xxxxxxxxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', 'Europe/Berlin'),
    ('read', '$2a$10$xxxxxxxxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', 'Europe/Berlin');

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON 1=1
WHERE (u.email = 'write'    AND r.name IN ('ADMIN','USER','READONLY'))
   OR (u.email = 'actuator' AND r.name IN ('ADMIN','USER','READONLY'))
   OR (u.email = 'swagger'  AND r.name IN ('ADMIN','USER','READONLY'))
   OR (u.email = 'read'     AND r.name = 'READONLY');
