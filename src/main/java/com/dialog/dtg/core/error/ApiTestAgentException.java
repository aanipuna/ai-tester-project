package com.dialog.dtg.core.error;

public class ApiTestAgentException extends RuntimeException {

    private final String errorCode;

    public ApiTestAgentException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApiTestAgentException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
