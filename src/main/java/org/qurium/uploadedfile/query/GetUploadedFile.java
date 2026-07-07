/* Qurium - 2026 */
package org.qurium.uploadedfile.query;

import static org.qurium.common.exception.QuriumExceptionCode.UPLOADED_FILE_NOT_FOUND;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.qurium.common.exception.QuriumException;
import org.qurium.schema.repository.SchemaRepository;
import org.qurium.uploadedfile.domain.UploadedFile;
import org.qurium.uploadedfile.dto.UploadedFileDTO;
import org.qurium.uploadedfile.mapper.UploadedFileMapper;
import org.qurium.uploadedfile.repository.UploadedFileRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class GetUploadedFile {

    private final UploadedFileRepository uploadedFileRepository;
    private final SchemaRepository schemaRepository;
    private final UploadedFileMapper uploadedFileMapper;

    public UploadedFileDTO query(UUID id) {
        UploadedFile uploadedFile =
                uploadedFileRepository
                        .findByIdOptional(id)
                        .orElseThrow(() -> new QuriumException(UPLOADED_FILE_NOT_FOUND));

        Integer tableCount = schemaRepository.findTableCountByUploadedFileId(id).orElse(null);

        return uploadedFileMapper.toDTO(uploadedFile, tableCount);
    }
}
