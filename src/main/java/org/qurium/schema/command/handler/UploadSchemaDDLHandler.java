/* Qurium - 2026 */
package org.qurium.schema.command.handler;

import static org.qurium.common.exception.QuriumExceptionCode.DATABASE_CONNECTION_NOT_FOUND;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.qurium.common.CommandHandler;
import org.qurium.common.exception.QuriumException;
import org.qurium.connection.domain.DatabaseConnection;
import org.qurium.connection.repository.DatabaseConnectionRepository;
import org.qurium.schema.command.data.UploadSchemaDDLCommand;
import org.qurium.schema.domain.SchemaSource;
import org.qurium.schema.repository.SchemaRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class UploadSchemaDDLHandler implements CommandHandler<UploadSchemaDDLCommand, UUID> {

    private final DatabaseConnectionRepository connectionRepository;
    private final SchemaRepository schemaRepository;

    @Override
    @Transactional
    public UUID handle(UploadSchemaDDLCommand command) {

        DatabaseConnection connection =
                connectionRepository
                        .findByIdOptional(command.connectionId())
                        .orElseThrow(() -> new QuriumException(DATABASE_CONNECTION_NOT_FOUND));

        schemaRepository.findByConnectionId(connection.getId()).ifPresent(schemaRepository::delete);

        return schemaRepository.store(connection, command.schemaJson(), SchemaSource.UPLOADED_DDL);
    }
}
