/* Qurium - 2026 */
package org.qurium.uploadedfile.command.data;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.qurium.common.Command;

public record ReuploadFileCommand(@NotNull UUID id, String name, String fileName, String schema)
        implements Command {}
