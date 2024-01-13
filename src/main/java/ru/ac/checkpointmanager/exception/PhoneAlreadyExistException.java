package ru.ac.checkpointmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class PhoneAlreadyExistException extends ObjectAlreadyExistsException {

    public PhoneAlreadyExistException(String message) {
        super(message);
    }

}
