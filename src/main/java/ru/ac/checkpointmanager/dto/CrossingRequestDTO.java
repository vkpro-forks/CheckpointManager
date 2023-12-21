package ru.ac.checkpointmanager.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
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

    @NotNull
    @PastOrPresent
    private ZonedDateTime performedAt;

}
