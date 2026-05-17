/* Qurium - 2026 */
package org.qurium.connection.command.handler;

import static org.qurium.common.exception.QuriumExceptionCode.DATABASE_CONNECTION_NOT_FOUND;

import jakarta.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import lombok.RequiredArgsConstructor;
import org.qurium.common.CommandHandler;
import org.qurium.common.exception.QuriumException;
import org.qurium.connection.DatabaseConnectionFactory;
import org.qurium.connection.command.data.TestDatabaseConnectionCommand;
import org.qurium.connection.domain.DatabaseConnection;
import org.qurium.connection.dto.TestConnectionDTO;
import org.qurium.connection.repository.DatabaseConnectionRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class TestDatabaseConnectionHandler
        implements CommandHandler<TestDatabaseConnectionCommand, TestConnectionDTO> {

    private final DatabaseConnectionRepository repository;
    private final DatabaseConnectionFactory connectionFactory;

    @Override
    public TestConnectionDTO handle(TestDatabaseConnectionCommand command) {

        DatabaseConnection databaseConnection =
                repository
                        .findByIdOptional(command.id())
                        .orElseThrow(() -> new QuriumException(DATABASE_CONNECTION_NOT_FOUND));

        long start = System.currentTimeMillis();

        try (Connection ignored = connectionFactory.open(databaseConnection)) {
            long latencyMs = System.currentTimeMillis() - start;
            return new TestConnectionDTO(true, "Connection established", latencyMs);

        } catch (Exception e) {
            long latencyMs = System.currentTimeMillis() - start;
            return new TestConnectionDTO(false, e.getMessage(), latencyMs);
        }
    }
}
