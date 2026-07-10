/* Qurium - 2026 */
package org.qurium.connection.command.handler;

import static org.qurium.common.exception.QuriumExceptionCode.DATABASE_CONNECTION_NOT_FOUND;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.sql.Connection;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qurium.common.CommandHandler;
import org.qurium.common.exception.QuriumException;
import org.qurium.connection.DatabaseConnectionFactory;
import org.qurium.connection.command.data.ReconnectDatabaseConnectionCommand;
import org.qurium.connection.domain.DatabaseConnection;
import org.qurium.connection.repository.DatabaseConnectionRepository;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class ReconnectDatabaseConnectionHandler
        implements CommandHandler<ReconnectDatabaseConnectionCommand, Void> {

    private final DatabaseConnectionRepository repository;
    private final DatabaseConnectionFactory connectionFactory;

    @Override
    @Transactional
    public Void handle(ReconnectDatabaseConnectionCommand command) {
        DatabaseConnection connection =
                repository
                        .findByIdOptional(command.id())
                        .orElseThrow(() -> new QuriumException(DATABASE_CONNECTION_NOT_FOUND));

        try (Connection ignored = connectionFactory.open(connection)) {
            connection.setConnectedAt(Instant.now());
        } catch (Exception e) {
            log.warn("Reconnect failed for '{}': {}", connection.getName(), e.getMessage());
        }

        return null;
    }
}
