-- Spring Authorization Server's JdbcOAuth2AuthorizationService binds the attributes / token-value /
-- metadata columns as Java Strings (JSON). MySQL's driver silently coerces String -> binary, but
-- MariaDB's driver rejects it ("Could not convert [...] to -4 / LONGVARBINARY"), so storing an
-- authorization during the authorization_code flow fails with a 500. These columns must therefore
-- be text types, not BLOB. (V18.0.0 created them as BLOB/MEDIUMBLOB; it is already applied, so this
-- follow-up migration converts them in place rather than editing the original.)

ALTER TABLE oauth2_authorization
    MODIFY attributes LONGTEXT NULL,
    MODIFY authorization_code_value TEXT NULL,
    MODIFY authorization_code_metadata LONGTEXT NULL,
    MODIFY access_token_value TEXT NULL,
    MODIFY access_token_metadata LONGTEXT NULL,
    MODIFY oidc_id_token_value TEXT NULL,
    MODIFY oidc_id_token_metadata LONGTEXT NULL,
    MODIFY refresh_token_value TEXT NULL,
    MODIFY refresh_token_metadata LONGTEXT NULL,
    MODIFY user_code_value TEXT NULL,
    MODIFY user_code_metadata LONGTEXT NULL,
    MODIFY device_code_value TEXT NULL,
    MODIFY device_code_metadata LONGTEXT NULL;
