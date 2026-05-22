-- ============================================================
-- nl_query
-- Stores natural-language questions, the AI-generated SQL,
-- the result snapshot (capped at 1000 rows as JSON),
-- and the AI explanation.
-- ============================================================
CREATE TABLE IF NOT EXISTS nl_query
(
    id              UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    connection_id   UUID        NOT NULL REFERENCES database_connection (id),
    question        TEXT        NOT NULL,
    generated_sql   TEXT,
    result_snapshot TEXT,
    explanation     TEXT,
    status          VARCHAR(50) NOT NULL CHECK ( status IN ('PENDING', 'SUCCESS', 'FAILED') ),
    error_message   TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ,
    deleted_at      TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_nl_query_connection ON nl_query (connection_id);
