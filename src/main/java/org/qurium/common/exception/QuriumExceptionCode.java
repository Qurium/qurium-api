package org.qurium.common.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.netty.handler.codec.http.HttpResponseStatus;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum QuriumExceptionCode {

    //Encryption exceptions
    UNABLE_TO_ENCRYPT(1001, "It was not possible to encrypt", Response.Status.INTERNAL_SERVER_ERROR),
    UNABLE_TO_DECRYPT(1002, "It was not possible to decrypt", Response.Status.INTERNAL_SERVER_ERROR),

    //Pagination
    PAGE_INDEX_NEGATIVE(2001, "Page index must not be negative", Response.Status.BAD_REQUEST),
    PAGE_SIZE_NEGATIVE(2002, "Page size must be at least 1", Response.Status.BAD_REQUEST),
    PAGE_SIZE_TOO_BIG(2003, "Page size must not exceed the limit", Response.Status.BAD_REQUEST);

    private final int errorCode;
    private final String errorMessage;
    private final Response.Status httpStatus;
}
