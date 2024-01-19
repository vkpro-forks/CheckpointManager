package ru.ac.checkpointmanager.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.dto.user.AuthRequestDTO;
import ru.ac.checkpointmanager.dto.user.AuthResponseDTO;
import ru.ac.checkpointmanager.dto.user.LoginResponseDTO;
import ru.ac.checkpointmanager.dto.user.PreAuthResponseDTO;
import ru.ac.checkpointmanager.dto.user.RefreshTokenDTO;
import ru.ac.checkpointmanager.dto.user.RegistrationConfirmationDTO;
import ru.ac.checkpointmanager.dto.user.RegistrationDTO;
import ru.ac.checkpointmanager.exception.EmailAlreadyExistsException;
import ru.ac.checkpointmanager.exception.EmailVerificationTokenException;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.mapper.UserMapper;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.security.jwt.JwtService;
import ru.ac.checkpointmanager.security.jwt.JwtValidator;
import ru.ac.checkpointmanager.service.email.EmailService;
import ru.ac.checkpointmanager.utils.FieldsValidation;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис регистрации и аутентификации пользователей.
 *
 * @author fifimova
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    public static final String METHOD_WAS_INVOKED = "Method {} was invoked";
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final JwtValidator jwtValidator;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final RedisCacheManager cacheManager;


    /**
     * Предварительная регистрация нового пользователя в системе.
     * <p>
     * Метод проверяет отсутствие электронной почты в базе данных.
     * <p>
     * Если все проверки пройдены успешно, создается объект {@code TemporaryUser} на основе переданного объекта {@code UserAuthDTO}.
     * Пароль пользователя шифруется с помощью {@link BCryptPasswordEncoder},
     * устанавливается дата и время создания.
     * Генерируется уникальный токен для верификации, который прикрепляется к письму и отправляется на электронную почту пользователя.
     * Если письмо не отправляется, регистрируется ошибка при отправке.
     * <p>
     * В случае сбоя на каком-либо этапе, все изменения, внесенные в методе, будут отменены.
     *
     * @param registrationDTO объект передачи данных пользователя.
     * @return TemporaryUser, представляющий предварительно зарегистрированного пользователя.
     * @throws EmailAlreadyExistsException если пользователь с указанным email уже существует.
     * @throws MailSendException           если отправка письма с токеном подтверждения не удалась.
     * @see RegistrationDTO
     * @see FieldsValidation
     * @see EmailService
     */
    @CachePut(value = "registration", key = "#result.verifiedToken")
    @Transactional
    @Override
    public RegistrationConfirmationDTO preRegister(RegistrationDTO registrationDTO) {
        log.debug(METHOD_WAS_INVOKED, MethodLog.getMethodName());
        boolean userExist = userRepository.findByEmail(registrationDTO.getEmail()).isPresent();
        if (userExist) {
            log.warn(ExceptionUtils.EMAIL_EXISTS.formatted(registrationDTO.getEmail()));
            throw new EmailAlreadyExistsException(ExceptionUtils.EMAIL_EXISTS.formatted(registrationDTO.getEmail()));
        }
        RegistrationConfirmationDTO confirmUser = userMapper.toConfirmRegistration(registrationDTO);
        String encodedPassword = passwordEncoder.encode(confirmUser.getPassword());
        confirmUser.setPassword(encodedPassword);
        String token = UUID.randomUUID().toString();
        confirmUser.setVerifiedToken(token);

        try {
            emailService.sendRegisterConfirm(registrationDTO.getEmail(), token);
            log.info("Mail message was sent to {}", registrationDTO.getEmail());
        } catch (MailException e) {
            log.error("Email sending failed for {}. Error: {}", registrationDTO.getEmail(), e.getMessage());
            throw new MailSendException("Email sending failed", e);
        }

        return confirmUser;
    }

    /**
     * Подтверждение регистрацию пользователя.
     * <p>
     * Метод находит {@code TemporaryUser} по предоставленному токену верификации. Если {@code TemporaryUser} найден,
     * он конвертируется в {@code User}, сохраняется в репозитории, а затем объект временного пользователя удаляется из репозитория {@code TemporaryUser}.
     * Для пользователя генерируется и сохраняется токен JWT.
     * Если с предоставленным токеном верификации {@code TemporaryUser} не найден, выбрасывается исключение.
     * <p>
     * В случае сбоя на каком-либо этапе, все изменения, внесенные в методе, будут отменены.
     *
     * @param token токен подтверждения регистрации.
     * @throws EmailVerificationTokenException если токен подтверждения не найден
     * @throws UserNotFoundException           если пользователь с указанным токеном не найден.
     * @see User
     * @see JwtService
     */
    @CacheEvict(value = "registration", key = "#token")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Override
    public void confirmRegistration(String token) {
        log.debug(METHOD_WAS_INVOKED, MethodLog.getMethodName());
        Optional<RegistrationConfirmationDTO> confirmUser = Optional.ofNullable(
                        cacheManager.getCache("registration"))
                .map(cache -> cache.get(token, RegistrationConfirmationDTO.class));
        if (confirmUser.isEmpty()) {
            log.warn(ExceptionUtils.INVALID_EMAIL_TOKEN_MSG.formatted(token));
            throw new EmailVerificationTokenException(ExceptionUtils.INVALID_EMAIL_TOKEN_MSG.formatted(token));
        }
        User user = userMapper.toUser(confirmUser);
        user.setRole(Role.USER);
        user.setIsBlocked(false);
        userRepository.save(user);
        log.debug("User registration completed successfully for {}", user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public PreAuthResponseDTO isUserAuthenticated(String email) {
        log.debug("Method {}, email {}", MethodLog.getMethodName(), email);
        Optional<User> foundUser = userRepository.findByEmail(email);
        return foundUser.map(user -> new PreAuthResponseDTO(true, user.getFullName()))
                .orElseGet(() -> new PreAuthResponseDTO(false, null));
    }

    /**
     * Аутентифицирует пользователя и генерирует для него JWT токен доступа и токен обновления.
     * <p>
     * Метод использует предоставленный запрос {@code AuthenticationRequest} для аутентификации пользователя с помощью {@link AuthenticationManager}.
     * Если аутентификация прошла успешно, идет поиск пользователя в репозитории по его электронной почте.
     * Для найденного пользователя генерируются и сохраняются токен доступа и токен обновления, а все предыдущие токены пользователя аннулируются.
     * Новый токен JWT сохраняется, и возвращается ответ AuthenticationResponse, содержащий токен JWT и токен обновления.
     * Если пользователь не найден в репозитории, выбрасывается исключение {@code UsernameNotFoundException}.
     *
     * @param request объект с данными {@code email} и {@code password}.
     * @return {@link AuthResponseDTO} - объект с данными аутентификации пользователя.
     * @throws UsernameNotFoundException если пользователь с указанным email не найден.
     * @see JwtService
     * @see UsernamePasswordAuthenticationToken
     */
    @Override
    public LoginResponseDTO authenticate(AuthRequestDTO request) {
        log.debug("Method {}, Username {}", MethodLog.getMethodName(), request.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                ));
        User user = (User) authentication.getPrincipal();

        String jwtToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        log.debug("Access and refresh tokens for {} created", user.getEmail());

        LoginResponseDTO response = userMapper.toLoginResponse(user);
        response.setAccessToken(jwtToken);
        response.setRefreshToken(refreshToken);
        return response;
    }

    /**
     * Обновляет токен доступа пользователя на основе предоставленного токена обновления.
     * <p>
     * Метод выполняет проверку токена обновления и, при успешной валидации,
     * извлекает идентификатор пользователя из токена. Затем осуществляется поиск пользователя в репозитории.
     * Если пользователь найден, генерируется новый токен доступа.
     * <p>
     *
     * @param refreshTokenDTO Объект {@link RefreshTokenDTO}, содержащий токен обновления.
     * @return {@link AuthResponseDTO}, содержащий новый токен доступа и токен обновления.
     * @throws UserNotFoundException если пользователь не найден.
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO refreshToken(RefreshTokenDTO refreshTokenDTO) {
        log.debug(METHOD_WAS_INVOKED, MethodLog.getMethodName());
        final String refreshToken = refreshTokenDTO.getRefreshToken();
        jwtValidator.validateRefreshToken(refreshToken);
        String username = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmail(username).orElseThrow(() -> {
            log.warn(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(username));
            return new UserNotFoundException(String.format(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(username)));
        });
        String accessToken = jwtService.generateAccessToken(user);
        return new AuthResponseDTO(accessToken, refreshToken);
    }
}
