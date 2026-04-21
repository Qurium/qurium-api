package org.qurium.common.exception;

public class QuriumException extends RuntimeException {

    private final QuriumExceptionCode code;

    public QuriumException(QuriumExceptionCode code) {
        super(code.getErrorMessage());
        this.code = code;
    }

    public QuriumExceptionCode getCode() {
        return code;
    }
}
