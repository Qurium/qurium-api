/* Qurium - 2026 */
package org.qurium.schema.query;

import static org.qurium.common.exception.QuriumExceptionCode.*;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.qurium.common.exception.QuriumException;
import org.qurium.connection.domain.DatabaseConnection;
import org.qurium.connection.repository.DatabaseConnectionRepository;
import org.qurium.schema.dto.SchemaDTO;
import org.qurium.schema.mapper.SchemaMapper;
import org.qurium.schema.repository.SchemaRepository;
import org.qurium.uploadedfile.domain.UploadedFile;
import org.qurium.uploadedfile.repository.UploadedFileRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class GetSchema {

    private final DatabaseConnectionRepository connectionRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final SchemaRepository schemaRepository;
    private final SchemaMapper schemaMapper;

    public SchemaDTO query(UUID ownerId) {

        Optional<DatabaseConnection> databaseConnection =
                connectionRepository.findByIdOptional(ownerId);
        Optional<UploadedFile> uploadedFile = uploadedFileRepository.findByIdOptional(ownerId);

        if (databaseConnection.isEmpty() && uploadedFile.isEmpty()) {
            throw new QuriumException(SCHEMA_NOT_FOUND);
        }

        return databaseConnection.isPresent()
                ? schemaRepository
                        .findByConnectionId(ownerId)
                        .map(schemaMapper::toDTO)
                        .orElseThrow(() -> new QuriumException(SCHEMA_NOT_FOUND))
                : schemaRepository
                        .findByUploadedFileId(ownerId)
                        .map(schemaMapper::toDTO)
                        .orElseThrow(() -> new QuriumException(SCHEMA_NOT_FOUND));
    }
}
