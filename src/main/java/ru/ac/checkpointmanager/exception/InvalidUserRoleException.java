package ru.ac.checkpointmanager.exception;

public class InvalidUserRoleException extends IllegalArgumentException {

    public InvalidUserRoleException(String message) {
        super(message);
    }
}
