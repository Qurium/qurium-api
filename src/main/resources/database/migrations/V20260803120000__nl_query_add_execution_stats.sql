ALTER TABLE nl_query
    ADD COLUMN IF NOT EXISTS execution_time_ms BIGINT,
    ADD COLUMN IF NOT EXISTS rows_returned      INTEGER,
    ADD COLUMN IF NOT EXISTS uploaded_file_id UUID,
    ADD CONSTRAINT fk_nl_query_uploaded_file FOREIGN KEY (uploaded_file_id) REFERENCES uploaded_file (id);
