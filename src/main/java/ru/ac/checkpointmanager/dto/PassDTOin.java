package ru.ac.checkpointmanager.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.ac.checkpointmanager.model.passes.PassTypeTime;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PassDTOin {

    private UUID id;

    @NotNull
    private UUID userId;

    String name;

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