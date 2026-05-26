/* Qurium - 2026 */
package org.qurium.nlquery.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.qurium.common.exception.QuriumErrorResponse;
import org.qurium.nlquery.command.data.ExecuteNlQueryCommand;
import org.qurium.nlquery.command.handler.ExecuteNlQueryHandler;
import org.qurium.nlquery.dto.ExecuteNlQueryRequestDTO;
import org.qurium.nlquery.dto.ExecuteNlQueryResponseDTO;

@Path("/api/connections/{connectionId}/query")
@RequiredArgsConstructor
public class NlQueryResource {

    private final ExecuteNlQueryHandler executeNlQueryHandler;

    @POST
    @Path("")
    @Operation(
            summary = "Execute query command",
            description =
                    "Connects to the external database and imports its schema (tables, columns,"
                            + " types).")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Query executed successfully",
                content =
                        @Content(
                                schema =
                                        @Schema(implementation = ExecuteNlQueryResponseDTO.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Failed to execute query",
                content = @Content(schema = @Schema(implementation = QuriumErrorResponse.class)))
    })
    public ExecuteNlQueryResponseDTO executeNlQuery(
            @Parameter(description = "connection id") @PathParam("connectionId") UUID connectionId,
            @Parameter(description = "question") @Valid ExecuteNlQueryRequestDTO request) {

        return executeNlQueryHandler.handle(
                new ExecuteNlQueryCommand(connectionId, request.question()));
    }
}
