package ru.ac.checkpointmanager.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ac.checkpointmanager.model.enums.Role;

import java.util.UUID;

@Data
@NoArgsConstructor
public class LoginResponseDTO {

    private UUID id;

    private String fullName;

    private String mainNumber;

    private String email;

    private Boolean isBlocked;

    private Role role;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;
}
