/* Qurium - 2026 */
package org.qurium.connection.query;

import jakarta.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qurium.connection.DatabaseConnectionFactory;
import org.qurium.connection.domain.DatabaseConnection;
import org.qurium.connection.dto.DatabaseConnectionDTO;
import org.qurium.connection.mapper.DatabaseConnectionMapper;
import org.qurium.connection.repository.DatabaseConnectionRepository;
import org.qurium.schema.repository.SchemaRepository;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class ListDatabaseConnections {

    private final DatabaseConnectionRepository connectionRepository;
    private final SchemaRepository schemaRepository;
    private final DatabaseConnectionMapper connectionMapper;
    private final DatabaseConnectionFactory connectionFactory;

    public List<DatabaseConnectionDTO> query() {

        Map<UUID, Integer> tableCountByConnectionId =
                schemaRepository.findTableCountsByConnectionId();

        return connectionRepository.findAll().list().stream()
                .map(
                        c ->
                                connectionMapper.toDTO(
                                        c,
                                        tableCountByConnectionId.getOrDefault(c.getId(), 0),
                                        isReachable(c)))
                .toList();
    }

    private boolean isReachable(DatabaseConnection c) {
        try (Connection ignored = connectionFactory.open(c)) {
            return true;
        } catch (Exception e) {
            log.warn("Connection check failed for '{}': {}", c.getName(), e.getMessage());
            return false;
        }
    }
}
