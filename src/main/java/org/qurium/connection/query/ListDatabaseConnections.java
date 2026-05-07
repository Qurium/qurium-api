/* Qurium - 2026 */
package org.qurium.connection.query;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.qurium.connection.dto.DatabaseConnectionDTO;
import org.qurium.connection.mapper.DatabaseConnectionMapper;
import org.qurium.connection.repository.DatabaseConnectionRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class ListDatabaseConnections {

    private final DatabaseConnectionRepository connectionRepository;
    private final DatabaseConnectionMapper connectionMapper;

    public List<DatabaseConnectionDTO> query() {

        return connectionMapper.toDTOs(connectionRepository.findAll().list());
    }
}
