package ru.ac.checkpointmanager.exception;

import ru.ac.checkpointmanager.exception.pass.PassException;

public class MismatchedTerritoryException extends PassException {

    public MismatchedTerritoryException(String message) {
        super(message);
    }

}
