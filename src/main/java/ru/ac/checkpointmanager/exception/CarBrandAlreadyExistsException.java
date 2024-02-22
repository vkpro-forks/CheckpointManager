package ru.ac.checkpointmanager.exception;

public class CarBrandAlreadyExistsException extends ObjectAlreadyExistsException {
    public CarBrandAlreadyExistsException(String message) {
        super(message);
    }
}
