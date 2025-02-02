package ru.ac.checkpointmanager.dto.passes;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PagingParams {

    @PositiveOrZero
    private Integer page;

    @Positive
    private Integer size;
}
