-- ============================================================
-- Make schema.connection_id nullable so DDL-uploaded schemas
-- can exist without a live database connection.
-- ============================================================
ALTER TABLE schema ALTER COLUMN connection_id DROP NOT NULL;
