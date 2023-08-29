package ru.ac.checkpointmanager.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.enums.CheckpointType;

import java.util.UUID;

@Data
public class CheckpointDTO {

    private UUID id;

    @NotEmpty()
    @Size(min = 2, max = 60)
    private String name;

    private CheckpointType type;

    private String note;

    @NotEmpty()
    private Territory territory;
}
