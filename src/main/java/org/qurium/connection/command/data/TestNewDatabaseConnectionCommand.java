/* Qurium - 2026 */
package org.qurium.connection.command.data;

import org.qurium.common.Command;
import org.qurium.connection.domain.DatabaseConnectionType;

public record TestNewDatabaseConnectionCommand(
        DatabaseConnectionType type,
        String host,
        Long port,
        String databaseName,
        String username,
        String password)
        implements Command {}
