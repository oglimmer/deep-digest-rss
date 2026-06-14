-- Spring Authorization Server persistence for the MCP OAuth2 endpoints.
-- Dynamic Client Registration (RFC 7591) creates clients at runtime, so the
-- registered-client / authorization / consent stores must be JDBC-backed to
-- survive restarts. Schema mirrors Spring Authorization Server's reference
-- schema, adapted to MariaDB: TIMESTAMP -> DATETIME (avoids the single
-- implicit-default TIMESTAMP restriction) and *_metadata/attributes widened to
-- MEDIUMBLOB so larger token/claim payloads do not hit the 64 KB BLOB limit.

CREATE TABLE oauth2_registered_client (
    id VARCHAR(100) NOT NULL,
    client_id VARCHAR(100) NOT NULL,
    client_id_issued_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    client_secret VARCHAR(200) DEFAULT NULL,
    client_secret_expires_at DATETIME(6) DEFAULT NULL,
    client_name VARCHAR(200) NOT NULL,
    client_authentication_methods VARCHAR(1000) NOT NULL,
    authorization_grant_types VARCHAR(1000) NOT NULL,
    redirect_uris VARCHAR(1000) DEFAULT NULL,
    post_logout_redirect_uris VARCHAR(1000) DEFAULT NULL,
    scopes VARCHAR(1000) NOT NULL,
    client_settings VARCHAR(2000) NOT NULL,
    token_settings VARCHAR(2000) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE oauth2_authorization_consent (
    registered_client_id VARCHAR(100) NOT NULL,
    principal_name VARCHAR(200) NOT NULL,
    authorities VARCHAR(1000) NOT NULL,
    PRIMARY KEY (registered_client_id, principal_name)
);

CREATE TABLE oauth2_authorization (
    id VARCHAR(100) NOT NULL,
    registered_client_id VARCHAR(100) NOT NULL,
    principal_name VARCHAR(200) NOT NULL,
    authorization_grant_type VARCHAR(100) NOT NULL,
    authorized_scopes VARCHAR(1000) DEFAULT NULL,
    attributes MEDIUMBLOB DEFAULT NULL,
    state VARCHAR(500) DEFAULT NULL,
    authorization_code_value BLOB DEFAULT NULL,
    authorization_code_issued_at DATETIME(6) DEFAULT NULL,
    authorization_code_expires_at DATETIME(6) DEFAULT NULL,
    authorization_code_metadata MEDIUMBLOB DEFAULT NULL,
    access_token_value BLOB DEFAULT NULL,
    access_token_issued_at DATETIME(6) DEFAULT NULL,
    access_token_expires_at DATETIME(6) DEFAULT NULL,
    access_token_metadata MEDIUMBLOB DEFAULT NULL,
    access_token_type VARCHAR(100) DEFAULT NULL,
    access_token_scopes VARCHAR(1000) DEFAULT NULL,
    oidc_id_token_value BLOB DEFAULT NULL,
    oidc_id_token_issued_at DATETIME(6) DEFAULT NULL,
    oidc_id_token_expires_at DATETIME(6) DEFAULT NULL,
    oidc_id_token_metadata MEDIUMBLOB DEFAULT NULL,
    refresh_token_value BLOB DEFAULT NULL,
    refresh_token_issued_at DATETIME(6) DEFAULT NULL,
    refresh_token_expires_at DATETIME(6) DEFAULT NULL,
    refresh_token_metadata MEDIUMBLOB DEFAULT NULL,
    user_code_value BLOB DEFAULT NULL,
    user_code_issued_at DATETIME(6) DEFAULT NULL,
    user_code_expires_at DATETIME(6) DEFAULT NULL,
    user_code_metadata MEDIUMBLOB DEFAULT NULL,
    device_code_value BLOB DEFAULT NULL,
    device_code_issued_at DATETIME(6) DEFAULT NULL,
    device_code_expires_at DATETIME(6) DEFAULT NULL,
    device_code_metadata MEDIUMBLOB DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_oauth2_authorization_principal ON oauth2_authorization (principal_name);
