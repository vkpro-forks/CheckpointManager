package ru.ac.checkpointmanager.exception;

public class NoActivePassException extends IllegalStateException  {


    public NoActivePassException() {
    }

    public NoActivePassException(String message) {
        super(message);
    }

    public NoActivePassException(String message, Throwable cause) {
        super(message, cause);
    }

}
