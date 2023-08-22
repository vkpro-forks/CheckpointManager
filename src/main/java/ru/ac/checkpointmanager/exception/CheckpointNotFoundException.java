package ru.ac.checkpointmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CheckpointNotFoundException extends RuntimeException {

    public CheckpointNotFoundException(String message) {
        super(message);
    }

    public CheckpointNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
