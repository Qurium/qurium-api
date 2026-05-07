/* Qurium - 2026 */
package org.qurium.common.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class QuriumExceptionMapper implements ExceptionMapper<QuriumException> {

    @Override
    public Response toResponse(QuriumException e) {
        return Response.status(e.getCode().getHttpStatus())
                .entity(new QuriumErrorResponse(e.getCode().getErrorCode(), e.getMessage()))
                .build();
    }
}
