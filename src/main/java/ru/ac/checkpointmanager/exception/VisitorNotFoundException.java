package ru.ac.checkpointmanager.exception;

import jakarta.persistence.EntityNotFoundException;

public class VisitorNotFoundException extends EntityNotFoundException {

    public VisitorNotFoundException(String message) {
        super(message);
    }

}
