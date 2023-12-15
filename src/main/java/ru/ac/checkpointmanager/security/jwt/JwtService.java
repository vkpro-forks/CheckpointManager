package ru.ac.checkpointmanager.security.jwt;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public interface JwtService {
    String extractUsername(String token);

    List<String> extractRole(String token);

    UUID extractId(String token);

    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    String generateToken(UserDetails userDetails);

    String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);

    String generateRefreshToken(UserDetails userDetails);

    Claims extractAllClaims(String token);
}
