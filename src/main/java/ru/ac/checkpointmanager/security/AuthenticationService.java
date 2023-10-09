package ru.ac.checkpointmanager.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.configuration.JwtService;
import ru.ac.checkpointmanager.dto.PhoneDTO;
import ru.ac.checkpointmanager.dto.UserAuthDTO;
import ru.ac.checkpointmanager.exception.DateOfBirthFormatException;
import ru.ac.checkpointmanager.exception.PhoneAlreadyExistException;
import ru.ac.checkpointmanager.model.Token;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.model.enums.TokenType;
import ru.ac.checkpointmanager.repository.PhoneRepository;
import ru.ac.checkpointmanager.repository.TokenRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.service.PhoneService;
import ru.ac.checkpointmanager.service.UserServiceImpl;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import static ru.ac.checkpointmanager.model.enums.PhoneNumberType.MOBILE;
import static ru.ac.checkpointmanager.utils.FieldsValidation.cleanPhone;
import static ru.ac.checkpointmanager.utils.FieldsValidation.validateDOB;
import static ru.ac.checkpointmanager.utils.Mapper.*;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PhoneRepository phoneRepository;
    private final PhoneService phoneService;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

    @Transactional
    public UserAuthDTO createUser(UserAuthDTO userAuthDTO) {
        logger.info("Method createUser was invoked");
        boolean userExist = userRepository.findByEmail(userAuthDTO.getEmail()).isPresent();
        if (userExist) {
            logger.warn("Email already taken");
            throw new IllegalStateException(String.format("Email %s already taken", userAuthDTO.getEmail()));
        }

        if (!validateDOB(userAuthDTO.getDateOfBirth())) {
            logger.warn("Invalid date of birth");
            throw new DateOfBirthFormatException("Date of birth should not be greater than the current date");
        }

        if (phoneRepository.existsByNumber(cleanPhone(userAuthDTO.getMainNumber()))) {
            logger.warn("Phone already taken");
            throw new PhoneAlreadyExistException(String.format
                    ("Phone number %s already exist", userAuthDTO.getMainNumber()));
        }

        User user = toUser(userAuthDTO);
        user.setRole(Role.USER);
        user.setMainNumber(cleanPhone(userAuthDTO.getMainNumber()));
        user.setIsBlocked(false);
        user.setAddedAt(currentTimestamp);

        String encodedPassword = passwordEncoder.encode(userAuthDTO.getPassword());
        user.setPassword(encodedPassword);

        userRepository.save(user);
        logger.info("User saved");

        PhoneDTO phoneDTO = createPhoneDTO(user);
        phoneService.createPhoneNumber(phoneDTO);
        
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(user, jwtToken);
        
        AuthenticationResponse response = new AuthenticationResponse();
                response.setRefreshToken(refreshToken);
                response.setAccessToken(jwtToken);

        return toUserAuthDTO(user);
    }

    private PhoneDTO createPhoneDTO(User user) {
        PhoneDTO phoneDTO = new PhoneDTO();
        phoneDTO.setUserId(user.getId());
        phoneDTO.setNumber(user.getMainNumber());
        phoneDTO.setType(MOBILE);
        return phoneDTO;
    }

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
