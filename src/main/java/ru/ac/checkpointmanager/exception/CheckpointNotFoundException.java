package ru.ac.checkpointmanager.exception;

import jakarta.persistence.EntityNotFoundException;

public class CheckpointNotFoundException extends EntityNotFoundException {

    public CheckpointNotFoundException(String message) {
        super(message);
    }

}
