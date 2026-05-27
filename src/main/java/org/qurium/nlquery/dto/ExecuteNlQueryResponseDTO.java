/* Qurium - 2026 */
package org.qurium.nlquery.dto;

public record ExecuteNlQueryResponseDTO(
        String sql, String explanation, String resultSnapshot, boolean executed) {}
