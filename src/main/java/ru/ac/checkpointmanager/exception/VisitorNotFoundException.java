package ru.ac.checkpointmanager.exception;

public class VisitorNotFoundException extends RuntimeException {
    public VisitorNotFoundException() {
    }

    public VisitorNotFoundException(String message) {
        super(message);
    }

    public VisitorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
