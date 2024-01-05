package ru.ac.checkpointmanager.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.dto.PhoneDTO;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.user.AuthResponseDTO;
import ru.ac.checkpointmanager.dto.user.NewEmailDTO;
import ru.ac.checkpointmanager.dto.user.NewPasswordDTO;
import ru.ac.checkpointmanager.dto.user.ConfirmationEmailDTO;
import ru.ac.checkpointmanager.dto.user.UserChangeDTO;
import ru.ac.checkpointmanager.dto.user.UserResponseDTO;
import ru.ac.checkpointmanager.exception.EmailAlreadyExistsException;
import ru.ac.checkpointmanager.exception.EmailVerificationTokenException;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.MismatchCurrentPasswordException;
import ru.ac.checkpointmanager.exception.ObjectAlreadyExistsException;
import ru.ac.checkpointmanager.exception.PasswordConfirmationException;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.mapper.TerritoryMapper;
import ru.ac.checkpointmanager.mapper.UserMapper;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.avatar.Avatar;
import ru.ac.checkpointmanager.model.enums.PhoneNumberType;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.repository.PhoneRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.security.authfacade.AuthFacade;
import ru.ac.checkpointmanager.security.jwt.JwtService;
import ru.ac.checkpointmanager.service.email.EmailService;
import ru.ac.checkpointmanager.service.phone.PhoneService;
import ru.ac.checkpointmanager.utils.FieldsValidation;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
 * @see AuthFacade
 */
