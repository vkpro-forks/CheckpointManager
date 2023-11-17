package ru.ac.checkpointmanager.dto.passes;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.dto.PersonDTO;
import ru.ac.checkpointmanager.model.passes.PassTypeTime;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PassDtoCreate {

    @NotNull
    private UUID userId;

    private String name;

    @NotNull
    private PassTypeTime typeTime;

    @NotNull
    private UUID territoryId;

    private String note;

    @NotNull
    @FutureOrPresent
    private LocalDateTime startTime;

    @NotNull
    @Future
    private LocalDateTime endTime;

    private PersonDTO person;

    private CarDTO car;
}