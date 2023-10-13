package ru.ac.checkpointmanager.exception;

public class InactivePassException extends IllegalStateException  {


    public InactivePassException() {
    }

    public InactivePassException(String message) {
        super(message);
    }

    public InactivePassException(String message, Throwable cause) {
        super(message, cause);
    }

}
