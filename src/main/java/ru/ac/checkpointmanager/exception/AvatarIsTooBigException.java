package ru.ac.checkpointmanager.exception;

public class AvatarIsTooBigException extends RuntimeException {
    public AvatarIsTooBigException(String message) {
        super(message);
    }
}
