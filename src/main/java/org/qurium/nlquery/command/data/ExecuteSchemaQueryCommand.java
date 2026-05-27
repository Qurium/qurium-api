/* Qurium - 2026 */
package org.qurium.nlquery.command.data;

import java.util.UUID;
import org.qurium.common.Command;

public record ExecuteSchemaQueryCommand(UUID schemaId, String question) implements Command {}
