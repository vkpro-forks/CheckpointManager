package ru.ac.checkpointmanager.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IsAuthenticatedResponse {

    @NotNull
    private Boolean isAuthenticated;

    private String fullName;
}
