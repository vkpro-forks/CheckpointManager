package ru.ac.checkpointmanager.exception.payment;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DonationException extends RuntimeException {

    public static final String MESSAGE = "Error occurred during payment at YooKassa API: %s";

    public DonationException(String message) {
        super(MESSAGE.formatted(message));
        log.error(getMessage() + " - " + this.getStackTrace()[0].toString());
    }
}
