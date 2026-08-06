package com.dialog.dtg.core.error;

public class ExecutionFailureException extends ApiTestAgentException {

    public ExecutionFailureException(String message) {
        super("EXECUTION_ERROR", message);
    }

    public ExecutionFailureException(String message, Throwable cause) {
        super("EXECUTION_ERROR", message, cause);
    }
}
