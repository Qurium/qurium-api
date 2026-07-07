/* Qurium - 2026 */
package org.qurium.uploadedfile.dto;

import java.time.Instant;
import java.util.UUID;

public record UploadedFileDTO(
        UUID id, String name, Instant createdAt, Instant updatedAt, Integer tableCount) {}
