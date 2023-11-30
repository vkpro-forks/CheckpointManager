package ru.ac.checkpointmanager.exception;

import jakarta.persistence.EntityNotFoundException;

public class TerritoryNotFoundException extends EntityNotFoundException {

    public TerritoryNotFoundException(String message) {
        super(message);
    }

}
