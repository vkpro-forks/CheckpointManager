package ru.ac.checkpointmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DateOfBirthFormatException extends RuntimeException {
    public DateOfBirthFormatException() {
    }

    public DateOfBirthFormatException(String message) {
        super(message);
    }

    public DateOfBirthFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
