/* Qurium - 2026 */
package org.qurium.connection.query;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.qurium.connection.dto.ConnectionDTO;
import org.qurium.connection.mapper.ConnectionMapper;
import org.qurium.connection.repository.ConnectionRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class ListConnections {

    private final ConnectionRepository connectionRepository;
    private final ConnectionMapper connectionMapper;

    public List<ConnectionDTO> query() {

        return connectionMapper.toDTOs(connectionRepository.findAll().list());
    }
}
