package com.Tithaal.Wallet.exception;

public class DomainException extends RuntimeException {
    private final ErrorType errorType;
    private final String message;

    public DomainException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
        this.message = message;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
