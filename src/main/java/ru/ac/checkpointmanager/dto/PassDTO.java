package ru.ac.checkpointmanager.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.ac.checkpointmanager.model.enums.PassStatus;
import ru.ac.checkpointmanager.model.enums.PassTypeTime;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PassDTO {

    private UUID id;

    @NotNull()
    private UserDTO user;

    String name;

    @NotNull()
    private PassStatus status;

    @NotNull()
    private PassTypeTime typeTime;

    @NotNull()
    private TerritoryDTO territory;

    private String note;

    @NotNull()
    @FutureOrPresent
    private LocalDateTime startTime;

    @NotNull()
    @FutureOrPresent
    private LocalDateTime endTime;

//    private Car car;

//    private Person person;

}