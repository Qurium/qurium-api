/* Qurium - 2026 */
package org.qurium.schema.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qurium.schema.domain.Schema;
import org.qurium.schema.dto.SchemaDTO;

@Mapper(componentModel = "cdi")
public interface SchemaMapper {

    @Mapping(source = "connection.id", target = "connectionId")
    @Mapping(source = "uploadedFile.id", target = "uploadedFileId")
    SchemaDTO toDTO(Schema schema);
}
