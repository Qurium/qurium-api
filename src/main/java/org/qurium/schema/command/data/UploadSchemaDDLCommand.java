/* Qurium - 2026 */
package org.qurium.schema.command.data;

import java.util.UUID;
import org.qurium.common.Command;

public record UploadSchemaDDLCommand(UUID connectionId, String schemaJson) implements Command {}
