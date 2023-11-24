package ru.ac.checkpointmanager.service.avatar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.dto.AvatarImageDTO;
import ru.ac.checkpointmanager.exception.AvatarLoadingException;
import ru.ac.checkpointmanager.dto.AvatarDTO;
import ru.ac.checkpointmanager.exception.AvatarNotFoundException;
import ru.ac.checkpointmanager.exception.AvatarProcessingException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.mapper.AvatarMapper;
import ru.ac.checkpointmanager.model.Avatar;
import ru.ac.checkpointmanager.model.AvatarProperties;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.repository.AvatarRepository;
import ru.ac.checkpointmanager.repository.UserRepository;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AvatarServiceImpl implements AvatarService {

    public static final String AVATAR_NOT_FOUND_LOG = "[Avatar with id: {}] not found";
    public static final String AVATAR_NOT_FOUND_MSG = "Avatar with id: %s not found";
    private final AvatarRepository repository;
    private final UserRepository userRepository;
    private final AvatarMapper avatarMapper;
    private final AvatarHelper avatarHelper;



    /**
     * Загружает и сохраняет аватар пользователя. Если для данного пользователя уже существует аватар,
     * он будет обновлен новым изображением. В противном случае будет создан новый аватар.
     *
     * @param userId     идентификатор пользователя, для которого загружается аватар.
     * @param avatarFile файл аватара, который нужно загрузить.
     * @return объект Avatar, представляющий загруженный или обновленный аватар.
     * @throws IOException если происходит ошибка ввода-вывода при обработке файла аватара.
     */
    @Override
    public AvatarDTO uploadAvatar(UUID userId, MultipartFile avatarFile) {
        log.info("Method uploadAvatar invoked for entityId: {}", userId);

        avatarHelper.validateAvatar(avatarFile);
        Avatar avatar = avatarHelper.getOrCreateAvatar(userId);
        avatarHelper.configureAvatar(avatar, avatarFile);
        avatarHelper.processAndSetAvatarImage(avatar, avatarFile);
        avatar = avatarHelper.saveAvatar(avatar);
        avatarHelper.updateUserAvatar(userId, avatar);

        log.info("Avatar ID updated for user {}", userId);
        return avatarMapper.toAvatarDTO(avatar);
    }


    /**
     * Возвращает аватар для указанного идентификатора сущности.
     *
     * @param userId Уникальный идентификатор сущности, аватар которой нужно получить.
     * @return Объект AvatarImageDTO, соответствующий указанному идентификатору сущности.
     */
    @Override
    public AvatarImageDTO getAvatarByUserId(UUID userId) {
        log.debug("Fetching avatar for user ID: {}", userId);
        UUID avatarId = userRepository.findAvatarIdByUserId(userId);
        if (avatarId == null) {
            log.warn("[User with id: {}] doesn't have avatar", userId);
            throw new AvatarNotFoundException("User with id: %s doesn't have avatar".formatted(userId));
        }
        log.debug("Searching [avatar with id: {}]", avatarId);
        Avatar avatar = repository.findById(avatarId)
                .orElseThrow(() -> new AvatarNotFoundException(AVATAR_NOT_FOUND_MSG.formatted(avatarId)));
        return avatarHelper.createAvatarImageDTO(avatar);
    }


    /**
     * Удаляет аватар пользователя, если он существует, и возвращает удаленный аватар.
     * Если аватар для указанного идентификатора сущности не найден, ничего не делает и возвращает null.
     *
     * @param entityID Уникальный идентификатор сущности, аватар которой нужно удалить.
     * @return Удаленный объект Avatar или null, если аватар не был найден.
     */
    public Avatar deleteAvatarIfExists(UUID entityID) {
        log.debug("Attempting to delete avatar for entity ID: {}", entityID);
        return findAvatarById(entityID);
    }

    @Override
    public AvatarImageDTO getAvatarImageByAvatarId(UUID avatarId) {
        log.debug("Fetching avatar image for avatar ID: {}", avatarId);
        Avatar avatar = repository.findById(avatarId)
                .orElseThrow(() -> new AvatarNotFoundException(AVATAR_NOT_FOUND_MSG.formatted(avatarId)));
        return avatarHelper.createAvatarImageDTO(avatar);
    }

    @Override
    public Avatar findAvatarById(UUID avatarId) {
        log.debug("Searching for avatar with ID: {}", avatarId);
        return repository.findById(avatarId).orElseThrow(() -> {
            log.warn(AVATAR_NOT_FOUND_LOG, avatarId);
            return new AvatarNotFoundException(AVATAR_NOT_FOUND_MSG.formatted(avatarId));
        });
    }
}
