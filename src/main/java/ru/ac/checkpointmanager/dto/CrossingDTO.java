package ru.ac.checkpointmanager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ac.checkpointmanager.model.enums.Direction;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrossingDTO {

    //А вот в результат-ответ я бы еще добавил идентификатор пересечения (пусть будет)
    private UUID id;

    @NotNull
    private UUID passId;

    @NotNull
    private UUID checkpointId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime localDateTime;

    @NotNull
    private Direction direction;
}
