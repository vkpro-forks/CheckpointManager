package ru.ac.checkpointmanager.service.avatar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ac.checkpointmanager.dto.avatar.AvatarImageDTO;
import ru.ac.checkpointmanager.exception.AvatarNotFoundException;
import ru.ac.checkpointmanager.model.avatar.Avatar;
import ru.ac.checkpointmanager.repository.AvatarRepository;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.ac.checkpointmanager.service.avatar.AvatarServiceImpl.AVATAR_NOT_FOUND_MSG;

@ExtendWith(MockitoExtension.class)
public class AvatarServiceImplTest {

    @InjectMocks
    private AvatarServiceImpl avatarService;
    @Mock
    private AvatarRepository avatarRepository;
    @Mock
    private AvatarHelper avatarHelper;

    @Test
    public void whenValidIdThenReturnAvatarImageDTO() {
        UUID id = TestUtils.AVATAR_ID;
        Avatar avatar = TestUtils.createTestAvatar();
        AvatarImageDTO expectedDto = new AvatarImageDTO();

        when(avatarRepository.findByTerritoryId(id)).thenReturn(Optional.of(avatar));
        when(avatarHelper.createAvatarImageDTO(avatar)).thenReturn(expectedDto);

        AvatarImageDTO result = avatarService.getAvatarImageByAvatarId(id);

        Assertions.assertEquals(expectedDto, result);
    }

    @Test
    public void whenInvalidIdThenThrowAvatarNotFoundExceptionInGetAvatarImage() {
        UUID id = TestUtils.AVATAR_ID;

        when(avatarRepository.findByTerritoryId(id)).thenReturn(Optional.empty());

        AvatarNotFoundException thrown = assertThrows(AvatarNotFoundException.class, () -> {
            avatarService.getAvatarImageByAvatarId(id);
        });

        Assertions.assertEquals(AVATAR_NOT_FOUND_MSG.formatted(id), thrown.getMessage());
        verify(avatarRepository).findByTerritoryId(id);
    }

    @Test
    public void whenValidIdThenReturnAvatar() {
        UUID id = TestUtils.AVATAR_ID;
        Avatar expectedAvatar = TestUtils.createTestAvatar();

        when(avatarRepository.findById(id)).thenReturn(Optional.of(expectedAvatar));

        Avatar result = avatarService.findAvatarById(id);

        Assertions.assertEquals(expectedAvatar, result);
        verify(avatarRepository).findById(id);
    }

    @Test
    public void whenInvalidIdThenThrowAvatarNotFoundExceptionInFindAvatarById() {
        UUID id = TestUtils.AVATAR_ID;

        when(avatarRepository.findById(id)).thenReturn(Optional.empty());

        AvatarNotFoundException thrown = assertThrows(AvatarNotFoundException.class, () -> {
            avatarService.findAvatarById(id);
        });

        Assertions.assertEquals(AVATAR_NOT_FOUND_MSG.formatted(id), thrown.getMessage());
        verify(avatarRepository).findById(id);
    }

}
