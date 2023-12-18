package ru.ac.checkpointmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ac.checkpointmanager.model.enums.Direction;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrossingDTO {

    //А вот в результат-ответ я бы еще добавил идентификатор пересечения (пусть будет)
    private UUID id;

    private UUID passId;

    private UUID checkpointId;

    private ZonedDateTime performedAt;

    private Direction direction;

}
