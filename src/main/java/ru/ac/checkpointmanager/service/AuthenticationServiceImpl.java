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
import ru.ac.checkpointmanager.repository.PhoneRepository;
import ru.ac.checkpointmanager.repository.TokenRepository;
import ru.ac.checkpointmanager.repository.UserRepository;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import static ru.ac.checkpointmanager.model.enums.PhoneNumberType.MOBILE;
import static ru.ac.checkpointmanager.utils.FieldsValidation.cleanPhone;
import static ru.ac.checkpointmanager.utils.FieldsValidation.validateDOB;
import static ru.ac.checkpointmanager.utils.Mapper.toTemporaryUser;
import static ru.ac.checkpointmanager.utils.Mapper.toUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PhoneRepository phoneRepository;
    private final PhoneService phoneService;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final TemporaryUserService temporaryUserService;
    private final Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

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

        if (phoneRepository.existsByNumber(cleanPhone(userAuthDTO.getMainNumber()))) {
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
            throw new MailSendException(String.format("Non-existent email address %s was provided", userAuthDTO.getEmail()));
        }

        temporaryUserService.create(temporaryUser);
        return temporaryUser;
    }

    @Transactional
    @Override
    public void confirmRegistration(String token) {
        log.info("Method confirmRegistration was invoked");
        TemporaryUser tempUser = temporaryUserService.findByVerifiedToken(token);

        if (tempUser != null) {
            User user = toUser(tempUser);
            user.setRole(Role.USER);
            user.setIsBlocked(false);
            user.setAddedAt(currentTimestamp);

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


    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate( // проверяет  правильность предоставленных учетных данных юзера
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

    /* метод отзыва всех предыдущих токенов пользователя.
     * Это может быть полезно, например, если у пользователя был украден или утерян токен доступа или обновления */
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

    /* метод позволяет обновить токен доступа пользователя на основе предоставленного токена обновления */
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

            if (jwtService.isTokenValid(refreshToken, user)) { // проверяет токен обновления действителен ли для данного пользователя
                String accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);

                AuthenticationResponse authResponse = new AuthenticationResponse(accessToken, refreshToken);
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse); // записывает ответ в JSON
            }
        }
    }
}
