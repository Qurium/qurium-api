/* Qurium - 2026 */
package org.qurium.nlquery.dto;

import java.time.Instant;
import org.qurium.nlquery.domain.NlQueryStatus;

public record QueryHistoryResponseDTO(
        String question,
        NlQueryStatus status,
        Instant executedAt,
        Long executionTimeMs,
        Integer rowsReturned) {}
