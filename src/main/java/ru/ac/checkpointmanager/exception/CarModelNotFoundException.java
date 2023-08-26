package ru.ac.checkpointmanager.exception;

public class CarModelNotFoundException extends RuntimeException {
    public CarModelNotFoundException() {
    }

    public CarModelNotFoundException(String message) {
        super(message);
    }

    public CarModelNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
