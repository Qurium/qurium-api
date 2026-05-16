/* Qurium - 2026 */
package org.qurium.connection.command.handler;

import static org.qurium.common.exception.QuriumExceptionCode.DATABASE_CONNECTION_ALREADY_EXISTS;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.qurium.common.CommandHandler;
import org.qurium.common.EncryptionService;
import org.qurium.common.exception.QuriumException;
import org.qurium.connection.DatabaseConnectionFactory;
import org.qurium.connection.command.data.CreateDatabaseConnectionCommand;
import org.qurium.connection.dto.requests.CreateDatabaseConnectionRequest;
import org.qurium.connection.repository.DatabaseConnectionRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class CreateDatabaseConnectionHandler
        implements CommandHandler<CreateDatabaseConnectionCommand, UUID> {

    private final DatabaseConnectionRepository connectionRepository;
    private final DatabaseConnectionFactory connectionFactory;
    private final EncryptionService encryptionService;

    @Override
    @Transactional
    public UUID handle(CreateDatabaseConnectionCommand command) {
        CreateDatabaseConnectionRequest request = command.request();

        connectionRepository
                .findByUniqueDataOptional(
                        request.getDatabaseName(),
                        request.getType(),
                        request.getHost(),
                        request.getPort())
                .ifPresent(
                        _ -> {
                            throw new QuriumException(DATABASE_CONNECTION_ALREADY_EXISTS);
                        });

        connectionFactory.verify(
                request.getType(),
                request.getHost(),
                request.getPort(),
                request.getDatabaseName(),
                request.getUsername(),
                request.getPassword());

        request.setPassword(encryptionService.encrypt(request.getPassword()));
        return connectionRepository.store(request);
    }
}
