package ru.ac.checkpointmanager.exception;

public class EmailAlreadyExistsException extends ObjectAlreadyExistsException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
