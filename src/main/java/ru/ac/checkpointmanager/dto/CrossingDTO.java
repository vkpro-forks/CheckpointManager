package ru.ac.checkpointmanager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.ac.checkpointmanager.model.enums.Direction;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CrossingDTO {

    @NotNull
    private UUID passId;

    @NotNull
    private UUID checkpointId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime localDateTime;

    @NotNull
    private Direction direction;
}
