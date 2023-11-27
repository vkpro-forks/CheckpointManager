package ru.ac.checkpointmanager.service.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.configuration.JwtService;
import ru.ac.checkpointmanager.dto.AuthenticationRequest;
import ru.ac.checkpointmanager.dto.AuthenticationResponse;
import ru.ac.checkpointmanager.dto.IsAuthenticatedResponse;
import ru.ac.checkpointmanager.dto.user.LoginResponse;
import ru.ac.checkpointmanager.dto.user.UserAuthDTO;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.mapper.UserMapper;
import ru.ac.checkpointmanager.model.TemporaryUser;
import ru.ac.checkpointmanager.model.Token;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.model.enums.TokenType;
import ru.ac.checkpointmanager.repository.TokenRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.service.email.EmailService;
import ru.ac.checkpointmanager.service.user.TemporaryUserService;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.io.IOException;
import java.util.List;
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
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final TemporaryUserService temporaryUserService;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private final UserMapper userMapper;

    /**
     * Предварительная регистрация нового пользователя в системе.
     * <p>
     * Метод проверяет отсутствие электронной почты в базе данных.
     * <p>
     * Если все проверки пройдены успешно, создается объект {@code TemporaryUser} на основе переданного объекта {@code UserAuthDTO}.
     * Пароль пользователя шифруется с помощью {@link org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder},
     * устанавливается дата и время создания.
     * Генерируется уникальный токен для верификации, который прикрепляется к письму и отправляется на электронную почту пользователя.
     * Если письмо не отправляется, регистрируется ошибка при отправке.
     * <p>
     * В случае сбоя на каком-либо этапе, все изменения, внесенные в методе, будут отменены.
     *
     * @param userAuthDTO объект передачи данных пользователя.
     * @return TemporaryUser, представляющий предварительно зарегистрированного пользователя.
     * @throws IllegalStateException если пользователь с указанным email уже существует.
     * @throws MailSendException     если отправка письма с токеном подтверждения не удалась.
     * @see TemporaryUser
     * @see UserAuthDTO
     * @see ru.ac.checkpointmanager.utils.FieldsValidation
     * @see EmailService
     */
    @Transactional
    @Override
    public TemporaryUser preRegister(UserAuthDTO userAuthDTO) {
        log.debug("Method {} was invoked", MethodLog.getMethodName());
        boolean userExist = userRepository.findByEmail(userAuthDTO.getEmail()).isPresent();
        if (userExist) {
            log.warn("Email {} already taken", userAuthDTO.getEmail());
            throw new IllegalStateException(String.format("Email %s already taken", userAuthDTO.getEmail()));
        }

        TemporaryUser temporaryUser = userMapper.toTemporaryUser(userAuthDTO);

        String encodedPassword = passwordEncoder.encode(temporaryUser.getPassword());
        temporaryUser.setPassword(encodedPassword);

        String token = UUID.randomUUID().toString();
        temporaryUser.setVerifiedToken(token);

        try {
            emailService.sendRegisterConfirm(userAuthDTO.getEmail(), token);
            log.info("Mail message was sent to {}", userAuthDTO.getEmail());
        } catch (MailException e) {
            log.error("Email sending failed for {}. Error: {}", userAuthDTO.getEmail(), e.getMessage());
            throw new MailSendException("Email sending failed", e);
        }

        temporaryUserService.create(temporaryUser);
        log.debug("Temporary user {} was saved", temporaryUser.getEmail());
        return temporaryUser;
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
     * @throws UserNotFoundException если пользователь с указанным токеном не найден.
     * @see TemporaryUser
     * @see User
     * @see JwtService
     */
    @Transactional
    @Override
    public void confirmRegistration(String token) {
        log.debug("Method {} was invoked", MethodLog.getMethodName());
        TemporaryUser tempUser = temporaryUserService.findByVerifiedToken(token);

        if (tempUser != null) {
            User user = userMapper.toUser(tempUser);
            user.setRole(Role.USER);
            user.setIsBlocked(false);

            userRepository.save(user);
            log.debug("User registration completed successfully for {}", user.getEmail());

            temporaryUserService.delete(tempUser);
            log.debug("Temporary user {} was deleted", tempUser.getEmail());

            String jwtToken = jwtService.generateToken(user);
            saveUserToken(user, jwtToken);
            log.debug("Access token for {} created and saved", user.getEmail());

        } else {
            log.error("Email not confirmed. Error: temporary user with token {} not found", token);
            throw new UserNotFoundException(String.format("User with token - '%s', not found  ", token));
        }
    }

    @Override
    public IsAuthenticatedResponse isUserAuthenticated(String email) {
        log.debug("Method {}, email {}", MethodLog.getMethodName(), email);
        Optional<User> foundUser = userRepository.findByEmail(email);
        return foundUser.map(user -> new IsAuthenticatedResponse(true, user.getFullName()))
                .orElseGet(() -> new IsAuthenticatedResponse(false, null));
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
     * @return {@link AuthenticationResponse} - объект с данными аутентификации пользователя.
     * @throws UsernameNotFoundException если пользователь с указанным email не найден.
     * @see JwtService
     * @see UsernamePasswordAuthenticationToken
     */
    @Override
    public LoginResponse authenticate(AuthenticationRequest request) {
        log.debug("Method {}, Username {}", MethodLog.getMethodName(), request.getEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                ));

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() ->
                new UserNotFoundException(String.format("User with email - '%s', not found  ", request.getEmail())));

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        log.debug("Access and refresh tokens for {} created", user.getEmail());

        revokeAllUserTokens(user);
        log.debug("Previous user {} tokens was revoked", user.getEmail());

        saveUserToken(user, jwtToken);
        log.debug("Access token for {} saved", user.getEmail());

        LoginResponse response = userMapper.toLoginResponse(user);
        response.setAccessToken(jwtToken);
        response.setRefreshToken(refreshToken);
        return response;
    }

    @Override
    public void saveUserToken(User user, String jwtToken) {
        log.info("Method {} was invoked", MethodLog.getMethodName());
        Token token = new Token();
        token.setUser(user);
        token.setToken(jwtToken);
        token.setTokenType(TokenType.BEARER);
        token.setExpired(false);
        token.setRevoked(false);

        tokenRepository.save(token);
    }

    /**
     * Отменяет все действительные токены для данного пользователя.
     * <p>
     * Метод находит все действительные токены пользователя в репозитории. Если такие есть,
     * они помечаются как просроченные и отозванные, а затем сохраняются обратно в репозиторий.
     *
     * @param user пользователь, токены которого должны быть отозваны.
     */
    @Override
    public void revokeAllUserTokens(User user) {
        log.info("Method {} was invoked", MethodLog.getMethodName());
        List<Token> validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    /**
     * Обновляет токен доступа пользователя.
     * <p>
     * Этот метод извлекает токен обновления из заголовка авторизации HTTP-запроса, проверяет его действительность и,
     * если токен действителен, генерирует новый токен доступа, отзывает все предыдущие токены пользователя и сохраняет новый токен.
     * Затем метод создает новый объект {@code AuthenticationResponse} с новым токеном доступа и токеном обновления,
     * преобразует его в JSON и записывает в поток ответа.
     * <p>
     * Если заголовок авторизации отсутствует или не начинается с "Bearer ", или если пользователь с указанным адресом электронной почты не найден,
     * метод просто возвращает управление.
     *
     * @param request  HTTP-запрос, содержащий заголовок авторизации с токеном обновления.
     * @param response HTTP-ответ, в который будет записан новый токен доступа.
     * @throws IOException если произошла ошибка при записи ответа.
     * @see JwtService
     */
    @Override
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.debug("Method {} was invoked", MethodLog.getMethodName());
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Authorization header is missing or does not start with Bearer String");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Token is missing or invalid");
            return;
        }

        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            User user = this.userRepository.findByEmail(userEmail).orElseThrow(() ->
                    new UsernameNotFoundException(String.format("User with email - '%s', not found  ", userEmail)));

            if (jwtService.isTokenValid(refreshToken, user)) {
                String accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);

                AuthenticationResponse authResponse = new AuthenticationResponse(accessToken, refreshToken);
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            } else {
                log.warn("Refresh token is not valid for user {}", userEmail);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Refresh Token");
            }
        } else {
            log.warn("User email could not be extracted from refresh token");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Refresh Token");
        }
    }
}