package ru.ac.checkpointmanager.service;

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
import ru.ac.checkpointmanager.dto.PhoneDTO;
import ru.ac.checkpointmanager.dto.UserAuthDTO;
import ru.ac.checkpointmanager.exception.DateOfBirthFormatException;
import ru.ac.checkpointmanager.exception.PhoneAlreadyExistException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.model.TemporaryUser;
import ru.ac.checkpointmanager.model.Token;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.model.enums.TokenType;
import ru.ac.checkpointmanager.repository.TokenRepository;
import ru.ac.checkpointmanager.repository.UserRepository;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static ru.ac.checkpointmanager.model.enums.PhoneNumberType.MOBILE;
import static ru.ac.checkpointmanager.utils.FieldsValidation.cleanPhone;
import static ru.ac.checkpointmanager.utils.FieldsValidation.validateDOB;
import static ru.ac.checkpointmanager.utils.Mapper.toTemporaryUser;
import static ru.ac.checkpointmanager.utils.Mapper.toUser;

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
    private final PhoneService phoneService;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final TemporaryUserService temporaryUserService;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * Предварительная регистрация нового пользователя в системе.
     * <p>
     * Метод проверяет отсутствие в базе данных указанных электронной почты и номера телефона,
     * валидность даты рождения.
     * <p>
     * Если все проверки пройдены успешно, создается объект {@code TemporaryUser} на основе переданного объекта {@code UserAuthDTO}.
     * Номер телефона очищается от лишних символов, пароль пользователя шифруется с помощью {@link org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder},
     * устанавливается дата и время создания.
     * Генерируется уникальный токен для верификации, который прикрепляется к письму и отправляется на электронную почту пользователя.
     * Если письмо не отправляется, регистрируется ошибка при отправке.
     * <p>
     * В случае сбоя на каком-либо этапе, все изменения, внесенные в методе, будут отменены.
     *
     * @param userAuthDTO объект передачи данных пользователя.
     * @return TemporaryUser, представляющий предварительно зарегистрированного пользователя.
     * @throws IllegalStateException      если пользователь с указанным email уже существует.
     * @throws DateOfBirthFormatException если указанная дата рождения больше текущей даты.
     * @throws PhoneAlreadyExistException если указанный номер телефона уже занят.
     * @throws MailSendException          если отправка письма с токеном подтверждения не удалась.
     * @see TemporaryUser
     * @see UserAuthDTO
     * @see ru.ac.checkpointmanager.utils.FieldsValidation
     * @see EmailService
     */
    @Transactional
    @Override
    public TemporaryUser preRegister(UserAuthDTO userAuthDTO) {
        log.info("Method preRegister was invoked");
        boolean userExist = userRepository.findByEmail(userAuthDTO.getEmail()).isPresent();
        if (userExist) {
            log.warn("Email already taken");
            throw new IllegalStateException(String.format("Email %s already taken", userAuthDTO.getEmail()));
        }

        if (!validateDOB(userAuthDTO.getDateOfBirth())) {
            log.warn("Invalid date of birth");
            throw new DateOfBirthFormatException("Date of birth should not be greater than the current date");
        }

        if (phoneService.existsByNumber(cleanPhone(userAuthDTO.getMainNumber()))) {
            log.warn("Phone already taken");
            throw new PhoneAlreadyExistException(String.format
                    ("Phone number %s already exist", userAuthDTO.getMainNumber()));
        }

        TemporaryUser temporaryUser = toTemporaryUser(userAuthDTO);
        temporaryUser.setMainNumber(cleanPhone(temporaryUser.getMainNumber()));

        String encodedPassword = passwordEncoder.encode(temporaryUser.getPassword());
        temporaryUser.setPassword(encodedPassword);

        String token = UUID.randomUUID().toString();
        temporaryUser.setVerifiedToken(token);

        try {
            emailService.send(userAuthDTO.getEmail(), token);
            log.info("Mail message was sent");
        } catch (MailException e) {
            log.error("Email sending failed");
            throw new MailSendException("Email sending failed", e);
        }

        temporaryUserService.create(temporaryUser);
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
        log.info("Method confirmRegistration was invoked");
        TemporaryUser tempUser = temporaryUserService.findByVerifiedToken(token);

        if (tempUser != null) {
            User user = toUser(tempUser);
            user.setRole(Role.USER);
            user.setIsBlocked(false);

            userRepository.save(user);
            log.info("User saved");

            temporaryUserService.delete(tempUser);
            log.info("tempUser deleted");

            String jwtToken = jwtService.generateToken(user);
            saveUserToken(user, jwtToken);

            PhoneDTO phoneDTO = createPhoneDTO(user);
            phoneService.createPhoneNumber(phoneDTO);
            log.info("Phone saved");
        } else {
            log.error("Email not confirmed");
            throw new UserNotFoundException(String.format("User with token - '%s', not found  ", token));
        }
    }

    private PhoneDTO createPhoneDTO(User user) {
        PhoneDTO phoneDTO = new PhoneDTO();
        phoneDTO.setUserId(user.getId());
        phoneDTO.setNumber(user.getMainNumber());
        phoneDTO.setType(MOBILE);
        return phoneDTO;
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
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() ->
                new UsernameNotFoundException(String.format("User with email - '%s', not found  ", request.getEmail())));

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        AuthenticationResponse response = new AuthenticationResponse();
        response.setAccessToken(jwtToken);
        response.setRefreshToken(refreshToken);
        return response;
    }

    private void saveUserToken(User user, String jwtToken) {
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
    private void revokeAllUserTokens(User user) {
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
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
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
            }
        }
    }
}