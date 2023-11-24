package ru.ac.checkpointmanager.service.avatar;

import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.dto.AvatarDTO;
import ru.ac.checkpointmanager.dto.AvatarImageDTO;
import ru.ac.checkpointmanager.model.Avatar;

import java.io.IOException;
import java.util.UUID;

/**
 * AvatarService interface that provides basic actions related to avatar.
 * In all operations ID of entity has to be specified.
 */
public interface AvatarService {

    AvatarDTO uploadAvatar(UUID entityID, MultipartFile avatarFile);

    AvatarImageDTO getAvatarByUserId(UUID userId);


    Avatar deleteAvatarIfExists(UUID entityID);

    AvatarImageDTO getAvatarImageByAvatarId(UUID avatarId);

    Avatar findAvatarById(UUID entityID);

}
