package ru.ac.checkpointmanager.exception;

import ru.ac.checkpointmanager.exception.pass.PassException;

public class PassAlreadyUsedException extends PassException {

    public PassAlreadyUsedException(String message) {
        super(message);
    }

}
