/* Qurium - 2026 */
package org.qurium.nlquery.query;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.qurium.common.PaginatedResponse;
import org.qurium.nlquery.dto.QueryHistoryResponseDTO;
import org.qurium.nlquery.mapper.NlQueryMapper;
import org.qurium.nlquery.repository.NlQueryRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class GetQueryExecutionHistory {

    private final NlQueryRepository nlQueryRepository;
    private final NlQueryMapper nlQueryMapper;

    public PaginatedResponse<QueryHistoryResponseDTO> query(UUID ownerId, int page, int size) {

        List<QueryHistoryResponseDTO> content =
                nlQueryRepository.findAllByOwnerId(ownerId, page, size).stream()
                        .map(nlQueryMapper::toDto)
                        .toList();

        long total = nlQueryRepository.countByOwnerId(ownerId);
        int totalPages = Math.max(1, (int) Math.ceil((double) total / size));

        return PaginatedResponse.of(content, page, size, total, totalPages);
    }
}
