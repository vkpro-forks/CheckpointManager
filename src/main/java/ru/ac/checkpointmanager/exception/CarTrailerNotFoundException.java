package ru.ac.checkpointmanager.exception;

public class CarTrailerNotFoundException extends RuntimeException {
    public CarTrailerNotFoundException() {
    }

    public CarTrailerNotFoundException(String message) {
        super(message);
    }

    public CarTrailerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
