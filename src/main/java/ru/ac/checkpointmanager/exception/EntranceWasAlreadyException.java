package ru.ac.checkpointmanager.exception;

public class EntranceWasAlreadyException extends IllegalStateException  {

    public EntranceWasAlreadyException() {
    }

    public EntranceWasAlreadyException(String message) {
        super(message);
    }

    public EntranceWasAlreadyException(String message, Throwable cause) {
        super(message, cause);
    }
}
