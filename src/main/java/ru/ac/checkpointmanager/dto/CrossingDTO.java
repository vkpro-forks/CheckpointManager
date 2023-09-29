package ru.ac.checkpointmanager.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.ac.checkpointmanager.model.Checkpoint;
import ru.ac.checkpointmanager.model.enums.Direction;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CrossingDTO {

    @NotNull
    private UUID passId;

    @NotNull
    private Checkpoint checkpoint;

    @NotNull
    private LocalDateTime localDateTime;

    @NotNull
    private Direction direction;

}

