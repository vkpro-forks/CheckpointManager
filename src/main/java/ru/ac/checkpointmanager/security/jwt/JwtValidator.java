package ru.ac.checkpointmanager.security.jwt;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtValidator {
    boolean validateAccessToken(String token);

    void validateRefreshToken(String token);

    boolean isUsernameValid(String token, UserDetails userDetails);
}
