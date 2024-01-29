package ru.ac.checkpointmanager.model.passes;

import lombok.extern.slf4j.Slf4j;
import ru.ac.checkpointmanager.exception.pass.InvalidPassStatusException;

import static ru.ac.checkpointmanager.exception.ExceptionUtils.PASS_STATUS_NOOOOOO;

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
@Slf4j
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
        try {
            return PassStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            log.error(PASS_STATUS_NOOOOOO.formatted(value));
            throw new InvalidPassStatusException(PASS_STATUS_NOOOOOO.formatted(value));
        }
    }

    public String getDescription() {
        return description;
    }
}