@Service
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final String METHOD_UUID = "Method {}, UUID - {}";
    private static final String USER_NOT_FOUND_MSG = "User with [id=%s] not found";

    private final UserMapper userMapper;
    private final TerritoryMapper territoryMapper;
    private final UserRepository userRepository;
    private final PhoneRepository phoneRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PhoneService phoneService;
    private final RedisCacheManager cacheManager;
    private final JwtService jwtService;

    @Qualifier("userFacade")
    private final AuthFacade authFacade;

    public UserServiceImpl(UserMapper userMapper,
                           TerritoryMapper territoryMapper,
                           UserRepository userRepository,
                           PhoneRepository phoneRepository,
                           PasswordEncoder passwordEncoder,
                           EmailService emailService,
                           PhoneService phoneService,
                           RedisCacheManager cacheManager,
                           JwtService jwtService,
                           @Qualifier("userFacade") AuthFacade authFacade) {
        this.userMapper = userMapper;
        this.territoryMapper = territoryMapper;
        this.userRepository = userRepository;
        this.phoneRepository = phoneRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.phoneService = phoneService;
        this.cacheManager = cacheManager;
        this.jwtService = jwtService;
        this.authFacade = authFacade;
    }

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
    @Cacheable(value = "user", key = "#id")
    public UserResponseDTO findById(UUID id) {
        log.debug(METHOD_UUID, MethodLog.getMethodName(), id);
        User foundUser = findUserById(id);
        return userMapper.toUserResponseDTO(foundUser);
    }

    @Override
    public User findUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(
                () -> {
                    log.warn(USER_NOT_FOUND_MSG.formatted(id));
                    return new UserNotFoundException(String.format(USER_NOT_FOUND_MSG.formatted(id)));
                });
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
    @Cacheable(value = "user-territory", key = "#userId")
    @Override
    public List<TerritoryDTO> findTerritoriesByUserId(UUID userId) {
        log.debug(METHOD_UUID, MethodLog.getMethodName(), userId);
        //FIXME Если юзера нет, то просто даст пустой список, вместо ошибки, баг?
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
    @Cacheable(value = "user", key = "#name")
    @Override
    public Collection<UserResponseDTO> findByName(String name) {
        log.info("Method {} was invoked", MethodLog.getMethodName());
        return userMapper.toUserResponseDTOs(userRepository
                .findUserByFullNameContainingIgnoreCase(name));
    }

    @Override
    public UserResponseDTO findByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.warn(USER_NOT_FOUND_MSG.formatted(email));
            return new UserNotFoundException(USER_NOT_FOUND_MSG.formatted(email));
        });
        return userMapper.toUserResponseDTO(user);
    }

    /**
     * Обновляет информацию о пользователе на основе предоставленных данных.
     * <p>
     * Этот метод выполняет обновление данных пользователя, включая его полное имя и основной номер телефона.
     * Если предоставленный основной номер телефона не пуст, он также очищается и обновляется.
     * После обновления данных пользователя, эти изменения сохраняются в базе данных.
     * </p>
     *
     * @param userChangeDTO DTO пользователя, содержащее обновленные данные. Должно включать идентификатор пользователя,
     *                   а также может включать новое полное имя и основной номер телефона.
     * @return UserResponseDTO, содержащий обновленные данные пользователя.
     * @throws UserNotFoundException если пользователь с предоставленным идентификатором не найден.
     * @see UserChangeDTO
     * @see UserResponseDTO
     */
    @CacheEvict(value = "user", key = "#userChangeDTO.id")
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserResponseDTO updateUser(UserChangeDTO userChangeDTO) {
        UUID updateUserId = userChangeDTO.getId();
        log.debug("Updating user with [UUID - {}]", updateUserId);
        User foundUser = userRepository.findById(updateUserId)
                .orElseThrow(() -> {
                    log.warn(USER_NOT_FOUND_MSG.formatted(updateUserId));
                    return new UserNotFoundException(USER_NOT_FOUND_MSG.formatted(updateUserId));
                });

        foundUser.setFullName(userChangeDTO.getFullName());

        Optional.ofNullable(userChangeDTO.getMainNumber())
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
     * @param request Объект {@link NewPasswordDTO}, содержащий текущий и новый пароли.
     * @throws IllegalStateException если текущий пароль не соответствует или новый пароль и его подтверждение не совпадают.
     * @see AuthFacade
     */
    @Override
    @Transactional
    public void changePassword(NewPasswordDTO request) {
        User user = authFacade.getCurrentUser();
        log.debug("Method {}, Username - {}", MethodLog.getMethodName(), user.getUsername());

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Current password not matched for {}", user.getEmail());
            throw new MismatchCurrentPasswordException("Current password not matched");
        }

        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            log.warn("Passwords are not the same ");
            throw new PasswordConfirmationException("Passwords are not the same");
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
     * <li>Генерирует уникальный токен подтверждения и связывает его с временным пользователем.</li>
     * <li>Отправляет электронное письмо с подтверждением на новую электронную почту пользователя.</li>
     * <li>Сохраняет временного пользователя в кэше.</li>
     * </ol>
     * <p>
     * В случае ошибки при отправке электронного письма генерируется исключение {@link MailSendException}.
     * <p>
     *
     * @param request объект запроса, содержащий текущую и новую электронные почты пользователя.
     * @return объект запроса {@link NewEmailDTO} с обновленными данными.
     * @throws IllegalStateException если текущая электронная почта пользователя не соответствует указанной в запросе.
     * @throws MailSendException     если происходит ошибка при отправке электронного письма.
     */
    @CachePut(value = "email", key = "#result.verifiedToken")
    @Override
    @Transactional
    public ConfirmationEmailDTO changeEmail(NewEmailDTO request) {
        User user = authFacade.getCurrentUser();
        log.debug("[Method {}], [Username - {}]", MethodLog.getMethodName(), user.getUsername());

        if (userRepository.findByEmail(request.getNewEmail()).isPresent()) {
            log.warn(ExceptionUtils.EMAIL_EXISTS.formatted(request.getNewEmail()));
            throw new EmailAlreadyExistsException(ExceptionUtils.EMAIL_EXISTS.formatted(request.getNewEmail()));
        }

        ConfirmationEmailDTO confirmEmail = userMapper.toConfirmChangeEmail(request);
        confirmEmail.setPreviousEmail(user.getEmail());

        String token = UUID.randomUUID().toString();
        confirmEmail.setVerifiedToken(token);

        try {
            emailService.sendEmailConfirm(confirmEmail.getNewEmail(), token);
            log.info("Email confirmation message was sent");
        } catch (MailException e) {
            log.error("Email sending failed");
            throw new MailSendException("Email sending failed", e);
        }

        return confirmEmail;
    }

    /**
     * Подтверждает изменение электронной почты пользователя.
     * <p>
     * Метод выполняет следующие действия:
     * <ol>
     * <li>Ищет временного пользователя по предоставленному токену.</li>
     * <li>Если временный пользователь найден, находит основного пользователя по предыдущей электронной почте.</li>
     * <li>Обновляет электронную почту основного пользователя на новую, указанную во временном пользователе.</li>
     * <li>Удаляет временного пользователя из кэшах после успешного обновления.</li>
     * </ol>
     * <p>
     * В случае ошибки, когда токен недействителен или истек, выводится сообщение об ошибке.
     * <p>
     *
     * @param token уникальный токен подтверждения, используемый для идентификации временного пользователя.
     * @throws UserNotFoundException если пользователь с указанной предыдущей электронной почтой не найден.
     */
    @CacheEvict(value = "email", key = "#token")
    @Override
    @Transactional
    public AuthResponseDTO confirmEmail(String token) {
        log.debug("[Method {}], [Temporary token {}]", MethodLog.getMethodName(), token);
        Optional<ConfirmationEmailDTO> confirmEmail = Optional.ofNullable(
                        cacheManager.getCache("email"))
                .map(cache -> cache.get(token, ConfirmationEmailDTO.class));
        if (confirmEmail.isPresent()) {
            String previousEmail = confirmEmail.get().getPreviousEmail();
            String newEmail = confirmEmail.get().getNewEmail();
            User user = userRepository.findByEmail(previousEmail).orElseThrow(
                    () -> {
                        log.warn("User with [email %s] not found".formatted(previousEmail));
                        return new UserNotFoundException("User with [email %s] not found".formatted(previousEmail));
                    });
            user.setEmail(newEmail);
            userRepository.save(user);
            log.info("User email updated from {} to {}, [UUID {}]", previousEmail, newEmail, user.getId());
            Objects.requireNonNull(cacheManager.getCache("user")).evict(user.getId());

            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            return new AuthResponseDTO(accessToken, refreshToken);
        } else {
            log.warn("Invalid or expired token");
            throw new EmailVerificationTokenException("Invalid or expired token");
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
    @CacheEvict(value = "user", key = "#id")
    @Override
    @Transactional
    public void changeRole(UUID id, Role role) {
        log.debug(METHOD_UUID, MethodLog.getMethodName(), id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(USER_NOT_FOUND_MSG.formatted(id));
                    return new UserNotFoundException(USER_NOT_FOUND_MSG.formatted(id));
                });
        User user = authFacade.getCurrentUser();
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
            throw new ObjectAlreadyExistsException(String.format("This user already has role %s", role));
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
     * <p>
     *
     * @param id        Идентификатор пользователя, статус блокировки которого нужно обновить.
     * @param isBlocked Новый статус блокировки пользователя (true для блокировки, false для разблокировки).
     * @return {@link UserResponseDTO} - DTO пользователя с обновленным статусом.
     * @throws UserNotFoundException если пользователь с заданным идентификатором не найден.
     */
    @CacheEvict(value = "user", key = "#id")
    @Override
    @Transactional
    public UserResponseDTO updateBlockStatus(UUID id, Boolean isBlocked) {
        log.debug(METHOD_UUID, MethodLog.getMethodName(), id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(USER_NOT_FOUND_MSG.formatted(id));
                    return new UserNotFoundException(USER_NOT_FOUND_MSG.formatted(id));
                });
        if (existingUser.getIsBlocked() != isBlocked) {
            existingUser.setIsBlocked(isBlocked);
            userRepository.save(existingUser);
            log.debug("Block status {} for {} successfully changed", isBlocked, id);
        } else {
            log.warn("User {} already has block status {}", id, isBlocked);
        }
        return userMapper.toUserResponseDTO(existingUser);
    }

    /**
     * Блокирует пользователя по его идентификатору.
     * <p>
     * Этот метод устанавливает статус блокировки пользователя на 'true'. Если пользователь
     * с указанным идентификатором не найден, выбрасывается исключение {@link UserNotFoundException}.
     * <p>
     *
     * @param id Идентификатор пользователя, которого необходимо заблокировать.
     * @throws UserNotFoundException если пользователь с указанным идентификатором не найден.
     */
    @CacheEvict(value = "user", key = "#id")
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
     */
    @CacheEvict(value = "user", key = "#id")
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
    @CacheEvict(value = "user", key = "#id")
    @Override
    @Transactional
    public void deleteUser(UUID id) {
        log.debug(METHOD_UUID, MethodLog.getMethodName(), id);
        if (userRepository.findById(id).isEmpty()) {
            log.warn(USER_NOT_FOUND_MSG.formatted(id));
            throw new UserNotFoundException(USER_NOT_FOUND_MSG.formatted(id));
        }
        userRepository.deleteById(id);
        log.info("User {} successfully deleted", id);
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
    public Collection<String> findUsersPhoneNumbers(UUID userId) {
        log.debug("Method {}, UUID {}", MethodLog.getMethodName(), userId);
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
