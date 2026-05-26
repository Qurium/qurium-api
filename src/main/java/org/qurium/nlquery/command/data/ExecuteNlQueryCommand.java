/* Qurium - 2026 */
package org.qurium.nlquery.command.data;

import java.util.UUID;
import org.qurium.common.Command;

public record ExecuteNlQueryCommand(UUID connectionId, String question) implements Command {}
