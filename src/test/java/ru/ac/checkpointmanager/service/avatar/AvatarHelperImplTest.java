package ru.ac.checkpointmanager.service.avatar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ac.checkpointmanager.dto.avatar.AvatarImageDTO;
import ru.ac.checkpointmanager.exception.AvatarLoadingException;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.avatar.Avatar;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class AvatarHelperImplTest {

    @Mock
    private TerritoryRepository territoryRepository;
    @InjectMocks
    private AvatarHelperImpl avatarHelper;

    @Test
    void whenUpdateTerritoryAvatarAndTerritoryExistsThenAvatarIsUpdated() {
        UUID territoryId = UUID.randomUUID();
        Avatar avatar = TestUtils.createTestAvatar();
        Territory territory = TestUtils.getTerritory();
        territory.setAvatar(avatar);
        when(territoryRepository.findById(territoryId)).thenReturn(Optional.of(territory));
        avatarHelper.updateTerritoryAvatar(territoryId, avatar);
        verify(territoryRepository).findById(territoryId);
        verify(territoryRepository).save(territory);
        Assertions.assertEquals(territory.getAvatar(), avatar);
    }

    @Test
    void whenUpdateTerritoryAvatarAndTerritoryNotFoundThenThrowException() {
        UUID territoryId = UUID.randomUUID();
        Avatar avatar = TestUtils.createTestAvatar();
        when(territoryRepository.findById(territoryId)).thenReturn(Optional.empty());
        Assertions.assertThrows(TerritoryNotFoundException.class, () -> {
            avatarHelper.updateTerritoryAvatar(territoryId, avatar);
        });
        verify(territoryRepository).findById(territoryId);
        verify(territoryRepository, never()).save(any(Territory.class));
    }

    @Test
    void whenUpdateTerritoryAvatarAndTerritoryNotFoundThenExceptionMessageIsCorrect() {
        UUID territoryId = UUID.randomUUID();
        Avatar avatar = TestUtils.createTestAvatar();
        when(territoryRepository.findById(territoryId)).thenReturn(Optional.empty());
        Exception exception = Assertions.assertThrows(TerritoryNotFoundException.class, () -> {
            avatarHelper.updateTerritoryAvatar(territoryId, avatar);
        });
        String expectedMessage = TestUtils.ERROR_MESSAGE_SHOULD;
        String actualMessage = exception.getMessage();
        Assertions.assertTrue(actualMessage.contains(expectedMessage));
        verify(territoryRepository).findById(territoryId);
        verify(territoryRepository, never()).save(any(Territory.class));
    }

    @Test
    void createAvatarImageDTOWithNullImageDataThrowsException() {
        Avatar avatar = TestUtils.createTestAvatarWithEmptyImageData();
        assertThrows(AvatarLoadingException.class, () -> avatarHelper.createAvatarImageDTO(avatar));
    }

    @Test
    void createAvatarImageDTOWithEmptyImageDataThrowsException() {
        Avatar avatar = TestUtils.createTestAvatarWithEmptyImageData();
        assertThrows(AvatarLoadingException.class, () -> avatarHelper.createAvatarImageDTO(avatar));
    }

    @Test
    void createAvatarImageDTOWithValidDataReturnsDTO() {
        Avatar avatar = TestUtils.createTestAvatar();
        AvatarImageDTO result = avatarHelper.createAvatarImageDTO(avatar);
        Assertions.assertEquals(avatar.getId(), result.getAvatarId());
        Assertions.assertEquals(avatar.getMediaType(), result.getMediaType());
        assertArrayEquals(avatar.getPreview(), result.getImageData());
        Assertions.assertEquals(avatar.getFileSize(), result.getFileSize());
    }

    @Test
    void createAvatarImageDTOUsesDefaultMimeType() {
        Avatar avatar = TestUtils.createTestAvatar();
        AvatarImageDTO result = avatarHelper.createAvatarImageDTO(avatar);
        Assertions.assertEquals("image/jpeg", result.getMediaType());
    }


}

