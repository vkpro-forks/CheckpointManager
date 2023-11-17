package ru.ac.checkpointmanager.dto;

import lombok.Data;
import ru.ac.checkpointmanager.dto.user.UserResponseDTO;

import java.util.UUID;

@Data
public class AvatarDTO {

    private UUID avatarId;
    private UserResponseDTO userResponseDTO;

}
