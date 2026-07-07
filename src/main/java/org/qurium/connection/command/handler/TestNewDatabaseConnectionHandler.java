/* Qurium - 2026 */
package org.qurium.connection.command.handler;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.qurium.common.CommandHandler;
import org.qurium.connection.DatabaseConnectionFactory;
import org.qurium.connection.command.data.TestNewDatabaseConnectionCommand;
import org.qurium.connection.dto.TestNewConnectionDTO;

@ApplicationScoped
@RequiredArgsConstructor
public class TestNewDatabaseConnectionHandler
        implements CommandHandler<TestNewDatabaseConnectionCommand, TestNewConnectionDTO> {

    private final DatabaseConnectionFactory connectionFactory;

    @Override
    public TestNewConnectionDTO handle(TestNewDatabaseConnectionCommand command) {

        try (var ignored =
                connectionFactory.open(
                        command.type(),
                        command.host(),
                        command.port(),
                        command.databaseName(),
                        command.username(),
                        command.password())) {
            return new TestNewConnectionDTO(true);
        } catch (Exception e) {
            return new TestNewConnectionDTO(false);
        }
    }
}
