package ru.ac.checkpointmanager.dto;

import lombok.Data;
import ru.ac.checkpointmanager.model.enums.Direction;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CrossingDTO {


    private UUID passId;

    private UUID checkpointId;

    private LocalDateTime localDateTime;

    private Direction direction;
}
