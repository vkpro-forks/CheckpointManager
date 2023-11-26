package ru.ac.checkpointmanager.exception;

import jakarta.persistence.EntityNotFoundException;

public class CarNotFoundException extends EntityNotFoundException {
    public CarNotFoundException() {
    }

    public CarNotFoundException(String message) {
        super(message);
    }

}
