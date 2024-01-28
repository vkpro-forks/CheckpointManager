package ru.ac.checkpointmanager.model.passes;

import ru.ac.checkpointmanager.exception.pass.InvalidPassTimeTypeException;

public enum PassTimeType {
    ONETIME("Разовый"),
    PERMANENT("Постоянный");

    private final String description;

    PassTimeType(String description) {
        this.description = description;
    }

    public static PassTimeType fromString(String value) {
        for (PassTimeType t : PassTimeType.values()) {
            if (t.name().equalsIgnoreCase(value)) {
                return t;
            }
        }
        throw new InvalidPassTimeTypeException("No PassTimeType for string value: " + value);
    }

    public String getDescription() {
        return description;
    }
}