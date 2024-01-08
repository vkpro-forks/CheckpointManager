package ru.ac.checkpointmanager.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PreAuthResponseDTO {

    @NotNull
    private Boolean isAuthenticated;

    private String fullName;
}
