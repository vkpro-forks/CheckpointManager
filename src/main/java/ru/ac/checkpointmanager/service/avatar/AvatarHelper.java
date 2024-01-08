package ru.ac.checkpointmanager.service.avatar;

import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.dto.avatar.AvatarImageDTO;
import ru.ac.checkpointmanager.model.avatar.Avatar;

import java.awt.image.BufferedImage;
import java.util.UUID;

interface AvatarHelper {

    BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight);

    Avatar getOrCreateAvatar(UUID userId);

    Avatar getOrCreateAvatarByTerritory(UUID territoryId);

    void configureAvatar(Avatar avatar, MultipartFile avatarFile);

    void processAndSetAvatarImage(Avatar avatar, MultipartFile avatarFile);

    BufferedImage processImage(BufferedImage originalImage);

    Avatar saveAvatar(Avatar avatar);

    void updateUserAvatar(UUID userId, Avatar avatar);

    AvatarImageDTO createAvatarImageDTO(Avatar avatar);

    void updateTerritoryAvatar(UUID territoryId, Avatar avatar);
}
