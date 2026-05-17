/* Qurium - 2026 */
package org.qurium.schema.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;
import org.qurium.connection.domain.DatabaseConnection;
import org.qurium.schema.domain.Schema;
import org.qurium.schema.domain.SchemaSource;

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

    public Optional<Schema> findByConnectionId(UUID connectionId) {
        return find("connection.id", connectionId).firstResultOptional();
    }
}
