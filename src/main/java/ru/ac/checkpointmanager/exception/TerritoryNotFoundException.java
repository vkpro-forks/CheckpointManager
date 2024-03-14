package ru.ac.checkpointmanager.exception;

import jakarta.persistence.EntityNotFoundException;

import java.util.UUID;

public class TerritoryNotFoundException extends EntityNotFoundException {

    public TerritoryNotFoundException(String message) {
        super(message);
    }

    public TerritoryNotFoundException(UUID territoryId) {
        super(String.format("Territory with id [%s] not found", territoryId));
    }
}
