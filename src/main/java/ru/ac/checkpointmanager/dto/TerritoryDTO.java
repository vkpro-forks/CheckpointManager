package ru.ac.checkpointmanager.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Setter
@Getter
public class TerritoryDTO {

    private UUID id;

    @NotEmpty()
    @Size(min = 2, max = 60)
    private String name;

    private String note;
}
