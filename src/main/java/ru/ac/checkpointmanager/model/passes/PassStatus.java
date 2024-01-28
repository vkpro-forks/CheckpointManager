package ru.ac.checkpointmanager.model.passes;

import ru.ac.checkpointmanager.exception.pass.InvalidPassStatusException;

/**
 * Возможные статусы пропусков
 * DELAYED - (дефолтное значение) созданный пропуск, который начнет действовать позже
 * ACTIVE - (дефолтное значение) активный пропуск, по которому можно пересекать кпп
 * COMPLETED - выполненный (разовый, по которому был выезд, или постоянный,
 * по которому были пересечения и время которого истекло)
 * CANCELLED - отмененный юзером (до пересечений)
 * OUTDATED - устаревший (если время истекло, а пересечений не было)
 * WARNING - предупреждение (если время истекло или он отменен, а последнее пересечение по пропуску было на въезд)
 */
public enum PassStatus {
    DELAYED("Ожидает"),
    ACTIVE("Активный"),
    COMPLETED("Выполнен"),
    CANCELLED("Отменен"),
    OUTDATED("Устарел"),
    WARNING("Нет выезда");

    private final String description;

    PassStatus(String description) {
        this.description = description;
    }

    public static PassStatus fromString(String value) {
        for (PassStatus s : PassStatus.values()) {
            if (s.name().equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new InvalidPassStatusException("No PassStatus for string value: " + value);
    }

    public String getDescription() {
        return description;
    }
}
