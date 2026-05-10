/* Qurium - 2026 */
package org.qurium.connection.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qurium.connection.domain.DatabaseConnection;
import org.qurium.connection.dto.DatabaseConnectionDTO;

@Mapper(componentModel = "jakarta")
public interface DatabaseConnectionMapper {

    @Mapping(target = "username", ignore = true)
    @Mapping(target = "encryptedPassword", ignore = true)
    List<DatabaseConnectionDTO> toDTOs(List<DatabaseConnection> connection);
}
