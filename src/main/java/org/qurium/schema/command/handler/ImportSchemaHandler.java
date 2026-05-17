/* Qurium - 2026 */
package org.qurium.schema.command.handler;

import static org.qurium.common.exception.QuriumExceptionCode.DATABASE_CONNECTION_NOT_FOUND;
import static org.qurium.common.exception.QuriumExceptionCode.SCHEMA_IMPORT_FAILED;

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

            schemaRepository
                    .findByConnectionId(connection.getId())
                    .ifPresent(schemaRepository::delete);

            return schemaRepository.store(connection, schemaJson, SchemaSource.CONNECTED);

        } catch (Exception e) {
            throw new QuriumException(SCHEMA_IMPORT_FAILED);
        }
    }
}
