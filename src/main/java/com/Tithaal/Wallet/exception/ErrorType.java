package com.Tithaal.Wallet.exception;

import org.springframework.http.HttpStatus;

public enum ErrorType {
    NOT_FOUND(HttpStatus.NOT_FOUND),
    INVALID_INPUT(HttpStatus.BAD_REQUEST),
    BUSINESS_RULE_VIOLATION(HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus defaultHttpStatus;

    ErrorType(HttpStatus defaultHttpStatus) {
        this.defaultHttpStatus = defaultHttpStatus;
    }

    public HttpStatus getDefaultHttpStatus() {
        return defaultHttpStatus;
    }
}
