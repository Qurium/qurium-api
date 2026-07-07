/* Qurium - 2026 */
package org.qurium.connection.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qurium.connection.domain.DatabaseConnection;
import org.qurium.connection.dto.DatabaseConnectionDTO;

@Mapper(componentModel = "cdi")
public interface DatabaseConnectionMapper {

    @Mapping(source = "tableCount", target = "tableCount")
    @Mapping(source = "isConnected", target = "isConnected")
    DatabaseConnectionDTO toDTO(
            DatabaseConnection connection, Integer tableCount, boolean isConnected);
}
