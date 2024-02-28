package ru.ac.checkpointmanager.dto.passes;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
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

    public PassUpdateDTO(@Nullable String comment, @NonNull PassTimeType timeType, @NonNull LocalDateTime startTime,
                         @NonNull LocalDateTime endTime, @Nullable VisitorDTO visitor, @Nullable CarDTO car, @NonNull UUID id) {
        super(comment, timeType, startTime, endTime, visitor, car);
        this.id = id;
    }

}
