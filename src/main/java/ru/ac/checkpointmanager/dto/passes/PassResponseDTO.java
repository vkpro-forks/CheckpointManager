package ru.ac.checkpointmanager.dto.passes;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import ru.ac.checkpointmanager.dto.user.UserResponseDTO;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.model.enums.Direction;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PassResponseDTO {

    private UUID id;

    private UserResponseDTO user;

    private String comment;

    private String statusDescription;

    private String typeTimeDescription;

    private TerritoryDTO territory;

    private LocalDateTime addedAt;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String dtype;

    private Boolean favorite;

    private Direction expectedDirection;

    private VisitorDTO visitor;

    private CarDTO car;

}