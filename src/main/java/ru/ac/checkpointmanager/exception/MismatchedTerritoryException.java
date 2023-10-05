package ru.ac.checkpointmanager.exception;

public class MismatchedTerritoryException extends IllegalArgumentException {
    public MismatchedTerritoryException() {
    }

    public MismatchedTerritoryException(String message) {
        super(message);
    }

    public MismatchedTerritoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
