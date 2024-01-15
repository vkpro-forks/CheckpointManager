package ru.ac.checkpointmanager.service.avatar;

import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;
import ru.ac.checkpointmanager.exception.AvatarNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.mapper.avatar.AvatarMapperImpl;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.avatar.Avatar;
import ru.ac.checkpointmanager.repository.AvatarRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AvatarServiceImplTest {

    @Mock
    AvatarRepository avatarRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    TerritoryRepository territoryRepository;

    @Mock
    AvatarHelper avatarHelper;

    @InjectMocks
    AvatarServiceImpl avatarService;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(avatarService, "avatarMapper", new AvatarMapperImpl(new ModelMapper()));
    }

    @Test
    void deleteAvatarByUserId_AllOk_DeleteAvatar() {
        User user = TestUtils.getUser();
        Avatar avatar = new Avatar();
        avatar.setId(TestUtils.AVATAR_ID);
        user.setAvatar(avatar);
        Mockito.when(userRepository.findUserWithAvatarIdById(TestUtils.USER_ID)).thenReturn(Optional.of(user));

        avatarService.deleteAvatarByUserId(TestUtils.USER_ID);

        Mockito.verify(avatarRepository, Mockito.times(1)).deleteById(TestUtils.AVATAR_ID);
        Mockito.verify(userRepository, Mockito.times(1)).findUserWithAvatarIdById(TestUtils.USER_ID);
    }

    @Test
    void deleteAvatarByUserId_UserNotFound_ThrowException() {
        Mockito.when(userRepository.findUserWithAvatarIdById(TestUtils.USER_ID)).thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> avatarService.deleteAvatarByUserId(TestUtils.USER_ID))
                .isInstanceOf(EntityNotFoundException.class);

        Mockito.verify(avatarRepository, Mockito.never()).deleteById(TestUtils.AVATAR_ID);
        Mockito.verify(userRepository, Mockito.times(1)).findUserWithAvatarIdById(TestUtils.USER_ID);
    }

    @Test
    void deleteAvatarByUserId_UserHasNoAvatar_ThrowException() {
        User user = TestUtils.getUser();
        Mockito.when(userRepository.findUserWithAvatarIdById(TestUtils.USER_ID)).thenReturn(Optional.of(user));

        Assertions.assertThatExceptionOfType(AvatarNotFoundException.class)
                .isThrownBy(() -> avatarService.deleteAvatarByUserId(TestUtils.USER_ID))
                .isInstanceOf(EntityNotFoundException.class);

        Mockito.verify(avatarRepository, Mockito.never()).deleteById(TestUtils.AVATAR_ID);
        Mockito.verify(userRepository, Mockito.times(1)).findUserWithAvatarIdById(TestUtils.USER_ID);
    }

}
