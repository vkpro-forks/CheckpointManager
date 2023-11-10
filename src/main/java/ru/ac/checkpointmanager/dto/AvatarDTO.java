package ru.ac.checkpointmanager.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AvatarDTO {

    private UUID avatarId;
    private UserDTO userDTO;

}
