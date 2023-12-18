package ru.ac.checkpointmanager.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrossingRequestDTO {

    @NotNull
    private UUID passId;

    @NotNull
    private UUID checkpointId;

    @NotNull// FIXME check me
    private ZonedDateTime performedAt;

}
