package ru.ac.checkpointmanager.dto;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.enums.CheckpointType;

@Data
public class CheckpointDTO {

    @Id
    private Integer id;

    @NotEmpty()
    @Size(min = 2, max = 60)
    private String name;

    @Enumerated(EnumType.STRING)
    private CheckpointType type;

    private String note;

    @NotEmpty()
    private Territory territory;
}
