package ru.ac.checkpointmanager.dto.passes;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.model.passes.PassTypeTime;
import ru.ac.checkpointmanager.validation.annotation.CarOrVisitorFieldsCheck;
import ru.ac.checkpointmanager.validation.group.CustomCheck;
import ru.ac.checkpointmanager.validation.annotation.PassTimeCheck;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@CarOrVisitorFieldsCheck(groups = CustomCheck.class)
@PassTimeCheck(groups = CustomCheck.class)
@GroupSequence({PassUpdateDTO.class, CustomCheck.class}) // custom checks will be performed before default
public class PassUpdateDTO {

    @NotNull
    private UUID id;

    @Size(max = 30)
    private String comment;

    @NotNull
    private PassTypeTime typeTime;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    @Future
    private LocalDateTime endTime;

    private VisitorDTO visitor;

    private CarDTO car;
}