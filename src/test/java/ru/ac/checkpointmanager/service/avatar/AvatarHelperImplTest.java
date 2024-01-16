package ru.ac.checkpointmanager.service.avatar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.dto.avatar.AvatarImageDTO;
import ru.ac.checkpointmanager.exception.AvatarLoadingException;
import ru.ac.checkpointmanager.exception.AvatarProcessingException;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
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
    @Mock
    private MultipartFile avatarFile;
    @InjectMocks
    private AvatarHelperImpl avatarHelper;

    private UUID userId;
    private UUID territoryId;
    private Territory territory;
    private Avatar avatar;

    @BeforeEach
    void setUp() {
        userId = TestUtils.USER_ID;
        territoryId = TestUtils.TERR_ID;
        territory = TestUtils.getTerritory();
        avatar = TestUtils.createTestAvatar();
    }

    @Test
    void whenUpdateTerritoryAvatarAndTerritoryExistsThenAvatarIsUpdated() {
        territory.setAvatar(avatar);
        when(territoryRepository.findById(territoryId)).thenReturn(Optional.of(territory));
        avatarHelper.updateTerritoryAvatar(territoryId, avatar);
        verify(territoryRepository).findById(territoryId);
        verify(territoryRepository).save(territory);
        Assertions.assertEquals(territory.getAvatar(), avatar);
    }

    @Test
    void whenUpdateTerritoryAvatarAndTerritoryNotFoundThenThrowException() {
        when(territoryRepository.findById(territoryId)).thenReturn(Optional.empty());
        Assertions.assertThrows(TerritoryNotFoundException.class, () -> {
            avatarHelper.updateTerritoryAvatar(territoryId, avatar);
        });
        verify(territoryRepository).findById(territoryId);
        verify(territoryRepository, never()).save(any(Territory.class));
    }

    @Test
    void whenUpdateTerritoryAvatarAndTerritoryNotFoundThenExceptionMessageIsCorrect() {
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
        AvatarImageDTO result = avatarHelper.createAvatarImageDTO(avatar);
        Assertions.assertEquals(avatar.getId(), result.getAvatarId());
        Assertions.assertEquals(avatar.getMediaType(), result.getMediaType());
        assertArrayEquals(avatar.getPreview(), result.getImageData());
        Assertions.assertEquals(avatar.getFileSize(), result.getFileSize());
    }

    @Test
    void createAvatarImageDTOUsesDefaultMimeType() {
        AvatarImageDTO result = avatarHelper.createAvatarImageDTO(avatar);
        Assertions.assertEquals(TestUtils.DEFAULT_MEDIA_TYPE, result.getMediaType());
    }

    @Test
    void updateUserAvatarWithExistingUserUpdatesAvatar() {
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
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> avatarHelper.updateUserAvatar(userId, avatar));
    }

    @Test
    void saveAvatarSavesAndReturnsAvatar() {
        avatar.setMediaType(TestUtils.DEFAULT_MEDIA_TYPE);
        avatar.setFilePath(TestUtils.DEFAULT_FILE_PATH);
        avatar.setFileSize(TestUtils.FILE_SIZE);
        when(avatarRepository.save(any(Avatar.class))).thenReturn(avatar);
        Avatar saveAvatar = avatarHelper.saveAvatar(avatar);
        verify(avatarRepository).save(saveAvatar);
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
    void processImageWithImageWithinMaxSizeReturnsOriginalImage() {
        BufferedImage originalImage = TestUtils.createSmallBufferedImage();
        when(avatarProperties.getMaxWidth()).thenReturn(TestUtils.NORMAL_WIDTH);
        when(avatarProperties.getMaxHeight()).thenReturn(TestUtils.NORMAL_HEIGHT);
        BufferedImage processedImage = avatarHelper.processImage(originalImage);
        Assertions.assertEquals(originalImage, processedImage);
    }

    @Test
    void processAndSetAvatarImageWithInvalidFileThrowsException() throws IOException {
        when(avatarFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        assertThrows(IllegalArgumentException.class,
                () -> avatarHelper.processAndSetAvatarImage(avatar, avatarFile));
    }

    @Test
    void processAndSetAvatarImageWithIOExceptionThrowsException() throws IOException {
        when(avatarFile.getInputStream()).thenThrow(new IOException());
        Avatar avatar = TestUtils.createTestAvatar();
        assertThrows(AvatarProcessingException.class,
                () -> avatarHelper.processAndSetAvatarImage(avatar, avatarFile));
    }

    @Test
    void processAndSetAvatarImageSuccess() throws IOException {
        BufferedImage testImages = TestUtils.createLargeBufferedImage();
        when(avatarProperties.getMaxWidth()).thenReturn(200);
        when(avatarProperties.getMaxHeight()).thenReturn(200);
        when(avatarFile.getInputStream())
                .thenReturn(new ByteArrayInputStream(TestUtils.convertBufferedImageToByteArray(testImages)));
        avatarHelper.processAndSetAvatarImage(avatar, avatarFile);
        Assertions.assertNotNull(avatar.getPreview());
    }

    @Test
    void processAndSetAvatarImageThrowsIOException() throws IOException {
        when(avatarFile.getInputStream()).thenThrow(new IOException());
        assertThrows(AvatarProcessingException.class, () -> avatarHelper.processAndSetAvatarImage(avatar, avatarFile));
    }

    @Test
    void getOrCreateAvatarByTerritory_ReturnsExistingAvatar() {
        when(avatarRepository.findByTerritoryId(territoryId)).thenReturn(Optional.of(avatar));
        Avatar result = avatarHelper.getOrCreateAvatarByTerritory(territoryId);
        Assertions.assertEquals(avatar, result);
    }

    @Test
    void getOrCreateAvatarByTerritory_CreatesNewAvatar() {
        when(avatarRepository.findByTerritoryId(territoryId)).thenReturn(Optional.empty());
        Avatar result = avatarHelper.getOrCreateAvatarByTerritory(territoryId);
        Assertions.assertNotNull(result);
    }

    @Test
    void configureAvatar_SetsFileSizeAndMediaType() {
        when(avatarFile.getSize()).thenReturn(TestUtils.FILE_SIZE);
        when(avatarFile.getContentType()).thenReturn(TestUtils.DEFAULT_MEDIA_TYPE);
        avatarHelper.configureAvatar(avatar, avatarFile);
        Assertions.assertEquals(TestUtils.FILE_SIZE, avatar.getFileSize());
        Assertions.assertEquals(TestUtils.DEFAULT_MEDIA_TYPE, avatar.getMediaType());
    }

    @Test
    void getOrCreateAvatar_ReturnsExistingAvatar() {
        when(avatarRepository.findByUserId(userId)).thenReturn(Optional.of(avatar));
        Avatar result = avatarHelper.getOrCreateAvatar(userId);
        Assertions.assertEquals(avatar, result);
    }

    @Test
    void getOrCreateAvatar_CreatesNewAvatar() {
        when(avatarRepository.findByUserId(userId)).thenReturn(Optional.empty());
        Avatar result = avatarHelper.getOrCreateAvatar(userId);
        Assertions.assertNotNull(result);
    }
}

