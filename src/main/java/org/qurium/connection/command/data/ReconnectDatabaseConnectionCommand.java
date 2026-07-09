/* Qurium - 2026 */
package org.qurium.connection.command.data;

import java.util.UUID;
import org.qurium.common.Command;

public record ReconnectDatabaseConnectionCommand(UUID id) implements Command {}
