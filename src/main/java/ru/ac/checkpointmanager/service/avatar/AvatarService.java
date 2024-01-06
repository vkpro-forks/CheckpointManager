package ru.ac.checkpointmanager.service.avatar;

import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.dto.avatar.AvatarDTO;
import ru.ac.checkpointmanager.dto.avatar.AvatarImageDTO;
import ru.ac.checkpointmanager.model.avatar.Avatar;

import java.util.UUID;

/**
 * AvatarService interface that provides basic actions related to avatar.
 * In all operations ID of entity has to be specified.
 */
public interface AvatarService {

    AvatarDTO uploadAvatar(UUID userId, MultipartFile avatarFile);

    AvatarImageDTO getAvatarByUserId(UUID userId);

    void deleteAvatarIfExists(UUID avatarId);

    AvatarImageDTO getAvatarImageByAvatarId(UUID avatarId);

    Avatar findAvatarById(UUID avatarId);

}
