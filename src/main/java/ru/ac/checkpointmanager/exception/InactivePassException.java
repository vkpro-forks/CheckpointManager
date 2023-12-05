package ru.ac.checkpointmanager.exception;

public class InactivePassException extends IllegalStateException {

    public InactivePassException(String message) {
        super(message);
    }
}