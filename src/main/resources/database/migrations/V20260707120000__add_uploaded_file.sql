-- ============================================================
-- uploaded_file
-- Stores metadata about DDL files uploaded by users.
-- The associated schema is linked via schema.uploaded_file_id.
-- ============================================================
CREATE TABLE IF NOT EXISTS uploaded_file
(
    id              UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    file_name       VARCHAR(255) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ,
    deleted_at      TIMESTAMPTZ
);

ALTER TABLE schema
    ADD COLUMN uploaded_file_id UUID REFERENCES uploaded_file (id);

CREATE INDEX IF NOT EXISTS idx_schema_uploaded_file ON schema (uploaded_file_id);

ALTER TABLE schema
    ADD CONSTRAINT schema_source_xor CHECK (
        (connection_id IS NOT NULL AND uploaded_file_id IS NULL)
        OR (connection_id IS NULL AND uploaded_file_id IS NOT NULL)
    );
