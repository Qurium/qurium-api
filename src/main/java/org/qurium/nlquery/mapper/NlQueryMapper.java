/* Qurium - 2026 */
package org.qurium.nlquery.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qurium.nlquery.domain.NlQuery;
import org.qurium.nlquery.dto.QueryHistoryResponseDTO;

@Mapper(componentModel = "cdi")
public interface NlQueryMapper {

    @Mapping(source = "createdAt", target = "executedAt")
    QueryHistoryResponseDTO toDto(NlQuery nlQuery);
}
