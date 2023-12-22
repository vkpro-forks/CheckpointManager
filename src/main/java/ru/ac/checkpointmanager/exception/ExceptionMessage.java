package ru.ac.checkpointmanager.exception;

public class ExceptionMessage {

    public static final String USER_NOT_FOUND_MSG = "User with [id: %s] not found";

    public static final String TERRITORY_NOT_FOUND_MSG = "Territory with [id: %s] not found";

    public static final String USER_TER_REL = "Reject: user [%s] not have permission to create passes for territory [%s]";

    private ExceptionMessage() {
    }
}
