package ru.ac.checkpointmanager.service.avatar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.dto.avatar.AvatarImageDTO;
import ru.ac.checkpointmanager.dto.avatar.AvatarDTO;
import ru.ac.checkpointmanager.exception.AvatarNotFoundException;
import ru.ac.checkpointmanager.mapper.avatar.AvatarMapper;
import ru.ac.checkpointmanager.model.avatar.Avatar;
import ru.ac.checkpointmanager.repository.AvatarRepository;
import ru.ac.checkpointmanager.repository.UserRepository;

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
     * Загружает и сохраняет аватар пользователя. Если аватар для пользователя уже существует,
     * обновляет его новым изображением, иначе создает новый.
     *
     * @param userId      идентификатор пользователя, для которого загружается аватар.
     * @param avatarFile  файл аватара, который нужно загрузить.
     * @return объект AvatarDTO, представляющий загруженный или обновленный аватар.
     * @throws IOException если происходит ошибка ввода-вывода при обработке файла аватара. FIXME we need to do smth to avoid this situation
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
     * Получает изображение аватара пользователя по идентификатору пользователя.
     *
     * @param userId Уникальный идентификатор пользователя.
     * @return AvatarImageDTO, содержащий данные изображения аватара пользователя.
     * @throws AvatarNotFoundException если аватар для указанного пользователя не найден.
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
     * Удаляет аватар пользователя, если он существует.
     *
     * @param avatarId Уникальный идентификатор сущности, аватар которой нужно удалить.
     * @return Удаленный объект Avatar, если он существует, иначе возвращает null.
     */
    public Avatar deleteAvatarIfExists(UUID avatarId) {
        log.debug("Attempting to delete avatar for entity ID: {}", avatarId);
        return findAvatarById(avatarId);
    }

    /**
     * Получает изображение аватара по его уникальному идентификатору.
     *
     * @param avatarId Уникальный идентификатор аватара.
     * @return AvatarImageDTO, содержащий данные изображения аватара.
     * @throws AvatarNotFoundException если аватар с указанным идентификатором не найден.
     */
    @Override
    public AvatarImageDTO getAvatarImageByAvatarId(UUID avatarId) {
        log.debug("Fetching avatar image for avatar ID: {}", avatarId);
        Avatar avatar = repository.findById(avatarId)
                .orElseThrow(() -> new AvatarNotFoundException(AVATAR_NOT_FOUND_MSG.formatted(avatarId)));
        return avatarHelper.createAvatarImageDTO(avatar);
    }

    /**
     * Находит аватар по его уникальному идентификатору.
     *
     * @param avatarId Уникальный идентификатор аватара.
     * @return Найденный объект Avatar.
     * @throws AvatarNotFoundException если аватар с указанным идентификатором не найден.
     */
    @Override
    public Avatar findAvatarById(UUID avatarId) {
        log.debug("Searching for avatar with ID: {}", avatarId);
        return repository.findById(avatarId).orElseThrow(() -> {
            log.warn(AVATAR_NOT_FOUND_LOG, avatarId);
            return new AvatarNotFoundException(AVATAR_NOT_FOUND_MSG.formatted(avatarId));
        });
    }
}
