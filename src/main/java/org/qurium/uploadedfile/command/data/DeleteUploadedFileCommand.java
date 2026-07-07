/* Qurium - 2026 */
package org.qurium.uploadedfile.command.data;

import java.util.UUID;
import org.qurium.common.Command;

public record DeleteUploadedFileCommand(UUID id) implements Command {}
