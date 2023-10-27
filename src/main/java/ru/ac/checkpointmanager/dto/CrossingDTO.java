package ru.ac.checkpointmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.ac.checkpointmanager.model.enums.Direction;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public class CrossingDTO {


    private UUID passId;

    private UUID checkpointId;

//    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private ZonedDateTime localDateTime;

    private Direction direction;
}
