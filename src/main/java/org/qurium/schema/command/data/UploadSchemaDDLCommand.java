/* Qurium - 2026 */
package org.qurium.schema.command.data;

import org.qurium.common.Command;

public record UploadSchemaDDLCommand(String name, String fileName, String schemaJson) implements Command {}
