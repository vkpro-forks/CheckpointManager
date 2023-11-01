package ru.ac.checkpointmanager.exception;

public class CrossingNotFoundException extends RuntimeException {

    public CrossingNotFoundException() {
    }

    public CrossingNotFoundException(String message) {
        super(message);
    }

    public CrossingNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
