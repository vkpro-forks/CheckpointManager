package ru.ac.checkpointmanager.dto.passes;

import jakarta.validation.GroupSequence;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.model.passes.PassTimeType;
import ru.ac.checkpointmanager.validation.annotation.CarOrVisitorFieldsCheck;
import ru.ac.checkpointmanager.validation.annotation.PassTimeCheck;
import ru.ac.checkpointmanager.validation.group.CustomCheck;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@CarOrVisitorFieldsCheck(groups = CustomCheck.class)
@PassTimeCheck(groups = CustomCheck.class)
@GroupSequence({PassBaseDTO.class, CustomCheck.class}) // custom checks will be performed after default
public abstract class PassBaseDTO {

    @Size(max = 30)
    private String comment;

    @NotNull
    private PassTimeType timeType;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    @Future
    private LocalDateTime endTime;

    @Valid
    private VisitorDTO visitor;

    @Valid
    private CarDTO car;

}
