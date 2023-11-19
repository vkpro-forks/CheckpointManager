package ru.ac.checkpointmanager.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class VisitorDTO {

    private UUID id;

    @NotNull
    private String name;

    private String phone;

    private String note;
}
