package ru.ac.checkpointmanager.service.avatar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ac.checkpointmanager.dto.avatar.AvatarImageDTO;
import ru.ac.checkpointmanager.exception.AvatarLoadingException;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.avatar.Avatar;
import ru.ac.checkpointmanager.model.avatar.AvatarProperties;
import ru.ac.checkpointmanager.repository.AvatarRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.util.TestUtils;

import java.awt.image.BufferedImage;
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
    @Mock
    private UserRepository userRepository;
    @Mock
    private AvatarRepository avatarRepository;
    @Mock
    private AvatarProperties avatarProperties;
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

    @Test
    void updateUserAvatarWithExistingUserUpdatesAvatar() {
        UUID userId = TestUtils.USER_ID;
        Avatar avatar = TestUtils.createTestAvatar();
        User user = TestUtils.getUser();
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        avatarHelper.updateUserAvatar(userId, avatar);
        verify(userRepository).save(userArgumentCaptor.capture());
        User updateUser = userArgumentCaptor.getValue();
        Assertions.assertEquals(avatar, updateUser.getAvatar());
    }

    @Test
    void updateUserAvatarWithNonExistingUserThrowsException() {
        UUID userId = UUID.randomUUID();
        Avatar newAvatar = TestUtils.createTestAvatar();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> avatarHelper.updateUserAvatar(userId, newAvatar));
    }

    @Test
    void saveAvatarSavesAndReturnsAvatar() {
        Avatar avatar = TestUtils.createTestAvatar();
        ArgumentCaptor<Avatar> avatarCaptor = ArgumentCaptor.forClass(Avatar.class);
        when(avatarRepository.save(any(Avatar.class))).thenReturn(avatar);
        Avatar saveAvatar = avatarHelper.saveAvatar(avatar);
        verify(avatarRepository).save(avatarCaptor.capture());
        Assertions.assertEquals(avatar, avatarCaptor.getValue());
        Assertions.assertEquals(avatar, saveAvatar);
    }

    @Test
    void processImageWithImageExceedingMaxSizeResizesImage() {
        BufferedImage originalImage = TestUtils.createLargeBufferedImage();
        when(avatarProperties.getMaxWidth()).thenReturn(100);
        when(avatarProperties.getMaxHeight()).thenReturn(100);
        BufferedImage processedImage = avatarHelper.processImage(originalImage);
        Assertions.assertNotEquals(originalImage, processedImage);
        Assertions.assertTrue(processedImage.getWidth() <= TestUtils.NORMAL_HEIGHT);
        Assertions.assertTrue(processedImage.getHeight() <= TestUtils.NORMAL_HEIGHT);
    }

    @Test
    void processImage_withImageWithinMaxSize_returnsOriginalImage() {
        BufferedImage originalImage = TestUtils.createSmallBufferedImage();
        when(avatarProperties.getMaxWidth()).thenReturn(TestUtils.NORMAL_WIDTH);
        when(avatarProperties.getMaxHeight()).thenReturn(TestUtils.NORMAL_HEIGHT);
        BufferedImage processedImage = avatarHelper.processImage(originalImage);
        Assertions.assertEquals(originalImage, processedImage);
    }

}

