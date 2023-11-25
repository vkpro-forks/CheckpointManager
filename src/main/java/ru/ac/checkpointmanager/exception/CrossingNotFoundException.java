package ru.ac.checkpointmanager.exception;

import jakarta.persistence.EntityNotFoundException;

public class CrossingNotFoundException extends EntityNotFoundException {

    public CrossingNotFoundException() {
    }

    public CrossingNotFoundException(String message) {
        super(message);
    }

}
