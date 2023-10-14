package ru.ac.checkpointmanager.exception.avatar;

public class AvatarIsTooBigException extends RuntimeException {
    public AvatarIsTooBigException(String message) {
        super(message);
    }
}
