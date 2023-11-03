package ru.ac.checkpointmanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.model.passes.PassTypeTime;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PassDTOout {

    private UUID id;

    private UserDTO user;

    String name;

    private PassStatus status;

    private PassTypeTime typeTime;

    private TerritoryDTO territory;

    private String note;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String dtype;

    private PersonDTO person;

    private CarDTO car;

}