package ru.ac.checkpointmanager.exception;

import jakarta.persistence.EntityNotFoundException;

public class PhoneNumberNotFoundException extends EntityNotFoundException {

    public PhoneNumberNotFoundException(String message) {
        super(message);
    }

}
