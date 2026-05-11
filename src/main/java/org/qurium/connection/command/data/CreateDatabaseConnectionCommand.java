/* Qurium - 2026 */
package org.qurium.connection.command.data;

import org.qurium.common.Command;
import org.qurium.connection.dto.requests.CreateDatabaseConnectionRequest;

public record CreateDatabaseConnectionCommand(CreateDatabaseConnectionRequest request)
        implements Command {}
