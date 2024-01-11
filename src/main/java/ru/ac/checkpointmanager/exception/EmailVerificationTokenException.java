package ru.ac.checkpointmanager.exception;

public class EmailVerificationTokenException extends RuntimeException {
    public EmailVerificationTokenException(String message) {
        super(message);
    }

}
