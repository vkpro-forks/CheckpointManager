package ru.ac.checkpointmanager.exception;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class ExceptionUtils {

    public static final String USER_NOT_FOUND_MSG = "User with [id: %s] not found";

    public static final String TERRITORY_NOT_FOUND_MSG = "Territory with [id: %s] not found";

    public static final String USER_TER_REL = "Reject: user [%s] not have permission to create passes for territory [%s]";

    private ExceptionUtils() {
    }

    public static TerritoryNotFoundException logAndReturnTerritoryNotFoundException(UUID territoryId) {
        log.warn(TERRITORY_NOT_FOUND_MSG.formatted(territoryId));
        return new TerritoryNotFoundException(TERRITORY_NOT_FOUND_MSG
                .formatted(territoryId));
    }

    public static UserNotFoundException logAndReturnUserNotFoundException(UUID userId) {
        log.warn(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(userId));
        return new UserNotFoundException(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(userId));
    }

}
