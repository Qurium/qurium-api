/* Qurium - 2026 */
package org.qurium.common.exception;

import lombok.Getter;

@Getter
public class QuriumException extends RuntimeException {

    private final QuriumExceptionCode code;

    public QuriumException(QuriumExceptionCode code) {
        super(code.getErrorMessage());
        this.code = code;
    }
}
