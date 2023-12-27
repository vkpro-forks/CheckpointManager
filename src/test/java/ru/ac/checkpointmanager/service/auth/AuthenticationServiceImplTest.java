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
import ru.ac.checkpointmanager.dto.user.ConfirmRegistration;
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
        Mockito.when(mockCache.get(TestUtils.EMAIL_STRING_TOKEN, ConfirmRegistration.class))
                .thenReturn(TestUtils.getConfirmRegistration());

        authenticationService.confirmRegistration(TestUtils.EMAIL_STRING_TOKEN);

        Mockito.verify(userRepository).save(userArgumentCaptor.capture());
        User captured = userArgumentCaptor.getValue();
        Assertions.assertThat(captured.getRole()).isEqualTo(Role.USER);
        Assertions.assertThat(captured.getIsBlocked()).isFalse();
        Assertions.assertThat(captured.getUsername()).isEqualTo(TestUtils.EMAIL);
        Assertions.assertThat(captured.getEmail()).isEqualTo(TestUtils.EMAIL);
        Assertions.assertThat(captured.getFullName()).isEqualTo(TestUtils.USERNAME);
    }

}