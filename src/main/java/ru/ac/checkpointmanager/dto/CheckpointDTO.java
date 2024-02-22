package ru.ac.checkpointmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ac.checkpointmanager.model.checkpoints.CheckpointType;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckpointDTO {

    private UUID id;

    @NotBlank()
    @Size(min = 2, max = 60)
    @Pattern(regexp = "^(?=.*[a-zA-Zа-яА-Я]).*$",
            message = "Name must contain at least one letter")
    private String name;

    @NotNull()
    private CheckpointType type;

    @Size(max = 200)
    private String note;

    @NotNull()
    private TerritoryDTO territory;
}
