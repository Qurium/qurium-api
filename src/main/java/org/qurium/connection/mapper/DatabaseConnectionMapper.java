/* Qurium - 2026 */
package org.qurium.connection.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qurium.connection.domain.DatabaseConnection;
import org.qurium.connection.dto.DatabaseConnectionDTO;

@Mapper(componentModel = "cdi")
public interface DatabaseConnectionMapper {

    List<DatabaseConnectionDTO> toDTOs(List<DatabaseConnection> connection);

    DatabaseConnectionDTO toDTO(DatabaseConnection connection);
}
