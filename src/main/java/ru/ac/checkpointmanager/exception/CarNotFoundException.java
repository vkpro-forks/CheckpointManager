package ru.ac.checkpointmanager.exception;

public class CarNotFoundException extends RuntimeException {
    public CarNotFoundException() {
    }

    public CarNotFoundException(String message) {
        super(message);
    }

    public CarNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
