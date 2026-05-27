/* Qurium - 2026 */
package org.qurium.schema.query;

import static org.qurium.common.exception.QuriumExceptionCode.SCHEMA_NOT_FOUND;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.qurium.common.exception.QuriumException;
import org.qurium.schema.dto.SchemaDTO;
import org.qurium.schema.mapper.SchemaMapper;
import org.qurium.schema.repository.SchemaRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class GetSchemaById {

    private final SchemaRepository schemaRepository;
    private final SchemaMapper schemaMapper;

    public SchemaDTO query(UUID schemaId) {

        return schemaRepository
                .findByIdOptional(schemaId)
                .map(schemaMapper::toDTO)
                .orElseThrow(() -> new QuriumException(SCHEMA_NOT_FOUND));
    }
}
