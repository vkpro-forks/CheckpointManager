package ru.ac.checkpointmanager.exception;

public class CarBrandNotFoundException extends RuntimeException {
    public CarBrandNotFoundException() {
    }

    public CarBrandNotFoundException(String message) {
        super(message);
    }

    public CarBrandNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
