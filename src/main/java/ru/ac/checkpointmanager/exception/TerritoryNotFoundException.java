package ru.ac.checkpointmanager.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class TerritoryNotFoundException extends EntityNotFoundException {

    static String message;

    public TerritoryNotFoundException(String message) {
        super(message);
    }

    public TerritoryNotFoundException(UUID territoryId) {
        super(message = "Territory with id [%s] not found".formatted(territoryId));
        log.warn(message + " - " + this.getStackTrace()[0].toString());
    }
}
