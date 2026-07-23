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
                            + " : extractDialectFromJson(schema.getSchemaJson()))")
    SchemaDTO toDTO(Schema schema);

    default JsonNode map(String value) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(value);
    }

    default String extractDialectFromJson(String schemaJson) {
        if (schemaJson == null) return null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(schemaJson);
            if (root.isArray() && !root.isEmpty()) {
                JsonNode dialect = root.get(0).get("dialect");
                if (dialect != null && !dialect.isNull()) return dialect.asText();
            }
        } catch (JsonProcessingException ignored) {
        }
        return null;
    }
}
