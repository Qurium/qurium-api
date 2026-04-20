-- ============================================================
-- database_connection
-- Stores user-provided external database connection details.
-- Passwords are stored AES-256-GCM encrypted (never plaintext).
-- ============================================================
CREATE TABLE IF NOT EXISTS database_connection
(
    id                 UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name               VARCHAR(255) NOT NULL,
    type               VARCHAR(50)  NOT NULL,
    host               VARCHAR(255) NOT NULL,
    port               BIGINT       NOT NULL,
    database_name      VARCHAR(255) NOT NULL,
    username           VARCHAR(255) NOT NULL,
    encrypted_password TEXT         NOT NULL,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ
);

CREATE INDEX idx_database_connection_type ON database_connection (type);
