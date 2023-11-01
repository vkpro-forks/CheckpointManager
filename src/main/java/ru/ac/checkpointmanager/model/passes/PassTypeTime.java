package ru.ac.checkpointmanager.model.passes;

public enum PassTypeTime {
    ONETIME("Разовый"),
    PERMANENT("Постоянный");

    private final String description;

    PassTypeTime(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}