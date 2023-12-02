package ru.ac.checkpointmanager.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.dto.ChangeEmailRequest;
import ru.ac.checkpointmanager.dto.ChangePasswordRequest;
import ru.ac.checkpointmanager.dto.PhoneDTO;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.user.UserPutDTO;
import ru.ac.checkpointmanager.dto.user.UserResponseDTO;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.mapper.TerritoryMapper;
import ru.ac.checkpointmanager.mapper.UserMapper;
import ru.ac.checkpointmanager.model.TemporaryUser;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.avatar.Avatar;
import ru.ac.checkpointmanager.model.enums.PhoneNumberType;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.repository.PhoneRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.service.email.EmailService;
import ru.ac.checkpointmanager.service.phone.PhoneService;
import ru.ac.checkpointmanager.utils.FieldsValidation;
import ru.ac.checkpointmanager.utils.MethodLog;
import ru.ac.checkpointmanager.utils.SecurityUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервисный класс для управления информацией о пользователях.
 * <p>
 * Этот класс обеспечивает основную бизнес-логику, связанную с пользователями в приложении.
 * Включает в себя функции поиска, обновления, блокировки/разблокировки пользователей,
 * а также изменения их паролей и ролей. Класс использует различные репозитории и сервисы
 * для выполнения операций с данными и для обеспечения безопасности и валидации ввода.
 * <p>
 *
 * @author fifimova
 * @author Ldv236
 * @see User
 * @see UserRepository
 * @see EmailService
 * @see SecurityUtils
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private static final String METHOD_UUID = "Method {}, UUID - {}";
    private static final String USER_NOT_FOUND_MSG = "User with [id=%s] not found";

    private final UserMapper userMapper;
    private final TerritoryMapper territoryMapper;
    private final UserRepository userRepository;
    private final PhoneRepository phoneRepository;
    private final PasswordEncoder passwordEncoder;
    private final TemporaryUserService temporaryUserService;
    private final EmailService emailService;
    private final PhoneService phoneService;

    /**
     * Находит пользователя по его уникальному идентификатору (UUID).
     * <p>
     * Этот метод осуществляет поиск пользователя в репозитории. Если пользователь не найден,
     * выбрасывается исключение {@link UserNotFoundException}.
     * <p>
     *
     * @param id Уникальный идентификатор пользователя, который необходимо найти.
     * @return {@link UserResponseDTO} - DTO пользователя для ответа со всеми основными полями, если он найден.
     * @throws UserNotFoundException если пользователь с указанным UUID не найден.
     * @see UserNotFoundException
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO findById(UUID id) {
        log.debug(METHOD_UUID, MethodLog.getMethodName(), id);
        User foundUser = findUserById(id);
        return userMapper.toUserResponseDTO(foundUser);
    }

    @Override
    public User findUserById(UUID id) {
        User foundUser = userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException(String.format("User not found [id=%s]", id)));
        return foundUser;
    }

    /**
     * Находит список территорий, связанных с пользователем по его уникальному идентификатору (UUID).
     * <p>
     * Этот метод выполняет поиск всех территорий, которые связаны с указанным пользователем.
     * Если территории для данного пользователя не найдены, выбрасывается исключение {@link TerritoryNotFoundException}.
     * <p>
     *
     * @param userId Уникальный идентификатор пользователя, для которого нужно найти территории.
     * @return Список {@link TerritoryDTO}, представляющий территории пользователя.
     * @throws TerritoryNotFoundException если территории для указанного пользователя не найдены.
     * @see TerritoryNotFoundException
     */
    @Override
    @Transactional(readOnly = true)
    public List<TerritoryDTO> findTerritoriesByUserId(UUID userId) {
        log.debug(METHOD_UUID, MethodLog.getMethodName(), userId);
        List<Territory> territories = userRepository.findTerritoriesByUserId(userId);
        return territoryMapper.toTerritoriesDTO(territories);
    }

    /**
     * Находит пользователей по части их полного имени.
     * <p>
     * Этот метод выполняет поиск пользователей, чье полное имя содержит указанную строку.
     * Поиск производится без учета регистра символов. Если пользователи не найдены, выбрасывается
     * исключение {@link UserNotFoundException}.
     * <p>
     *
     * @param name Строка, содержащаяся в полном имени пользователя, по которой будет осуществляться поиск.
     * @return Коллекция {@link UserResponseDTO}, представляющая найденных пользователей.
     * @throws UserNotFoundException если пользователи с именем, содержащим указанную строку, не найдены.
     */
    @Override
    @Transactional(readOnly = true)
    public Collection<UserResponseDTO> findByName(String name) {
        log.info("Method {} was invoked", MethodLog.getMethodName());
        return userMapper.toUserResponseDTOs(userRepository
                .findUserByFullNameContainingIgnoreCase(name));
    }

    /**
     * Обновляет информацию о пользователе на основе предоставленных данных.
     * <p>
     * Этот метод выполняет обновление данных пользователя, включая его полное имя и основной номер телефона.
     * Если предоставленный основной номер телефона не пуст, он также очищается и обновляется.
     * После обновления данных пользователя, эти изменения сохраняются в базе данных.
     * </p>
     *
     * @param userPutDTO DTO пользователя, содержащее обновленные данные. Должно включать идентификатор пользователя,
     *                   а также может включать новое полное имя и основной номер телефона.
     * @return UserResponseDTO, содержащий обновленные данные пользователя.
     * @throws UserNotFoundException если пользователь с предоставленным идентификатором не найден.
     * @see UserPutDTO
     * @see UserResponseDTO
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserResponseDTO updateUser(UserPutDTO userPutDTO) {
        log.debug("Updating user with [UUID - {}]", userPutDTO.getId());
        User foundUser = userRepository.findById(userPutDTO.getId())
                .orElseThrow(() -> new UserNotFoundException(
                        String.format(USER_NOT_FOUND_MSG, userPutDTO.getId())));

        foundUser.setFullName(userPutDTO.getFullName());

        Optional.ofNullable(userPutDTO.getMainNumber())
                .filter(mainNumber -> !mainNumber.isEmpty())
                .map(FieldsValidation::cleanPhone)
                .ifPresent(newMainNumber -> {
                    foundUser.setMainNumber(newMainNumber);
                    phoneService.createPhoneNumber(createPhoneDTO(foundUser));
                });

        userRepository.save(foundUser);
        log.info("[User {}] updated", foundUser.getId());

        return userMapper.toUserResponseDTO(foundUser);
    }

    /**
     * Создает DTO телефона на основе данных пользователя.
     * <p>
     * Этот вспомогательный метод используется для создания DTO телефона из данных пользователя.
     * </p>
     *
     * @param user Объект пользователя, для которого создается DTO телефона.
     * @return PhoneDTO, содержащий данные телефона пользователя.
     */
    private PhoneDTO createPhoneDTO(User user) {
        PhoneDTO phoneDTO = new PhoneDTO();
        phoneDTO.setNumber(user.getMainNumber());
        phoneDTO.setType(PhoneNumberType.MOBILE);
        phoneDTO.setUserId(user.getId());
        return phoneDTO;
    }

    /**
     * Изменяет пароль текущего аутентифицированного пользователя.
     * <p>
     * Этот метод позволяет пользователю изменить свой пароль. Сначала проверяется, соответствует ли текущий пароль
     * указанному в запросе. Если нет, выбрасывается исключение. Далее проверяется, совпадают ли новый пароль и его
     * подтверждение; в случае несовпадения также выбрасывается исключение. После успешной проверки пароль пользователя
     * обновляется.
     * <p>
     *
     * @param request Объект {@link ChangePasswordRequest}, содержащий текущий и новый пароли.
     * @throws IllegalStateException если текущий пароль не соответствует или новый пароль и его подтверждение не совпадают.
     * @see SecurityUtils
     */
    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = SecurityUtils.getCurrentUser();
        log.debug("Method {}, Username - {}", MethodLog.getMethodName(), user.getUsername());

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Current password not matched for {}", user.getEmail());
            throw new IllegalStateException("Current password not matched");
        }

        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            log.warn("Passwords are not the same ");
            throw new IllegalStateException("Passwords are not the same");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.debug("Password for {} successfully changed", user.getEmail());
    }

    /**
     * Инициирует процесс изменения электронной почты пользователя.
     * <p>
     * Метод выполняет следующие действия:
     * <ol>
     * <li>Проверяет, соответствует ли текущая электронная почта пользователя электронной почте, указанной в запросе.</li>
     * <li>Создает временного пользователя {@link TemporaryUser} на основе данных основного пользователя.</li>
     * <li>Генерирует уникальный токен подтверждения и связывает его с временным пользователем.</li>
     * <li>Отправляет электронное письмо с подтверждением на новую электронную почту пользователя.</li>
     * <li>Сохраняет временного пользователя в базе данных.</li>
     * </ol>
     * <p>
     * В случае ошибки при отправке электронного письма генерируется исключение {@link MailSendException}.
     * <p>
     *
     * @param request объект запроса, содержащий текущую и новую электронные почты пользователя.
     * @return объект запроса {@link ChangeEmailRequest} с обновленными данными.
     * @throws IllegalStateException если текущая электронная почта пользователя не соответствует указанной в запросе.
     * @throws MailSendException     если происходит ошибка при отправке электронного письма.
     */
    @Transactional
    @Override
    public String changeEmail(ChangeEmailRequest request) {
        User user = SecurityUtils.getCurrentUser();
        log.debug("[Method {}], [Username - {}]", MethodLog.getMethodName(), user.getUsername());

        if (userRepository.findByEmail(request.getNewEmail()).isPresent()) {
            log.warn("[Email {}] already taken", request.getNewEmail());
            throw new IllegalStateException(String.format("Email %s already taken", request.getNewEmail()));
        }

        TemporaryUser tempUser = userMapper.toTemporaryUser(user);
        tempUser.setPreviousEmail(user.getEmail());
        tempUser.setEmail(request.getNewEmail());

        String token = UUID.randomUUID().toString();
        tempUser.setVerifiedToken(token);

        try {
            emailService.sendEmailConfirm(tempUser.getEmail(), token);
            log.info("Email confirmation message was sent");
        } catch (MailException e) {
            log.error("Email sending failed");
            throw new MailSendException("Email sending failed", e);
        }

        temporaryUserService.create(tempUser);
        return token;
    }

    /**
     * Подтверждает изменение электронной почты пользователя.
     * <p>
     * Метод выполняет следующие действия:
     * <ol>
     * <li>Ищет временного пользователя {@link TemporaryUser} по предоставленному токену.</li>
     * <li>Если временный пользователь найден, находит основного пользователя по предыдущей электронной почте.</li>
     * <li>Обновляет электронную почту основного пользователя на новую, указанную во временном пользователе.</li>
     * <li>Удаляет временного пользователя из базы данных после успешного обновления.</li>
     * </ol>
     * <p>
     * В случае ошибки, когда токен недействителен или истек, выводится сообщение об ошибке.
     * <p>
     *
     * @param token уникальный токен подтверждения, используемый для идентификации временного пользователя.
     * @throws UserNotFoundException если пользователь с указанной предыдущей электронной почтой не найден.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Override
    public void confirmEmail(String token) {
        log.debug("[Method {}], [Temporary token {}]", MethodLog.getMethodName(), token);
        TemporaryUser tempUser = temporaryUserService.findByVerifiedToken(token);

        if (tempUser != null) {
            User user = userRepository.findByEmail(tempUser.getPreviousEmail()).orElseThrow(()
                    -> new UserNotFoundException(String.format("User email %s not found", tempUser.getPreviousEmail())));

            user.setEmail(tempUser.getEmail());
            userRepository.save(user);
            log.info("User email updated from {} to {}, [UUID {}]", tempUser.getPreviousEmail(), user.getEmail(), user.getId());

            temporaryUserService.delete(tempUser);
            log.info("[Temporary user {}] deleted", tempUser.getId());
        } else {
            log.warn("Invalid or expired token");
        }
    }

    /**
     * Изменяет роль пользователя по его идентификатору.
     * <p>
     * Метод позволяет изменить роль заданного пользователя. Проверяется право текущего пользователя
     * (connectedUser) на выполнение данного действия. Если текущий пользователь не имеет права назначить
     * или изменить роль ADMIN, выбрасывается исключение {@link AccessDeniedException}. Также исключение
     * генерируется, если пытается измениться роль на уже существующую.
     * <p>
     *
     * @param id   Идентификатор пользователя, роль которого нужно изменить.
     * @param role Новая роль, которую необходимо назначить пользователю.
     * @throws UserNotFoundException если пользователь с заданным идентификатором не найден.
     * @throws AccessDeniedException если у пользователя, выполняющего операцию, нет прав на изменение роли.
     * @throws IllegalStateException если пользователь уже имеет указанную роль.
     */
    @Override
    @Transactional
    public void changeRole(UUID id, Role role) {
        log.debug(METHOD_UUID, MethodLog.getMethodName(), id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User not found [Id=%s]", id)));

        User user = SecurityUtils.getCurrentUser();

        if (role == Role.ADMIN && !user.getRole().equals(Role.ADMIN)) {
            log.error("Users with role {} do not have permission to change the role to ADMIN", user.getRole());
            throw new AccessDeniedException("You do not have permission to change the role to ADMIN");
        }

        if (existingUser.getRole().equals(Role.ADMIN) && !user.getRole().equals(Role.ADMIN)) {
            log.error("Users with role {} do not have permission to change the role ADMIN to another role", user.getRole());
            throw new AccessDeniedException("You do not have permission to change the role ADMIN to another role");
        }
        if (existingUser.getRole().equals(role)) {
            log.warn("User {} already has role {}", existingUser.getId(), existingUser.getRole());
            throw new IllegalStateException(String.format("This user already has role %s", role));
        }
        existingUser.setRole(role);
        userRepository.save(existingUser);
        log.debug("Role for {} successfully changed", existingUser.getId());
    }

    /**
     * Обновляет статус блокировки пользователя по его идентификатору.
     * <p>
     * Этот метод позволяет изменить статус блокировки пользователя. Если пользователь с указанным
     * идентификатором не найден, выбрасывается исключение {@link UserNotFoundException}. Метод проверяет,
     * отличается ли новый статус блокировки от текущего, и, в случае различия, обновляет его.
     * Если статус уже соответствует указанному, генерируется {@link IllegalStateException}.
     * <p>
     *
     * @param id        Идентификатор пользователя, статус блокировки которого нужно обновить.
     * @param isBlocked Новый статус блокировки пользователя (true для блокировки, false для разблокировки).
     * @return {@link UserResponseDTO} - DTO пользователя с обновленным статусом.
     * @throws UserNotFoundException если пользователь с заданным идентификатором не найден.
     * @throws IllegalStateException если статус блокировки пользователя уже соответствует указанному значению.
     */
    @Override
    @Transactional
    public UserResponseDTO updateBlockStatus(UUID id, Boolean isBlocked) {
        log.debug(METHOD_UUID, MethodLog.getMethodName(), id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User not found [Id=%s]", id)));

        if (existingUser.getIsBlocked() != isBlocked) {
            existingUser.setIsBlocked(isBlocked);
            userRepository.save(existingUser);
            log.debug("Block status {} for {} successfully changed", isBlocked, id);
        } else {
            log.warn("User {} already has block status {}", id, isBlocked);
            throw new IllegalStateException(String.format("User already %s [id=%s]", isBlocked ? "blocked" : "unblocked", id));
        }
        return userMapper.toUserResponseDTO(existingUser);
    }

    /**
     * Блокирует пользователя по его идентификатору.
     * <p>
     * Этот метод устанавливает статус блокировки пользователя на 'true'. Если пользователь
     * с указанным идентификатором не найден, выбрасывается исключение {@link UserNotFoundException}.
     * В случае, если пользователь уже заблокирован, выбрасывается {@link IllegalStateException}.
     * <p>
     *
     * @param id Идентификатор пользователя, которого необходимо заблокировать.
     * @throws UserNotFoundException если пользователь с указанным идентификатором не найден.
     * @throws IllegalStateException если пользователь уже заблокирован.
     */
    @Override
    @Transactional
    public void blockById(UUID id) {
        log.debug(METHOD_UUID, MethodLog.getMethodName(), id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format(USER_NOT_FOUND_MSG, id)));
        if (!existingUser.getIsBlocked()) {
            userRepository.blockById(id);
            log.debug("User {} successfully blocked", id);
        } else {
            log.warn("User {} already blocked", id);
            throw new IllegalStateException(String.format("User already blocked [id=%s]", id));
        }
    }

    /**
     * Разблокирует пользователя по его идентификатору.
     * <p>
     * Этот метод устанавливает статус блокировки пользователя на 'false'. Если пользователь
     * с указанным идентификатором не найден, выбрасывается исключение {@link UserNotFoundException}.
     * В случае, если пользователь уже разблокирован, выбрасывается {@link IllegalStateException}.
     * <p>
     *
     * @param id Идентификатор пользователя, которого необходимо разблокировать.
     * @throws UserNotFoundException если пользователь с указанным идентификатором не найден.
     * @throws IllegalStateException если пользователь уже разблокирован.
     */
    @Override
    @Transactional
    public void unblockById(UUID id) {
        log.debug(METHOD_UUID, MethodLog.getMethodName(), id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format(USER_NOT_FOUND_MSG, id)));
        if (existingUser.getIsBlocked()) {
            userRepository.unblockById(id);
            log.debug("User {} successfully unblocked", id);
        } else {
            log.warn("User {} already unblocked", id);
            throw new IllegalStateException(String.format("User already unblocked [id=%s]", id));
        }
    }

    /**
     * Удаляет пользователя по его идентификатору.
     * <p>
     * Этот метод выполняет удаление пользователя из базы данных. Если пользователь
     * с указанным идентификатором не найден, выбрасывается исключение {@link UserNotFoundException}.
     * <p>
     *
     * @param id Идентификатор пользователя, которого необходимо удалить.
     * @throws UserNotFoundException если пользователь с указанным идентификатором не найден.
     */
    @Override
    @Transactional
    public void deleteUser(UUID id) {
        log.debug(METHOD_UUID, MethodLog.getMethodName(), id);
        if (userRepository.findById(id).isEmpty()) {
            log.warn("Error deleting user {}", id);
            throw new UserNotFoundException("Error deleting user with ID" + id);
        }
        userRepository.deleteById(id);
        log.debug("User {} successfully deleted", id);
    }

    /**
     * Получает список всех пользователей из базы данных.
     * <p>
     * Этот метод возвращает коллекцию DTO всех пользователей. Если пользователи отсутствуют
     * в базе данных, выбрасывается исключение {@link UserNotFoundException}.
     * <p>
     *
     * @return Коллекция {@link UserResponseDTO}, содержащая информацию о всех пользователях.
     * @throws UserNotFoundException если в базе данных нет пользователей.
     */
    @Override
    @Transactional(readOnly = true)
    public Collection<UserResponseDTO> getAll() {
        log.debug("Method {}", MethodLog.getMethodName());
        return userMapper.toUserResponseDTOs(userRepository.findAll());
    }

    /**
     * Находит все номера телефонов, связанные с пользователем.
     * <p>
     * Этот метод возвращает коллекцию номеров телефонов для указанного пользователя. Если пользователь
     * с заданным идентификатором не найден, выбрасывается исключение {@link UserNotFoundException}.
     * <p>
     *
     * @param userId Идентификатор пользователя, чьи номера телефонов нужно найти.
     * @return Коллекция строк, представляющих номера телефонов пользователя.
     * @throws UserNotFoundException если пользователь с указанным идентификатором не найден.
     */
    @Override
    @Transactional(readOnly = true)
    public Collection<String> findUsersPhoneNumbers(UUID userId) {
        log.debug("Method {}, UUID {}", MethodLog.getMethodName(), userId);
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("Error getting users {} phones with", userId);
            throw new UserNotFoundException(String.format("Error getting users %s phones", userId));
        }
        return phoneRepository.getNumbersByUserId(userId);
    }

    /**
     * Назначает аватар указанному пользователю.
     * <p>
     * Этот метод обновляет аватар пользователя в базе данных.
     * <p>
     *
     * @param userId Идентификатор пользователя, которому нужно назначить аватар.
     * @param avatar Объект {@link Avatar}, представляющий аватар пользователя.
     */
    @Override
    @Transactional
    public void assignAvatarToUser(UUID userId, Avatar avatar) {
        userRepository.setAvatarForUser(avatar, userId);
    }

    @Override
    public User findByPassId(UUID passId) {
        return userRepository.findByPassId(passId);
    }
}