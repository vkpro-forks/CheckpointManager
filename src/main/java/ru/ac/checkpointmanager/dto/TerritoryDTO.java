package ru.ac.checkpointmanager.dto;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class TerritoryDTO {

    @Id
    private Integer id;

    @NotEmpty()
    @Size(min = 2, max = 60)
    private String name;

    private String note;
}
