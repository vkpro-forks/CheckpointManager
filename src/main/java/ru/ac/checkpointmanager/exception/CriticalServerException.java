package ru.ac.checkpointmanager.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CriticalServerException extends RuntimeException {

    public CriticalServerException(String message) {
        super(message);
    }

}
