package ru.ac.checkpointmanager.service.avatar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.dto.avatar.AvatarDTO;
import ru.ac.checkpointmanager.dto.avatar.AvatarImageDTO;
import ru.ac.checkpointmanager.exception.AvatarNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.mapper.avatar.AvatarMapper;
import ru.ac.checkpointmanager.model.User;
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

    private static final String USER_NOT_FOUND_LOG = "User with [id: {}] not found";
    private static final String USER_NOT_FOUND_MSG = "User with id: %s not found";

    private final AvatarRepository repository;
    private final UserRepository userRepository;

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
        log.info("Method uploadAvatar invoked for entityId: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn(USER_NOT_FOUND_LOG, userId);
                    return new UserNotFoundException("This check should be not here");//FIXME это должно перед обработкой картинки
                });
        //- достали юзера из бд
        //- сделали работу по подготовке аватарки к загрузке
        //- сохранили аватарку в репо
        //- присоединили аватарку к текущему юзеру
        //- апдейт юзера с новой аватаркой
        //- удалили старую аватарку из репозитория
        //- в рамках одной транзакции
        //- в рамках данной таски я на входе в метод проверю есть ли юзер, и проверю что если его нет вылетит ошибка
        //TODO Делаем кучу работы с объектом аватара, а потом вдруг выясняется что юзера нет в бд
        avatarHelper.validateAvatar(avatarFile);
        Avatar avatar = avatarHelper.getOrCreateAvatar(userId);//TODO вот тут две ситуации может быть
        //TODO либо нет юзера, либо нет аватара, если нет аватара - ок, а если нет юзера объект все равно создастся
        avatarHelper.configureAvatar(avatar, avatarFile);
        avatarHelper.processAndSetAvatarImage(avatar, avatarFile);//TODO мы его с конфигурируем, сделали работу
        avatar = avatarHelper.saveAvatar(avatar);//TODO  сохраняем даже в бд.
        avatarHelper.updateUserAvatar(userId, avatar);//TODO проверяем есть ли юзер в базе только здесь
        //

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
        //TODO начал переписывать тесты на реальные репозитории вместо моков и выяснил, что эту ситуацию очень сложно
        //смоделировать, зачем сначала лазить в юзер репо за аваАйди, а потом лезть в ава репо за самой авой
        //можно за один заход сходить в аватар репозиторий и достать по юзер айди, там даже метод такой есть
        log.debug("Searching [avatar with for user id: {}]", userId);
        Avatar avatar = repository.findByUserId(userId)
                .orElseThrow(() -> new AvatarNotFoundException("Avatar for user [id: {}] not found"));
        return avatarHelper.createAvatarImageDTO(avatar);
    }

    /**
     * Удаляет аватар пользователя, если он существует.
     *
     * @param avatarId Уникальный идентификатор сущности, аватар которой нужно удалить.
     * @return Удаленный объект Avatar, если он существует, иначе возвращает null.
     */
    public void deleteAvatarIfExists(UUID avatarId) {
        log.debug("Attempting to delete avatar for avatarId ID: {}", avatarId);
        Avatar avatar = findAvatarById(avatarId);
        repository.delete(avatar);
        log.info("Avatar with ID: {} deleted successfully", avatarId);
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
