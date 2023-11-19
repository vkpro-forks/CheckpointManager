package ru.ac.checkpointmanager.dto.passes;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import ru.ac.checkpointmanager.dto.user.UserResponseDTO;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.dto.TerritoryDTO;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PassDtoResponse {

    private UUID id;

    private UserResponseDTO user;

    private String name;

    private String statusDescription;

    private String typeTimeDescription;

    private TerritoryDTO territory;

    private String note;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String dtype;

    private Boolean favorite;

    private VisitorDTO visitor;

    private CarDTO car;

}