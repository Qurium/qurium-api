/* Qurium - 2026 */
package org.qurium.connection.query;

import static org.qurium.common.exception.QuriumExceptionCode.DATABASE_CONNECTION_NOT_FOUND;

import jakarta.enterprise.context.ApplicationScoped;

import java.sql.Connection;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qurium.common.exception.QuriumException;
import org.qurium.connection.DatabaseConnectionFactory;
import org.qurium.connection.domain.DatabaseConnection;
import org.qurium.connection.dto.DatabaseConnectionDTO;
import org.qurium.connection.mapper.DatabaseConnectionMapper;
import org.qurium.connection.repository.DatabaseConnectionRepository;
import org.qurium.schema.repository.SchemaRepository;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class GetDatabaseConnection {

    private final DatabaseConnectionMapper mapper;
    private final DatabaseConnectionRepository repository;
    private final SchemaRepository schemaRepository;
    private final DatabaseConnectionFactory connectionFactory;

    public DatabaseConnectionDTO query(UUID id) {
        DatabaseConnection connection = repository
                .findByIdOptional(id)
                .orElseThrow(() -> new QuriumException(DATABASE_CONNECTION_NOT_FOUND));

        Integer tableCount = schemaRepository.findTableCountByConnectionId(id).orElse(null);

        return mapper.toDTO(connection, tableCount, isReachable(connection));
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
