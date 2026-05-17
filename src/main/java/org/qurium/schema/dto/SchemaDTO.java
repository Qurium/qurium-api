/* Qurium - 2026 */
package org.qurium.schema.dto;

import java.time.Instant;
import java.util.UUID;
import org.qurium.schema.domain.SchemaSource;

public record SchemaDTO(
        UUID id, UUID connectionId, String schemaJson, SchemaSource source, Instant createdAt) {}
