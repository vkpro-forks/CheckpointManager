package ru.ac.checkpointmanager.model.checkpoints;

public enum CheckpointType {
    UNIVERSAL("Универсальный"),
    AUTO("Автомобильный"),
    WALKING("Пешеходный");

    private final String description;

    CheckpointType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
