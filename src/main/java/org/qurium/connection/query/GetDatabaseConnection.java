/* Qurium - 2026 */
package org.qurium.connection.query;

import static org.qurium.common.exception.QuriumExceptionCode.DATABASE_CONNECTION_NOT_FOUND;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.qurium.common.exception.QuriumException;
import org.qurium.connection.domain.DatabaseConnection;
import org.qurium.connection.dto.DatabaseConnectionDTO;
import org.qurium.connection.mapper.DatabaseConnectionMapper;
import org.qurium.connection.repository.DatabaseConnectionRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class GetDatabaseConnection {

    private final DatabaseConnectionMapper mapper;
    private final DatabaseConnectionRepository repository;

    public DatabaseConnectionDTO query(UUID id) {

        DatabaseConnection databaseConnection =
                repository
                        .findByIdOptional(id)
                        .orElseThrow(() -> new QuriumException(DATABASE_CONNECTION_NOT_FOUND));

        return mapper.toDTO(databaseConnection);
    }
}
