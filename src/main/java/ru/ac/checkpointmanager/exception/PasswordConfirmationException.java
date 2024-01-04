package ru.ac.checkpointmanager.exception;

public class PasswordConfirmationException extends RuntimeException {
    public PasswordConfirmationException(String message) {
        super(message);
    }
}
