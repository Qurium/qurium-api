/* Qurium - 2026 */
package org.qurium.connection.dto;

import java.time.Instant;
import java.util.UUID;
import org.qurium.connection.domain.DatabaseConnectionType;

public record DatabaseConnectionDTO(
        UUID id,
        String name,
        DatabaseConnectionType type,
        String host,
        Long port,
        String databaseName,
        String username,
        Integer tableCount,
        boolean isConnected,
        Instant connectedAt,
        Instant createdAt,
        Instant updatedAt) {}
