package ru.ac.checkpointmanager.dto.passes;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.model.passes.PassTypeTime;
import ru.ac.checkpointmanager.validation.annotation.CarOrVisitorFieldsCheck;
import ru.ac.checkpointmanager.validation.annotation.PassTimeCheck;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@CarOrVisitorFieldsCheck
@PassTimeCheck
public class PassCreateDTO {

    @NotNull
    private UUID userId;

    private String comment;

    @NotNull
    private PassTypeTime typeTime;

    @NotNull
    private UUID territoryId;

    @NotNull
    @FutureOrPresent
    private LocalDateTime startTime;

    @NotNull
    @Future
    private LocalDateTime endTime;

    private VisitorDTO visitor;

    private CarDTO car;
}