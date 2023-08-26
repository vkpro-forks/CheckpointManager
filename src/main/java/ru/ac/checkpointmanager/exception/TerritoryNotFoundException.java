package ru.ac.checkpointmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TerritoryNotFoundException extends RuntimeException {

    public TerritoryNotFoundException(String message) {
        super(message);
    }
    public TerritoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
