/* Qurium - 2026 */
package org.qurium.uploadedfile.query;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.qurium.schema.repository.SchemaRepository;
import org.qurium.uploadedfile.dto.UploadedFileDTO;
import org.qurium.uploadedfile.mapper.UploadedFileMapper;
import org.qurium.uploadedfile.repository.UploadedFileRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class ListUploadedFiles {

    private final UploadedFileRepository uploadedFileRepository;
    private final SchemaRepository schemaRepository;
    private final UploadedFileMapper uploadedFileMapper;

    public List<UploadedFileDTO> query() {
        Map<UUID, Integer> tableCountByUploadedFileId =
                schemaRepository.findTableCountsByUploadedFileId();

        return uploadedFileRepository.findAll().list().stream()
                .map(
                        uf ->
                                uploadedFileMapper.toDTO(
                                        uf, tableCountByUploadedFileId.getOrDefault(uf.getId(), 0)))
                .toList();
    }
}
