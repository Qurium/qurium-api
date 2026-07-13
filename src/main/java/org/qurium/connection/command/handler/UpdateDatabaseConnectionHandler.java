/* Qurium - 2026 */
package org.qurium.connection.command.handler;

import static org.qurium.common.exception.QuriumExceptionCode.DATABASE_CONNECTION_ALREADY_EXISTS;
import static org.qurium.common.exception.QuriumExceptionCode.DATABASE_CONNECTION_NOT_FOUND;
import static org.qurium.common.exception.QuriumExceptionCode.DATABASE_CONNECTION_UNREACHABLE;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.sql.Connection;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.qurium.common.CommandHandler;
import org.qurium.common.EncryptionService;
import org.qurium.common.exception.QuriumException;
import org.qurium.connection.DatabaseConnectionFactory;
import org.qurium.connection.command.data.UpdateDatabaseConnectionCommand;
import org.qurium.connection.domain.DatabaseConnection;
import org.qurium.connection.dto.DatabaseConnectionDTO;
import org.qurium.connection.dto.requests.UpdateDatabaseConnectionRequest;
import org.qurium.connection.mapper.DatabaseConnectionMapper;
import org.qurium.connection.repository.DatabaseConnectionRepository;
import org.qurium.schema.repository.SchemaRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class UpdateDatabaseConnectionHandler
        implements CommandHandler<UpdateDatabaseConnectionCommand, DatabaseConnectionDTO> {

    private final DatabaseConnectionRepository connectionRepository;
    private final SchemaRepository schemaRepository;
    private final DatabaseConnectionFactory connectionFactory;
    private final EncryptionService encryptionService;
    private final DatabaseConnectionMapper connectionMapper;

    @Override
    @Transactional
    public DatabaseConnectionDTO handle(UpdateDatabaseConnectionCommand command) {
        DatabaseConnection connection =
                connectionRepository
                        .findByIdOptional(command.id())
                        .orElseThrow(() -> new QuriumException(DATABASE_CONNECTION_NOT_FOUND));

        UpdateDatabaseConnectionRequest request = command.request();

        connectionRepository
                .findByUniqueDataOptional(
                        request.getDatabaseName(),
                        connection.getType(),
                        request.getHost(),
                        request.getPort())
                .filter(c -> !c.getId().equals(command.id()))
                .ifPresent(
                        _ -> {
                            throw new QuriumException(DATABASE_CONNECTION_ALREADY_EXISTS);
                        });

        String effectiveUsername =
                request.getUsername() != null ? request.getUsername() : connection.getUsername();
        String effectivePassword =
                request.getPassword() != null
                        ? request.getPassword()
                        : connection.getEncryptedPassword() != null
                                ? encryptionService.decrypt(connection.getEncryptedPassword())
                                : null;

        try (Connection ignored =
                connectionFactory.open(
                        connection.getType(),
                        request.getHost(),
                        request.getPort(),
                        request.getDatabaseName(),
                        effectiveUsername,
                        effectivePassword)) {
            connection.setConnectedAt(Instant.now());
        } catch (QuriumException e) {
            throw e;
        } catch (Exception e) {
            throw new QuriumException(DATABASE_CONNECTION_UNREACHABLE);
        }

        connection.setName(request.getName());
        connection.setHost(request.getHost());
        connection.setPort(request.getPort());
        connection.setDatabaseName(request.getDatabaseName());

        if (request.getUsername() != null) {
            connection.setUsername(request.getUsername());
            connection.setEncryptedPassword(encryptionService.encrypt(request.getPassword()));
        } else {
            connection.setUsername(null);
            connection.setEncryptedPassword(null);
        }

        Integer tableCount =
                schemaRepository.findTableCountByConnectionId(connection.getId()).orElse(0);

        return connectionMapper.toDTO(connection, tableCount);
    }
}
