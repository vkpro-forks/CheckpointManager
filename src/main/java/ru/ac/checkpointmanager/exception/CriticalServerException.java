package ru.ac.checkpointmanager.exception;

public class CriticalServerException extends RuntimeException {

    public CriticalServerException(String message) {
        super(message);
    }

}
