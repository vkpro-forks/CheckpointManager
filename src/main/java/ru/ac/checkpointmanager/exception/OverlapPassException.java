package ru.ac.checkpointmanager.exception;

public class OverlapPassException extends IllegalArgumentException {

    public OverlapPassException(String message) {
        super(message);
    }
}