CREATE UNIQUE INDEX uq_uploaded_file_name
    ON uploaded_file (name)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_uploaded_file_file_name
    ON uploaded_file (lower(trim(file_name)))
    WHERE deleted_at IS NULL;
