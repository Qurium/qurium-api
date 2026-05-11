/* Qurium - 2026 */
package org.qurium.connection.command.handler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.qurium.common.CommandHandler;
import org.qurium.common.EncryptionService;
import org.qurium.connection.command.data.CreateDatabaseConnectionCommand;
import org.qurium.connection.repository.DatabaseConnectionRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class CreateDatabaseConnectionHandler
        implements CommandHandler<CreateDatabaseConnectionCommand, UUID> {

    private final DatabaseConnectionRepository connectionRepository;
    private final EncryptionService encryptionService;

    @Override
    @Transactional
    public UUID handle(CreateDatabaseConnectionCommand command) {

        String encryptedPassword = encryptionService.encrypt(command.request().getPassword());
        command.request().setPassword(encryptedPassword);
        return connectionRepository.store(command.request());
    }
}
