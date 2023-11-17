package ru.ac.checkpointmanager.dto.passes;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.dto.PersonDTO;
import ru.ac.checkpointmanager.model.passes.PassTypeTime;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PassDtoUpdate {

    @NotNull
    private UUID id;

    String name;

    @NotNull
    private PassTypeTime typeTime;

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