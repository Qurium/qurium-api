/* Qurium - 2026 */
package org.qurium.nlquery.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.qurium.common.PaginatedResponse;
import org.qurium.common.exception.QuriumErrorResponse;
import org.qurium.nlquery.command.data.ExecuteNlQueryCommand;
import org.qurium.nlquery.command.handler.ExecuteNlQueryHandler;
import org.qurium.nlquery.dto.ExecuteNlQueryRequestDTO;
import org.qurium.nlquery.dto.ExecuteNlQueryResponseDTO;
import org.qurium.nlquery.dto.QueryHistoryResponseDTO;
import org.qurium.nlquery.query.GetQueryExecutionHistory;

@Path("/api/{ownerId}/query")
@RequiredArgsConstructor
public class NlQueryResource {

    private final ExecuteNlQueryHandler executeNlQueryHandler;
    private final GetQueryExecutionHistory getQueryExecutionHistory;

    @POST
    @Path("")
    @Operation(
            summary = "Execute NL query",
            description =
                    "Translates a natural-language question into SQL using the owner's schema. If"
                        + " the owner is a live connection the response is marked as executable;"
                        + " for uploaded-file schemas it returns SQL only.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "SQL generated successfully",
                content =
                        @Content(
                                schema =
                                        @Schema(implementation = ExecuteNlQueryResponseDTO.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Schema not found",
                content = @Content(schema = @Schema(implementation = QuriumErrorResponse.class)))
    })
    public ExecuteNlQueryResponseDTO executeNlQuery(
            @Parameter(description = "connection or uploaded-file id") @PathParam("ownerId")
                    UUID ownerId,
            @Parameter(description = "question") @Valid ExecuteNlQueryRequestDTO request) {

        return executeNlQueryHandler.handle(new ExecuteNlQueryCommand(ownerId, request.question()));
    }

    @GET
    @Path("/history")
    public PaginatedResponse<QueryHistoryResponseDTO> getQueryExecutionHistory(
            @Parameter(description = "connection or uploaded-file id") @PathParam("ownerId")
                    UUID ownerId,
            @Parameter(description = "Number of the page") @QueryParam("page") int page,
            @Parameter(description = "Number of items per page")
                    @QueryParam("size")
                    @DefaultValue("20")
                    @Min(1)
                    @Max(100)
                    int size) {

        return getQueryExecutionHistory.query(ownerId, page, size);
    }
}
