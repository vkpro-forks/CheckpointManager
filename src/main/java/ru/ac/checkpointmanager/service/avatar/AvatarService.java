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
    Avatar uploadAvatar(UUID entityID, MultipartFile avatarFile) throws IOException;

    Avatar getAvatarByUserId(UUID entityID) throws IOException;

    /**
     * Method searches for avatar in table by entity id.
     * If there is one, then removal of file in directory is performed.
     * If result of search is nothing, then method returns with no error.
     *
     * @param entityID id of entity, which avatar needs to deleted
     * @return
     */
    Avatar deleteAvatarIfExists(UUID entityID) throws IOException;

    Avatar findAvatarOrThrow(UUID entityID);

    /**
     * If avatar is present in DB then it will be returned
     * otherwise AvatarNotFoundException will be thrown
     * @param entityID ID of entity whose avatar is being searched
     * @return Avatar entity if entity has one
     */
}
