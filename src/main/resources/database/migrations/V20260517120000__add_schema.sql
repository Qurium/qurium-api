-- ============================================================
-- schema_snapshot
-- Stores imported schema metadata (tables/columns as JSON)
-- from an external database connection.
-- ============================================================
CREATE TABLE IF NOT EXISTS schema
(
    id            UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    connection_id UUID         NOT NULL REFERENCES database_connection (id),
    schema_json   TEXT         NOT NULL,
    source        VARCHAR(50)  NOT NULL CHECK ( source IN ('CONNECTED', 'UPLOADED') ),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ,
    deleted_at    TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_schema_snapshot_connection ON schema (connection_id);
