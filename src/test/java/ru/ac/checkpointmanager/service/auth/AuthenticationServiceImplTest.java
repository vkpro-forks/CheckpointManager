package ru.ac.checkpointmanager.service.auth;

import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import ru.ac.checkpointmanager.dto.user.AuthRequestDTO;
import ru.ac.checkpointmanager.dto.user.AuthResponseDTO;
import ru.ac.checkpointmanager.dto.user.LoginResponseDTO;
import ru.ac.checkpointmanager.dto.user.PreAuthResponseDTO;
import ru.ac.checkpointmanager.dto.user.RefreshTokenDTO;
import ru.ac.checkpointmanager.dto.user.RegistrationConfirmationDTO;
import ru.ac.checkpointmanager.dto.user.RegistrationDTO;
import ru.ac.checkpointmanager.exception.EmailAlreadyExistsException;
import ru.ac.checkpointmanager.exception.EmailVerificationTokenException;
import ru.ac.checkpointmanager.exception.InvalidTokenException;
import ru.ac.checkpointmanager.mapper.UserMapper;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.security.jwt.JwtService;
import ru.ac.checkpointmanager.security.jwt.JwtValidator;
import ru.ac.checkpointmanager.service.email.EmailService;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    RedisCacheManager cacheManager;
    @Mock
    UserRepository userRepository;
    @Mock
    JwtService jwtService;
    @Mock
    JwtValidator jwtValidator;
    @Mock
    EmailService emailService;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    AuthenticationManager authenticationManager;

    @InjectMocks
    AuthenticationServiceImpl authenticationService;

    @Captor
    ArgumentCaptor<User> userArgumentCaptor;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(authenticationService, "userMapper", new UserMapper(new ModelMapper()));
    }

    @Test
    void shouldThrowEmailAlreadyExistsExceptionIfUserExists() {
        RegistrationDTO registrationDTO = TestUtils.getRegistrationDTO();
        Mockito.when(userRepository.existsByEmail(registrationDTO.getEmail())).thenReturn(true);

        Assertions.assertThatThrownBy(() -> authenticationService.preRegister(registrationDTO))
                .as("Check if EmailAlreadyExistsException is thrown")
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void shouldThrowMailSendExceptionIfEmailSendingFails() {
        RegistrationDTO registrationDTO = TestUtils.getRegistrationDTO();
        Mockito.when(userRepository.existsByEmail(registrationDTO.getEmail())).thenReturn(false);
        Mockito.doThrow(new MailException("fail") {
        }).when(emailService).sendRegisterConfirm(Mockito.anyString(), Mockito.anyString());

        Assertions.assertThatThrownBy(() -> authenticationService.preRegister(registrationDTO))
                .isInstanceOf(MailSendException.class)
                .hasMessageContaining("Email sending failed");
    }

    @Test
    void shouldReturnRegistrationConfirmationDTOBeforeRegistration() {
        RegistrationDTO registrationDTO = TestUtils.getRegistrationDTO();
        String encodedPassword = TestUtils.PASSWORD;
        Mockito.when(userRepository.existsByEmail(registrationDTO.getEmail())).thenReturn(false);
        Mockito.doNothing().when(emailService).sendRegisterConfirm(Mockito.anyString(), Mockito.anyString());
        Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn(encodedPassword);

        RegistrationConfirmationDTO result = authenticationService.preRegister(registrationDTO);
        Assertions.assertThat(result).hasNoNullFieldsOrProperties();
        Assertions.assertThat(result.getEmail()).isEqualTo(registrationDTO.getEmail());
    }

    @Test
    void shouldThrowEmailVerificationExceptionIfTokenNotInCache() {
        Assertions.assertThatThrownBy(() -> authenticationService.confirmRegistration(TestUtils.EMAIL_STRING_TOKEN))
                .as("Check if EmailVerificationToken exception throws if no token in cache")
                .isInstanceOf(EmailVerificationTokenException.class);
    }

    @Test
    void shouldSaveUserIfVerificationTokenIsOk() {
        Cache mockCache = Mockito.mock(Cache.class);
        Mockito.when(cacheManager.getCache("registration")).thenReturn(mockCache);
        RegistrationConfirmationDTO registrationConfirmationDTO = TestUtils.getRegistrationConfirmationDTO();
        Mockito.when(mockCache.get(TestUtils.EMAIL_STRING_TOKEN, RegistrationConfirmationDTO.class))
                .thenReturn(registrationConfirmationDTO);

        authenticationService.confirmRegistration(TestUtils.EMAIL_STRING_TOKEN);

        Mockito.verify(userRepository).save(userArgumentCaptor.capture());
        User captured = userArgumentCaptor.getValue();
        Assertions.assertThat(captured).extracting("role").as("Check if Role is USER").isEqualTo(Role.USER);
        Assertions.assertThat(captured.getIsBlocked()).as("Check if isBlocked status set to False").isFalse();
        Assertions.assertThat(captured.getUsername()).as("Check if user name is equals value from ConfirmRegistration")
                .isEqualTo(registrationConfirmationDTO.getEmail());
        Assertions.assertThat(captured.getEmail()).as("Check if email is equals value from ConfirmRegistration")
                .isEqualTo(registrationConfirmationDTO.getEmail());
        Assertions.assertThat(captured.getFullName()).as("Check if full name is equals value from ConfirmRegistration")
                .isEqualTo(registrationConfirmationDTO.getFullName());
    }

    @Test
    void shouldReturnAuthenticatedUser() {
        User user = TestUtils.getUser();
        String email = user.getEmail();
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        PreAuthResponseDTO result = authenticationService.isUserAuthenticated(email);
        Assertions.assertThat(result).hasNoNullFieldsOrProperties();
        Assertions.assertThat(result.getIsAuthenticated()).isTrue();
        Assertions.assertThat(result.getFullName()).isEqualTo(user.getFullName());
    }

    @Test
    void shouldReturnNotAuthenticatedUser() {
        String email = TestUtils.EMAIL;
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        PreAuthResponseDTO result = authenticationService.isUserAuthenticated(email);
        Assertions.assertThat(result.getIsAuthenticated()).isFalse();
        Assertions.assertThat(result.getFullName()).isNull();
    }

    @Test
    void shouldNotThrowException() {
        String email = TestUtils.EMAIL;
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Assertions.assertThatNoException().isThrownBy(() -> authenticationService.isUserAuthenticated(email));
    }

    @Test
    void shouldAuthenticateUserAndReturnLoginResponse() {
        AuthRequestDTO request = TestUtils.getAuthRequestDTO();
        User user = TestUtils.getUser();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        Authentication authentication = TestUtils.getAuthToken(user);

        Mockito.when(authenticationManager.authenticate(Mockito.any())).thenReturn(authentication);
        Mockito.when(jwtService.generateAccessToken(user)).thenReturn("accessToken");
        Mockito.when(jwtService.generateRefreshToken(user)).thenReturn("refreshToken");

        LoginResponseDTO result = authenticationService.authenticate(request);

        Assertions.assertThat(result).hasNoNullFieldsOrProperties();
        Assertions.assertThat("accessToken").isEqualTo(result.getAccessToken());
        Assertions.assertThat("refreshToken").isEqualTo(result.getRefreshToken());
        Mockito.verify(jwtService).generateAccessToken(user);
        Mockito.verify(jwtService).generateRefreshToken(user);
    }

    @Test
    void shouldThrowExceptionWhenBadCredentials() {
        AuthRequestDTO request = TestUtils.getAuthRequestDTO();
        User user = TestUtils.getUser();

        Mockito.when(authenticationManager.authenticate(Mockito.any()))
                .thenThrow(new UsernameNotFoundException("Username Not Found"));

        Assertions.assertThatExceptionOfType(UsernameNotFoundException.class)
                .isThrownBy(() -> authenticationService.authenticate(request));

        Mockito.verify(jwtService, Mockito.never()).generateAccessToken(user);
        Mockito.verify(jwtService, Mockito.never()).generateRefreshToken(user);
    }

    @Test
    void shouldRefreshAccessToken() {
        User user = TestUtils.getUser();
        String email = user.getEmail();
        RefreshTokenDTO refreshTokenDTO = TestUtils.getRefreshTokenDTO();
        String refreshToken = refreshTokenDTO.getRefreshToken();
        String accessToken = TestUtils.getSimpleValidAccessToken();

        Mockito.doNothing().when(jwtValidator).validateRefreshToken(refreshToken);
        Mockito.when(jwtService.extractUsername(refreshToken)).thenReturn(email);
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(jwtService.generateAccessToken(user)).thenReturn(accessToken);

        AuthResponseDTO result = authenticationService.refreshToken(refreshTokenDTO);
        Assertions.assertThat(result).hasNoNullFieldsOrProperties();
        Assertions.assertThat(result.getAccessToken()).isEqualTo(accessToken);
        Mockito.verify(jwtValidator).validateRefreshToken(refreshToken);
    }

    @Test
    void shouldThrowExceptionWhenRefreshTokenInvalid() {
        User user = TestUtils.getUser();
        RefreshTokenDTO refreshTokenDTO = TestUtils.getRefreshTokenDTO();
        String refreshToken = refreshTokenDTO.getRefreshToken();

        Mockito.doThrow(InvalidTokenException.class).when(jwtValidator).validateRefreshToken(refreshToken);

        Assertions.assertThatExceptionOfType(InvalidTokenException.class)
                .isThrownBy(() -> authenticationService.refreshToken(refreshTokenDTO));
        Mockito.verify(jwtService, Mockito.never()).generateAccessToken(user);
    }

    @Test
    void shouldThrowExceptionWhenUsernameNotFound() {
        String email = TestUtils.EMAIL;
        RefreshTokenDTO refreshTokenDTO = TestUtils.getRefreshTokenDTO();
        String refreshToken = refreshTokenDTO.getRefreshToken();

        Mockito.doNothing().when(jwtValidator).validateRefreshToken(refreshToken);
        Mockito.when(jwtService.extractUsername(refreshToken)).thenReturn(email);
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(
                        () -> authenticationService.refreshToken(refreshTokenDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");
    }
}
