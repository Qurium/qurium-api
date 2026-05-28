-- ============================================================
-- Make nl_query.connection_id nullable so schema-only queries
-- (POST /api/schema/{id}/query) can be stored without a live
-- database connection.
-- ============================================================
ALTER TABLE nl_query ALTER COLUMN connection_id DROP NOT NULL;
