package ru.ac.checkpointmanager.dto;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import ru.ac.checkpointmanager.model.enums.CheckpointType;

@Setter
@Getter
public class CheckpointDTO {

    @Id
    private Integer id;

    @NotEmpty()
    @Size(min = 2, max = 60)
    private String name;

    @Enumerated(EnumType.STRING)
    private CheckpointType type;

    private String note;
}
