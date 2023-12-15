package ru.ac.checkpointmanager.security.jwt;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtValidator {
    boolean validateAccessToken(String token);

    boolean validateRefreshToken(String token);

    boolean isUsernameValid(String token, UserDetails userDetails);
}
