/* Qurium - 2026 */
package org.qurium.schema.command.handler;

import static org.qurium.common.exception.QuriumExceptionCode.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.sql.Connection;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.qurium.common.CommandHandler;
import org.qurium.common.exception.QuriumException;
import org.qurium.connection.DatabaseConnectionFactory;
import org.qurium.connection.domain.DatabaseConnection;
import org.qurium.connection.repository.DatabaseConnectionRepository;
import org.qurium.schema.command.data.ImportSchemaCommand;
import org.qurium.schema.domain.Schema;
import org.qurium.schema.domain.SchemaSource;
import org.qurium.schema.repository.SchemaRepository;
import org.qurium.schema.service.SchemaIntrospectionService;

@ApplicationScoped
@RequiredArgsConstructor
public class ImportSchemaHandler implements CommandHandler<ImportSchemaCommand, UUID> {

    private final DatabaseConnectionRepository connectionRepository;
    private final SchemaRepository schemaRepository;
    private final DatabaseConnectionFactory connectionFactory;
    private final SchemaIntrospectionService introspectionService;

    @Override
    @Transactional
    public UUID handle(ImportSchemaCommand command) {

        DatabaseConnection connection =
                connectionRepository
                        .findByIdOptional(command.connectionId())
                        .orElseThrow(() -> new QuriumException(DATABASE_CONNECTION_NOT_FOUND));

        try (Connection jdbc = connectionFactory.open(connection)) {
            String schemaJson = introspectionService.introspect(jdbc);

            Schema schema =
                    schemaRepository
                            .findByConnectionId(connection.getId())
                            .orElseGet(
                                    () -> {
                                        Schema s = new Schema();
                                        s.setConnection(connection);
                                        return s;
                                    });

            schema.setSchemaJson(schemaJson);
            schema.setSource(SchemaSource.CONNECTED);

            if (schema.getId() == null) {
                schemaRepository.persist(schema);
            }
            return schema.getId();

        } catch (QuriumException e) {
            throw e;
        } catch (Exception e) {
            throw new QuriumException(SCHEMA_IMPORT_FAILED);
        }
    }
}
