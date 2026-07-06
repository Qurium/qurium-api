/* Qurium - 2026 */
package org.qurium.uploadedfile.mapper;

import org.mapstruct.Mapper;
import org.qurium.uploadedfile.domain.UploadedFile;
import org.qurium.uploadedfile.dto.UploadedFileDTO;

@Mapper(componentModel = "cdi")
public interface UploadedFileMapper {

    UploadedFileDTO toDTO(UploadedFile uploadedFile, Integer tableCount);
}
