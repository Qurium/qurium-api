/* Qurium - 2026 */
package org.qurium.connection.api;

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
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.qurium.common.PaginatedResponse;
import org.qurium.common.exception.QuriumErrorResponse;
import org.qurium.connection.command.data.*;
import org.qurium.connection.command.handler.*;
import org.qurium.connection.dto.DatabaseConnectionDTO;
import org.qurium.connection.dto.TestConnectionDTO;
import org.qurium.connection.dto.TestNewConnectionDTO;
import org.qurium.connection.dto.requests.CreateDatabaseConnectionRequest;
import org.qurium.connection.dto.requests.TestNewDatabaseConnectionRequest;
import org.qurium.connection.dto.requests.UpdateDatabaseConnectionRequest;
import org.qurium.connection.query.GetDatabaseConnection;
import org.qurium.connection.query.ListDatabaseConnections;
import org.qurium.connection.query.ListOnlineDatabaseConnections;

@Path("/api/connections")
@RequiredArgsConstructor
public class DatabaseConnectionResource {

    private final ListDatabaseConnections listConnections;
    private final CreateDatabaseConnectionHandler createDatabaseConnectionHandler;
    private final GetDatabaseConnection getDatabaseConnection;
    private final DeleteDatabaseConnectionHandler deleteDatabaseConnectionHandler;
    private final TestDatabaseConnectionHandler testDatabaseConnectionHandler;
    private final TestNewDatabaseConnectionHandler testNewDatabaseConnectionHandler;
    private final ReconnectDatabaseConnectionHandler reconnectDatabaseConnectionHandler;
    private final UpdateDatabaseConnectionHandler updateDatabaseConnectionHandler;
    private final ListOnlineDatabaseConnections listOnlineDatabaseConnections;

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

    @GET
    @Path("/online")
    @Operation(
            summary = "List online connections",
            description = "Returns a paginated list of all database connections that are online.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Paginated list of online connections",
                content = @Content(schema = @Schema(implementation = PaginatedResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid pagination parameters",
                content = @Content(schema = @Schema(implementation = QuriumErrorResponse.class)))
    })
    public PaginatedResponse<DatabaseConnectionDTO> listOnlineConnections(
            @Parameter(description = "Number of the page") @QueryParam("page") int page,
            @Parameter(description = "Number of items per page")
                    @QueryParam("size")
                    @Min(1)
                    @Max(100)
                    int size) {

        return PaginatedResponse.of(listOnlineDatabaseConnections.query(), page, size);
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

    @GET
    @Path("/{id}")
    @Operation(summary = "Get a connection", description = "Returns a specific database connection")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Paginated list of connections",
                content = @Content(schema = @Schema(implementation = PaginatedResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Error while fetching database connection",
                content = @Content(schema = @Schema(implementation = QuriumErrorResponse.class)))
    })
    public DatabaseConnectionDTO getConnection(
            @Parameter(description = "connection id") @PathParam("id") UUID id) {

        return getDatabaseConnection.query(id);
    }

    @DELETE
    @Path("/{id}")
    @Operation(
            summary = "Delete database connection",
            description = "Soft-deletes a database connection by setting its deleted_at timestamp.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "204",
                description = "Database connection deleted successfully"),
        @ApiResponse(
                responseCode = "404",
                description = "Database connection not found",
                content = @Content(schema = @Schema(implementation = QuriumErrorResponse.class)))
    })
    public Response deleteConnection(
            @Parameter(description = "connection id") @PathParam("id") UUID id) {

        deleteDatabaseConnectionHandler.handle(new DeleteDatabaseConnectionCommand(id));
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/test")
    @Operation(
            summary = "Test a database connection",
            description = "Tests the connection of a stored database")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Database is connected successfully"),
        @ApiResponse(
                responseCode = "404",
                description = "Could not re-establish the connection to the selected database",
                content = @Content(schema = @Schema(implementation = QuriumErrorResponse.class)))
    })
    public TestConnectionDTO testConnection(
            @Parameter(description = "connection id") @PathParam("id") UUID id) {

        return testDatabaseConnectionHandler.handle(new TestDatabaseConnectionCommand(id));
    }

    @PATCH
    @Path("/{id}/reconnect")
    @Operation(
            summary = "Reconnect a database connection",
            description =
                    "Attempts to re-establish the connection. On success, persists connectedAt."
                            + " On failure, leaves the existing connectedAt unchanged.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Reconnect attempted successfully"),
        @ApiResponse(
                responseCode = "404",
                description = "Database connection not found",
                content = @Content(schema = @Schema(implementation = QuriumErrorResponse.class)))
    })
    public void reconnectConnection(
            @Parameter(description = "connection id") @PathParam("id") UUID id) {

        reconnectDatabaseConnectionHandler.handle(new ReconnectDatabaseConnectionCommand(id));
    }

    @PUT
    @Path("/{id}")
    @Operation(
            summary = "Update database connection",
            description =
                    "Updates connection details, re-tests connectivity, and refreshes connectedAt."
                            + " Type cannot be changed. Credentials are optional but must be sent"
                            + " together.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Connection updated successfully",
                content = @Content(schema = @Schema(implementation = DatabaseConnectionDTO.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Could not reach the database with the provided details",
                content = @Content(schema = @Schema(implementation = QuriumErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Database connection not found",
                content = @Content(schema = @Schema(implementation = QuriumErrorResponse.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Another connection already targets the same database",
                content = @Content(schema = @Schema(implementation = QuriumErrorResponse.class)))
    })
    public DatabaseConnectionDTO updateConnection(
            @Parameter(description = "connection id") @PathParam("id") UUID id,
            @Valid UpdateDatabaseConnectionRequest request) {

        return updateDatabaseConnectionHandler.handle(
                new UpdateDatabaseConnectionCommand(id, request));
    }

    @POST
    @Path("/test")
    @Operation(
            summary = "Test new connection credentials",
            description =
                    "Tests whether the provided connection details can reach the database."
                            + " Returns true if reachable, false otherwise.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Connectivity result returned")
    })
    public TestNewConnectionDTO testNonStoredConnection(
            @Valid TestNewDatabaseConnectionRequest request) {

        return testNewDatabaseConnectionHandler.handle(
                new TestNewDatabaseConnectionCommand(
                        request.getType(),
                        request.getHost(),
                        request.getPort(),
                        request.getDatabaseName(),
                        request.getUsername(),
                        request.getPassword()));
    }
}
