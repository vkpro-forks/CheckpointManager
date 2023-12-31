package ru.ac.checkpointmanager.exception;

public class MismatchCurrentPasswordException extends RuntimeException {
    public MismatchCurrentPasswordException(String message) {
        super(message);
    }
}
