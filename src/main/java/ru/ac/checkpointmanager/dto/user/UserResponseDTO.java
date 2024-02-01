package ru.ac.checkpointmanager.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ac.checkpointmanager.dto.avatar.AvatarDTO;
import ru.ac.checkpointmanager.model.enums.Role;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private UUID id;

    private String fullName;

    private String mainNumber;

    private String email;

    private Boolean isBlocked;

    private Role role;

    @JsonProperty("avatar")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private AvatarDTO avatarDTO;
}
