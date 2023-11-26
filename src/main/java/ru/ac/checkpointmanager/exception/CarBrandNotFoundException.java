package ru.ac.checkpointmanager.exception;

import jakarta.persistence.EntityNotFoundException;

public class CarBrandNotFoundException extends EntityNotFoundException {
    public CarBrandNotFoundException() {
    }

    public CarBrandNotFoundException(String message) {
        super(message);
    }

}
