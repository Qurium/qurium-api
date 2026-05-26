/* Qurium - 2026 */
package org.qurium.nlquery.dto;

import jakarta.validation.constraints.NotBlank;

public record ExecuteNlQueryRequestDTO(@NotBlank String question) {}
