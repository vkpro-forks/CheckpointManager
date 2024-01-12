package ru.ac.checkpointmanager.dto.passes;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.model.passes.PassTimeType;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PassUpdateDTO extends PassBaseDTO {

    @NotNull
    private UUID id;

    public PassUpdateDTO(String comment, PassTimeType timeType, LocalDateTime startTime,
                         LocalDateTime endTime, VisitorDTO visitor, CarDTO car, UUID id) {
        super(comment, timeType, startTime, endTime, visitor, car);
        this.id = id;
    }

}
