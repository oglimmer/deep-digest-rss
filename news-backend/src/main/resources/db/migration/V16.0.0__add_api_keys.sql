CREATE TABLE api_keys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE,
    key_hash VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_used_at DATETIME(6) NULL,
    revoked_at DATETIME(6) NULL
);

CREATE TABLE api_key_roles (
    api_key_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (api_key_id, role_id),
    CONSTRAINT fk_apikey_roles_apikey FOREIGN KEY (api_key_id) REFERENCES api_keys(id) ON DELETE CASCADE,
    CONSTRAINT fk_apikey_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
);

-- Placeholder row for the scraper; real key hash is written by StartupPasswordSyncer
-- on boot from the SCRAPER_API_KEY env var. Until that runs, no key matches.
INSERT IGNORE INTO api_keys (name, key_hash)
VALUES ('scraper', '$2a$10$xxxxxxxxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx');

INSERT IGNORE INTO api_key_roles (api_key_id, role_id)
SELECT ak.id, r.id
FROM api_keys ak
JOIN roles r ON r.name IN ('ADMIN', 'USER', 'READONLY')
WHERE ak.name = 'scraper';
