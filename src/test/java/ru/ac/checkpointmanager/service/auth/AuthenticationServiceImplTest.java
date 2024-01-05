package ru.ac.checkpointmanager.service.auth;

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
import org.springframework.test.util.ReflectionTestUtils;
import ru.ac.checkpointmanager.dto.user.ConfirmationRegistrationDTO;
import ru.ac.checkpointmanager.exception.EmailVerificationTokenException;
import ru.ac.checkpointmanager.mapper.UserMapper;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.util.TestUtils;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    RedisCacheManager cacheManager;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    AuthenticationServiceImpl authenticationService;

    @Captor
    ArgumentCaptor<User> userArgumentCaptor;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(authenticationService, "userMapper", new UserMapper(new ModelMapper()));
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
        ConfirmationRegistrationDTO confirmationRegistrationDTO = TestUtils.getConfirmationRegistrationDTO();
        Mockito.when(mockCache.get(TestUtils.EMAIL_STRING_TOKEN, ConfirmationRegistrationDTO.class))
                .thenReturn(confirmationRegistrationDTO);

        authenticationService.confirmRegistration(TestUtils.EMAIL_STRING_TOKEN);

        Mockito.verify(userRepository).save(userArgumentCaptor.capture());
        User captured = userArgumentCaptor.getValue();
        Assertions.assertThat(captured).extracting("role").as("Check if Role is USER").isEqualTo(Role.USER);
        Assertions.assertThat(captured.getIsBlocked()).as("Check if isBlocked status set to False").isFalse();
        Assertions.assertThat(captured.getUsername()).as("Check if user name is equals value from ConfirmRegistration")
                .isEqualTo(confirmationRegistrationDTO.getEmail());
        Assertions.assertThat(captured.getEmail()).as("Check if email is equals value from ConfirmRegistration")
                .isEqualTo(confirmationRegistrationDTO.getEmail());
        Assertions.assertThat(captured.getFullName()).as("Check if full name is equals value from ConfirmRegistration")
                .isEqualTo(confirmationRegistrationDTO.getFullName());
    }

}
