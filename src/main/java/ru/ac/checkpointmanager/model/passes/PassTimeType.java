package ru.ac.checkpointmanager.model.passes;

public enum PassTimeType {
    ONETIME("Разовый"),
    PERMANENT("Постоянный");

    private final String description;

    PassTimeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}