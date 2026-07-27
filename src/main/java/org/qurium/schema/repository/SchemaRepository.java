/* Qurium - 2026 */
package org.qurium.schema.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.qurium.connection.domain.DatabaseConnection;
import org.qurium.schema.domain.Schema;
import org.qurium.schema.domain.SchemaSource;
import org.qurium.uploadedfile.domain.UploadedFile;

@ApplicationScoped
public class SchemaRepository implements PanacheRepositoryBase<Schema, UUID> {

    public UUID store(DatabaseConnection connection, String schemaJson, SchemaSource source) {

        Schema schema = new Schema();
        schema.setConnection(connection);
        schema.setSchemaJson(schemaJson);
        schema.setSource(source);
        persist(schema);

        return schema.getId();
    }

    public UUID store(UploadedFile uploadedFile, String schemaJson) {

        Schema schema = new Schema();
        schema.setUploadedFile(uploadedFile);
        schema.setSchemaJson(schemaJson);
        schema.setSource(SchemaSource.UPLOADED_DDL);
        persist(schema);

        return schema.getId();
    }

    public Optional<Schema> findByConnectionId(UUID connectionId) {
        return find("connection.id", connectionId).firstResultOptional();
    }

    public Optional<Schema> findByUploadedFileId(UUID uploadedFileId) {
        return find("uploadedFile.id", uploadedFileId).firstResultOptional();
    }

    public Optional<Schema> findByOwnerId(UUID ownerId) {
        return findByConnectionId(ownerId).or(() -> findByUploadedFileId(ownerId));
    }

    @SuppressWarnings("unchecked")
    public Map<UUID, Integer> findTableCountsByConnectionId() {
        List<Object[]> rows =
                getEntityManager()
                        .createNativeQuery(
                                "SELECT connection_id, json_array_length(schema_json::json)"
                                        + " FROM \"schema\""
                                        + " WHERE connection_id IS NOT NULL AND deleted_at IS NULL")
                        .getResultList();

        return rows.stream()
                .collect(
                        Collectors.toMap(
                                row -> (UUID) row[0], row -> ((Number) row[1]).intValue()));
    }

    public Optional<Integer> findTableCountByConnectionId(UUID connectionId) {
        return getEntityManager()
                .createNativeQuery(
                        "SELECT json_array_length(schema_json::json)"
                                + " FROM \"schema\""
                                + " WHERE connection_id = ?1 AND deleted_at IS NULL")
                .setParameter(1, connectionId)
                .getResultStream()
                .findFirst()
                .map(r -> ((Number) r).intValue());
    }

    @SuppressWarnings("unchecked")
    public Map<UUID, Integer> findTableCountsByUploadedFileId() {
        List<Object[]> rows =
                getEntityManager()
                        .createNativeQuery(
                                "SELECT uploaded_file_id, json_array_length(schema_json::json) FROM"
                                        + " \"schema\" WHERE uploaded_file_id IS NOT NULL AND"
                                        + " deleted_at IS NULL")
                        .getResultList();

        return rows.stream()
                .collect(
                        Collectors.toMap(
                                row -> (UUID) row[0], row -> ((Number) row[1]).intValue()));
    }

    public Optional<Integer> findTableCountByUploadedFileId(UUID uploadedFileId) {
        return getEntityManager()
                .createNativeQuery(
                        "SELECT json_array_length(schema_json::json)"
                                + " FROM \"schema\""
                                + " WHERE uploaded_file_id = ?1 AND deleted_at IS NULL")
                .setParameter(1, uploadedFileId)
                .getResultStream()
                .findFirst()
                .map(r -> ((Number) r).intValue());
    }
}
