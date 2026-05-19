/* Qurium - 2026 */
package org.qurium.schema;

import static java.nio.file.Files.readString;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.qurium.common.exception.QuriumErrorResponse;
import org.qurium.schema.command.data.ImportSchemaCommand;
import org.qurium.schema.command.data.UploadSchemaDDLCommand;
import org.qurium.schema.command.handler.ImportSchemaHandler;
import org.qurium.schema.command.handler.UploadSchemaDDLHandler;
import org.qurium.schema.dto.SchemaDTO;
import org.qurium.schema.query.GetSchema;
import org.qurium.schema.service.DDLParserService;

@Path("/api/connections/{connectionId}/schema")
@RequiredArgsConstructor
public class SchemaResource {

    private final ImportSchemaHandler importSchemaHandler;
    private final UploadSchemaDDLHandler uploadSchemaDDLHandler;
    private final GetSchema getSchema;
    private final DDLParserService ddlParserService;

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
            @Parameter(description = "connection id") @PathParam("connectionId")
                    UUID connectionId) {

        return importSchemaHandler.handle(new ImportSchemaCommand(connectionId));
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(
            summary = "Upload schema DDL file",
            description =
                    "Accepts a .sql file containing CREATE TABLE statements, parses it into JSON,"
                            + " and stores it as the schema for a connection.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Schema uploaded and parsed successfully",
                content = @Content(schema = @Schema(implementation = UUID.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Failed to parse DDL file",
                content = @Content(schema = @Schema(implementation = QuriumErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Database connection not found",
                content = @Content(schema = @Schema(implementation = QuriumErrorResponse.class)))
    })
    public UUID uploadDDL(
            @Parameter(description = "connection id") @PathParam("connectionId") UUID connectionId,
            @RestForm("file") FileUpload file)
            throws IOException {

        String ddl = readString(file.uploadedFile(), StandardCharsets.UTF_8);
        String schemaJson = ddlParserService.parse(ddl);

        return uploadSchemaDDLHandler.handle(new UploadSchemaDDLCommand(connectionId, schemaJson));
    }

    @GET
    @Operation(
            summary = "Get schema",
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
            @Parameter(description = "connection id") @PathParam("connectionId")
                    UUID connectionId) {

        return getSchema.query(connectionId);
    }
}
