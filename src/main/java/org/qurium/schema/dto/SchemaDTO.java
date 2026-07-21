/* Qurium - 2026 */
package org.qurium.schema.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;
import org.qurium.schema.domain.SchemaSource;

public record SchemaDTO(
        UUID id,
        UUID ownerId,
        String ownerName,
        String dialect,
        JsonNode schemaJson,
        SchemaSource source,
        Instant createdAt) {}
