package ru.ac.checkpointmanager.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VisitorDTO {

    private UUID id;

    @NotNull
    private String name;

    private String phone;

    private String note;
}
