/* Qurium - 2026 */
package org.qurium.uploadedfile.api;

import static java.nio.file.Files.readString;

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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.qurium.common.PaginatedResponse;
import org.qurium.common.exception.QuriumErrorResponse;
import org.qurium.schema.command.data.UploadSchemaDDLCommand;
import org.qurium.schema.command.handler.UploadSchemaDDLHandler;
import org.qurium.schema.service.DDLParserService;
import org.qurium.uploadedfile.command.data.DeleteUploadedFileCommand;
import org.qurium.uploadedfile.command.handler.DeleteUploadedFileHandler;
import org.qurium.uploadedfile.dto.UploadedFileDTO;
import org.qurium.uploadedfile.dto.request.UploadFileRequest;
import org.qurium.uploadedfile.query.GetUploadedFile;
import org.qurium.uploadedfile.query.ListUploadedFiles;

@Path("/api/uploaded-files")
@RequiredArgsConstructor
public class UploadedFileResource {

    private final UploadSchemaDDLHandler uploadSchemaDDLHandler;
    private final ListUploadedFiles listUploadedFiles;
    private final GetUploadedFile getUploadedFile;
    private final DeleteUploadedFileHandler deleteUploadedFileHandler;
    private final DDLParserService ddlParserService;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(
            summary = "Upload schema DDL file",
            description =
                    "Accepts a .sql file containing CREATE TABLE statements, parses it into JSON,"
                            + " and stores it as a standalone schema (no live connection required)."
                            + " Returns the id of the created uploaded file.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "File uploaded and schema parsed successfully",
                content = @Content(schema = @Schema(implementation = UUID.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Failed to parse DDL file",
                content = @Content(schema = @Schema(implementation = QuriumErrorResponse.class)))
    })
    public UUID uploadFile(@Valid UploadFileRequest request) throws IOException {

        String ddl = readString(request.file().uploadedFile(), StandardCharsets.UTF_8);
        String schemaJson = ddlParserService.parse(ddl);
        return uploadSchemaDDLHandler.handle(
                new UploadSchemaDDLCommand(request.name() ,request.file().fileName(), schemaJson));
    }

    @GET
    @Operation(
            summary = "List uploaded files",
            description = "Returns a paginated list of uploaded DDL files.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Paginated list of uploaded files",
                content = @Content(schema = @Schema(implementation = PaginatedResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid pagination parameters",
                content = @Content(schema = @Schema(implementation = QuriumErrorResponse.class)))
    })
    public PaginatedResponse<UploadedFileDTO> listFiles(
            @Parameter(description = "Number of the page") @QueryParam("page") int page,
            @Parameter(description = "Number of items per page")
                    @QueryParam("size")
                    @Min(1)
                    @Max(100)
                    int size) {

        return PaginatedResponse.of(listUploadedFiles.query(), page, size);
    }

    @GET
    @Path("/{id}")
    @Operation(
            summary = "Get an uploaded file",
            description = "Returns the metadata of a single uploaded DDL file.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Uploaded file retrieved successfully",
                content = @Content(schema = @Schema(implementation = UploadedFileDTO.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Uploaded file not found",
                content = @Content(schema = @Schema(implementation = QuriumErrorResponse.class)))
    })
    public UploadedFileDTO getFile(
            @Parameter(description = "uploaded file id") @PathParam("id") UUID id) {

        return getUploadedFile.query(id);
    }

    @DELETE
    @Path("/{id}")
    @Operation(
            summary = "Delete an uploaded file",
            description = "Soft-deletes an uploaded file and its associated schema.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Uploaded file deleted successfully"),
        @ApiResponse(
                responseCode = "404",
                description = "Uploaded file not found",
                content = @Content(schema = @Schema(implementation = QuriumErrorResponse.class)))
    })
    public Response deleteFile(
            @Parameter(description = "uploaded file id") @PathParam("id") UUID id) {

        deleteUploadedFileHandler.handle(new DeleteUploadedFileCommand(id));
        return Response.noContent().build();
    }
}
