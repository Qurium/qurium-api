/* Qurium - 2026 */
package org.qurium.connection.command.handler;

import static org.qurium.common.exception.QuriumExceptionCode.DATABASE_CONNECTION_NOT_FOUND;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.qurium.common.CommandHandler;
import org.qurium.common.exception.QuriumException;
import org.qurium.connection.command.data.DeleteDatabaseConnectionCommand;
import org.qurium.connection.domain.DatabaseConnection;
import org.qurium.connection.repository.DatabaseConnectionRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class DeleteDatabaseConnectionHandler
        implements CommandHandler<DeleteDatabaseConnectionCommand, Void> {

    private final DatabaseConnectionRepository repository;

    @Transactional
    public Void handle(DeleteDatabaseConnectionCommand command) {

        DatabaseConnection connection =
                repository
                        .findActiveById(command.connectionId())
                        .orElseThrow(() -> new QuriumException(DATABASE_CONNECTION_NOT_FOUND));

        connection.setDeletedAt(Instant.now());

        return null;
    }
}
