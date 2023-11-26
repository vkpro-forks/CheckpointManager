package ru.ac.checkpointmanager.exception;

import java.io.IOException;

public class AvatarProcessingException extends RuntimeException {
    public AvatarProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
