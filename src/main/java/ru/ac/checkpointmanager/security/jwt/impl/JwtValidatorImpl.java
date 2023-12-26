package ru.ac.checkpointmanager.security.jwt.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.InvalidTokenException;
import ru.ac.checkpointmanager.security.jwt.JwtService;
import ru.ac.checkpointmanager.security.jwt.JwtValidator;
import ru.ac.checkpointmanager.utils.MethodLog;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtValidatorImpl implements JwtValidator {

    public static final String BLANK_SUBJECT_MSG = "JWT is not valid, username is blank or empty";
    private final JwtService jwtService;
    public static final String ERROR_MESSAGE = "Error occurred during parsing access token [{}]";

    public static final String REFRESH = "refresh";

    /**
     * Метод проверяет токен доступа, если при парсинге всех клеймов не вылетает исключений, токен не содержит поля
     * refresh и subject не пустой, то токен валиден
     *
     * @param token JWT access token
     * @return boolean true: если токен валидный, false если возникли исключения, и токен не валидный
     * @throws ExpiredJwtException если истекло время действия (бросается из {@link JwtService})
     */
    @Override
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = jwtService.extractAllClaims(token);
            String username = claims.getSubject();
            if (StringUtils.isBlank(username)) {
                log.debug(BLANK_SUBJECT_MSG);
                return false;
            }
            if (!ObjectUtils.isEmpty(claims.get(REFRESH))) {
                log.debug("JWT is not valid, contains label for refresh token");
                return false;
            }
            log.debug("Access token validated");
            return true;
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException ex) {
            log.warn(ERROR_MESSAGE, ex.getMessage());
            return false;
        }
    }

    @Override
    public void validateRefreshToken(String token) {
        try {
            Claims claims = jwtService.extractAllClaims(token);
            String username = claims.getSubject();
            if (StringUtils.isBlank(username)) {
                log.debug(BLANK_SUBJECT_MSG);
                throw new InvalidTokenException(BLANK_SUBJECT_MSG);
            }
            if (ObjectUtils.isEmpty(claims.get(REFRESH))) {
                log.debug("JWT is not valid, doesn't contains label for refresh token");
                throw new InvalidTokenException("Jwt is not a refresh token");
            }
            log.debug("Refresh token validated");
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException |
                 ExpiredJwtException | IllegalArgumentException e) {
            log.warn(ERROR_MESSAGE, e.getMessage());
            throw new InvalidTokenException(e.getMessage());
        }
    }

    @Override
    public boolean isUsernameValid(String token, UserDetails userDetails) {
        log.debug("Method {}, User {}", MethodLog.getMethodName(), userDetails.getUsername());
        final String username = jwtService.extractUsername(token);
        return username.equals(userDetails.getUsername());
    }
}
