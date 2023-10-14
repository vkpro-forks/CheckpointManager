package ru.ac.checkpointmanager.service.avatar;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.model.Avatar;

import java.io.IOException;
import java.util.UUID;

/**
 * AvatarService interface that provides basic actions related to avatar.
 * In all operations ID of entity has to be specified.
 */
public interface AvatarService {

    /**
     * Sets avatar image for entity whose ID is passed.
     * This method responds well on updating avatar request,
     * because it deletes previous image file associated with the entity if one is detected in the directory
     * @param entityID ID of entity for passed avatar
     * @param avatarFile avatar file
     * @throws IOException when I/O errors occurs
     */
    void uploadAvatar(UUID entityID, MultipartFile avatarFile) throws IOException;

    void getAvatar(UUID entityID, HttpServletResponse response) throws IOException;

    void deleteAvatar(UUID entityID);

    /**
     * If avatar is present in DB then it will be returned
     * otherwise AvatarNotFoundException will be thrown
     * @param entityID ID of entity whose avatar is being searched
     * @return Avatar entity if entity has one
     */
    Avatar findAvatarOrThrow(UUID entityID);
}
