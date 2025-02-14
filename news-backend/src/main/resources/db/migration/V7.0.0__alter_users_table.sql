
alter TABLE users
    ADD COLUMN auth_token VARCHAR(255) NULL;

alter TABLE users
    ADD COLUMN auth_token_valid_until datetime(6) NULL;
