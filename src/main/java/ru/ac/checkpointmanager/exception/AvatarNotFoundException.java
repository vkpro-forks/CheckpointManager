package ru.ac.checkpointmanager.exception;

import jakarta.persistence.EntityNotFoundException;

public class AvatarNotFoundException extends EntityNotFoundException {
    
    public AvatarNotFoundException(String message) {
        super(message);
    }
    
}
