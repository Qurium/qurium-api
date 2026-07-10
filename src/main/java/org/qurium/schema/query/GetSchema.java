/* Qurium - 2026 */
package org.qurium.schema.query;

import static org.qurium.common.exception.QuriumExceptionCode.DATABASE_CONNECTION_NOT_FOUND;
import static org.qurium.common.exception.QuriumExceptionCode.SCHEMA_NOT_FOUND;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.qurium.common.exception.QuriumException;
import org.qurium.connection.repository.DatabaseConnectionRepository;
import org.qurium.schema.dto.SchemaDTO;
import org.qurium.schema.mapper.SchemaMapper;
import org.qurium.schema.repository.SchemaRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class GetSchema {

    private final DatabaseConnectionRepository connectionRepository;
    private final SchemaRepository schemaRepository;
    private final SchemaMapper schemaMapper;

    public SchemaDTO query(UUID connectionId) {

        connectionRepository
                .findByIdOptional(connectionId)
                .orElseThrow(() -> new QuriumException(DATABASE_CONNECTION_NOT_FOUND));

        return schemaRepository
                .findByConnectionId(connectionId)
                .map(schemaMapper::toDTO)
                .orElseThrow(() -> new QuriumException(SCHEMA_NOT_FOUND));
    }
}
