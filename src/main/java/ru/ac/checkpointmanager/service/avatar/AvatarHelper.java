package ru.ac.checkpointmanager.service.avatar;

import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.dto.AvatarImageDTO;
import ru.ac.checkpointmanager.model.Avatar;

import java.awt.image.BufferedImage;
import java.util.UUID;

interface AvatarHelper {

    String getExtension(String fileName);

    BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight);

    void validateAvatar(MultipartFile avatarFile);

    Avatar getOrCreateAvatar(UUID entityId);

    void configureAvatar(Avatar avatar, MultipartFile avatarFile);

    void processAndSetAvatarImage(Avatar avatar, MultipartFile avatarFile);

    BufferedImage processImage(BufferedImage originalImage);

    Avatar saveAvatar(Avatar avatar);

    void updateUserAvatar(UUID entityId, Avatar avatar);

    AvatarImageDTO createAvatarImageDTO(Avatar avatar);
}
