/* Qurium - 2026 */
package org.qurium.connection;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.qurium.common.PaginatedResponse;
import org.qurium.common.exception.QuriumErrorResponse;
import org.qurium.connection.command.data.CreateDatabaseConnectionCommand;
import org.qurium.connection.command.handler.CreateDatabaseConnectionHandler;
import org.qurium.connection.dto.DatabaseConnectionDTO;
import org.qurium.connection.dto.requests.CreateDatabaseConnectionRequest;
import org.qurium.connection.query.ListDatabaseConnections;

@Path("/api/connections")
@RequiredArgsConstructor
public class DatabaseConnectionResource {

    private final ListDatabaseConnections listConnections;
    private final CreateDatabaseConnectionHandler createDatabaseConnectionHandler;

    @GET
    @Operation(
            summary = "List connections",
            description = "Returns a paginated list of all database connections.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Paginated list of connections",
                content = @Content(schema = @Schema(implementation = PaginatedResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid pagination parameters",
                content = @Content(schema = @Schema(implementation = QuriumErrorResponse.class)))
    })
    public PaginatedResponse<DatabaseConnectionDTO> listConnections(
            @Parameter(description = "Number of the page") @QueryParam("page") int page,
            @Parameter(description = "Number of items per page")
                    @QueryParam("size")
                    @Min(1)
                    @Max(100)
                    int size) {

        return PaginatedResponse.of(listConnections.query(), page, size);
    }

    @POST
    @Operation(
            summary = "Create database connection",
            description = "Returns the UUID of a newly created database connection")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Database connection created successfully",
                content = @Content(schema = @Schema(implementation = UUID.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Error while creating database connection",
                content = @Content(schema = @Schema(implementation = QuriumErrorResponse.class)))
    })
    public UUID createConnection(@Valid CreateDatabaseConnectionRequest request) {

        return createDatabaseConnectionHandler.handle(new CreateDatabaseConnectionCommand(request));
    }
}
