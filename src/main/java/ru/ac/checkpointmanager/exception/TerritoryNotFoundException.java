package ru.ac.checkpointmanager.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class TerritoryNotFoundException extends EntityNotFoundException {

    public static final String MESSAGE = "Territory with id [%s] not found";

    public TerritoryNotFoundException(String message) {
        super(message);
        log.warn(getMessage() + " - " + this.getStackTrace()[0].toString());
    }

    public TerritoryNotFoundException(UUID territoryId) {
        super(MESSAGE.formatted(territoryId));
        log.warn(getMessage() + " - " + this.getStackTrace()[0].toString());
    }
}
