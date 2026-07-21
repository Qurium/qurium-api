/* Qurium - 2026 */
package org.qurium.schema.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qurium.schema.domain.Schema;
import org.qurium.schema.dto.SchemaDTO;

@Mapper(componentModel = "cdi")
public interface SchemaMapper {

    @Mapping(
            target = "ownerId",
            expression =
                    "java(schema.getConnection() != null ? schema.getConnection().getId() :"
                        + " schema.getUploadedFile() != null ? schema.getUploadedFile().getId() :"
                        + " null)")
    @Mapping(
            target = "ownerName",
            expression =
                    "java(schema.getConnection() != null ? schema.getConnection().getName() :"
                        + " schema.getUploadedFile() != null ? schema.getUploadedFile().getName() :"
                        + " null)")
    @Mapping(
            target = "dialect",
            expression =
                    "java(schema.getConnection() != null ? schema.getConnection().getType().name()"
                            + " : null)")
    SchemaDTO toDTO(Schema schema);

    default JsonNode map(String value) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(value);
    }
}
