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
            1002, "It was not possible to decrypt", Response.Status.INTERNAL_SERVER_ERROR);

    private final int errorCode;
    private final String errorMessage;
    private final Response.Status httpStatus;
}
