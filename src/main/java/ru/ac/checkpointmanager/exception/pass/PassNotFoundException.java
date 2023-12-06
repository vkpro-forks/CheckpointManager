package ru.ac.checkpointmanager.exception.pass;

import jakarta.persistence.EntityNotFoundException;

public class PassNotFoundException extends EntityNotFoundException {

    public PassNotFoundException(String message) {
        super(message);
    }
}
