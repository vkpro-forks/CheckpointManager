package ru.ac.checkpointmanager.dto;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.ac.checkpointmanager.model.CheckpointType;

@Setter
@Getter
public class CheckpointDTO {

    @Id
    private Integer id;

    @NotEmpty()
    @Size(min = 2, max = 60, message = "Name should be between 2 and 60 symbols")
    private String name;

    @Enumerated(EnumType.STRING)
    private CheckpointType type;

    private String note;
}
