/* Qurium - 2026 */
package org.qurium.schema.api;

import static java.nio.file.Files.readString;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.qurium.common.exception.QuriumErrorResponse;
import org.qurium.nlquery.command.data.ExecuteSchemaQueryCommand;
import org.qurium.nlquery.command.handler.ExecuteSchemaQueryHandler;
import org.qurium.nlquery.dto.ExecuteNlQueryRequestDTO;
import org.qurium.nlquery.dto.ExecuteNlQueryResponseDTO;
import org.qurium.schema.command.data.UploadSchemaDDLCommand;
import org.qurium.schema.command.handler.UploadSchemaDDLHandler;
import org.qurium.schema.dto.SchemaDTO;
import org.qurium.schema.query.GetSchemaById;
import org.qurium.schema.service.DDLParserService;

@Path("/api/schema")
@RequiredArgsConstructor
public class StandaloneSchemaResource {

    private final UploadSchemaDDLHandler uploadSchemaDDLHandler;
    private final GetSchemaById getSchemaById;
    private final DDLParserService ddlParserService;
    private final ExecuteSchemaQueryHandler executeSchemaQueryHandler;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(
            summary = "Upload schema DDL file",
            description =
                    "Accepts a .sql file containing CREATE TABLE statements, parses it into JSON,"
                        + " and stores it as a standalone schema (no live connection required).")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Schema uploaded and parsed successfully",
                content = @Content(schema = @Schema(implementation = UUID.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Failed to parse DDL file",
                content = @Content(schema = @Schema(implementation = QuriumErrorResponse.class)))
    })
    public UUID uploadDDL(@RestForm("file") FileUpload file) throws IOException {
        String ddl = readString(file.uploadedFile(), StandardCharsets.UTF_8);
        String schemaJson = ddlParserService.parse(ddl);
        return uploadSchemaDDLHandler.handle(new UploadSchemaDDLCommand(schemaJson));
    }

    @GET
    @Path("/{schemaId}")
    @Operation(summary = "Get schema by ID", description = "Returns a standalone schema by its ID.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Schema retrieved successfully",
                content = @Content(schema = @Schema(implementation = SchemaDTO.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Schema not found",
                content = @Content(schema = @Schema(implementation = QuriumErrorResponse.class)))
    })
    public SchemaDTO getSchema(
            @Parameter(description = "schema id") @PathParam("schemaId") UUID schemaId) {

        return getSchemaById.query(schemaId);
    }

    @POST
    @Path("/{schemaId}/query")
    @Operation(
            summary = "Query schema (SQL only)",
            description =
                    "Translates a natural-language question into SQL using the stored schema."
                        + " Returns the generated SQL and explanation. Does not execute the query"
                        + " (no live connection).")
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
    public ExecuteNlQueryResponseDTO querySchema(
            @Parameter(description = "schema id") @PathParam("schemaId") UUID schemaId,
            @Parameter(description = "question") @Valid ExecuteNlQueryRequestDTO request) {

        return executeSchemaQueryHandler.handle(
                new ExecuteSchemaQueryCommand(schemaId, request.question()));
    }
}
