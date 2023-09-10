package ru.ac.checkpointmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PhoneAlreadyExistException extends RuntimeException {
    public PhoneAlreadyExistException() {
    }

    public PhoneAlreadyExistException(String message) {
        super(message);
    }

    public PhoneAlreadyExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
