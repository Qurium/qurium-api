/* Qurium - 2026 */
package org.qurium.common.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum QuriumExceptionCode {

    // Encryption exceptions
    UNABLE_TO_ENCRYPT(
            1001, "It was not possible to encrypt", Response.Status.INTERNAL_SERVER_ERROR),
    UNABLE_TO_DECRYPT(
            1002, "It was not possible to decrypt", Response.Status.INTERNAL_SERVER_ERROR),

    // Database Connection
    DATABASE_CONNECTION_NOT_FOUND(2001, "Database connection not found", Response.Status.NOT_FOUND),
    DATABASE_CONNECTION_UNREACHABLE(
            2002, "Could not establish a connection to the database", Response.Status.BAD_REQUEST),
    DATABASE_CONNECTION_ALREADY_EXISTS(
            2003, "This database connection already exists", Response.Status.CONFLICT),

    // Schema
    SCHEMA_NOT_FOUND(3001, "Schema not found for this connection", Response.Status.NOT_FOUND),
    SCHEMA_IMPORT_FAILED(
            3002, "Failed to import schema from database", Response.Status.BAD_REQUEST),
    SCHEMA_PARSE_FAILED(
            3003,
            "Failed to parse DDL file. Ensure it contains valid CREATE TABLE statements.",
            Response.Status.BAD_REQUEST);

    private final int errorCode;
    private final String errorMessage;
    private final Response.Status httpStatus;
}
