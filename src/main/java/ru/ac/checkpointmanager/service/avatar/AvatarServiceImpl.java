package ru.ac.checkpointmanager.service.avatar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.dto.avatar.AvatarDTO;
import ru.ac.checkpointmanager.dto.avatar.AvatarImageDTO;
import ru.ac.checkpointmanager.exception.AvatarNotFoundException;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.mapper.avatar.AvatarMapper;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.avatar.Avatar;
import ru.ac.checkpointmanager.repository.AvatarRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AvatarServiceImpl implements AvatarService {
    public static final String AVATAR_NOT_FOUND_LOG = "[Avatar with id: {}] not found";

    private final AvatarRepository avatarRepository;
    private final UserRepository userRepository;
    private final TerritoryRepository territoryRepository;
    private final AvatarMapper avatarMapper;
    private final AvatarHelper avatarHelper;


    /**
     * Загружает и сохраняет аватар пользователя. Если аватар для пользователя уже существует,
     * обновляет его новым изображением, иначе создает новый.
     *
     * @param userId     идентификатор пользователя, для которого загружается аватар.
     * @param avatarFile файл аватара, который нужно загрузить.
     * @return объект AvatarDTO, представляющий загруженный или обновленный аватар.
     */
    @Override
    public AvatarDTO uploadAvatar(UUID userId, MultipartFile avatarFile) {
        if (!userRepository.existsById(userId)) {
            log.warn(ExceptionUtils.USER_NOT_FOUND_MSG, userId);
            throw new UserNotFoundException(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(userId));
        }
        //- достали юзера из бд
        //- сделали работу по подготовке аватарки к загрузке
        //- сохранили аватарку в репо
        //- присоединили аватарку к текущему юзеру
        //- апдейт юзера с новой аватаркой
        //- удалили старую аватарку из репозитория
        //- в рамках одной транзакции
        //- в рамках данной таски я на входе в метод проверю есть ли юзер, и проверю что если его нет вылетит ошибка
        //TODO Делаем кучу работы с объектом аватара, а потом вдруг выясняется что юзера нет в бд, валидирую до входа в контроллер
        //TODO вот тут две ситуации может быть
        //TODO либо нет юзера, либо нет аватара, если нет аватара - ок, а если нет юзера объект все равно создастся
        Avatar avatar = avatarHelper.getOrCreateAvatar(userId);

        avatarHelper.configureAvatar(avatar, avatarFile);
        avatarHelper.processAndSetAvatarImage(avatar, avatarFile);//TODO мы его с конфигурируем, сделали работу
        avatar = avatarHelper.saveAvatar(avatar);//TODO  сохраняем даже в бд.
        avatarHelper.updateUserAvatar(userId, avatar);//TODO проверяем есть ли юзер в базе только здесь

        log.info("Avatar ID updated for user {}", userId);
        return avatarMapper.toAvatarDTO(avatar);
    }

    //TODO есть идея сделать метод загрузки один для всех сущностей, а далее распределять.
    @Override
    public AvatarDTO uploadAvatarByTerritory(UUID territoryId, MultipartFile avatarFile) {
        if (!territoryRepository.existsById(territoryId)) {
            log.warn(ExceptionUtils.TERRITORY_NOT_FOUND_MSG.formatted(territoryId));
            throw new TerritoryNotFoundException(ExceptionUtils.TERRITORY_NOT_FOUND_MSG
                    .formatted(territoryId));
        }

        Avatar avatar = avatarHelper.getOrCreateAvatarByTerritory(territoryId);
        avatarHelper.configureAvatar(avatar, avatarFile);
        avatarHelper.processAndSetAvatarImage(avatar, avatarFile);//TODO мы его с конфигурируем, сделали работу
        avatar = avatarHelper.saveAvatar(avatar);//TODO  сохраняем даже в бд.
        avatarHelper.updateTerritoryAvatar(territoryId, avatar);//TODO проверяем есть ли юзер в базе только здесь

        log.info("Avatar ID updated for user {}", territoryId);
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
        //TODO начал переписывать тесты на реальные репозитории вместо моков и выяснил, что эту ситуацию очень сложно
        //смоделировать, зачем сначала лазить в юзер репо за аваАйди, а потом лезть в ава репо за самой авой
        //можно за один заход сходить в аватар репозиторий и достать по юзер айди, там даже метод такой есть
        log.debug("Searching [avatar with for user id: {}]", userId);
        Avatar avatar = avatarRepository.findByUserId(userId)
                .orElseThrow(() -> new AvatarNotFoundException("Avatar for user [id: {}] not found"));
        return avatarHelper.createAvatarImageDTO(avatar);
    }

    /**
     * Удаляет аватар пользователя, если он существует.
     *
     * @param avatarId Уникальный идентификатор сущности, аватар которой нужно удалить.
     */
    public void deleteAvatarIfExists(UUID avatarId) {
        log.debug("Attempting to delete avatar for avatarId ID: {}", avatarId);
        Avatar avatar = findAvatarById(avatarId);
        avatarRepository.delete(avatar);
        log.info("Avatar with ID: {} deleted successfully", avatarId);
    }

    /**
     * Удаляет аватар пользователя по id пользователя
     *
     * @param userId идентификатор пользователя
     * @throws UserNotFoundException   если пользователь не существует
     * @throws AvatarNotFoundException если аватар не найден (у юзера нет аватарок)
     */
    @Override
    @Transactional
    public void deleteAvatarByUserId(UUID userId) {
        User user = userRepository.findUserWithAvatarIdById(userId)
                .orElseThrow(() -> {
                    log.warn(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(userId));
                    return new UserNotFoundException(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(userId));
                });
        if (user.getAvatar() == null) {
            log.warn(ExceptionUtils.AVATAR_NOT_FOUND_FOR_USER.formatted(userId));
            throw new AvatarNotFoundException(ExceptionUtils.AVATAR_NOT_FOUND_FOR_USER.formatted(userId));
        }
        UUID avatarId = user.getAvatar().getId();
        avatarRepository.deleteById(avatarId);
        log.info("Avatar for user with [id: {}] was deleted", userId);
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
        Avatar avatar = avatarRepository.findById(avatarId)
                .orElseThrow(() -> new AvatarNotFoundException(ExceptionUtils.AVATAR_NOT_FOUND.formatted(avatarId)));
        return avatarHelper.createAvatarImageDTO(avatar);
    }

    /**
     * Получает изображение аватара по его идентификатору территории.
     *
     * @param territoryId Уникальный идентификатор аватара.
     * @return AvatarImageDTO, содержащий данные изображения аватара.
     * @throws AvatarNotFoundException если аватар с указанным идентификатором не найден.
     */
    @Override
    public AvatarImageDTO getAvatarImageByTerritoryId(UUID territoryId) {
        log.debug("Fetching avatar image for territory id: {}", territoryId);
        Territory territory = territoryRepository.findTerritoryByIdWithAvatar(territoryId)
                .orElseThrow(() -> {
                            log.warn(ExceptionUtils.TERRITORY_NOT_FOUND_MSG.formatted(territoryId));
                            return new TerritoryNotFoundException(
                                    ExceptionUtils.TERRITORY_NOT_FOUND_MSG.formatted(territoryId));
                        }
                );
        Avatar avatar = territory.getAvatar();
        if (avatar == null) {
            log.warn(ExceptionUtils.AVATAR_NOT_FOUND_FOR_TERRITORY.formatted(territoryId));
            throw new AvatarNotFoundException(ExceptionUtils.AVATAR_NOT_FOUND_FOR_TERRITORY.formatted(territoryId));
        }
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
        return avatarRepository.findById(avatarId).orElseThrow(() -> {
            log.warn(AVATAR_NOT_FOUND_LOG, avatarId);
            return new AvatarNotFoundException(ExceptionUtils.AVATAR_NOT_FOUND.formatted(avatarId));
        });
    }
}
