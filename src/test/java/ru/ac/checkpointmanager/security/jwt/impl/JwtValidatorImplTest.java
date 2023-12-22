package ru.ac.checkpointmanager.security.jwt.impl;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.security.SignatureException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ac.checkpointmanager.exception.InvalidTokenException;
import ru.ac.checkpointmanager.security.jwt.JwtService;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class JwtValidatorImplTest {

    public static final String REFRESH = "refresh";
    @Mock
    JwtService jwtService;

    @InjectMocks
    JwtValidatorImpl jwtValidator;

    @Test
    void shouldValidateAccessTokenWithoutRefreshClaim() {
        String token = TestUtils.getSimpleValidAccessToken();
        DefaultClaims claims = new DefaultClaims();
        claims.setSubject(TestUtils.USERNAME);
        Mockito.when(jwtService.extractAllClaims(token)).thenReturn(claims);

        boolean isValid = jwtValidator.validateAccessToken(token);

        Assertions.assertThat(isValid).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldNotValidateAccessTokenWithoutSubject(String subject) {
        String token = TestUtils.getSimpleValidAccessToken();
        DefaultClaims claims = new DefaultClaims();
        claims.setSubject(subject);

        Mockito.when(jwtService.extractAllClaims(token)).thenReturn(claims);
        boolean isValid = jwtValidator.validateAccessToken(token);

        Assertions.assertThat(isValid).isFalse();
    }

    @Test
    void shouldNotValidateAccessTokenWithRefreshClaim() {
        String token = TestUtils.getSimpleValidAccessToken();
        DefaultClaims claims = new DefaultClaims();
        claims.setSubject(TestUtils.USERNAME);
        claims.put(REFRESH, true);
        Mockito.when(jwtService.extractAllClaims(token)).thenReturn(claims);

        boolean isValid = jwtValidator.validateAccessToken(token);

        Assertions.assertThat(isValid).isFalse();
    }

    @ParameterizedTest
    @MethodSource("getThrownExceptionsForAccessToken")
    void shouldNotValidateIfExceptionWasThrown(Exception exception) {
        String token = TestUtils.getSimpleValidAccessToken();
        Mockito.when(jwtService.extractAllClaims(token)).thenThrow(exception);

        boolean isValid = jwtValidator.validateAccessToken(token);

        Assertions.assertThat(isValid).isFalse();
    }

    @Test
    void shouldThrowJwtExpiredException() {
        String token = TestUtils.getSimpleValidAccessToken();
        Mockito.when(jwtService.extractAllClaims(token)).thenThrow(new ExpiredJwtException(null, null, "msg"));
        Assertions.assertThatThrownBy(() -> jwtValidator.validateAccessToken(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void shouldValidateRefreshTokenWithRefreshClaim() {
        String token = TestUtils.getSimpleValidAccessToken();
        DefaultClaims claims = new DefaultClaims();
        claims.setSubject(TestUtils.USERNAME);
        claims.put(REFRESH, true);
        Mockito.when(jwtService.extractAllClaims(token)).thenReturn(claims);

        Assertions.assertThatNoException().isThrownBy(() -> jwtValidator.validateRefreshToken(token));
    }

    @Test
    void shouldNotValidateRefreshTokenWithoutRefreshClaim() {
        String token = TestUtils.getSimpleValidAccessToken();
        DefaultClaims claims = new DefaultClaims();
        claims.setSubject(TestUtils.USERNAME);
        Mockito.when(jwtService.extractAllClaims(token)).thenReturn(claims);

        Assertions.assertThatThrownBy(() -> jwtValidator.validateRefreshToken(token))
                .isInstanceOf(InvalidTokenException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldNotValidateRefreshTokenWithoutSubjectClaim(String subject) {
        String token = TestUtils.getSimpleValidAccessToken();
        DefaultClaims claims = new DefaultClaims();
        claims.setSubject(subject);
        Mockito.when(jwtService.extractAllClaims(token)).thenReturn(claims);

        Assertions.assertThatThrownBy(() -> jwtValidator.validateRefreshToken(token))
                .isInstanceOf(InvalidTokenException.class);
    }

    @ParameterizedTest
    @MethodSource("getThrownExceptionForRefreshToken")
    void shouldNotValidateRefreshTokenAndThrowInvalidTokenException(Exception exception) {
        String token = TestUtils.getSimpleValidAccessToken();
        Mockito.when(jwtService.extractAllClaims(token)).thenThrow(exception);

        Assertions.assertThatThrownBy(() -> jwtValidator.validateRefreshToken(token))
                .isInstanceOf(InvalidTokenException.class);
    }

    private static Stream<Exception> getThrownExceptionsForAccessToken() {
        return Stream.of(
                new UnsupportedJwtException(""),
                new MalformedJwtException(""),
                new SignatureException(""),
                new IllegalArgumentException("")
        );
    }

    private static Stream<Exception> getThrownExceptionForRefreshToken() {
        return Stream.concat(getThrownExceptionsForAccessToken(),
                Stream.of(new ExpiredJwtException(null, null, "msg")));
    }

}