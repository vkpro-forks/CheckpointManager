package ru.ac.checkpointmanager.exception;

import jakarta.persistence.EntityNotFoundException;

import java.util.UUID;

public class TerritoryNotFoundException extends EntityNotFoundException {

    public TerritoryNotFoundException(String message) {
        super(message);
    }

    public TerritoryNotFoundException(UUID territoryId, Class<?> className) {
        super(String.format("Territory with id [%s] not found (%s)", territoryId, className));
    }
}
