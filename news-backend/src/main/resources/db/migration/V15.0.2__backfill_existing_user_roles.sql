-- Backfill non-service-account users with USER + READONLY roles.
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name IN ('USER','READONLY')
WHERE u.email NOT IN ('write','actuator','swagger','read');
