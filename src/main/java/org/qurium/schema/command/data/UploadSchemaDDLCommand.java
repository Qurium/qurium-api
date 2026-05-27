/* Qurium - 2026 */
package org.qurium.schema.command.data;

import org.qurium.common.Command;

public record UploadSchemaDDLCommand(String schemaJson) implements Command {}
