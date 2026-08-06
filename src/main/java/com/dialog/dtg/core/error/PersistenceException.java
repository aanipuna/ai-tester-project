package com.dialog.dtg.core.error;

public class PersistenceException extends ApiTestAgentException {

    public PersistenceException(String message) {
        super("PERSISTENCE_ERROR", message);
    }

    public PersistenceException(String message, Throwable cause) {
        super("PERSISTENCE_ERROR", message, cause);
    }
}
