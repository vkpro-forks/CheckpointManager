package ru.ac.checkpointmanager.security.jwt.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.security.jwt.JwtService;
import ru.ac.checkpointmanager.security.jwt.JwtValidator;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtValidatorImpl implements JwtValidator {

    private final JwtService jwtService;
    public static final String ERROR_MESSAGE = "Error occurred during parsing access token [{}]";

    public static final String REFRESH = "refresh";

    @Override
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = jwtService.extractAllClaims(token);
            if (claims.getExpiration().before(new Date())) {
                return false;
            }
            log.info("Access token validated");
            return ObjectUtils.isEmpty(claims.get(REFRESH));
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException |
                 ExpiredJwtException | IllegalArgumentException e) {
            log.warn(ERROR_MESSAGE, e.getMessage());
            return false;
        }
    }


    @Override
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = jwtService.extractAllClaims(token);
            if (claims.getExpiration().before(new Date())) {
                return false;
            }
            log.info("Refresh token validated");
            return !ObjectUtils.isEmpty(claims.get(REFRESH));
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException |
                 ExpiredJwtException | IllegalArgumentException exception) {
            log.error(ERROR_MESSAGE, exception.getMessage());
            return false;
        }
    }

    @Override
    public boolean isUsernameValid(String token, UserDetails userDetails) {
        log.debug("Method {}, User {}", MethodLog.getMethodName(), userDetails.getUsername());
        final String username = jwtService.extractUsername(token);
        return username.equals(userDetails.getUsername());
    }
}
