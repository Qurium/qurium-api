/* Qurium - 2026 */
package org.qurium.connection.dto;

import java.time.Instant;
import java.util.UUID;
import org.qurium.connection.domain.DatabaseConnection;

public record DatabaseConnectionDTO(
        UUID id,
        String name,
        DatabaseConnection type,
        String host,
        Long port,
        String databaseName,
        Instant createdAt,
        Instant updatedAt) {}
