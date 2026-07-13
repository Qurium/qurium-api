/* Qurium - 2026 */
package org.qurium.connection.command.data;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.qurium.common.Command;
import org.qurium.connection.dto.requests.UpdateDatabaseConnectionRequest;

public record UpdateDatabaseConnectionCommand(
        @NotNull UUID id, UpdateDatabaseConnectionRequest request) implements Command {}
