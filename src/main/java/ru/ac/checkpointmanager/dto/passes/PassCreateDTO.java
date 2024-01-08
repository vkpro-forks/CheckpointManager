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
public class PassCreateDTO extends PassBaseDTO {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID territoryId;

    public PassCreateDTO(String comment, PassTimeType timeType, LocalDateTime startTime,
                         LocalDateTime endTime, VisitorDTO visitor, CarDTO car,
                         UUID userId, UUID territoryId) {
        super(comment, timeType, startTime, endTime, visitor, car);
        this.userId = userId;
        this.territoryId = territoryId;
    }

}
