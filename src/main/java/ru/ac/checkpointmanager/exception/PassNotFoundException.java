package ru.ac.checkpointmanager.exception;

import jakarta.persistence.EntityNotFoundException;

public class PassNotFoundException extends EntityNotFoundException {

    public PassNotFoundException(String message) {
        super(message);
    }

}
