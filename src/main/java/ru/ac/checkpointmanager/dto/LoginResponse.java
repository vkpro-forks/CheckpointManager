package ru.ac.checkpointmanager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ac.checkpointmanager.model.enums.Role;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
public class LoginResponse {

    private UUID id;

    private String fullName;

    private LocalDate dateOfBirth;

    private String mainNumber;

    private String email;

    private String password;

    private Boolean isBlocked;

    private Role role;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;
}
