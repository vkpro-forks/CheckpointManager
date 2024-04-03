package ru.ac.checkpointmanager.dto.passes;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PassFilterParams {

    private String dtype;

    private String territory;

    private String status;

    private Boolean favorite;
}