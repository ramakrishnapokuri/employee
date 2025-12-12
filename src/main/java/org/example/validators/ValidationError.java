package org.example.validators;

/**
 * Represents a validation error found during employee data validation.
 */
public class ValidationError {

    private final ErrorCode errorCode;
    private final String message;

    public ValidationError(ErrorCode errorCode, Object... args) {
        this.errorCode = errorCode;
        this.message = errorCode.getMessage(args);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }


    @Override
    public String toString() {
        return "[" + errorCode + "] " + message;
    }
}
