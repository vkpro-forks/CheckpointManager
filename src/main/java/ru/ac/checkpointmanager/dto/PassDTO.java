package ru.ac.checkpointmanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.Data;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.model.passes.PassTypeTime;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PassDTO {

    private UUID id;

    @NotNull()
    private UserDTO user;

    String name;

    private PassStatus status;

    @NotNull()
    private PassTypeTime typeTime;

    @NotNull()
    private TerritoryDTO territory;

    private String note;

    @NotNull()
    @Future
    private LocalDateTime startTime;

    @NotNull()
    @Future
    private LocalDateTime endTime;

    private String dtype;

    private PersonDTO person;

    private CarDTO car;

}