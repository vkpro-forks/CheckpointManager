package ru.ac.checkpointmanager.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {

    @NotEmpty
    private String email;

    @NotEmpty
    private String password;
}
