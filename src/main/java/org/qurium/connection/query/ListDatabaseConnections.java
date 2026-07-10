/* Qurium - 2026 */
package org.qurium.connection.query;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.qurium.connection.dto.DatabaseConnectionDTO;
import org.qurium.connection.mapper.DatabaseConnectionMapper;
import org.qurium.connection.repository.DatabaseConnectionRepository;
import org.qurium.schema.repository.SchemaRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class ListDatabaseConnections {

    private final DatabaseConnectionRepository connectionRepository;
    private final SchemaRepository schemaRepository;
    private final DatabaseConnectionMapper connectionMapper;

    public List<DatabaseConnectionDTO> query() {

        Map<UUID, Integer> tableCountByConnectionId =
                schemaRepository.findTableCountsByConnectionId();

        return connectionRepository.findAll().list().stream()
                .map(
                        c ->
                                connectionMapper.toDTO(
                                        c, tableCountByConnectionId.getOrDefault(c.getId(), 0)))
                .toList();
    }
}
