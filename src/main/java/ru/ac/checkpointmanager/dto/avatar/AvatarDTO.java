package ru.ac.checkpointmanager.dto.avatar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ac.checkpointmanager.dto.user.UserResponseDTO;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvatarDTO {

    private UUID avatarId;
    private String mediaType;
}
