package ru.ac.checkpointmanager.model.passes;

import lombok.extern.slf4j.Slf4j;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.pass.InvalidPassTimeTypeException;

@Slf4j
public enum PassTimeType {
    ONETIME("Разовый"),
    PERMANENT("Постоянный");

    private final String description;

    PassTimeType(String description) {
        this.description = description;
    }

    public static PassTimeType fromString(String value) {
        try {
            return PassTimeType.valueOf(value);
        } catch (IllegalArgumentException e) {
            log.error(ExceptionUtils.PASS_TIME_TYPE_NOOOOOO.formatted(value));
            throw new InvalidPassTimeTypeException(ExceptionUtils.PASS_TIME_TYPE_NOOOOOO.formatted(value));
        }
    }

    public String getDescription() {
        return description;
    }
}