/* Qurium - 2026 */
package org.qurium.schema.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.*;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.qurium.common.exception.QuriumErrorResponse;
import org.qurium.schema.command.data.ImportSchemaCommand;
import org.qurium.schema.command.handler.ImportSchemaHandler;
import org.qurium.schema.dto.SchemaDTO;
import org.qurium.schema.query.GetSchema;

@Path("/api/{ownerId}/schema")
@RequiredArgsConstructor
public class SchemaResource {

    private final ImportSchemaHandler importSchemaHandler;
    private final GetSchema getSchema;

    @POST
    @Path("/introspect")
    @Operation(
            summary = "Import schema from connection",
            description =
                    "Connects to the external database and imports its schema (tables, columns,"
                            + " types).")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Schema imported successfully",
                content = @Content(schema = @Schema(implementation = UUID.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Failed to import schema from database",
                content = @Content(schema = @Schema(implementation = QuriumErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Database connection not found",
                content = @Content(schema = @Schema(implementation = QuriumErrorResponse.class)))
    })
    public UUID introspect(
            @Parameter(description = "ownerId id") @PathParam("ownerId") UUID connectionId) {

        return importSchemaHandler.handle(new ImportSchemaCommand(connectionId));
    }

    @GET
    @Operation(
            summary = "Get schema by connection",
            description = "Returns the stored schema for a database connection.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Schema retrieved successfully",
                content = @Content(schema = @Schema(implementation = SchemaDTO.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Schema not found for this connection",
                content = @Content(schema = @Schema(implementation = QuriumErrorResponse.class)))
    })
    public SchemaDTO getSchema(
            @Parameter(description = "ownerId id") @PathParam("ownerId") UUID ownerId) {

        return getSchema.query(ownerId);
    }
}
